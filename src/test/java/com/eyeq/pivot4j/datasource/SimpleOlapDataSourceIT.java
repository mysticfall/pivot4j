/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.datasource;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import mondrian.rolap.RolapConnectionProperties;

import org.apache.derby.jdbc.ClientDriver;
import org.junit.Before;
import org.junit.Test;

public class SimpleOlapDataSourceIT {

	@Before
	public void before() throws ClassNotFoundException {
		Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
	}

	/**
	 * Test method for
	 * {@link com.eyeq.pivot4j.datasource.AbstractOlapDataSource#getConnection()}
	 * @throws SQLException
	 */
	@Test
	public void testGetMondrianConnection() throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:mondrian:");
		builder.append(RolapConnectionProperties.Jdbc.name());
		builder.append("=jdbc:derby://localhost/foodmart;");
		builder.append(";");
		builder.append(RolapConnectionProperties.JdbcDrivers.name());
		builder.append("=");
		builder.append(ClientDriver.class.getName());
		builder.append(";");
		builder.append(RolapConnectionProperties.JdbcUser.name());
		builder.append("=sa;");

		builder.append(RolapConnectionProperties.Catalog.name());
		builder.append("=file:");

		String basedir = System.getProperty("basedir");
		if (basedir == null) {
			basedir = System.getProperty("user.dir");
		}

		builder.append(basedir);
		builder.append(File.separator);
		builder.append("src");
		builder.append(File.separator);
		builder.append("test");
		builder.append(File.separator);
		builder.append("config");
		builder.append(File.separator);
		builder.append("FoodMart.xml");

		builder.append(";");

		String url = builder.toString();

		SimpleOlapDataSource dataSource = new SimpleOlapDataSource();
		dataSource.setConnectionString(url);

		Connection con = dataSource.getConnection();
		assertNotNull("Failed to create a connection : " + url, con);

		if (con != null) {
			con.close();
		}
	}
}
