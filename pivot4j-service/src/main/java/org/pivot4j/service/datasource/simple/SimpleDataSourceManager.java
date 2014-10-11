/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.datasource.simple;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.pivot4j.PivotException;
import org.pivot4j.datasource.SimpleOlapDataSource;
import org.pivot4j.service.datasource.AbstractDataSourceManager;
import org.pivot4j.service.model.CatalogModel;
import org.pivot4j.service.model.CubeModel;
import org.springframework.stereotype.Service;

@Service("dataSourceManager")
public class SimpleDataSourceManager extends
		AbstractDataSourceManager<SimpleDataSourceInfo> {

	/**
	 * @see org.pivot4j.service.datasource.AbstractDataSourceManager#initialize()
	 */
	@PostConstruct
	protected void initialize() {
		super.initialize();
	}

	/**
	 * @see org.pivot4j.service.datasource.AbstractDataSourceManager#destroy()
	 */
	@PreDestroy
	protected void destroy() {
		super.destroy();
	}

	/**
	 * @see org.pivot4j.service.datasource.AbstractDataSourceManager#createDataSourceDefinition(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	protected SimpleDataSourceInfo createDataSourceDefinition(
			HierarchicalConfiguration configuration) {
		SimpleDataSourceInfo definition = new SimpleDataSourceInfo();
		definition.restoreSettings(configuration);

		return definition;
	}

	/**
	 * @see org.pivot4j.service.datasource.AbstractDataSourceManager#createDataSource(org.pivot4j.service.datasource.DataSourceInfo)
	 */
	@Override
	protected OlapDataSource createDataSource(SimpleDataSourceInfo definition) {
		if (definition == null) {
			throw new NullArgumentException("definition");
		}

		String driverName = definition.getDriverClass();

		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			String msg = "Failed to load JDBC driver : " + driverName;
			throw new PivotException(msg, e);
		}

		SimpleOlapDataSource dataSource = new SimpleOlapDataSource();

		dataSource.setConnectionString(definition.getUrl());
		dataSource.setUserName(definition.getUserName());
		dataSource.setPassword(definition.getPassword());
		dataSource.setConnectionProperties(definition.getProperties());

		return dataSource;
	}

	/**
	 * @see org.pivot4j.service.datasource.DataSourceManager#getCatalogs()
	 */
	@Override
	public List<CatalogModel> getCatalogs() {
		List<CatalogModel> catalogs = new LinkedList<CatalogModel>();

		for (SimpleDataSourceInfo definition : getDefinitions()) {
			catalogs.add(new CatalogModel(definition.getName(), definition
					.getDescription()));
		}

		return catalogs;
	}

	/**
	 * @see org.pivot4j.service.datasource.DataSourceManager#getCubes(java.lang.String)
	 */
	@Override
	public List<CubeModel> getCubes(String catalogName) {
		if (catalogName == null) {
			throw new NullArgumentException("catalogName");
		}

		SimpleDataSourceInfo definition = getDefinition(catalogName);

		if (definition == null) {
			throw new IllegalArgumentException(
					"Data source with the given name does not exist : "
							+ catalogName);
		}

		OlapDataSource dataSource = createDataSource(definition);

		List<CubeModel> cubes = new LinkedList<CubeModel>();

		try (OlapConnection connection = dataSource.getConnection()) {
			for (Cube cube : connection.getOlapSchema().getCubes()) {
				if (cube.isVisible()) {
					cubes.add(new CubeModel(cube));
				}
			}
		} catch (SQLException e) {
			throw new PivotException(e);
		}

		return cubes;
	}
}
