package com.eyeq.pivot4j.analytics.repository;

import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;

public class DataSourceNotFoundException extends Exception {

	private static final long serialVersionUID = -6634494832895522425L;
	
	private ConnectionMetadata connectionInfo;

	/**
	 * @param connectionInfo
	 */
	public DataSourceNotFoundException(ConnectionMetadata connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	/**
	 * @return the connectionInfo
	 */
	public ConnectionMetadata getConnectionInfo() {
		return connectionInfo;
	}
}
