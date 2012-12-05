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
 * Allows to place selected members on the slicer axis.
 */
public interface ChangeSlicer extends Transform {

	List<Hierarchy> getHierarchies();

	/**
	 * @return the current slicer.
	 */
	List<Member> getSlicer();

	/**
	 * @param hierarchy
	 * @return
	 */
	List<Member> getSlicer(Hierarchy hierarchy);

	/**
	 * sets the slicer
	 */
	void setSlicer(List<Member> members);

	/**
	 * @param hierarchy
	 * @param members
	 */
	void setSlicer(Hierarchy hierarchy, List<Member> members);
}
