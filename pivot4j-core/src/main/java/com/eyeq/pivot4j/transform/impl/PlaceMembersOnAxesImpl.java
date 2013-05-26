/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.mdx.metadata.MemberExp;
import com.eyeq.pivot4j.query.Quax;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;
import com.eyeq.pivot4j.util.MemberSelection;
import com.eyeq.pivot4j.util.OlapUtils;

public class PlaceMembersOnAxesImpl extends AbstractTransform implements
		PlaceMembersOnAxes {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param queryAdapter
	 */
	public PlaceMembersOnAxesImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#placeMembers(org.olap4j.metadata.Hierarchy,
	 *      java.util.List)
	 */
	@Override
	public void placeMembers(Hierarchy hierarchy, List<Member> members) {
		QueryAdapter adapter = getQueryAdapter();

		Quax quax = adapter.findQuax(hierarchy.getDimension());
		if (quax == null) {
			throw new IllegalArgumentException(
					"Cannot find the specified hierarchy on any axis.");
		}

		List<Member> selection = new ArrayList<Member>();

		List<Hierarchy> hierarchies = quax.getHierarchies();
		for (Hierarchy hier : hierarchies) {
			if (OlapUtils.equals(hier, hierarchy)) {
				selection.addAll(members);
			} else {
				selection.addAll(findVisibleMembers(hier));
			}
		}

		int iAx = quax.getOrdinal();
		if (adapter.isAxesSwapped()) {
			iAx = (iAx + 1) % 2;
		}

		Axis axis = Axis.Factory.forOrdinal(iAx);

		placeMembers(axis, selection);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#placeMembers(org.olap4j.Axis,
	 *      java.util.List)
	 */
	@Override
	public void placeMembers(Axis axis, List<Member> members) {
		QueryAdapter adapter = getQueryAdapter();

		Quax quax = adapter.getQuax(axis);

		if (quax == null) {
			quax = adapter.createQuax(axis);
		}

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(members.size());
		Map<Hierarchy, List<Member>> memberMap = new HashMap<Hierarchy, List<Member>>(
				hierarchies.size());

		for (Member member : members) {
			Hierarchy hierarchy = member.getHierarchy();

			if (!hierarchies.contains(hierarchy)) {
				hierarchies.add(hierarchy);
			}

			List<Member> selection = memberMap.get(hierarchy);
			if (selection == null) {
				selection = new ArrayList<Member>(members.size());
				memberMap.put(hierarchy, selection);
			}

			if (!selection.contains(member)) {
				selection.add(member);
			}
		}

		OlapUtils utils = new OlapUtils(getModel().getCube());

		List<Exp> expressions = new ArrayList<Exp>(hierarchies.size());

		for (Hierarchy hierarchy : hierarchies) {
			List<Member> selection = memberMap.get(hierarchy);

			List<Exp> sets = new ArrayList<Exp>(selection.size());

			if (selection.size() == 1) {
				expressions.add(new MemberExp(utils
						.wrapRaggedIfNecessary(selection.get(0))));
			} else {
				for (Member member : selection) {
					sets.add(new MemberExp(utils.wrapRaggedIfNecessary(member)));
				}

				expressions.add(new FunCall("{}", Syntax.Braces, sets));
			}
		}

		// generate the crossjoins
		quax.regeneratePosTree(expressions, true);

		if (logger.isInfoEnabled()) {
			logger.info("setQueryAxis axis=" + quax.getOrdinal()
					+ " nDimension=" + hierarchies.size());
			logger.info("Expression for Axis=" + quax.toString());
		}
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#addMember(org.olap4j.metadata
	 *      .Member, int)
	 */
	@Override
	public void addMember(Member member, int position) {
		Hierarchy hierarchy = member.getHierarchy();

		List<Member> selection = new ArrayList<Member>(
				findVisibleMembers(hierarchy));
		if (selection.contains(member)) {
			moveMember(member, position);
			return;
		}

		if (position < 0 || position >= selection.size()) {
			selection.add(member);
		} else {
			selection.add(position, member);
		}

		placeMembers(hierarchy, selection);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#addMembers(org.olap4j.metadata.Hierarchy,
	 *      java.util.List)
	 */
	@Override
	public void addMembers(Hierarchy hierarchy, List<Member> members) {
		if (members.isEmpty()) {
			return;
		}

		PlaceMembersOnAxes transform = getQueryAdapter().getModel()
				.getTransform(PlaceMembersOnAxes.class);

		MemberSelection selection = new MemberSelection(
				transform.findVisibleMembers(hierarchy), getModel().getCube());
		selection.addMembers(members);

		placeMembers(hierarchy, selection.getMembers());
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#addMember(org.olap4j.Axis,
	 *      org.olap4j.metadata.Member, int)
	 */
	@Override
	public void addMember(Axis axis, Member member, int position) {
		QueryAdapter adapter = getQueryAdapter();

		Quax quax = adapter.findQuax(member.getDimension());
		if (quax == null) {
			quax = adapter.getQuax(axis);

			List<Hierarchy> hierarchies;

			if (quax == null) {
				hierarchies = new ArrayList<Hierarchy>();
			} else {
				hierarchies = new ArrayList<Hierarchy>(quax.getHierarchies());
			}

			if (position < 0 || position >= hierarchies.size()) {
				hierarchies.add(member.getHierarchy());
			} else {
				hierarchies.add(position, member.getHierarchy());
			}

			List<Member> members = new ArrayList<Member>();
			for (Hierarchy hierarchy : hierarchies) {
				if (OlapUtils.equals(member.getHierarchy(), hierarchy)) {
					members.add(member);
				} else {
					members.addAll(findVisibleMembers(hierarchy));
				}
			}

			placeMembers(axis, members);
		} else {
			addMember(member, position);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#addMembers(org.olap4j.Axis,
	 *      java.util.List,int)
	 */
	@Override
	public void addMembers(Axis axis, List<Member> members, int position) {
		QueryAdapter adapter = getQueryAdapter();

		Quax quax = adapter.getQuax(axis);

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(
				quax.getHierarchies());

		if (position < 0 || position >= hierarchies.size()) {
			hierarchies.add(null);
		} else {
			hierarchies.add(position, null);
		}

		List<Member> memberList = new ArrayList<Member>();
		for (Hierarchy hierarchy : hierarchies) {
			if (hierarchy == null) {
				memberList.addAll(members);
			} else {
				memberList.addAll(findVisibleMembers(hierarchy));
			}
		}

		placeMembers(axis, members);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#removeMember(org.olap4j
	 *      .metadata.Member)
	 */
	@Override
	public void removeMember(Member member) {
		Hierarchy hierarchy = member.getHierarchy();

		List<Member> members = new ArrayList<Member>(
				findVisibleMembers(hierarchy));
		members.remove(member);

		placeMembers(hierarchy, members);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#removeMembers(org.olap4j.metadata.Hierarchy,
	 *      java.util.List)
	 */
	@Override
	public void removeMembers(Hierarchy hierarchy, List<Member> members) {
		List<Member> selection = new ArrayList<Member>(
				findVisibleMembers(hierarchy));
		selection.removeAll(members);

		placeMembers(hierarchy, selection);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#moveMember(org.olap4j.metadata
	 *      .Member, int)
	 */
	@Override
	public void moveMember(Member member, int position) {
		Hierarchy hierarchy = member.getHierarchy();

		List<Member> selection = new ArrayList<Member>(
				findVisibleMembers(hierarchy));
		if (!selection.contains(member)) {
			if (logger.isWarnEnabled()) {
				logger.warn("The specified member is not visible on the current result : "
						+ member.getUniqueName());
			}
			return;
		}

		if (position < 0 || position >= selection.size()) {
			selection.remove(member);
			selection.add(member);
		} else {
			int index = selection.indexOf(member);

			if (position < index) {
				selection.remove(member);
				selection.add(position, member);
			} else if (position > index) {
				selection.add(position, member);
				selection.remove(index);
			}
		}

		placeMembers(hierarchy, selection);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#findVisibleMembers(org.olap4j.Axis)
	 */
	public List<Member> findVisibleMembers(Axis axis) {
		List<Member> visibleMembers = new ArrayList<Member>();

		QueryAdapter adapter = getQueryAdapter();

		// find the Quax for this hierarchy
		Quax quax = adapter.getQuax(axis);
		if (quax == null) {
			return Collections.emptyList();
		}

		CellSet cellSet = adapter.getModel().getCellSet();
		CellSetAxis cellAxis = getCellSetAxis(cellSet, axis);

		if (cellAxis == null) {
			return Collections.emptyList();
		}

		List<Position> positions = cellAxis.getPositions();
		for (Position position : positions) {
			List<Member> members = position.getMembers();
			for (Member member : members) {
				if (member != null && !visibleMembers.contains(member)) {
					visibleMembers.add(member);
				}
			}
		}

		return visibleMembers;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#findVisibleMembers(org.
	 *      olap4j.metadata.Hierarchy)
	 */
	public List<Member> findVisibleMembers(Hierarchy hierarchy) {
		List<Member> visibleMembers = new ArrayList<Member>();

		QueryAdapter adapter = getQueryAdapter();

		// find the Quax for this hierarchy
		Quax quax = adapter.findQuax(hierarchy.getDimension());
		if (quax == null) {
			return Collections.emptyList(); // should not occur
		}

		int iDim = quax.dimIdx(hierarchy.getDimension());

		// use result
		// problem: if NON EMPTY is on the axis then a member, which is excluded
		// by Non Empty,
		// will not be visible.
		// It would be possible to add it (again) to the axis, which must be
		// avoided

		Axis axis = Axis.Factory.forOrdinal(quax.getOrdinal());

		CellSet cellSet = adapter.getModel().getCellSet();
		CellSetAxis cellAxis = getCellSetAxis(cellSet, axis);

		if (cellAxis == null) {
			return Collections.emptyList();
		}

		List<Position> positions = cellAxis.getPositions();
		for (Position position : positions) {
			List<Member> members = position.getMembers();
			Member member = members.get(iDim);
			if (member != null && !visibleMembers.contains(member)) {
				visibleMembers.add(member);
			}
		}

		return visibleMembers;
	}
}
