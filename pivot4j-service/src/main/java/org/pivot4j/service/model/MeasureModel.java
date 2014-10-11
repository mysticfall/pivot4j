/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.metadata.Measure;

public class MeasureModel extends MetadataModel {

	private static final long serialVersionUID = -3417773936773894998L;

	/**
	 * @param measure
	 */
	public MeasureModel(Measure measure) {
		super(measure);
	}
}
