package com.eyeq.pivot4j.analytics.datasource.simple;

import javax.faces.FacesException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;

import com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager;
import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;

@ManagedBean(name = "dataSourceManager")
@ApplicationScoped
public class SimpleDataSourceManager extends
		AbstractDataSourceManager<SimpleDataSourceDefinition> {

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSourceDefinition(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	protected SimpleDataSourceDefinition createDataSourceDefinition(
			HierarchicalConfiguration configuration) {
		SimpleDataSourceDefinition definition = new SimpleDataSourceDefinition();
		definition.restoreSettings(configuration);

		return definition;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSource(com.eyeq.pivot4j.analytics.datasource.DataSourceDefinition)
	 */
	@Override
	protected OlapDataSource createDataSource(
			SimpleDataSourceDefinition definition) {
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
}
