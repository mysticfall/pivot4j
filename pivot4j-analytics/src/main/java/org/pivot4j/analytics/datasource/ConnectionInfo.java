package org.pivot4j.analytics.datasource;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.pivot4j.state.Configurable;

public class ConnectionInfo implements Configurable, Serializable {

	private static final long serialVersionUID = 1613489385973603487L;

	private String catalogName;

	private String cubeName;

	public ConnectionInfo() {
	}

	/**
	 * @param catalogName
	 * @param cubeName
	 */
	public ConnectionInfo(String catalogName, String cubeName) {
		this.catalogName = catalogName;
		this.cubeName = cubeName;
	}

	/**
	 * @return the cubeName
	 */
	public String getCubeName() {
		return cubeName;
	}

	/**
	 * @param cubeName
	 *            the cubeName to set
	 */
	public void setCubeName(String cubeName) {
		this.cubeName = cubeName;
	}

	/**
	 * @return the catalogName
	 */
	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * @param catalogName
	 *            the catalogName to set
	 */
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * @see org.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(
					"Configuration object cannot be null.");
		}

		configuration.addProperty("catalog", catalogName);
		configuration.addProperty("cube", cubeName);
	}

	/**
	 * @see org.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(
					"Configuration object cannot be null.");
		}

		this.catalogName = configuration.getString("catalog");
		this.cubeName = configuration.getString("cube");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("cubeName", cubeName)
				.append("catalogName", catalogName).toString();
	}
}
