/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.pivot4j.service.datasource.ConnectionInfo;
import org.pivot4j.service.datasource.DataSourceManager;
import org.pivot4j.service.model.CatalogModel;
import org.pivot4j.service.model.CubeDetail;
import org.pivot4j.service.model.CubeModel;
import org.pivot4j.service.model.DimensionDetail;
import org.pivot4j.service.model.HierarchyDetail;
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
	public List<CatalogModel> getCatalogs() {
		return dataSourceManager.getCatalogs();
	}

	@RequestMapping(value = "/{catalogName:.+}", method = RequestMethod.GET, headers = HEADER_JSON)
	public List<CubeModel> getCubes(@PathVariable String catalogName,
			HttpServletResponse response) throws IOException {
		List<CubeModel> cubes = null;

		try {
			cubes = dataSourceManager.getCubes(catalogName);
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Unable to find a catalog with the specified name: "
							+ catalogName);
		}

		return cubes;
	}

	@RequestMapping(value = "/{catalogName:.+}/{cubeName:.+}", method = RequestMethod.GET, headers = HEADER_JSON)
	public CubeDetail getCube(@PathVariable String catalogName,
			@PathVariable String cubeName, HttpServletResponse response)
			throws SQLException, IOException {
		CubeCallback<CubeDetail> callback = new CubeCallback<CubeDetail>() {
			public CubeDetail run(Cube cube) {
				return new CubeDetail(cube);
			}
		};

		return runWithCube(catalogName, cubeName, response, callback);
	}

	@RequestMapping(value = "/{catalogName:.+}/{cubeName:.+}/dimensions/{dimensionName:.+}", method = RequestMethod.GET, headers = HEADER_JSON)
	public DimensionDetail getDimension(@PathVariable String catalogName,
			@PathVariable String cubeName, @PathVariable String dimensionName,
			HttpServletResponse response) throws SQLException, IOException {
		DimensionCallback<DimensionDetail> callback = new DimensionCallback<DimensionDetail>() {
			public DimensionDetail run(Dimension dimension) {
				return new DimensionDetail(dimension);
			}
		};

		return runWithDimension(catalogName, cubeName, dimensionName, response,
				callback);
	}

	@RequestMapping(value = "/{catalogName:.+}/{cubeName:.+}/dimensions/{dimensionName:.+}/{hierarchyName:.+}", method = RequestMethod.GET, headers = HEADER_JSON)
	public HierarchyDetail getHierarchy(@PathVariable String catalogName,
			@PathVariable String cubeName, @PathVariable String dimensionName,
			@PathVariable String hierarchyName, HttpServletResponse response)
			throws SQLException, IOException {
		HierarchyCallback<HierarchyDetail> callback = new HierarchyCallback<HierarchyDetail>() {
			public HierarchyDetail run(Hierarchy hierarchy) {
				return new HierarchyDetail(hierarchy);
			}
		};

		return runWithHierarchy(catalogName, cubeName, dimensionName,
				hierarchyName, response, callback);
	}

	protected interface DataCallback<T, R> {

		R run(T data);
	}

	protected interface CubeCallback<T> extends DataCallback<Cube, T> {
	}

	protected interface DimensionCallback<T> extends DataCallback<Dimension, T> {
	}

	protected interface HierarchyCallback<T> extends DataCallback<Hierarchy, T> {
	}

	protected <T> T runWithCube(final String catalogName,
			final String cubeName, HttpServletResponse response,
			CubeCallback<T> callback) throws IOException, SQLException {
		ConnectionInfo info = new ConnectionInfo(catalogName, cubeName);
		OlapDataSource dataSource = dataSourceManager.getDataSource(info);

		T result = null;

		if (dataSource == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Unable to find a data source matching the specified connection information: "
							+ info);
		} else {
			try (OlapConnection connection = dataSource.getConnection()) {
				Cube cube = connection.getOlapSchema().getCubes().get(cubeName);

				if (cube == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND,
							"Cube with the specified name does not exist: "
									+ cubeName);
				} else {
					result = callback.run(cube);
				}
			}
		}

		return result;
	}

	protected <T> T runWithDimension(final String catalogName,
			final String cubeName, final String dimensionName,
			HttpServletResponse response, DimensionCallback<T> callback)
			throws IOException, SQLException {
		T result = null;

		CubeCallback<Dimension> cubeCallback = new CubeCallback<Dimension>() {
			public Dimension run(Cube cube) {
				return cube.getDimensions().get(dimensionName);
			}
		};

		Dimension dimension = runWithCube(catalogName, cubeName, response,
				cubeCallback);

		if (dimension == null) {
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Dimension with the specified name does not exist: "
								+ dimensionName);
			}
		} else {
			result = callback.run(dimension);
		}

		return result;
	}

	protected <T> T runWithHierarchy(final String catalogName,
			final String cubeName, final String dimensionName,
			final String hierarchyName, HttpServletResponse response,
			HierarchyCallback<T> callback) throws IOException, SQLException {
		T result = null;

		DimensionCallback<Hierarchy> dimensionCallback = new DimensionCallback<Hierarchy>() {
			public Hierarchy run(Dimension dimension) {
				return dimension.getHierarchies().get(hierarchyName);
			}
		};

		Hierarchy hierarchy = runWithDimension(catalogName, cubeName,
				dimensionName, response, dimensionCallback);

		if (hierarchy == null) {
			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Hierarchy with the specified name does not exist: "
								+ hierarchyName);
			}
		} else {
			result = callback.run(hierarchy);
		}

		return result;
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