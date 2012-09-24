/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import java.util.List;

import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.DrillExpandPosition;

public class DrillExpandPositionImpl extends AbstractTransform implements
		DrillExpandPosition {

	/**
	 * @param queryAdapter
	 */
	public DrillExpandPositionImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillExpandPosition#canExpand(org.olap4j.Position,
	 *      org.olap4j.metadata.Member)
	 * @param position
	 *            position to be expanded
	 * @param member
	 *            member to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canExpand(Position position, Member member) {
		List<Member> pathMembers = memberPath(position, member);
		return getQueryAdapter().canExpand(pathMembers);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillExpandPosition#canCollapse(org.olap4j.Position,
	 *      org.olap4j.metadata.Member)
	 * @param position
	 *            position to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canCollapse(Position position, Member member) {
		List<Member> pathMembers = memberPath(position, member);
		return getQueryAdapter().canCollapse(pathMembers);

	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillExpandPosition#expand(org.olap4j.Position,
	 *      org.olap4j.metadata.Member)
	 * @param position
	 *            position to be expanded
	 * @param member
	 *            member to be expanded
	 */
	public void expand(Position position, Member member) {
		List<Member> pathMembers = memberPath(position, member);
		getQueryAdapter().expand(pathMembers);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillExpandPosition#collapse(org.olap4j.Position,
	 *      org.olap4j.metadata.Member)
	 * @param position
	 *            position to be collapsed
	 * @param position
	 *            member to be collapsed
	 */
	public void collapse(Position position, Member member) {
		List<Member> pathMembers = memberPath(position, member);
		getQueryAdapter().collapse(pathMembers);
	}

	/**
	 * determine path to member
	 * 
	 * @param position
	 * @param member
	 * @return path to Member
	 */
	private List<Member> memberPath(Position position, Member member) {
		List<Member> posMembers = position.getMembers();

		int index = posMembers.indexOf(member);
		if (index < 0) {
			throw new IllegalArgumentException(
					"Position does not contains the specified member.");
		}

		return posMembers.subList(0, index + 1);
	}
}
