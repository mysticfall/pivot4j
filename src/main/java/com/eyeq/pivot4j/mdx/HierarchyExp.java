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
import org.olap4j.metadata.Hierarchy;

public class HierarchyExp extends AbstractMetadataElementExp<Hierarchy> {

	private static final long serialVersionUID = 3116369522934630935L;

	/**
	 * @param hierarchy
	 */
	public HierarchyExp(Hierarchy hierarchy) {
		super(hierarchy);
	}

	/**
	 * @param name
	 * @param uniqueName
	 */
	public HierarchyExp(String name, String uniqueName) {
		super(name, uniqueName);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		visitor.visitHierarchy(this);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public HierarchyExp clone() {
		return new HierarchyExp(getName(), getUniqueName());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.AbstractMetadataElementExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Hierarchy lookupMetadata(Cube cube) {
		return cube.getHierarchies().get(getName());
	}
}
