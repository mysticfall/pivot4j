package com.eyeq.pivot4j.pentaho.datasource;

import java.sql.SQLException;
import java.util.Properties;

import org.olap4j.OlapConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection;

import com.eyeq.pivot4j.datasource.AbstractOlapDataSource;

public class MdxOlap4JDataSource extends AbstractOlapDataSource {

	private IPentahoSession session;

	private Properties properties;

	/**
	 * @param session
	 * @param properties
	 */
	public MdxOlap4JDataSource(IPentahoSession session, Properties properties) {
		if (session == null) {
			throw new IllegalAccessError("Required argument 'session' is null.");
		}

		if (properties == null) {
			throw new IllegalAccessError(
					"Required argument 'properties' is null.");
		}

		this.session = session;
		this.properties = properties;
	}

	/**
	 * @return the session
	 */
	public IPentahoSession getSession() {
		return session;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @see com.eyeq.pivot4j.datasource.AbstractOlapDataSource#createConnection(java
	 *      .lang.String, java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		MDXOlap4jConnection connection = (MDXOlap4jConnection) PentahoConnectionFactory
				.getConnection(IPentahoConnection.MDX_OLAP4J_DATASOURCE,
						properties, session, null);
		return connection.getConnection();
	}
}
