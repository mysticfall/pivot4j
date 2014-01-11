/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform;

import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

/**
 * Substitues the members of a hierarchy with the children of a member. Example:
 * A table shows the continents "America", "Asia", "Europe" etc. DrillReplace
 * for "Europe" will replace the continents with the countries of "Europe".
 */
public interface DrillReplace extends Transform {

	/**
	 * Drill down is possible if <code>member</code> has children
	 */
	boolean canDrillDown(Member member);

	/**
	 * Drill up is possible if not all members of the top level hierarchy are
	 * shown.
	 */
	boolean canDrillUp(Hierarchy hierarchy);

	/**
	 * Replaces the members. Let <code>H</code> be the hierarchy that member
	 * belongs to. Then drillDown will replace all members from <code>H</code>
	 * that are currently visible with the children of <code>member</code>.
	 */
	void drillDown(Member member);

	/**
	 * Replaces all visible members of hier with the members of the next higher
	 * level.
	 */
	void drillUp(Hierarchy hierarchy);
}
