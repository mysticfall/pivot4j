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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.mdx.metadata.MemberExp;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.ChangeSlicer;
import com.eyeq.pivot4j.util.OlapUtils;

public class ChangeSlicerImpl extends AbstractTransform implements ChangeSlicer {

	/**
	 * @param queryAdapter
	 */
	public ChangeSlicerImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#getHierarchies()
	 */
	@Override
	public List<Hierarchy> getHierarchies() {
		if (!getModel().isInitialized()) {
			return Collections.emptyList();
		}

		CellSet cellSet = getModel().getCellSet();
		if (cellSet == null) {
			return Collections.emptyList();
		}

		CellSetAxis slicer = cellSet.getFilterAxis();

		return slicer.getAxisMetaData().getHierarchies();
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#getSlicer()
	 */
	public List<Member> getSlicer() {
		if (!getModel().isInitialized()) {
			return Collections.emptyList();
		}

		// Use result rather than query
		CellSet cellSet = getModel().getCellSet();
		CellSetAxis slicer = cellSet.getFilterAxis();

		List<Position> positions = slicer.getPositions();
		List<Member> members = new ArrayList<Member>();

		for (Position position : positions) {
			List<Member> posMembers = position.getMembers();
			for (Member posMember : posMembers) {
				if (!members.contains(posMember)) {
					members.add(posMember);
				}
			}
		}

		return members;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#getSlicer(org.olap4j.metadata
	 *      .Hierarchy)
	 */
	@Override
	public List<Member> getSlicer(Hierarchy hierarchy) {
		if (hierarchy == null) {
			return getSlicer();
		}

		CellSet cellSet = getModel().getCellSet();
		CellSetAxis slicer = cellSet.getFilterAxis();

		List<Position> positions = slicer.getPositions();
		List<Member> members = new ArrayList<Member>();

		for (Position position : positions) {
			List<Member> posMembers = position.getMembers();
			for (Member posMember : posMembers) {
				if (OlapUtils.equals(posMember.getHierarchy(), hierarchy)
						&& !members.contains(posMember)) {
					members.add(posMember);
				}
			}
		}

		return members;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#setSlicer(java.util.List)
	 */
	public void setSlicer(List<Member> members) {
		Exp exp = null;

		if (members != null && !members.isEmpty()) {
			List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(
					members.size());

			Map<Hierarchy, List<Member>> memberMap = new HashMap<Hierarchy, List<Member>>();
			for (Member member : members) {
				Hierarchy hierarchy = member.getHierarchy();

				if (!hierarchies.contains(hierarchy)) {
					hierarchies.add(hierarchy);
				}

				List<Member> hierarchyMembers = memberMap.get(hierarchy);
				if (hierarchyMembers == null) {
					hierarchyMembers = new ArrayList<Member>(members.size());
					memberMap.put(hierarchy, hierarchyMembers);
				}

				hierarchyMembers.add(member);
			}

			if (hierarchies.size() == 1) {
				Hierarchy hierarchy = hierarchies.get(0);
				exp = createMemberSetExpression(hierarchy,
						memberMap.get(hierarchy));
			} else {
				int index = 0;

				Exp[] sets = new Exp[2];

				for (Hierarchy hierarchy : hierarchies) {
					Exp set = createMemberSetExpression(hierarchy,
							memberMap.get(hierarchy));
					if (set == null) {
						continue;
					}

					if (index < 2) {
						sets[index] = set;
					} else {
						sets[0] = new FunCall("CrossJoin", Syntax.Function,
								Arrays.asList(sets));
						sets[1] = set;
					}

					index++;
				}

				exp = new FunCall("CrossJoin", Syntax.Function,
						Arrays.asList(sets));
			}
		}

		getQueryAdapter().changeSlicer(exp);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#setSlicer(org.olap4j.metadata
	 *      .Hierarchy, java.util.List)
	 */
	@Override
	public void setSlicer(Hierarchy hierarchy, List<Member> members) {
		if (hierarchy == null) {
			setSlicer(members);
			return;
		}

		Exp exp = null;

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();

		Map<Hierarchy, List<Member>> memberMap = new HashMap<Hierarchy, List<Member>>();

		List<Member> membersOnSlicer = getSlicer();

		if (membersOnSlicer != null && !membersOnSlicer.isEmpty()) {
			for (Member member : membersOnSlicer) {
				Hierarchy memberHierarchy = member.getHierarchy();

				if (!hierarchies.contains(memberHierarchy)) {
					hierarchies.add(memberHierarchy);
				}

				if (!OlapUtils.equals(memberHierarchy, hierarchy)) {
					List<Member> hierarchyMembers = memberMap
							.get(memberHierarchy);
					if (hierarchyMembers == null) {
						hierarchyMembers = new ArrayList<Member>();
						memberMap.put(memberHierarchy, hierarchyMembers);
					}

					hierarchyMembers.add(member);
				}
			}
		}

		if (members == null || members.isEmpty()) {
			hierarchies.remove(hierarchy);
		} else {
			if (!hierarchies.contains(hierarchy)) {
				hierarchies.add(hierarchy);
			}

			memberMap.put(hierarchy, members);
		}

		int size = hierarchies.size();
		if (size == 1) {
			Hierarchy hier = hierarchies.get(0);
			exp = createMemberSetExpression(hier, memberMap.get(hier));
		} else if (size > 1) {
			int index = 0;

			Exp[] sets = new Exp[2];

			for (Hierarchy hier : hierarchies) {
				Exp set = createMemberSetExpression(hier, memberMap.get(hier));
				if (set == null) {
					continue;
				}

				if (index < 2) {
					sets[index] = set;
				} else {
					sets[0] = new FunCall("CrossJoin", Syntax.Function,
							Arrays.asList(sets));
					sets[1] = set;
				}

				index++;
			}

			exp = new FunCall("CrossJoin", Syntax.Function, Arrays.asList(sets));
		}

		getQueryAdapter().changeSlicer(exp);
	}

	/**
	 * @param hierachy
	 * @param members
	 * @return
	 */
	protected Exp createMemberSetExpression(Hierarchy hierachy,
			List<Member> members) {
		if (members == null || members.isEmpty()) {
			return null;
		} else if (members.size() == 1) {
			return new MemberExp(members.get(0));
		}

		List<Exp> expressions = new ArrayList<Exp>(members.size());
		for (Member member : members) {
			expressions.add(new MemberExp(member));
		}

		return new FunCall("{}", Syntax.Braces, expressions);
	}
}
