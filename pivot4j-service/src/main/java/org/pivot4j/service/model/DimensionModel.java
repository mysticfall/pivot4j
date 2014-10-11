/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.pivot4j.PivotException;

public class DimensionModel extends MetadataModel {

	private static final long serialVersionUID = 6110999166123903904L;

	private String type;

	private int hierarchyCount;

	/**
	 * @param dimension
	 */
	public DimensionModel(Dimension dimension) {
		super(dimension);

		try {
			this.type = dimension.getDimensionType().name();
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		this.hierarchyCount = dimension.getHierarchies().size();
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the hierarchyCount
	 */
	public int getHierarchyCount() {
		return hierarchyCount;
	}
}
