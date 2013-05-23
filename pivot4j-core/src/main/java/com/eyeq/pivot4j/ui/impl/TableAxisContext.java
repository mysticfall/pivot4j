/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;

public class TableAxisContext implements Cloneable {

	private PivotRenderer renderer;

	private Axis axis;

	private List<Hierarchy> hierarchies;

	private Map<AggregatorPosition, List<Aggregator>> aggregators;

	private Map<Hierarchy, List<Level>> levelMap;

	private Map<Level, List<Property>> propertyMap;

	private Map<String, Member> cachedParents = new HashMap<String, Member>();

	/**
	 * @param axis
	 * @param hierarchies
	 * @param levels
	 * @param aggregators
	 * @param renderer
	 */
	public TableAxisContext(Axis axis, List<Hierarchy> hierarchies,
			Map<Hierarchy, List<Level>> levels,
			Map<AggregatorPosition, List<Aggregator>> aggregators,
			PivotRenderer renderer) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (hierarchies == null) {
			throw new NullArgumentException("hierarchies");
		}

		if (levels == null) {
			throw new NullArgumentException("levels");
		}

		if (renderer == null) {
			throw new NullArgumentException("renderer");
		}

		this.axis = axis;
		this.hierarchies = hierarchies;
		this.levelMap = levels;
		this.renderer = renderer;

		if (aggregators == null) {
			this.aggregators = new HashMap<AggregatorPosition, List<Aggregator>>();
		} else {
			this.aggregators = aggregators;
		}
	}

	/**
	 * @return the axis
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @return the renderer
	 */
	public PivotRenderer getPivotRenderer() {
		return renderer;
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
	 * @param position
	 * @return
	 */
	public List<Aggregator> getAggregators(AggregatorPosition position) {
		List<Aggregator> result = aggregators.get(position);

		if (result == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(result);
		}
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

	/**
	 * Temporary workaround for performance issue.
	 * 
	 * See http://jira.pentaho.com/browse/MONDRIAN-1292
	 * 
	 * @param member
	 * @return
	 */
	public Member getParentMember(Member member) {
		Member parent = cachedParents.get(member.getUniqueName());

		if (parent == null) {
			parent = member.getParentMember();
			cachedParents.put(member.getUniqueName(), parent);
		}

		return parent;
	}

	/**
	 * Temporary workaround for performance issue.
	 * 
	 * See http://jira.pentaho.com/browse/MONDRIAN-1292
	 * 
	 * @param member
	 * @return
	 */
	public List<Member> getAncestorMembers(Member member) {
		List<Member> ancestors = new ArrayList<Member>();

		Member parent = member;

		while ((parent = getParentMember(parent)) != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}

	Map<String, Member> getParentMembersCache() {
		return cachedParents;
	}
}
