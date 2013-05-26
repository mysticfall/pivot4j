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
import org.olap4j.metadata.Level;

/**
 * Allows to place levels on the visible query axes.
 * <p>
 * Use Case.
 * <ul>
 * <li>The GUI will examine the result of the olap query to find out which
 * levels are currently displayed on what axes.</li>
 * </ul>
 */
public interface PlaceLevelsOnAxes extends Transform {

	/**
	 * @param axis
	 *            The target axis
	 * @param levels
	 *            The levels to put
	 */
	void placeLevels(Axis axis, List<Level> levels);

	/**
	 * @param axis
	 *            The target axis
	 * @param level
	 *            The level to add
	 * @param position
	 *            The position index where to add the level. Any value less than
	 *            ZERO will put the hierarchy at the end of the axis. Note that
	 *            it's the index of the parent hierarchy, not the level itself
	 *            since the order of a level in a hierarchy cannot be changed by
	 *            definition. If the parent hierarchy exists on any axis, the
	 *            position argument will be ignored.
	 */
	void addLevel(Axis axis, Level level, int position);

	/**
	 * @param axis
	 *            The target axis
	 * @param level
	 *            The level to remove
	 */
	void removeLevel(Axis axis, Level level);

	/**
	 * Collects all levels on a given axis in the result. If no levels are
	 * visible, it returns an empty list.
	 * 
	 * @param axis
	 *            the axis to use
	 * @return A list of levels
	 */
	List<Level> findVisibleLevels(Axis axis);

	/**
	 * Collects all levels on a given hierarchy in the result. If no levels are
	 * visible, it returns an empty list.
	 * 
	 * @param hierarchy
	 *            the hierarchy to use
	 * @return A list of levels
	 */
	List<Level> findVisibleLevels(Hierarchy hierarchy);
}
