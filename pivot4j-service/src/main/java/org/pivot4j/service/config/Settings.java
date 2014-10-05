/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.el.ExpressionEvaluatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service("settings")
public class Settings {

	public static final String CONFIG_FILE = "pivot4j.config";

	public static final String APPLICATION_HOME = "pivot4j.home";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ExpressionEvaluatorFactory expressionEvaluatorFactory;

	private Resource configLocation;

	private File applicationHome;

	private HierarchicalConfiguration configuration;

	@PostConstruct
	protected void initialize() throws IOException, ConfigurationException {
		if (logger.isInfoEnabled()) {
			logger.info("Reading configuration parameters.");
		}

		if (applicationHome == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Parameter 'applicationHome' is not set. Using the default path.");
			}

			this.applicationHome = new File(System.getProperty("user.home")
					+ File.separator + ".pivot4j");
		}

		if (logger.isInfoEnabled()) {
			logger.info("Using application home : {}", applicationHome);
		}

		if (!applicationHome.exists()) {
			applicationHome.mkdirs();
		}

		InputStream in = null;

		try {
			ServletContext servletContext = applicationContext
					.getServletContext();

			if (configLocation == null || !configLocation.exists()) {
				StringBuilder builder = new StringBuilder();

				builder.append(applicationHome.getPath());
				builder.append(File.separator);
				builder.append(".pivot4j");
				builder.append(File.separator);
				builder.append("pivot4j-config.xml");

				this.configLocation = new FileSystemResource(builder.toString());
			}

			if (configLocation.exists()) {
				if (logger.isInfoEnabled()) {
					logger.info("Using config location: {}", configLocation);
				}

				in = configLocation.getInputStream();
			} else {
				String defaultConfig = "/WEB-INF/pivot4j-config.xml";

				if (logger.isInfoEnabled()) {
					logger.info("Config file does not exist. Using default : "
							+ defaultConfig);
				}

				String location = servletContext.getRealPath(defaultConfig);

				if (location != null) {
					in = new FileInputStream(location);
				}
			}

			if (in == null) {
				throw new IOException(
						"Unable to find a valid pivot4j-config.xml file.");
			}

			this.configuration = readConfiguration(servletContext, in);
		} finally {
			IOUtils.closeQuietly(in);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Pivot4J Analytics has been initialized successfully.");
		}
	}

	/**
	 * @param context
	 * @param in
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	protected HierarchicalConfiguration readConfiguration(
			ServletContext context, InputStream in)
			throws ConfigurationException, IOException {
		ExpressionEvaluator evaluator = expressionEvaluatorFactory
				.createEvaluator();

		String source = IOUtils.toString(in);
		source = (String) evaluator.evaluate(source, createELContext(context));

		XMLConfiguration config = new XMLConfiguration();
		config.load(new StringReader(source));

		return config;
	}

	/**
	 * @param context
	 * @return
	 */
	protected ExpressionContext createELContext(ServletContext context) {
		ExpressionContext elContext = new ExpressionContext();

		elContext.put("FS", File.separator);

		elContext.put("userHome", System.getProperty("user.dir"));
		elContext.put("appHome", applicationHome.getPath());

		String webRoot = context.getRealPath("/WEB-INF");

		if (webRoot != null) {
			elContext.put("webRoot", webRoot);
		}

		return elContext;
	}

	/**
	 * @return the applicationHome
	 */
	public File getApplicationHome() {
		return applicationHome;
	}

	/**
	 * @return the configuration
	 */
	public HierarchicalConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @return the applicationContext
	 */
	public WebApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * @param applicationContext
	 *            the applicationContext to set
	 */
	public void setApplicationContext(WebApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return the configLocation
	 */
	public Resource getConfigLocation() {
		return configLocation;
	}

	/**
	 * @param configLocation
	 *            the configLocation to set
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * @return the expressionEvaluatorFactory
	 */
	public ExpressionEvaluatorFactory getExpressionEvaluatorFactory() {
		return expressionEvaluatorFactory;
	}

	/**
	 * @param expressionEvaluatorFactory
	 *            the expressionEvaluatorFactory to set
	 */
	public void setExpressionEvaluatorFactory(
			ExpressionEvaluatorFactory expressionEvaluatorFactory) {
		this.expressionEvaluatorFactory = expressionEvaluatorFactory;
	}
}
