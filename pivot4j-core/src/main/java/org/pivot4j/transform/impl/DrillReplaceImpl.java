/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import org.olap4j.OlapConnection;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.impl.QueryAdapter;
import org.pivot4j.transform.AbstractTransform;
import org.pivot4j.transform.DrillReplace;

public class DrillReplaceImpl extends AbstractTransform implements DrillReplace {

	/**
	 * @param queryAdapter
	 * @param connection
	 */
	public DrillReplaceImpl(QueryAdapter queryAdapter, OlapConnection connection) {
		super(queryAdapter, connection);
	}

	/**
	 * Drill down is possible if <code>member</code> has children
	 * 
	 * @see org.pivot4j.transform.DrillReplace#canDrillDown(org.olap4j.metadata.Member)
	 */
	public boolean canDrillDown(Member member) {
		return getQueryAdapter().canDrillDown(member);
	}

	/**
	 * Drill up is possible if not all members of the top level hierarchy are
	 * shown.
	 * 
	 * @see org.pivot4j.transform.DrillReplace#canDrillUp(org.olap4j.metadata.Hierarchy)
	 */
	public boolean canDrillUp(Hierarchy hierarchy) {
		return getQueryAdapter().canDrillUp(hierarchy);
	}

	/**
	 * Replaces the members. Let <code>H</code> be the hierarchy that member
	 * belongs to. Then drillDown will replace all members from <code>H</code>
	 * that are currently visible with the children of <code>member</code>.
	 * 
	 * @see org.pivot4j.transform.DrillReplace#drillDown(org.olap4j.metadata.Member)
	 */
	public void drillDown(Member member) {
		getQueryAdapter().drillDown(member);
	}

	/**
	 * Replaces all visible members of hier with the members of the next higher
	 * level.
	 * 
	 * @see org.pivot4j.transform.DrillReplace#drillUp(org.olap4j.metadata.Hierarchy)
	 */
	public void drillUp(Hierarchy hierarchy) {
		getQueryAdapter().drillUp(hierarchy);
	}
}
