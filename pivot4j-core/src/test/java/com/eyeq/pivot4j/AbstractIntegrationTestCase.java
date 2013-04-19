/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import mondrian.rolap.RolapConnectionProperties;

import org.apache.derby.jdbc.ClientDriver;
import org.junit.After;
import org.junit.Before;
import org.olap4j.OlapDataSource;

import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;
import com.eyeq.pivot4j.impl.PivotModelImpl;

public abstract class AbstractIntegrationTestCase {

	private OlapDataSource dataSource;

	private PivotModel model;

	@Before
	public void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.ClientDriver");

		this.dataSource = createMondrianDataSource();
		this.model = createPivotModel(dataSource);
	}

	@After
	public void tearDown() throws Exception {
		if (model != null && model.isInitialized()) {
			model.destroy();

			this.model = null;
		}

		this.dataSource = null;
	}

	protected OlapDataSource createMondrianDataSource()
			throws ClassNotFoundException {
		Class.forName("mondrian.olap4j.MondrianOlap4jDriver");

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

		return dataSource;
	}

	protected PivotModel createPivotModel(OlapDataSource dataSource) {
		return new PivotModelImpl(dataSource);
	}

	/**
	 * @return the dataSource
	 */
	protected OlapDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @return the model
	 */
	protected PivotModel getPivotModel() {
		return model;
	}

	/**
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected String readTestResource(String name) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append('/');
		builder.append(getClass().getPackage().getName().replace('.', '/'));
		builder.append('/');
		builder.append(name);

		String path = builder.toString();

		InputStream in = getClass().getResourceAsStream(path);

		if (in == null) {
			assertThat("No resource was found with given name : " + path, in,
					is(notNullValue()));
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		StringWriter writer = new StringWriter();

		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.append(line);
			writer.append('\n');
		}

		reader.close();
		writer.flush();
		writer.close();

		return writer.toString().trim();
	}
}
