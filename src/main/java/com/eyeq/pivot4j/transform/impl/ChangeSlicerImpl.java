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
import java.util.List;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.NotInitializedException;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.MemberExp;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.ChangeSlicer;

public class ChangeSlicerImpl extends AbstractTransform implements ChangeSlicer {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param queryAdapter
	 */
	public ChangeSlicerImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#getSlicer()
	 */
	public List<Member> getSlicer() {
		// Use result rather than query
		CellSet cellSet;

		try {
			cellSet = getQueryAdapter().getModel().getCellSet();
		} catch (NotInitializedException e) {
			return Collections.emptyList();
		}

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
	 * @see com.eyeq.pivot4j.transform.ChangeSlicer#setSlicer(java.util.List)
	 */
	public void setSlicer(List<Member> members) {
		Exp slicerExp = null;

		if (members.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Slicer set to null.");
			}
		} else {
			List<Exp> collectedMemberExpressions = new ArrayList<Exp>();
			List<Exp> conditions = new ArrayList<Exp>();

			String hierarchyName = "";
			String prevHierarchyName = "";

			FunCall func = null;

			boolean firstCondition = true;

			for (Member member : members) {
				String uniqueName = member.getUniqueName();

				hierarchyName = uniqueName
						.substring(1, uniqueName.indexOf("]"));

				if (!hierarchyName.equals(prevHierarchyName)) {
					if (!collectedMemberExpressions.isEmpty()) {
						if (firstCondition) {
							func = new FunCall(
									"{}",
									collectedMemberExpressions
											.toArray(new Exp[collectedMemberExpressions
													.size()]), Syntax.Braces);
						} else {
							conditions
									.add(new FunCall(
											"{}",
											collectedMemberExpressions
													.toArray(new Exp[collectedMemberExpressions
															.size()]),
											Syntax.Braces));
							func = new FunCall("CrossJoin",
									conditions.toArray(new Exp[conditions
											.size()]), Syntax.Function);
							conditions.clear();
						}

						conditions.add(func);
						firstCondition = false;

						if (logger.isInfoEnabled()) {
							logger.info("Added a new filter condition for Hierarchy: "
									+ prevHierarchyName
									+ ", Conditions number: "
									+ collectedMemberExpressions.size());
						}

						collectedMemberExpressions.clear();

						if (logger.isInfoEnabled()) {
							logger.info("Clear conditions list. Size = "
									+ collectedMemberExpressions.size());
						}
					}

					prevHierarchyName = hierarchyName;

					if (logger.isInfoEnabled()) {
						logger.info("Collecting filters on member: "
								+ hierarchyName);
					}
				}

				collectedMemberExpressions.add(new MemberExp(member));
			}

			// Add lastly collected member to filters conditions list
			if (!collectedMemberExpressions.isEmpty()) {
				if (members.size() == 1) {
					conditions.add(collectedMemberExpressions.get(0));
				} else {
					conditions.add(new FunCall("{}",
							collectedMemberExpressions
									.toArray(new Exp[collectedMemberExpressions
											.size()]), Syntax.Braces));
				}

				if (logger.isInfoEnabled()) {
					logger.info("Added a new filter condition for Hierarchy: "
							+ hierarchyName);
				}
			}

			if (conditions.size() == 1) {
				slicerExp = conditions.get(0);
			} else {
				// SeraSoft - More dimensions selected. Build a CrossJoin
				// function
				FunCall intersectConditions = new FunCall("Crossjoin",
						conditions.toArray(new Exp[conditions.size()]),
						Syntax.Function);

				slicerExp = intersectConditions;
			}
		}

		getQueryAdapter().changeSlicer(slicerExp);
	}
}
