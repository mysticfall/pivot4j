/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.metadata.Level;

public class LevelModel extends MetadataModel {

	private static final long serialVersionUID = -8457366359193719874L;

	private int depth;

	private String type;

	private boolean calculated;

	/**
	 * @param level
	 */
	public LevelModel(Level level) {
		super(level);

		this.depth = level.getDepth();
		this.type = level.getLevelType().name();
		this.calculated = level.isCalculated();
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the calculated
	 */
	public boolean isCalculated() {
		return calculated;
	}
}
