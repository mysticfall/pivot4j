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
import org.pivot4j.service.datasource.CatalogInfo;
import org.pivot4j.service.datasource.CubeInfo;
import org.pivot4j.service.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;

public class DataSourceControllerTest extends AbstractIntegrationTest {

	@Autowired
	private DataSourceManager dataSourceManager;

	@Test
	public void thatCatalogListCanBeRead() throws Exception {
		List<CatalogInfo> catalogs = dataSourceManager.getCatalogs();

		assertNotNull(catalogs);
		assertFalse(catalogs.isEmpty());

		getMvc().perform(
				get("/api/datasource/catalogs").accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(catalogs)));
	}

	@Test
	public void thatCubeListCanBeRead() throws Exception {
		String catalog = "FoodMart Mondrian";

		List<CubeInfo> cubes = dataSourceManager.getCubes(catalog);

		assertNotNull(cubes);
		assertEquals(cubes.size(), 7);

		String url = "/api/datasource/catalogs/" + catalog + "/cubes";

		getMvc().perform(get(url).accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(cubes)));
	}
}