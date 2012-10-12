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

import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;

/**
 * Allows to place hierarchies on the visible query axes.
 * <p>
 * Use Case.
 * <ul>
 * <li>The GUI will examine the result of the olap query to find out which
 * hierarchies are currently displayed on what axes. It will use
 * Axis.getHierarchies() for this
 * 
 * <li>Then it will find out what Hierarchies exist by calling
 * OlapModel.getDimensions() and Dimension.getHierarchies().
 * 
 * <li>The Information will be presented to the user and he will be allowed to
 * change the mapping between axes and hierarchies.
 * 
 * <li>For every Hierarchy that the user selected for display on an axis, the
 * GUI will call createMemberExpression().
 * 
 * <li>For each axis the system will build the the array of memberExpressions
 * and call setAxis once.
 * </ul>
 */
public interface PlaceHierarchiesOnAxes extends Transform {

	/**
	 * @param axis
	 *            The target axis
	 * @param hierarchies
	 *            The hierarchies to put
	 * @param expandAllMember
	 *            If this flag is set and an "All" member is put onto an axis,
	 *            the children of the All member will be added as well.
	 */
	void placeHierarchies(Axis axis, List<Hierarchy> hierarchies,
			boolean expandAllMember);

	/**
	 * Collects all hierarchies on a given axis in the result. If no hierarchies
	 * are visible, it returns an empty list.
	 * 
	 * @param axis
	 *            the axis to use
	 * @return A list of hierarchies
	 */
	List<Hierarchy> findVisibleHierarchies(Axis axis);
}
