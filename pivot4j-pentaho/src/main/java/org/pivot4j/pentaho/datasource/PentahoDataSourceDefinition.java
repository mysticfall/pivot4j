package org.pivot4j.pentaho.datasource;

import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pivot4j.analytics.datasource.AbstractDataSourceInfo;

public class PentahoDataSourceDefinition extends AbstractDataSourceInfo {

	private static final long serialVersionUID = 85152867828084878L;

	/**
	 * @param catalog
	 */
	public PentahoDataSourceDefinition(MondrianCatalog catalog) {
		if (catalog != null) {
			setName(catalog.getName());
			setDescription(catalog.getDefinition());
		}
	}

	/**
	 * @param catalogName
	 * @param description
	 */
	public PentahoDataSourceDefinition(String catalogName, String description) {
		setName(catalogName);
		setDescription(description);
	}
}
