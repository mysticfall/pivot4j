package org.pivot4j.pentaho.datasource;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import mondrian.olap.Util;
import mondrian.olap4j.MondrianOlap4jDriver;
import mondrian.util.Pair;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pivot4j.analytics.datasource.AbstractDataSourceManager;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.CubeInfo;
import org.slf4j.Logger;

public class PentahoDataSourceManager extends
		AbstractDataSourceManager<PentahoDataSourceDefinition> {

	private IPentahoSession session;

	private IMondrianCatalogService catalogService;

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#initialize()
	 */
	@Override
	protected void initialize() {
		this.session = PentahoSessionHolder.getSession();
		this.catalogService = PentahoSystem.get(IMondrianCatalogService.class,
				session);

		super.initialize();
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#destroy()
	 */
	@Override
	protected void destroy() {
		this.session = null;
		this.catalogService = null;
	}

	/**
	 * @return the session
	 */
	protected IPentahoSession getSession() {
		return session;
	}

	/**
	 * @return the catalogService
	 */
	protected IMondrianCatalogService getCatalogService() {
		return catalogService;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.DataSourceManager#getCatalogs()
	 */
	@Override
	public List<CatalogInfo> getCatalogs() {
		List<MondrianCatalog> catalogs = catalogService.listCatalogs(session,
				false);

		List<CatalogInfo> result = new LinkedList<CatalogInfo>();

		for (MondrianCatalog catalog : catalogs) {
			result.add(new CatalogInfo(catalog.getName(), catalog.getName(),
					catalog.getDefinition()));
		}

		return result;
	}

	/**
	 * @param name
	 * @return
	 */
	public MondrianCatalog getCatalog(String name) {
		return catalogService.getCatalog(name, session);
	}

	/**
	 * @see org.pivot4j.analytics.datasource.DataSourceManager#getCubes(java.lang.String)
	 */
	@Override
	public List<CubeInfo> getCubes(String catalogName) {
		if (catalogName == null) {
			throw new NullArgumentException("catalogName");
		}

		List<CubeInfo> cubes = new LinkedList<CubeInfo>();

		MondrianCatalog catalog = getCatalog(catalogName);

		if (catalog == null) {
			throw new IllegalArgumentException(
					"The catalog with the given name does not exist : "
							+ catalogName);
		}

		List<MondrianCube> mondrianCubes = catalog.getSchema().getCubes();

		for (MondrianCube cube : mondrianCubes) {
			cubes.add(new CubeInfo(cube.getId(), cube.getName(), cube.getName()));
		}

		return cubes;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#registerDefinitions()
	 */
	@Override
	protected void registerDefinitions() {
		List<MondrianCatalog> catalogs = catalogService.listCatalogs(session,
				false);

		for (MondrianCatalog catalog : catalogs) {
			registerDefinition(new PentahoDataSourceDefinition(catalog));
		}
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#getDefinition(org.pivot4j.analytics.datasource.ConnectionInfo)
	 */
	@Override
	protected synchronized PentahoDataSourceDefinition getDefinition(
			ConnectionInfo connectionInfo) {
		PentahoDataSourceDefinition definition = super
				.getDefinition(connectionInfo);

		if (definition == null) {
			MondrianCatalog catalog = catalogService.getCatalog(
					connectionInfo.getCatalogName(), session);

			if (catalog != null) {
				definition = new PentahoDataSourceDefinition(catalog);
				registerDefinition(definition);
			}
		}

		return definition;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSourceDefinition(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	protected PentahoDataSourceDefinition createDataSourceDefinition(
			HierarchicalConfiguration configuration) {
		return null;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceManager#createDataSource(org.pivot4j.analytics.datasource.DataSourceInfo)
	 */
	@Override
	protected OlapDataSource createDataSource(
			PentahoDataSourceDefinition definition) {
		if (definition == null) {
			return null;
		}

		MondrianCatalog catalog = getCatalog(definition.getName());

		if (catalog == null) {
			Logger logger = getLogger();
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to find catalog with name : "
						+ definition.getName());
			}

			return null;
		}

		Util.PropertyList parsedProperties = Util.parseConnectString(catalog
				.getDataSourceInfo());

		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:mondrian:");
		builder.append("Catalog=");
		builder.append(catalog.getDefinition());
		builder.append("; ");

		Iterator<Pair<String, String>> it = parsedProperties.iterator();

		while (it.hasNext()) {
			Pair<String, String> pair = it.next();
			builder.append(pair.getKey());
			builder.append("=");
			builder.append(pair.getValue());
			builder.append("; ");
		}

		builder.append("PoolNeeded=false; ");
		builder.append("Locale=");
		builder.append(LocaleHelper.getLocale().toString());
		builder.append(";");

		String url = builder.toString();

		Properties properties = new Properties();
		properties.put("url", url);
		properties.put("driver", MondrianOlap4jDriver.class.getName());

		Logger logger = getLogger();
		if (logger.isInfoEnabled()) {
			logger.info("Using connection URL : " + url);
		}

		return new MdxOlap4JDataSource(session, properties);
	}
}
