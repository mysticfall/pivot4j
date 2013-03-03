/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledOlapDataSource extends AbstractOlapDataSource {

	private Logger logger = LoggerFactory.getLogger(getClass());

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
			throw new NullArgumentException("dataSource");
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
			if (logger.isInfoEnabled()) {
				logger.info("Disposing the connection pool.");
			}

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
		if (logger.isInfoEnabled()) {
			logger.info("Creating connection pool with following parameters : ");
			logger.info("	- max active : " + config.maxActive);
			logger.info("	- max idle : " + config.maxIdle);
			logger.info("	- min idle: " + config.minIdle);
			logger.info("	- max wait : " + config.maxWait);
		}
		return new GenericObjectPool<OlapConnection>(factory, config);
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
	 * @return the number of active connections
	 */
	public int getNumActive() {
		return pool.getNumActive();
	}

	/**
	 * @return the number of idle connections
	 */
	public int getNumIdle() {
		return pool.getNumIdle();
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
			final OlapConnection connection = super.makeObject();

			InvocationHandler handler = new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {

					if (method.getName().equals("close")) {
						if (logger.isDebugEnabled()) {
							logger.debug("Return a connection to the pool : "
									+ connection);
						}

						pool.returnObject((OlapConnection) proxy);

						if (logger.isDebugEnabled()) {
							logger.debug("	- current pool size : "
									+ pool.getNumActive() + " / "
									+ pool.getMaxActive());
						}

						return null;
					} else {
						return method.invoke(connection, args);
					}
				}
			};

			return (OlapConnection) Proxy.newProxyInstance(getClass()
					.getClassLoader(), new Class[] { OlapConnection.class },
					handler);
		}

		/**
		 * @see com.eyeq.pivot4j.datasource.OlapConnectionFactory#destroyObject(org
		 *      .olap4j.OlapConnection)
		 */
		@Override
		public void destroyObject(OlapConnection con) throws Exception {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing a returned connection object : " + con);
			}

			super.destroyObject(con);
			con.unwrap(OlapConnection.class).close();
		}
	}
}
