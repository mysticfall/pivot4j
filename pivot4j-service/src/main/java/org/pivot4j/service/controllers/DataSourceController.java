/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import java.util.List;

import org.pivot4j.service.datasource.CatalogInfo;
import org.pivot4j.service.datasource.CubeInfo;
import org.pivot4j.service.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController implements ServiceController {

	@Autowired
	private DataSourceManager dataSourceManager;

	@RequestMapping(method = RequestMethod.GET, headers = HEADER_JSON)
	public List<CatalogInfo> getCatalogs() {
		return dataSourceManager.getCatalogs();
	}

	@RequestMapping(value = "/{catalog}", method = RequestMethod.GET, headers = HEADER_JSON)
	public List<CubeInfo> getCubes(@PathVariable String catalog) {
		return dataSourceManager.getCubes(catalog);
	}

	/**
	 * @return the dataSourceManager
	 */
	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	/**
	 * @param dataSourceManager
	 *            the dataSourceManager to set
	 */
	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}
}