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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.ui.PivotRenderer;

public class TableAxisContext implements Cloneable {

	private PivotRenderer renderer;

	private Axis axis;

	private List<Hierarchy> hierarchies;

	private Map<Hierarchy, List<Level>> levelMap;

	private Map<Level, List<Property>> propertyMap;

	/**
	 * @param axis
	 * @param hierarchies
	 * @param levels
	 * @param renderer
	 */
	public TableAxisContext(Axis axis, List<Hierarchy> hierarchies,
			Map<Hierarchy, List<Level>> levels, PivotRenderer renderer) {
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
					"Missing required argument 'levelMap'.");
		}

		if (renderer == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'renderer'.");
		}

		this.axis = axis;
		this.hierarchies = hierarchies;
		this.levelMap = levels;
		this.renderer = renderer;
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
		return Collections.unmodifiableList(levelMap.get(hierarchy));
	}

	/**
	 * @return the renderer
	 */
	public PivotRenderer getPivotRenderer() {
		return renderer;
	}

	/**
	 * @param level
	 * @return
	 */
	public List<Property> getProperties(Level level) {
		if (renderer.getPropertyCollector() == null) {
			return Collections.emptyList();
		}

		if (propertyMap == null) {
			this.propertyMap = new HashMap<Level, List<Property>>();
		}

		List<Property> properties = propertyMap.get(level);

		if (properties == null) {
			properties = renderer.getPropertyCollector().getProperties(level);
			propertyMap.put(level, properties);
		}

		return Collections.unmodifiableList(properties);
	}
}
