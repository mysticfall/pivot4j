package com.eyeq.pivot4j.pentaho.datasource;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pentaho.platform.api.engine.IPentahoSession;

import com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceDefinition;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;

public class PentahoDataSourceDefinition extends AbstractDataSourceDefinition {

	private static final long serialVersionUID = 85152867828084878L;

	private transient IPentahoSession session;

	private String cubeName;

	private String catalogName;

	public PentahoDataSourceDefinition() {
		this(null);
	}

	public PentahoDataSourceDefinition(ConnectionMetadata connectionInfo) {
		setName("Pentaho");
		setDescription("Pentaho catalog service.");

		if (connectionInfo != null) {
			this.catalogName = connectionInfo.getCatalogName();
			this.cubeName = connectionInfo.getCubeName();
		}
	}

	/**
	 * @param connectionInfo
	 * @return
	 */
	public boolean supports(ConnectionMetadata connectionInfo) {
		if (connectionInfo == null) {
			throw new NullArgumentException("connectionInfo");
		}

		return connectionInfo.getCatalogName().equals(catalogName)
				&& connectionInfo.getCubeName().equals(cubeName);
	}

	/**
	 * @return the session
	 */
	public IPentahoSession getSession() {
		return session;
	}

	/**
	 * @param session
	 *            the session to set
	 */
	public void setSession(IPentahoSession session) {
		this.session = session;
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
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceDefinition#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(catalogName).append(cubeName)
				.toHashCode();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceDefinition#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof PentahoDataSourceDefinition)) {
			return false;
		}

		PentahoDataSourceDefinition other = (PentahoDataSourceDefinition) obj;

		return new EqualsBuilder().append(catalogName, other.catalogName)
				.append(cubeName, other.cubeName).isEquals();
	}
}
