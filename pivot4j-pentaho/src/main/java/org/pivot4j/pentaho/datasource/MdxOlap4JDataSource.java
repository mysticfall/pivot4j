package org.pivot4j.pentaho.datasource;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import mondrian.olap4j.MondrianOlap4jConnection;

import org.olap4j.OlapConnection;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection;
import org.pivot4j.datasource.AbstractOlapDataSource;

public class MdxOlap4JDataSource extends AbstractOlapDataSource {

	private IPentahoSession session;

	private Properties properties;

	private List<String> roleNames;

	/**
	 * @param session
	 * @param properties
	 * @param roleNames
	 */
	public MdxOlap4JDataSource(IPentahoSession session, Properties properties,
			List<String> roleNames) {
		if (session == null) {
			throw new IllegalAccessError("Required argument 'session' is null.");
		}

		if (properties == null) {
			throw new IllegalAccessError(
					"Required argument 'properties' is null.");
		}

		this.session = session;
		this.properties = properties;
		this.roleNames = roleNames;
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
	 * @return the roleNames
	 */
	public List<String> getRoles() {
		return roleNames;
	}

	/**
	 * @see org.pivot4j.datasource.AbstractOlapDataSource#createConnection(java
	 *      .lang.String, java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		MDXOlap4jConnection connection = (MDXOlap4jConnection) PentahoConnectionFactory
				.getConnection(IPentahoConnection.MDX_OLAP4J_DATASOURCE,
						properties, session, null);

		OlapConnection olap4JCon = connection.getConnection();

		if (roleNames != null && !roleNames.isEmpty()) {
			MondrianOlap4jConnection monCon = (MondrianOlap4jConnection) olap4JCon;
			monCon.setRoleNames(roleNames);
		}

		return olap4JCon;
	}
}
