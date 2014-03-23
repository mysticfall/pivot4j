package org.pivot4j.analytics.datasource.simple;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.pivot4j.PivotException;
import org.pivot4j.analytics.datasource.AbstractDataSourceManager;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.CubeInfo;
import org.pivot4j.datasource.SimpleOlapDataSource;

@ManagedBean(name = "dataSourceManager")
@ApplicationScoped
public class SimpleDataSourceManager extends
		AbstractDataSourceManager<SimpleDataSourceInfo> {

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#initialize()
	 */
	@PostConstruct
	protected void initialize() {
		super.initialize();
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#destroy()
	 */
	@PreDestroy
	protected void destroy() {
		super.destroy();
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSourceDefinition(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	protected SimpleDataSourceInfo createDataSourceDefinition(
			HierarchicalConfiguration configuration) {
		SimpleDataSourceInfo definition = new SimpleDataSourceInfo();
		definition.restoreSettings(configuration);

		return definition;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSource(org.pivot4j.analytics.datasource.DataSourceInfo)
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
			throw new FacesException(msg, e);
		}

		SimpleOlapDataSource dataSource = new SimpleOlapDataSource();

		dataSource.setConnectionString(definition.getUrl());
		dataSource.setUserName(definition.getUserName());
		dataSource.setPassword(definition.getPassword());
		dataSource.setConnectionProperties(definition.getProperties());

		return dataSource;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.DataSourceManager#getCatalogs()
	 */
	@Override
	public List<CatalogInfo> getCatalogs() {
		List<CatalogInfo> catalogs = new LinkedList<CatalogInfo>();

		for (SimpleDataSourceInfo definition : getDefinitions()) {
			catalogs.add(new CatalogInfo(definition.getName(), definition
					.getName(), definition.getDescription()));
		}

		return catalogs;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.DataSourceManager#getCubes(java.lang.String)
	 */
	@Override
	public List<CubeInfo> getCubes(String catalogName) {
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

		List<CubeInfo> cubes = new LinkedList<CubeInfo>();

		OlapConnection connection = null;

		try {
			connection = dataSource.getConnection();

			for (Cube cube : connection.getOlapSchema().getCubes()) {
				if (cube.isVisible()) {
					cubes.add(new CubeInfo(cube.getName(), cube.getCaption(),
							cube.getDescription()));
				}
			}
		} catch (SQLException e) {
			throw new PivotException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new PivotException(e);
				}
			}
		}

		return cubes;
	}
}
