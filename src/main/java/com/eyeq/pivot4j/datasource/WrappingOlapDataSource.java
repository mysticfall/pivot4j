/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.olap4j.OlapConnection;

/**
 * OlapDataSource implementation which wraps another JDBC data source. It can be
 * useful when wrapping an existing data source configured in an application
 * server.
 */
public class WrappingOlapDataSource extends AbstractOlapDataSource {

	private DataSource dataSource;

	public WrappingOlapDataSource(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'dataSource'.");
		}
		this.dataSource = dataSource;
	}

	/**
	 * @see com.eyeq.pivot4j.datasource.AbstractOlapDataSource#createConnection(java
	 *      .lang.String, java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		return dataSource.getConnection(userName, password).unwrap(
				OlapConnection.class);
	}

	/**
	 * @return the dataSource
	 */
	protected DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @see com.eyeq.pivot4j.datasource.AbstractOlapDataSource#unwrap(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.equals(dataSource.getClass())) {
			return (T) dataSource;
		}

		return super.unwrap(iface);
	}
}
