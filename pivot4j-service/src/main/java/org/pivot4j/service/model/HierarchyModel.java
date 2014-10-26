/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.pivot4j.PivotException;

public class HierarchyModel extends MetadataModel {

	private static final long serialVersionUID = -493360561789587307L;

	private int levelCount;

	private int rootMemberCount;

	/**
	 * @param hierarchy
	 */
	public HierarchyModel(Hierarchy hierarchy) {
		super(hierarchy);

		this.levelCount = hierarchy.getLevels().size();

		try {
			this.rootMemberCount = hierarchy.getRootMembers().size();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @return the levelCount
	 */
	public int getLevelCount() {
		return levelCount;
	}

	/**
	 * @return the rootMemberCount
	 */
	public int getRootMemberCount() {
		return rootMemberCount;
	}
}
