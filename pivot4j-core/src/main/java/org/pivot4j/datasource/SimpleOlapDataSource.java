/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.olap4j.OlapConnection;

public class SimpleOlapDataSource extends AbstractOlapDataSource {

	private String connectionString;

	private Properties connectionProperties;

	/**
	 * @return the connectionString
	 */
	public String getConnectionString() {
		return connectionString;
	}

	/**
	 * @param connectionString
	 *            the connectionString to set
	 */
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	/**
	 * @return the connectionProperties
	 */
	public Properties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * @param connectionProperties
	 *            the connectionProperties to set
	 */
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * @throws SQLException
	 * @see org.pivot4j.datasource.AbstractOlapDataSource#createConnection(java
	 *      .lang.String, java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		Properties properties = getConnectionProperties();
		if (properties == null) {
			properties = new Properties();
		}

		if (userName != null) {
			properties.put("user", userName);
		}

		if (password != null) {
			properties.put("password", password);
		}

		Connection connection = DriverManager.getConnection(
				getConnectionString(), properties);
		return connection.unwrap(OlapConnection.class);
	}
}
