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
import org.olap4j.metadata.Level;

public class LevelExp extends AbstractMetadataElementExp<Level> {

	private static final long serialVersionUID = -2485622016528439815L;

	private String hierarchyName;

	/**
	 * @param level
	 */
	public LevelExp(Level level) {
		super(level);
		this.hierarchyName = level.getHierarchy().getName();
	}

	/**
	 * @param name
	 * @param uniqueName
	 * @param hierarchyName
	 */
	public LevelExp(String name, String uniqueName, String hierarchyName) {
		super(name, uniqueName);

		if (hierarchyName == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'hierarchyName'.");
		}

		this.hierarchyName = hierarchyName;
	}

	/**
	 * @return the hierarchyName
	 */
	public String getHierarchyName() {
		return hierarchyName;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		visitor.visitLevel(this);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public LevelExp clone() {
		return new LevelExp(getName(), getUniqueName(), getHierarchyName());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.AbstractMetadataElementExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Level lookupMetadata(Cube cube) {
		Hierarchy hierarchy = cube.getHierarchies().get(hierarchyName);
		return hierarchy.getLevels().get(getName());
	}
}
