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

import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;

public class DimensionDetail extends DimensionModel {

	private static final long serialVersionUID = 8563155827280990808L;

	private List<HierarchyModel> hierarchies;

	private HierarchyModel defaultHierarchy;

	/**
	 * @param dimension
	 */
	public DimensionDetail(Dimension dimension) {
		super(dimension);

		List<HierarchyModel> hierarchyList = new LinkedList<HierarchyModel>();

		for (Hierarchy hierarchy : dimension.getHierarchies()) {
			hierarchyList.add(new HierarchyModel(hierarchy));
		}

		this.hierarchies = Collections.unmodifiableList(hierarchyList);

		if (dimension.getDefaultHierarchy() != null) {
			this.defaultHierarchy = new HierarchyModel(
					dimension.getDefaultHierarchy());
		}
	}

	/**
	 * @return the hierarchies
	 */
	public List<HierarchyModel> getHierarchies() {
		return hierarchies;
	}

	/**
	 * @return the defaultHierarchy
	 */
	public HierarchyModel getDefaultHierarchy() {
		return defaultHierarchy;
	}
}
