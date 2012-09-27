/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform;

import java.util.List;

import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

/**
 * Allows to place a set of members on a visible query axis.
 */
public interface PlaceMembersOnAxes extends Transform {

	/**
	 * @param members
	 *            a List of Members
	 */
	void placeMembers(List<Member> members);

	/**
	 * Collects all members from the visible axes in the result. If no members
	 * of the hierarchy are on a visible axis, returns an empty list.
	 * 
	 * @param hier
	 *            the Hierarchy
	 * @return A list of Members
	 */
	List<Member> findVisibleMembers(Hierarchy hierarchy);
}
