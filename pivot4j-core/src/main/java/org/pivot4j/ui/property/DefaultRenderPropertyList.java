/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.property;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.pivot4j.ui.condition.ConditionFactory;

public class DefaultRenderPropertyList implements RenderPropertyList {

    private SortedMap<String, RenderProperty> properties = new TreeMap<String, RenderProperty>();

    private ConditionFactory conditionFactory;

    /**
     * @param conditionFactory
     */
    public DefaultRenderPropertyList(ConditionFactory conditionFactory) {
        if (conditionFactory == null) {
            throw new NullArgumentException("conditionFactory");
        }

        this.conditionFactory = conditionFactory;
    }

    protected ConditionFactory getConditionFactory() {
        return conditionFactory;
    }

    /**
     * @return the renderProperties
     * @see org.pivot4j.ui.property.RenderPropertyList#getRenderProperties()
     */
    public List<RenderProperty> getRenderProperties() {
        return new LinkedList<RenderProperty>(properties.values());
    }

    /**
     * @param name
     * @see
     * org.pivot4j.ui.property.RenderPropertyList#getRenderProperty(java.lang.String)
     */
    public RenderProperty getRenderProperty(String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        return properties.get(name);
    }

    /**
     * @param property
     * @see
     * org.pivot4j.ui.property.RenderPropertyList#setRenderProperty(org.pivot4j.ui.property.RenderProperty)
     */
    public void setRenderProperty(RenderProperty property) {
        if (property == null) {
            throw new NullArgumentException("property");
        }

        properties.put(property.getName(), property);
    }

    /**
     * @param name
     * @see
     * org.pivot4j.ui.property.RenderPropertyList#removeRenderProperty(java.lang.String)
     */
    public void removeRenderProperty(String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        properties.remove(name);
    }

    /**
     * @param name
     * @see
     * org.pivot4j.ui.property.RenderPropertyList#hasRenderProperty(java.lang.String)
     */
    public boolean hasRenderProperty(String name) {
        if (name == null) {
            throw new NullArgumentException("name");
        }

        return properties.containsKey(name);
    }

    /**
     * @see org.pivot4j.state.Bookmarkable#saveState()
     */
    @Override
    public Serializable saveState() {
        Collection<RenderProperty> propertyList = this.properties.values();

        Serializable[] states = new Serializable[propertyList.size()];

        int index = 0;
        for (RenderProperty property : propertyList) {
            boolean conditional = property instanceof ConditionalRenderProperty;

            states[index++] = new Serializable[]{conditional,
                property.saveState()};
        }

        return states;
    }

    /**
     * @see org.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
     */
    @Override
    public void restoreState(Serializable state) {
        if (state == null) {
            throw new NullArgumentException("state");
        }

        Serializable[] states = (Serializable[]) state;

        this.properties.clear();

        for (Serializable st : states) {
            Serializable[] pair = (Serializable[]) st;

            boolean conditional = (Boolean) pair[0];

            RenderProperty property;

            // TODO Need more robust method to determine property types.
            if (conditional) {
                property = new ConditionalRenderProperty(conditionFactory);
            } else {
                property = new SimpleRenderProperty();
            }

            property.restoreState(pair[1]);

            this.properties.put(property.getName(), property);
        }
    }

    /**
     * @see
     * org.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public void saveSettings(HierarchicalConfiguration configuration) {
        int index = 0;

        for (RenderProperty property : properties.values()) {
            String name = String.format("property(%s)", index++);

            configuration.setProperty(name, "");

            SubnodeConfiguration propertyConfig = configuration
                    .configurationAt(name);
            property.saveSettings(propertyConfig);
        }
    }

    /**
     * @see
     * org.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public void restoreSettings(HierarchicalConfiguration configuration) {
        this.properties.clear();

        try {
            List<HierarchicalConfiguration> configurations = configuration
                    .configurationsAt("property");

            for (HierarchicalConfiguration propertyConfig : configurations) {
                boolean conditional = propertyConfig.containsKey("conditions");

                RenderProperty property;

                // TODO Need more robust method to determine property types.
                if (conditional) {
                    property = new ConditionalRenderProperty(conditionFactory);
                } else {
                    property = new SimpleRenderProperty();
                }

                property.restoreSettings(propertyConfig);

                this.properties.put(property.getName(), property);
            }
        } catch (IllegalArgumentException e) {
        }
    }
}
