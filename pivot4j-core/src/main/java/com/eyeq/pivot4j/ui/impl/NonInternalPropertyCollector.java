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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olap4j.metadata.Level;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.ui.PropertyCollector;

public class NonInternalPropertyCollector implements PropertyCollector {

	static Set<String> INTERNAL_PROPERTIES;

	static {
		INTERNAL_PROPERTIES = new HashSet<String>();

		for (Property property : Property.StandardMemberProperty.values()) {
			INTERNAL_PROPERTIES.add(property.getName());
		}

		// Workaround for Olap4J issue #77
		INTERNAL_PROPERTIES.add("CELL_FORMATTER");
		INTERNAL_PROPERTIES.add("CELL_FORMATTER_SCRIPT");
		INTERNAL_PROPERTIES.add("CELL_FORMATTER_SCRIPT_LANGUAGE");
		INTERNAL_PROPERTIES.add("DISPLAY_FOLDER");
		INTERNAL_PROPERTIES.add("KEY");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PropertyCollector#getProperties(org.olap4j.metadata.Level)
	 */
	@Override
	public List<Property> getProperties(Level level) {
		List<Property> properties = level.getProperties();

		List<Property> selection = new ArrayList<Property>(properties.size());

		for (Property property : properties) {
			if (property.isVisible()
					&& !INTERNAL_PROPERTIES.contains(property.getName())) {
				selection.add(property);
			}
		}

		return selection;
	}
}
