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
import com.eyeq.pivot4j.query.Quax;
import com.eyeq.pivot4j.query.QuaxUtil;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;

public class PlaceMembersOnAxesImpl extends AbstractTransform implements
		PlaceMembersOnAxes {

	protected static Logger logger = LoggerFactory
			.getLogger(PlaceHierarchiesOnAxesImpl.class);

	/**
	 * @param queryAdapter
	 */
	public PlaceMembersOnAxesImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.PlaceMembersOnAxes#placeMembers(java.util.
	 *      List)
	 */
	public void placeMembers(List<Member> members) {
		QueryAdapter adapter = getQueryAdapter();

		Map<Quax, List<Hierarchy>> hierarchyMap = new HashMap<Quax, List<Hierarchy>>();
		for (Member member : members) {
			Quax quax = adapter.findQuax(member.getDimension());

			if (quax == null) {
				if (logger.isWarnEnabled()) {
					logger.warn("Unable to find query axis for dimension : "
							+ member.getDimension().getUniqueName());
				}

				continue;
			}

			List<Hierarchy> hierarchies = hierarchyMap.get(quax);
			if (hierarchies == null) {
				hierarchies = new ArrayList<Hierarchy>();
				hierarchyMap.put(quax, hierarchies);
			}

			if (!hierarchies.contains(member.getHierarchy())) {
				hierarchies.add(member.getHierarchy());
			}
		}

		for (Quax quax : hierarchyMap.keySet()) {
			List<Hierarchy> hierarchies = hierarchyMap.get(quax);

			List<Exp> expressions = new ArrayList<Exp>(hierarchies.size());

			for (Hierarchy hierarchy : hierarchies) {
				List<Exp> sets = new ArrayList<Exp>(members.size());

				for (Member member : members) {
					if (hierarchy.equals(member.getHierarchy())) {
						sets.add(QuaxUtil.expForMember(member));
					}
				}

				if (sets.size() == 1) {
					expressions.add(sets.get(0));
				} else {
					expressions.add(new FunCall("{}", sets.toArray(new Exp[sets
							.size()]), Syntax.Braces));
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

		CellSet cellSet = adapter.getModel().getCellSet();

		// locate the appropriate result axis
		int iAx = quax.getOrdinal();
		if (adapter.isAxesSwapped()) {
			iAx = (iAx + 1) % 2;
		}

		CellSetAxis axis = cellSet.getAxes().get(iAx);

		List<Position> positions = axis.getPositions();
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
