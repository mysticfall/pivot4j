package com.eyeq.pivot4j.analytics.datasource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedProperty;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.analytics.config.Settings;
import com.eyeq.pivot4j.datasource.CloseableDataSource;

public abstract class AbstractDataSourceManager<T extends DataSourceDefinition>
		implements DataSourceManager {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	private List<T> definitions = new LinkedList<T>();

	private Map<T, OlapDataSource> dataSources = new HashMap<T, OlapDataSource>();

	@PostConstruct
	protected void initialize() {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing data source manager.");
		}

		registerDefinitions();
	}

	@PreDestroy
	protected void destroy() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying data source manager.");
		}

		List<T> definitions = new LinkedList<T>(this.definitions);

		for (T definition : definitions) {
			unregisterDefinition(definition);
		}

		this.dataSources.clear();
		this.definitions.clear();
	}

	protected void registerDefinitions() {
		List<HierarchicalConfiguration> configurations = settings
				.getConfiguration().configurationsAt("datasources.datasource");

		for (HierarchicalConfiguration configuration : configurations) {
			registerDefinition(configuration);
		}
	}

	/**
	 * @param configuration
	 */
	protected void registerDefinition(HierarchicalConfiguration configuration) {
		T definition = createDataSourceDefinition(configuration);

		if (definition != null) {
			registerDefinition(definition);
		}
	}

	/**
	 * @param definition
	 */
	protected void registerDefinition(T definition) {
		if (definition == null) {
			throw new NullArgumentException("definition");
		}

		synchronized (this) {
			if (definitions.contains(definition)) {
				unregisterDefinition(definition);
			}

			definitions.add(definition);
		}
	}

	/**
	 * @param definition
	 */
	protected void unregisterDefinition(T definition) {
		if (definition == null) {
			throw new NullArgumentException("definition");
		}

		synchronized (this) {
			if (logger.isInfoEnabled()) {
				logger.info("Disposing data source : " + definition);
			}

			OlapDataSource dataSource = dataSources.get(definition);

			if (dataSource != null) {
				if (dataSource instanceof CloseableDataSource) {
					CloseableDataSource closeable = (CloseableDataSource) dataSource;

					try {
						closeable.close();
					} catch (SQLException e) {
						if (logger.isErrorEnabled()) {
							logger.error("Failed to close data source : "
									+ definition, e);
						}
					}
				}

				dataSources.remove(definition);
			}

			definitions.remove(definition);
		}
	}

	protected List<T> getDefinitions() {
		return Collections.unmodifiableList(definitions);
	}

	/**
	 * @param configuration
	 * @return
	 */
	protected abstract T createDataSourceDefinition(
			HierarchicalConfiguration configuration);

	/**
	 * @param definition
	 * @return
	 */
	protected abstract OlapDataSource createDataSource(T definition);

	/**
	 * @param definition
	 * @return
	 */
	protected OlapDataSource getDataSource(T definition) {
		synchronized (this) {
			if (!dataSources.containsKey(definition)) {
				if (logger.isInfoEnabled()) {
					logger.info("Registering data source : " + definition);
				}

				OlapDataSource dataSource = createDataSource(definition);
				dataSources.put(definition, dataSource);
			}
		}

		return dataSources.get(definition);
	}

	/**
	 * @param connectionInfo
	 * @return
	 */
	protected T getDefinition(ConnectionMetadata connectionInfo) {
		if (connectionInfo == null) {
			throw new NullArgumentException("connectionInfo");
		}

		T definition = null;

		if (connectionInfo.getDataSourceName() == null) {
			if (!definitions.isEmpty()) {
				definition = definitions.get(0);
				connectionInfo.setDataSourceName(definition.getName());
			}
		} else {
			synchronized (this) {
				for (T def : definitions) {
					if (connectionInfo.getDataSourceName()
							.equals(def.getName())) {
						definition = def;
						break;
					}
				}
			}
		}

		return definition;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.DataSourceManager#getDataSource(com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata)
	 */
	@Override
	public OlapDataSource getDataSource(ConnectionMetadata connectionInfo) {
		OlapDataSource dataSource = null;

		T definition = getDefinition(connectionInfo);

		if (definition != null) {
			dataSource = getDataSource(definition);
		}

		return dataSource;
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
}