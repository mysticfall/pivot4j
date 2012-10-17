/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;

public class DimensionExp extends AbstractMetadataElementExp<Dimension> {

	private static final long serialVersionUID = 1343874529354268937L;

	/**
	 * @param dimension
	 */
	public DimensionExp(Dimension dimension) {
		super(dimension);
	}

	/**
	 * @param name
	 * @param uniqueName
	 */
	public DimensionExp(String name, String uniqueName) {
		super(name, uniqueName);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		visitor.visitDimension(this);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DimensionExp clone() {
		return new DimensionExp(getName(), getUniqueName());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.AbstractMetadataElementExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Dimension lookupMetadata(Cube cube) {
		return cube.getDimensions().get(getName());
	}
}
