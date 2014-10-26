/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.MetadataElement;

public class MetadataModel implements DataModel {

	private static final long serialVersionUID = -5649031145312931817L;

	private String name;

	private String uniqueName;

	private String caption;

	private String description;

	private boolean visible = true;

	/**
	 * @param metadata
	 */
	protected MetadataModel(MetadataElement metadata) {
		if (metadata == null) {
			throw new NullArgumentException("metadata");
		}

		this.name = metadata.getName();
		this.uniqueName = metadata.getUniqueName();
		this.caption = metadata.getCaption();
		this.description = metadata.getDescription();

		// TODO: http://jira.pentaho.com/browse/MONDRIAN-1967
		this.visible = !(metadata instanceof Measure) || metadata.isVisible();
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return uniqueName
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getCaption()
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @see org.pivot4j.service.model.DataModel#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return visible
	 */
	public boolean isVisible() {
		return visible;
	}
}
