/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.collector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

public class ConfigurablePropertyCollector implements PropertyCollector,
        Serializable {

    private static final long serialVersionUID = -2831138114754450877L;

    private List<String> propertyNames;

    /**
     * @param propertyNames
     */
    public ConfigurablePropertyCollector(List<String> propertyNames) {
        if (propertyNames == null) {
            throw new NullArgumentException("propertyNames");
        }

        this.propertyNames = propertyNames;
    }

    /**
     * @return the propertyNames
     */
    protected List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * @see
     * org.pivot4j.ui.collector.PropertyCollector#getProperties(org.olap4j.metadata.Level)
     */
    @Override
    public List<Property> getProperties(Level level) {
        List<Property> selection = new ArrayList<Property>(propertyNames.size());
        NamedList<Property> properties = level.getProperties();

        for (String name : propertyNames) {
            Property property = properties.get(name);
            if (property != null) {
                selection.add(property);
            }
        }

        return selection;
    }
}
