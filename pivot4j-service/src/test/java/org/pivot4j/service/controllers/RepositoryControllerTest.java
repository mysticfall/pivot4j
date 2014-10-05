/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.pivot4j.service.repository.ReportFile;
import org.pivot4j.service.repository.test.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class RepositoryControllerTest extends AbstractIntegrationTest {

	@Autowired
	private TestRepository repository;

	/**
	 * @see org.pivot4j.service.controllers.AbstractIntegrationTest#setup()
	 */
	@Override
	public void setup() {
		super.setup();
		repository.initialize();
	}

	@Test
	public void thatFileInfoCanBeRead() throws Exception {
		ReportFile root = repository.getRoot();
		ReportFile file = repository.getFile("/Advanced Tests/Properties");

		getMvc().perform(
				get("/api/repository/info").accept(APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(root)));

		getMvc().perform(
				get("/api/repository/" + file.getId() + "/info").accept(
						APPLICATION_JSON_VALUE)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_VALUE))
				.andExpect(content().json(asString(file)));
	}
}