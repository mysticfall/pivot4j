/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;

public class TableAxisContext implements Cloneable {

	private Axis axis;

	private List<Hierarchy> hierarchies;

	private Map<Hierarchy, List<Level>> levels;

	/**
	 * @param axis
	 * @param hierarchies
	 * @param rootLevels
	 */
	public TableAxisContext(Axis axis, List<Hierarchy> hierarchies,
			Map<Hierarchy, List<Level>> levels) {
		if (axis == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'axis'.");
		}

		if (hierarchies == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		if (levels == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'levels'.");
		}

		this.axis = axis;
		this.hierarchies = hierarchies;
		this.levels = levels;
	}

	/**
	 * @return the axis
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @return the hierarchies
	 */
	public List<Hierarchy> getHierarchies() {
		return Collections.unmodifiableList(hierarchies);
	}

	/**
	 * @return the rootLevels
	 */
	public List<Level> getLevels(Hierarchy hierarchy) {
		return Collections.unmodifiableList(levels.get(hierarchy));
	}
}
