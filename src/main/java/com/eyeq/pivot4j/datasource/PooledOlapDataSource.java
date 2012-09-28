/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.datasource;

import java.sql.SQLException;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;

public class PooledOlapDataSource extends AbstractOlapDataSource {

	private GenericObjectPool.Config poolConfig;

	private GenericObjectPool<OlapConnection> pool;

	private OlapDataSource dataSource;

	private PooledOlapConnectionFactory connectionFactory;

	/**
	 * @param dataSource
	 */
	public PooledOlapDataSource(OlapDataSource dataSource) {
		this(dataSource, null);
	}

	/**
	 * @param dataSource
	 * @param poolConfig
	 */
	public PooledOlapDataSource(OlapDataSource dataSource,
			GenericObjectPool.Config poolConfig) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'dataSource'.");
		}

		this.dataSource = dataSource;
		this.poolConfig = poolConfig == null ? new GenericObjectPool.Config()
				: poolConfig;
		this.connectionFactory = createConnectionFactory(dataSource);
		this.pool = createPool(connectionFactory, poolConfig);
	}

	/**
	 * @return the dataSource
	 */
	public OlapDataSource getDataSource() {
		return dataSource;
	}

	public synchronized void close() throws SQLException {
		try {
			this.getPool().close();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	/**
	 * @return the connectionFactory
	 */
	protected PooledOlapConnectionFactory createConnectionFactory(
			OlapDataSource dataSource) {
		return new PooledOlapConnectionFactory(dataSource);
	}

	/**
	 * @return the connectionFactory
	 */
	protected PooledOlapConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * @param factory
	 * @param config
	 * @return the pool
	 */
	protected GenericObjectPool<OlapConnection> createPool(
			PoolableObjectFactory<OlapConnection> factory,
			GenericObjectPool.Config config) {
		return new GenericObjectPool<OlapConnection>(factory);
	}

	/**
	 * @return the pool
	 */
	protected GenericObjectPool<OlapConnection> getPool() {
		return pool;
	}

	/**
	 * Note: Both 'userName' and 'password' are ignored.
	 * 
	 * @see com.eyeq.pivot4j.datasource.AbstractOlapDataSource#createConnection(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected OlapConnection createConnection(String userName, String password)
			throws SQLException {
		try {
			return pool.borrowObject();
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	/**
	 * @return the maxIdle
	 */
	public int getMaxIdle() {
		return poolConfig.maxIdle;
	}

	/**
	 * @return the maxActive
	 */
	public int getMaxActive() {
		return poolConfig.maxActive;
	}

	/**
	 * @return the maxWait
	 */
	public long getMaxWait() {
		return poolConfig.maxWait;
	}

	/**
	 * @return the whenExhaustedAction
	 */
	public byte getWhenExhaustedAction() {
		return poolConfig.whenExhaustedAction;
	}

	/**
	 * @return the testOnBorrow
	 */
	public boolean isTestOnBorrow() {
		return poolConfig.testOnBorrow;
	}

	/**
	 * @return the testOnReturn
	 */
	public boolean isTestOnReturn() {
		return poolConfig.testOnReturn;
	}

	/**
	 * @return the testWhileIdle
	 */
	public boolean isTestWhileIdle() {
		return poolConfig.testWhileIdle;
	}

	/**
	 * @return the timeBetweenEvictionRunsMillis
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return poolConfig.timeBetweenEvictionRunsMillis;
	}

	/**
	 * @return the numTestsPerEvictionRun
	 */
	public int getNumTestsPerEvictionRun() {
		return poolConfig.numTestsPerEvictionRun;
	}

	/**
	 * @return the minEvictableIdleTimeMillis
	 */
	public long getMinEvictableIdleTimeMillis() {
		return poolConfig.minEvictableIdleTimeMillis;
	}

	class PooledOlapConnectionFactory extends OlapConnectionFactory {

		/**
		 * @param dataSource
		 */
		public PooledOlapConnectionFactory(OlapDataSource dataSource) {
			super(dataSource);
		}

		/**
		 * @see com.eyeq.pivot4j.datasource.OlapConnectionFactory#makeObject()
		 */
		@Override
		public OlapConnection makeObject() throws Exception {
			return new PooledOlapConnection(super.makeObject(), pool);
		}

		/**
		 * @see com.eyeq.pivot4j.datasource.OlapConnectionFactory#destroyObject(org
		 *      .olap4j.OlapConnection)
		 */
		@Override
		public void destroyObject(OlapConnection con) throws Exception {
			super.destroyObject(con);
			con.unwrap(OlapConnection.class).close();
		}
	}
}
