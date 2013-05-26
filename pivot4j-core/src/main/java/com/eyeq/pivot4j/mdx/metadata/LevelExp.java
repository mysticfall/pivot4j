/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx.metadata;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;

import com.eyeq.pivot4j.mdx.ExpVisitor;

public class LevelExp extends AbstractMetadataExp<Level> {

	private static final long serialVersionUID = -2485622016528439815L;

	private String hierarchyName;

	public LevelExp() {
	}

	/**
	 * @param level
	 */
	public LevelExp(Level level) {
		super(level);
	}

	/**
	 * @param name
	 * @param uniqueName
	 * @param hierarchyName
	 */
	public LevelExp(String name, String uniqueName, String hierarchyName) {
		super(name, uniqueName);

		this.hierarchyName = hierarchyName;
	}

	/**
	 * @return the hierarchyName
	 */
	public String getHierarchyName() {
		return hierarchyName;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.metadata.AbstractMetadataExp#setMetadata(org.olap4j
	 *      .metadata.MetadataElement)
	 */
	@Override
	public void setMetadata(Level element) {
		super.setMetadata(element);

		if (element == null) {
			this.hierarchyName = null;
		} else {
			this.hierarchyName = element.getHierarchy().getName();
		}
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.metadata.AbstractMetadataExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Level lookupMetadata(Cube cube) {
		Hierarchy hierarchy = cube.getHierarchies().get(hierarchyName);
		return hierarchy.getLevels().get(getName());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		if (visitor instanceof MetadataExpVisitor) {
			((MetadataExpVisitor) visitor).visitLevel(this);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#copy()
	 */
	public LevelExp copy() {
		return new LevelExp(getName(), getUniqueName(), getHierarchyName());
	}
}
