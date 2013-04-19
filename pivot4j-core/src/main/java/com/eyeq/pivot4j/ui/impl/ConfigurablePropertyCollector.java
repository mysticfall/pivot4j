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
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.ui.PropertyCollector;

public class ConfigurablePropertyCollector implements PropertyCollector {

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
	 * @see com.eyeq.pivot4j.ui.PropertyCollector#getProperties(org.olap4j.metadata.Level)
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
