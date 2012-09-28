/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;
import org.olap4j.OlapConnection;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.datasource.PooledOlapDataSource;

public class PooledOlapDataSourceIT extends AbstractIntegrationTestCase {

	/**
	 * Test method for
	 * {@link com.eyeq.pivot4j.datasource.AbstractOlapDataSource#getConnection()}
	 * .
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testGetConnection() throws SQLException {
		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.maxActive = 3;
		config.maxIdle = 3;

		PooledOlapDataSource dataSource = new PooledOlapDataSource(
				getDataSource());
		OlapConnection connection1 = dataSource.getConnection();
		OlapConnection connection2 = dataSource.getConnection();
		OlapConnection connection3 = dataSource.getConnection();

		assertTrue("Invalid connection returned.", connection1.isValid(10));
		assertTrue("Invalid connection returned.", connection2.isValid(10));
		assertTrue("Invalid connection returned.", connection3.isValid(10));

		assertFalse("Closed connection returned.", connection1.isClosed());
		assertFalse("Closed connection returned.", connection2.isClosed());
		assertFalse("Closed connection returned.", connection3.isClosed());

		assertFalse("Should return a new Connection instance.",
				connection1.unwrap(OlapConnection.class) == connection2
						.unwrap(OlapConnection.class));
		assertFalse("Should return a new Connection instance.",
				connection2.unwrap(OlapConnection.class) == connection3
						.unwrap(OlapConnection.class));

		connection3.close();

		assertFalse("Connection remains open.", connection3.isClosed());

		OlapConnection connection4 = dataSource.getConnection();
		assertTrue("Should reuse an existing connection.",
				connection3.unwrap(OlapConnection.class) == connection4
						.unwrap(OlapConnection.class));

		assertFalse("Closed connection returned.", connection4.isClosed());

		dataSource.close();

		assertFalse(
				"Connection remains open after data source has been closed.",
				connection1.isClosed());
		assertFalse(
				"Connection remains open after data source has been closed.",
				connection2.isClosed());
		assertFalse(
				"Connection remains open after data source has been closed.",
				connection3.isClosed());
	}
}
