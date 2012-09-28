/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.datasource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.pool.ObjectPool;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.PreparedOlapStatement;
import org.olap4j.Scenario;
import org.olap4j.mdx.parser.MdxParserFactory;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Database;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Schema;

public class PooledOlapConnection implements OlapConnection {

	private OlapConnection connection;

	private ObjectPool<OlapConnection> pool;

	/**
	 * @param connection
	 * @param pool
	 */
	public PooledOlapConnection(OlapConnection connection,
			ObjectPool<OlapConnection> pool) {
		if (connection == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'connection'.");
		}

		if (pool == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'pool'.");
		}

		this.connection = connection;
		this.pool = pool;
	}

	/**
	 * @return the connection
	 */
	public OlapConnection getConnection() {
		return connection;
	}

	/**
	 * @return the pool
	 */
	public ObjectPool<OlapConnection> getPool() {
		return pool;
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		try {
			pool.returnObject(connection);
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	/**
	 * @param iface
	 * @return
	 * @throws SQLException
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return connection.unwrap(iface);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getMetaData()
	 */
	public OlapDatabaseMetaData getMetaData() throws OlapException {
		return connection.getMetaData();
	}

	/**
	 * @param mdx
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#prepareOlapStatement(java.lang.String)
	 */
	public PreparedOlapStatement prepareOlapStatement(String mdx)
			throws OlapException {
		return connection.prepareOlapStatement(mdx);
	}

	/**
	 * @return
	 * @see org.olap4j.OlapConnection#getParserFactory()
	 */
	public MdxParserFactory getParserFactory() {
		return connection.getParserFactory();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#createStatement()
	 */
	public OlapStatement createStatement() throws OlapException {
		return connection.createStatement();
	}

	/**
	 * @param iface
	 * @return
	 * @throws SQLException
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return connection.isWrapperFor(iface);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getDatabase()
	 */
	public String getDatabase() throws OlapException {
		return connection.getDatabase();
	}

	/**
	 * @param databaseName
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#setDatabase(java.lang.String)
	 */
	public void setDatabase(String databaseName) throws OlapException {
		connection.setDatabase(databaseName);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapDatabase()
	 */
	public Database getOlapDatabase() throws OlapException {
		return connection.getOlapDatabase();
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapDatabases()
	 */
	public NamedList<Database> getOlapDatabases() throws OlapException {
		return connection.getOlapDatabases();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getCatalog()
	 */
	public String getCatalog() throws OlapException {
		return connection.getCatalog();
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	/**
	 * @param catalogName
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalogName) throws OlapException {
		connection.setCatalog(catalogName);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapCatalog()
	 */
	public Catalog getOlapCatalog() throws OlapException {
		return connection.getOlapCatalog();
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	/**
	 * @param autoCommit
	 * @throws SQLException
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapCatalogs()
	 */
	public NamedList<Catalog> getOlapCatalogs() throws OlapException {
		return connection.getOlapCatalogs();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getSchema()
	 */
	public String getSchema() throws OlapException {
		return connection.getSchema();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		connection.commit();
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/**
	 * @param schemaName
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#setSchema(java.lang.String)
	 */
	public void setSchema(String schemaName) throws OlapException {
		connection.setSchema(schemaName);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapSchema()
	 */
	public Schema getOlapSchema() throws OlapException {
		return connection.getOlapSchema();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getOlapSchemas()
	 */
	public NamedList<Schema> getOlapSchemas() throws OlapException {
		return connection.getOlapSchemas();
	}

	/**
	 * @param readOnly
	 * @throws SQLException
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/**
	 * @param locale
	 * @see org.olap4j.OlapConnection#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		connection.setLocale(locale);
	}

	/**
	 * @return
	 * @see org.olap4j.OlapConnection#getLocale()
	 */
	public Locale getLocale() {
		return connection.getLocale();
	}

	/**
	 * @param roleName
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#setRoleName(java.lang.String)
	 */
	public void setRoleName(String roleName) throws OlapException {
		connection.setRoleName(roleName);
	}

	/**
	 * @return
	 * @see org.olap4j.OlapConnection#getRoleName()
	 */
	public String getRoleName() {
		return connection.getRoleName();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getAvailableRoleNames()
	 */
	public List<String> getAvailableRoleNames() throws OlapException {
		return connection.getAvailableRoleNames();
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#createScenario()
	 */
	public Scenario createScenario() throws OlapException {
		return connection.createScenario();
	}

	/**
	 * @param scenario
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#setScenario(org.olap4j.Scenario)
	 */
	public void setScenario(Scenario scenario) throws OlapException {
		connection.setScenario(scenario);
	}

	/**
	 * @return
	 * @throws OlapException
	 * @see org.olap4j.OlapConnection#getScenario()
	 */
	public Scenario getScenario() throws OlapException {
		return connection.getScenario();
	}

	/**
	 * @param level
	 * @throws SQLException
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/**
	 * @throws SQLException
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/**
	 * @param map
	 * @throws SQLException
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	/**
	 * @param holdability
	 * @throws SQLException
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	/**
	 * @param name
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	/**
	 * @param savepoint
	 * @throws SQLException
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	/**
	 * @param savepoint
	 * @throws SQLException
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	/**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
	 *      int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	/**
	 * @param sql
	 * @param autoGeneratedKeys
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	/**
	 * @param sql
	 * @param columnIndexes
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	/**
	 * @param sql
	 * @param columnNames
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#prepareStatement(java.lang.String,
	 *      java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createClob()
	 */
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createBlob()
	 */
	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createNClob()
	 */
	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createSQLXML()
	 */
	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}

	/**
	 * @param timeout
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isValid(int)
	 */
	public boolean isValid(int timeout) throws SQLException {
		return connection.isValid(timeout);
	}

	/**
	 * @param name
	 * @param value
	 * @throws SQLClientInfoException
	 * @see java.sql.Connection#setClientInfo(java.lang.String,
	 *      java.lang.String)
	 */
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		connection.setClientInfo(name, value);
	}

	/**
	 * @param properties
	 * @throws SQLClientInfoException
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		connection.setClientInfo(properties);
	}

	/**
	 * @param name
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	public String getClientInfo(String name) throws SQLException {
		return connection.getClientInfo(name);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getClientInfo()
	 */
	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}

	/**
	 * @param typeName
	 * @param elements
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createArrayOf(java.lang.String,
	 *      java.lang.Object[])
	 */
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return connection.createArrayOf(typeName, elements);
	}

	/**
	 * @param typeName
	 * @param attributes
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStruct(java.lang.String,
	 *      java.lang.Object[])
	 */
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return connection.createStruct(typeName, attributes);
	}

	/**
	 * @param executor
	 * @throws SQLException
	 * @see java.sql.Connection#abort(java.util.concurrent.Executor)
	 */
	public void abort(Executor executor) throws SQLException {
		connection.abort(executor);
	}

	/**
	 * @param executor
	 * @param milliseconds
	 * @throws SQLException
	 * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor,
	 *      int)
	 */
	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		connection.setNetworkTimeout(executor, milliseconds);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getNetworkTimeout()
	 */
	public int getNetworkTimeout() throws SQLException {
		return connection.getNetworkTimeout();
	}
}
