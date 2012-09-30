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

import org.olap4j.OlapConnection;

/**
 * OlapDataSource implementation which returns existing OlapConnection instance.
 */
public class SingleConnectionOlapDataSource extends AbstractOlapDataSource {

	private OlapConnection connection;

	public SingleConnectionOlapDataSource(OlapConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'connection'.");
		}
		this.connection = connection;
	}

	/**
	 * @see com.eyeq.pivot4j.datasource.AbstractOlapDataSource#createConnection(java
	 *      .lang.String, java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		return connection;
	}
}
