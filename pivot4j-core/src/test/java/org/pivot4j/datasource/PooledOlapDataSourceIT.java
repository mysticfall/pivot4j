/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.datasource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;
import org.olap4j.OlapConnection;
import org.pivot4j.AbstractIntegrationTestCase;
import org.pivot4j.datasource.PooledOlapDataSource;

public class PooledOlapDataSourceIT extends AbstractIntegrationTestCase {

    /**
     * Test method for
     * {@link org.pivot4j.datasource.AbstractOlapDataSource#getConnection()} .
     *
     * @throws SQLException
     */
    @Test
    public void testGetConnection() throws SQLException {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        config.maxActive = 3;
        config.maxIdle = 3;

        PooledOlapDataSource dataSource = new PooledOlapDataSource(
                getDataSource(), config);
        OlapConnection con1 = dataSource.getConnection();
        OlapConnection con2 = dataSource.getConnection();
        OlapConnection con3 = dataSource.getConnection();

        assertThat("Invalid connection returned.", con1.isValid(10), is(true));
        assertThat("Invalid connection returned.", con2.isValid(10), is(true));
        assertThat("Invalid connection returned.", con3.isValid(10), is(true));

        assertThat("Closed connection returned.", con1.isClosed(), is(false));
        assertThat("Closed connection returned.", con2.isClosed(), is(false));
        assertThat("Closed connection returned.", con3.isClosed(), is(false));

        assertThat("Should return a new Connection instance.",
                con1.unwrap(OlapConnection.class),
                not(sameInstance(con2.unwrap(OlapConnection.class))));
        assertThat("Should return a new Connection instance.",
                con2.unwrap(OlapConnection.class),
                not(sameInstance(con3.unwrap(OlapConnection.class))));

        con3.close();

        assertThat("Connection should remain open.", con3.isClosed(), is(false));

        OlapConnection con4 = dataSource.getConnection();
        assertThat("Should reuse an existing connection.",
                con3.unwrap(OlapConnection.class),
                sameInstance(con4.unwrap(OlapConnection.class)));

        assertThat("Closed connection returned.", con4.isClosed(), is(false));

        dataSource.close();

        con1.close();
        con2.close();
        con4.close();

        assertThat(
                "Connection remains open after data source has been closed.",
                con1.isClosed(), is(true));
        assertThat(
                "Connection remains open after data source has been closed.",
                con2.isClosed(), is(true));
        assertThat(
                "Connection remains open after data source has been closed.",
                con3.isClosed(), is(true));
    }
}
