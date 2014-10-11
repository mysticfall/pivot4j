/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.metadata.Hierarchy;

public class HierarchyModel extends MetadataModel {

	private static final long serialVersionUID = -493360561789587307L;

	private int levelCount;

	/**
	 * @param hierarchy
	 */
	public HierarchyModel(Hierarchy hierarchy) {
		super(hierarchy);

		this.levelCount = hierarchy.getLevels().size();
	}

	/**
	 * @return the levelCount
	 */
	public int getLevelCount() {
		return levelCount;
	}
}
