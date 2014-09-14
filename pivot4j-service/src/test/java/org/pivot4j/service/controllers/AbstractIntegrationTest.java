/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import static org.pivot4j.service.controllers.AbstractIntegrationTest.TEST_CONTEXT;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = TEST_CONTEXT)
abstract public class AbstractIntegrationTest {

	public static final String TEST_CONTEXT = "classpath:/test-context.xml";

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setup() {
		this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@After
	public void tearDown() {
		this.mvc = null;
	}

	/**
	 * @return the context
	 */
	public WebApplicationContext getContext() {
		return context;
	}

	/**
	 * @return the mvc
	 */
	public MockMvc getMvc() {
		return mvc;
	}

	protected String asString(Object object) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(object);
	}
}
