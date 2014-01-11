/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx.metadata;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.pivot4j.mdx.ExpVisitor;

public class DimensionExp extends AbstractMetadataExp<Dimension> {

	private static final long serialVersionUID = 1343874529354268937L;

	public DimensionExp() {
	}

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
	 * @see org.pivot4j.mdx.metadata.AbstractMetadataExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Dimension lookupMetadata(Cube cube) {
		return cube.getDimensions().get(getName());
	}

	/**
	 * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		if (visitor instanceof MetadataExpVisitor) {
			((MetadataExpVisitor) visitor).visitDimension(this);
		}
	}

	/**
	 * @see org.pivot4j.mdx.Exp#copy()
	 */
	@Override
	public DimensionExp copy() {
		return new DimensionExp(getName(), getUniqueName());
	}
}
