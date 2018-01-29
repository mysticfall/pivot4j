/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.datasource;

import java.sql.SQLException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlapConnectionFactory extends
        BasePoolableObjectFactory<OlapConnection> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private OlapDataSource dataSource;

    private String userName;

    private String password;

    private int timeout = 30;

    /**
     * @param dataSource
     */
    public OlapConnectionFactory(OlapDataSource dataSource) {
        if (dataSource == null) {
            throw new NullArgumentException("dataSource");
        }

        this.dataSource = dataSource;
    }

    /**
     * @return the logger
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
     */
    @Override
    public OlapConnection makeObject() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating a new OLAP connection.");
        }
        try {
            return dataSource.getConnection(userName, password);
        } catch (SQLException e) {
            throw new PivotException(e);
        }
    }

    /**
     * @see
     * org.apache.commons.pool.BasePoolableObjectFactory#destroyObject(java.
     * lang.Object)
     */
    @Override
    public void destroyObject(OlapConnection con) {
        try {
            super.destroyObject(con);

            if (logger.isDebugEnabled()) {
                logger.debug("Closing a returned OLAP connection.");
            }

            con.close();
        } catch (Exception e) {
            throw new PivotException(e);
        }
    }

    /**
     * @see
     * org.apache.commons.pool.BasePoolableObjectFactory#validateObject(java
     * .lang.Object)
     */
    @Override
    public boolean validateObject(OlapConnection con) {
        try {
            return super.validateObject(con) && con.isValid(timeout);
        } catch (SQLException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to validate an OLAP connection.", e);
            }
            return false;
        }
    }
}
