/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.apache.commons.lang.NullArgumentException;

public class CatalogModel implements DataModel {

	private static final long serialVersionUID = 4044635234017364971L;

	private String name;

	private String description;

	/**
	 * @param name
	 * @param description
	 */
	public CatalogModel(String name, String description) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		this.name = name;
		this.description = description;
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getCaption()
	 */
	@Override
	public String getCaption() {
		return name;
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}
}
