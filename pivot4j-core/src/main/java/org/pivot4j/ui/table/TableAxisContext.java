/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Property;
import org.pivot4j.ui.aggregator.Aggregator;
import org.pivot4j.ui.aggregator.AggregatorPosition;
import org.pivot4j.ui.collector.PropertyCollector;
import org.pivot4j.util.MemberHierarchyCache;

class TableAxisContext implements Cloneable {

    private Axis axis;

    private List<Hierarchy> hierarchies;

    private Map<AggregatorPosition, List<Aggregator>> aggregators;

    private Map<Hierarchy, List<Level>> levelMap;

    private Map<Level, List<Property>> propertyMap;

    private TableRenderer renderer;

    private MemberHierarchyCache memberHierarchyCache;

    private Map<String, Integer> rowSpanCache = new HashMap<String, Integer>();

    /**
     * @param cube
     * @param axis
     * @param hierarchies
     * @param levels
     * @param aggregators
     * @param cache
     * @param renderer
     */
    TableAxisContext(Cube cube, Axis axis, List<Hierarchy> hierarchies,
            Map<Hierarchy, List<Level>> levels,
            Map<AggregatorPosition, List<Aggregator>> aggregators,
            MemberHierarchyCache cache, TableRenderer renderer) {
        if (cube == null) {
            throw new NullArgumentException("cube");
        }

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

        if (aggregators == null) {
            this.aggregators = new HashMap<AggregatorPosition, List<Aggregator>>();
        } else {
            this.aggregators = aggregators;
        }

        if (cache == null) {
            this.memberHierarchyCache = new MemberHierarchyCache(cube);
        } else {
            this.memberHierarchyCache = cache;
        }

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
     * @return the renderer
     */
    public TableRenderer getRenderer() {
        return renderer;
    }

    /**
     * @param level
     * @return
     */
    public List<Property> getProperties(Level level) {
        PropertyCollector collector = renderer.getPropertyCollector();

        if (collector == null) {
            return Collections.emptyList();
        }

        if (propertyMap == null) {
            this.propertyMap = new HashMap<Level, List<Property>>();
        }

        List<Property> properties = propertyMap.get(level);

        if (properties == null) {
            properties = collector.getProperties(level);
            propertyMap.put(level, properties);
        }

        return Collections.unmodifiableList(properties);
    }

    /**
     * @return the memberHierarchyCache
     */
    public MemberHierarchyCache getMemberHierarchyCache() {
        return memberHierarchyCache;
    }

    /**
     * @return the rowSpanCache
     */
    public Map<String, Integer> getRowSpanCache() {
        return rowSpanCache;
    }
}
