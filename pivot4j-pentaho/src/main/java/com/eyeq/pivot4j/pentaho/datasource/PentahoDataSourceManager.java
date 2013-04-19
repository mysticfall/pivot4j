package com.eyeq.pivot4j.pentaho.datasource;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import mondrian.olap.Util;
import mondrian.olap4j.MondrianOlap4jDriver;
import mondrian.util.Pair;

import org.olap4j.OlapDataSource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.util.messages.LocaleHelper;

import com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;

public class PentahoDataSourceManager extends AbstractDataSourceManager {

	private IPentahoSession session;

	private IMondrianCatalogService catalogService;

	@PostConstruct
	protected void initialize() {
		this.session = PentahoSessionHolder.getSession();
		this.catalogService = PentahoSystem.get(IMondrianCatalogService.class,
				session);
	}

	@PreDestroy
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

	public List<MondrianCatalog> getCatalogs() {
		return catalogService.listCatalogs(session, false);
	}

	/**
	 * @param name
	 * @return
	 */
	public MondrianCatalog getCatalog(String name) {
		return catalogService.getCatalog(name, session);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager#doCreateDataSource(com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata)
	 */
	@Override
	protected OlapDataSource doCreateDataSource(
			ConnectionMetadata connectionInfo) {
		if (connectionInfo == null) {
			return null;
		}

		ISolutionRepository repository = PentahoSystem.get(
				ISolutionRepository.class, session);

		MondrianCatalog catalog = getCatalog(connectionInfo.getCatalogName());

		if (catalog == null) {
			if (log.isWarnEnabled()) {
				log.warn("Unable to find catalog with name : "
						+ connectionInfo.getCatalogName());
			}

			return null;
		}

		SolutionReposHelper.setSolutionRepositoryThreadVariable(repository);

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

		if (log.isInfoEnabled()) {
			log.info("Using connection URL : " + url);
		}

		return new MdxOlap4JDataSource(session, properties);
	}
}
