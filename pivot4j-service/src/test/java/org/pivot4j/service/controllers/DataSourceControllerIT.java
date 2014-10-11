/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.pivot4j.service.datasource.ConnectionInfo;
import org.pivot4j.service.datasource.DataSourceManager;
import org.pivot4j.service.model.CatalogModel;
import org.pivot4j.service.model.CubeDetail;
import org.pivot4j.service.model.CubeModel;
import org.pivot4j.service.model.DimensionDetail;
import org.pivot4j.service.model.DimensionModel;
import org.pivot4j.service.model.HierarchyModel;
import org.pivot4j.service.model.MeasureModel;
import org.springframework.beans.factory.annotation.Autowired;

public class DataSourceControllerIT extends AbstractIntegrationTest {

	@Autowired
	private DataSourceManager dataSourceManager;

	@Test
	public void thatCatalogListCanBeRead() throws Exception {
		List<CatalogModel> catalogs = dataSourceManager.getCatalogs();

		assertNotNull(catalogs);
		assertFalse(catalogs.isEmpty());

		String url = "/api/datasource";

		getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(catalogs)));

		getMvc().perform(get(url + "/").accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(catalogs)));
	}

	@Test
	public void thatCubeListCanBeRead() throws Exception {
		String catalogName = "FoodMart Mondrian";

		List<CubeModel> cubes = dataSourceManager.getCubes(catalogName);

		assertNotNull(cubes);
		assertEquals(7, cubes.size());

		CubeModel cube = cubes.get(6);

		assertNotNull(cube);
		assertEquals("Sales", cube.getName());
		assertEquals("[Sales]", cube.getUniqueName());
		assertEquals("Sales", cube.getCaption());
		assertEquals(null, cube.getDescription());

		assertEquals(13, cube.getDimensionCount());
		assertEquals(9, cube.getMeasureCount());
		assertEquals(true, cube.isDrillThroughEnabled());

		String url = "/api/datasource/" + catalogName;

		getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(cubes)));

		getMvc().perform(get(url + "/").accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(cubes)));
	}

	@Test
	public void thatInvalidCatalogNameThrowsNotFoundError() throws Exception {
		getMvc().perform(
				get("/api/datasource/Dummy Catalog").accept(
						APPLICATION_JSON_VALUE)).andExpect(
				status().isNotFound());

		getMvc().perform(
				get("/api/datasource/Dummy Catalog/Sales").accept(
						APPLICATION_JSON_VALUE)).andExpect(
				status().isNotFound());
	}

	@Test
	public void thatCubeCanBeRead() throws Exception {
		String catalogName = "FoodMart Mondrian";
		String cubeName = "Sales";

		OlapDataSource dataSource = dataSourceManager
				.getDataSource(new ConnectionInfo(catalogName, cubeName));

		try (OlapConnection connection = dataSource.getConnection()) {
			Cube cube = connection.getOlapSchema().getCubes().get(cubeName);

			CubeDetail model = new CubeDetail(cube);

			assertEquals("Sales", model.getName());
			assertEquals("[Sales]", model.getUniqueName());
			assertEquals("Sales", model.getCaption());
			assertEquals(null, model.getDescription());

			assertEquals(13, model.getDimensionCount());
			assertEquals(9, model.getMeasureCount());
			assertEquals(true, model.isDrillThroughEnabled());

			List<DimensionModel> dimensions = model.getDimensions();

			assertEquals(13, dimensions.size());

			DimensionModel dimension = dimensions.get(10);

			assertEquals("Gender", dimension.getName());
			assertEquals("[Gender]", dimension.getUniqueName());
			assertEquals("Gender", dimension.getCaption());
			assertEquals(null, dimension.getDescription());
			assertEquals(1, dimension.getHierarchyCount());

			List<MeasureModel> measures = model.getMeasures();

			assertEquals(9, measures.size());

			MeasureModel measure = measures.get(4);

			assertEquals("Customer Count", measure.getName());
			assertEquals("[Measures].[Customer Count]", measure.getUniqueName());
			assertEquals("Customer Count", measure.getCaption());
			assertEquals(null, measure.getDescription());

			String url = "/api/datasource/" + catalogName + "/" + cubeName;

			getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(model)));

			getMvc().perform(get(url + "/").accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(model)));
		}
	}

	@Test
	public void thatDimensionsCanBeRead() throws Exception {
		String catalogName = "FoodMart Mondrian";
		String cubeName = "Sales";

		OlapDataSource dataSource = dataSourceManager
				.getDataSource(new ConnectionInfo(catalogName, cubeName));

		try (OlapConnection connection = dataSource.getConnection()) {
			Cube cube = connection.getOlapSchema().getCubes().get(cubeName);

			List<DimensionModel> dimensions = new CubeDetail(cube)
					.getDimensions();

			assertEquals(13, dimensions.size());

			DimensionModel dimension = dimensions.get(10);

			assertEquals("Gender", dimension.getName());
			assertEquals("[Gender]", dimension.getUniqueName());
			assertEquals("Gender", dimension.getCaption());
			assertEquals(null, dimension.getDescription());
			assertEquals(1, dimension.getHierarchyCount());

			String url = "/api/datasource/" + catalogName + "/" + cubeName
					+ "/dimensions";

			getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(dimensions)));

			getMvc().perform(get(url + "/").accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(dimensions)));
		}
	}

	@Test
	public void thatInvalidCubeNameThrowsNotFoundError() throws Exception {
		getMvc().perform(
				get("/api/datasource/FoodMart Mondrian/Dummy Cube").accept(
						APPLICATION_JSON_VALUE)).andExpect(
				status().isNotFound());

		getMvc().perform(
				get("/api/datasource/FoodMart Mondrian/Dummy Cube/dimensions")
						.accept(APPLICATION_JSON_VALUE)).andExpect(
				status().isNotFound());

		getMvc().perform(
				get(
						"/api/datasource/FoodMart Mondrian/Dummy Cube/dimensions/Product")
						.accept(APPLICATION_JSON_VALUE)).andExpect(
				status().isNotFound());
	}

	@Test
	public void thatDimensionCanBeRead() throws Exception {
		String catalogName = "FoodMart Mondrian";
		String cubeName = "Sales";
		String dimensionName = "Product";

		OlapDataSource dataSource = dataSourceManager
				.getDataSource(new ConnectionInfo(catalogName, cubeName));

		try (OlapConnection connection = dataSource.getConnection()) {
			Cube cube = connection.getOlapSchema().getCubes().get(cubeName);

			Dimension dimension = cube.getDimensions().get(dimensionName);
			DimensionDetail model = new DimensionDetail(dimension);

			assertEquals("Product", model.getName());
			assertEquals("[Product]", model.getUniqueName());
			assertEquals("Product", model.getCaption());
			assertEquals(null, model.getDescription());

			assertEquals(1, model.getHierarchyCount());

			List<HierarchyModel> hierarchies = model.getHierarchies();

			assertEquals(1, hierarchies.size());

			String url = "/api/datasource/" + catalogName + "/" + cubeName
					+ "/dimensions/" + dimensionName;

			getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(model)));

			getMvc().perform(get(url + "/").accept(APPLICATION_JSON_VALUE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(APPLICATION_JSON_VALUE))
					.andExpect(content().json(asString(model)));
		}
	}
}