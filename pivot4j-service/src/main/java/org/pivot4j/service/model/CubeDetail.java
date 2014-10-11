/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Measure;

public class CubeDetail extends CubeModel {

	private static final long serialVersionUID = -4034636979086691592L;

	private List<DimensionModel> dimensions;

	private List<MeasureModel> measures;

	/**
	 * @param cube
	 */
	public CubeDetail(Cube cube) {
		super(cube);

		List<DimensionModel> dimensionList = new LinkedList<DimensionModel>();

		for (Dimension dimension : cube.getDimensions()) {
			dimensionList.add(new DimensionModel(dimension));
		}

		this.dimensions = Collections.unmodifiableList(dimensionList);

		List<MeasureModel> measureList = new LinkedList<MeasureModel>();

		for (Measure measure : cube.getMeasures()) {
			measureList.add(new MeasureModel(measure));
		}

		this.measures = Collections.unmodifiableList(measureList);
	}

	/**
	 * @return the dimensions
	 */
	public List<DimensionModel> getDimensions() {
		return dimensions;
	}

	/**
	 * @return the measures
	 */
	public List<MeasureModel> getMeasures() {
		return measures;
	}
}
