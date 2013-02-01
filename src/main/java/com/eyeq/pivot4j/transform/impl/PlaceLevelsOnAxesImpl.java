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
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.mdx.metadata.MemberExp;
import com.eyeq.pivot4j.query.Quax;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.PlaceHierarchiesOnAxes;
import com.eyeq.pivot4j.transform.PlaceLevelsOnAxes;
import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;

public class PlaceLevelsOnAxesImpl extends AbstractTransform implements
		PlaceLevelsOnAxes {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param queryAdapter
	 */
	public PlaceLevelsOnAxesImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceLevelsOnAxes#placeLevels(org.olap4j.Axis,
	 *      java.util.List)
	 */
	@Override
	public void placeLevels(Axis axis, List<Level> levels) {
		try {
			QueryAdapter adapter = getQueryAdapter();

			Quax quax = adapter.getQuaxes().get(axis.axisOrdinal());

			List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(
					levels.size());
			Map<Hierarchy, List<Member>> memberMap = new HashMap<Hierarchy, List<Member>>(
					hierarchies.size());

			for (Level level : levels) {
				Hierarchy hierarchy = level.getHierarchy();

				List<Member> members = level.getMembers();

				for (Member member : members) {
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
			}

			List<Exp> expressions = new ArrayList<Exp>(hierarchies.size());

			for (Hierarchy hierarchy : hierarchies) {
				List<Member> selection = memberMap.get(hierarchy);

				List<Exp> sets = new ArrayList<Exp>(selection.size());

				if (selection.size() == 1) {
					expressions.add(new MemberExp(selection.get(0)));
				} else {
					for (Member member : selection) {
						sets.add(new MemberExp(member));
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
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceLevelsOnAxes#addLevel(org.olap4j.Axis,
	 *      org.olap4j.metadata.Level, int)
	 */
	@Override
	public void addLevel(Axis axis, Level level, int position) {
		PlaceHierarchiesOnAxes hierarchyTransform = getQueryAdapter()
				.getModel().getTransform(PlaceHierarchiesOnAxes.class);
		List<Hierarchy> hierarchies = hierarchyTransform
				.findVisibleHierarchies(axis);

		PlaceMembersOnAxes membersTransform = getQueryAdapter().getModel()
				.getTransform(PlaceMembersOnAxes.class);

		Hierarchy hierarchy = level.getHierarchy();
		if (hierarchies.contains(hierarchy)) {
			try {
				membersTransform.addMembers(hierarchy, level.getMembers());
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		} else {
			hierarchies = new ArrayList<Hierarchy>(hierarchies);

			if (position < 0 || position >= hierarchies.size()) {
				hierarchies.add(hierarchy);
			} else {
				hierarchies.add(position, hierarchy);
			}

			List<Member> selection = new ArrayList<Member>();

			for (Hierarchy hier : hierarchies) {
				selection.addAll(membersTransform.findVisibleMembers(hier));

				if (hier.equals(hierarchy)) {
					List<Member> members;
					try {
						members = level.getMembers();
					} catch (OlapException e) {
						throw new PivotException(e);
					}

					for (Member member : members) {
						if (!selection.contains(member)) {
							selection.add(member);
						}
					}
				}
			}

			membersTransform.placeMembers(axis, selection);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceLevelsOnAxes#removeLevel(org.olap4j.Axis,
	 *      org.olap4j.metadata.Level)
	 */
	@Override
	public void removeLevel(Axis axis, Level level) {
		PlaceMembersOnAxes transform = getQueryAdapter().getModel()
				.getTransform(PlaceMembersOnAxes.class);

		Hierarchy hierarchy = level.getHierarchy();

		List<Member> members = transform.findVisibleMembers(axis);
		List<Member> selection = new ArrayList<Member>(members.size());

		for (Member member : members) {
			if (!member.getLevel().equals(level)) {
				selection.add(member);
			}
		}

		transform.placeMembers(hierarchy, selection);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceLevelsOnAxes#findVisibleLevels(org.olap4j
	 *      .Axis)
	 */
	@Override
	public List<Level> findVisibleLevels(Axis axis) {
		List<Level> visibleLevels = new ArrayList<Level>();

		QueryAdapter adapter = getQueryAdapter();

		CellSet cellSet = adapter.getModel().getCellSet();

		// locate the appropriate result axis
		int iQuax = axis.axisOrdinal();
		if (adapter.isAxesSwapped()) {
			iQuax = (iQuax + 1) % 2;
		}

		CellSetAxis cellAxis = cellSet.getAxes().get(iQuax);

		List<Position> positions = cellAxis.getPositions();

		if (positions.isEmpty()) {
			return Collections.emptyList();
		}

		int size = positions.get(0).getMembers().size();

		for (int i = 0; i < size; i++) {
			for (Position position : positions) {
				Member member = position.getMembers().get(i);

				if (!visibleLevels.contains(member.getLevel())) {
					visibleLevels.add(member.getLevel());
				}
			}
		}

		return visibleLevels;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceLevelsOnAxes#findVisibleLevels(org.olap4j
	 *      .metadata.Hierarchy)
	 */
	@Override
	public List<Level> findVisibleLevels(Hierarchy hierarchy) {
		PlaceMembersOnAxes transform = getQueryAdapter().getModel()
				.getTransform(PlaceMembersOnAxes.class);

		List<Member> members = transform.findVisibleMembers(hierarchy);
		List<Level> visibleLevels = new ArrayList<Level>(members.size());

		for (Member member : members) {
			if (!visibleLevels.contains(member.getLevel())) {
				visibleLevels.add(member.getLevel());
			}
		}

		return visibleLevels;
	}
}
