/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olap4j.metadata.Level;
import org.olap4j.metadata.Property;


public class NonInternalPropertyCollector implements PropertyCollector {

	private static Set<String> internalProperties;

	static {
		internalProperties = new HashSet<String>();

		for (Property property : Property.StandardMemberProperty.values()) {
			internalProperties.add(property.getName());
		}

		// Workaround for Olap4J issue #77
		internalProperties.add("CELL_FORMATTER");
		internalProperties.add("CELL_FORMATTER_SCRIPT");
		internalProperties.add("CELL_FORMATTER_SCRIPT_LANGUAGE");
		internalProperties.add("DISPLAY_FOLDER");
		internalProperties.add("KEY");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.collector.PropertyCollector#getProperties(org.olap4j.metadata.Level)
	 */
	@Override
	public List<Property> getProperties(Level level) {
		List<Property> properties = level.getProperties();

		List<Property> selection = new ArrayList<Property>(properties.size());

		for (Property property : properties) {
			if (property.isVisible()
					&& !internalProperties.contains(property.getName())) {
				selection.add(property);
			}
		}

		return selection;
	}
}
