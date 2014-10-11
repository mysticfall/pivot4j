/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.metadata.Cube;

public class CubeModel extends MetadataModel {

	private static final long serialVersionUID = 4044635234017364971L;

	private int dimensionCount;

	private int measureCount;

	private boolean drillThroughEnabled;

	/**
	 * @param cube
	 */
	public CubeModel(Cube cube) {
		super(cube);

		this.dimensionCount = cube.getDimensions().size();
		this.measureCount = cube.getMeasures().size();

		this.drillThroughEnabled = cube.isDrillThroughEnabled();
	}

	/**
	 * @return the dimensionCount
	 */
	public int getDimensionCount() {
		return dimensionCount;
	}

	/**
	 * @return the measureCount
	 */
	public int getMeasureCount() {
		return measureCount;
	}

	/**
	 * @return the drillThroughEnabled
	 */
	public boolean isDrillThroughEnabled() {
		return drillThroughEnabled;
	}
}
