package org.pivot4j.analytics.datasource;

import java.util.List;

import org.olap4j.OlapDataSource;

public interface DataSourceManager {

	List<CatalogInfo> getCatalogs();

	/**
	 * @param catalogName
	 * @return
	 */
	List<CubeInfo> getCubes(String catalogName);

	/**
	 * Create an OLAP datasource from the specified connection information. Note
	 * that the returned OlapDataSource should be able to serve multiple
	 * connections, so returning a SingleConnectionDataSource instance would
	 * cause an error for certain operations.
	 * 
	 * @param connectionInfo
	 * @return
	 */
	OlapDataSource getDataSource(ConnectionInfo connectionInfo);
}