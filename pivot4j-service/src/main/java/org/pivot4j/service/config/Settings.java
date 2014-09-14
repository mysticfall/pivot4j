package org.pivot4j.service.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.el.ExpressionEvaluatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
public class Settings {

	public static final String CONFIG_FILE = "pivot4j.config";

	public static final String APPLICATION_HOME = "pivot4j.home";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ExpressionEvaluatorFactory expressionEvaluatorFactory;

	private File applicationHome;

	private HierarchicalConfiguration configuration;

	private String theme;

	private String editorTheme;

	private String resourcePrefix;

	private String viewParameterName;

	private String fileParameterName;

	private String pathParameterName;

	private String localeAttributeName;

	private SortedMap<String, String> availableThemes;

	@PostConstruct
	protected void initialize() throws IOException, ConfigurationException {
		if (logger.isInfoEnabled()) {
			logger.info("Reading configuration parameters.");
		}

		if (applicationHome == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Property 'applicationHome' is not set. Using the default path.");
			}

			String path = System.getProperty("user.home") + File.separator
					+ ".pivot4j";
			this.applicationHome = new File(path);
		}

		if (!applicationHome.exists()) {
			applicationHome.mkdirs();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Using application home : {}",
					applicationHome.getPath());
		}

		InputStream in = null;

		try {
			ServletContext servletContext = applicationContext
					.getServletContext();

			File configFile = new File(applicationHome.getPath()
					+ File.separator + "pivot4j-config.xml");

			if (!configFile.exists()) {
				String defaultConfig = "/WEB-INF/pivot4j-config.xml";

				if (logger.isInfoEnabled()) {
					logger.info("Config file does not exist. Using default : "
							+ defaultConfig);
				}

				String location = servletContext.getRealPath(defaultConfig);

				if (location != null) {
					configFile = new File(location);
				}
			}

			if (!configFile.exists()) {
				throw new IOException("Unable to read the default config : "
						+ configFile);
			}

			in = new FileInputStream(configFile);

			this.configuration = readConfiguration(servletContext, in);
		} finally {
			IOUtils.closeQuietly(in);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Pivot4J Analytics has been initialized successfully.");
		}

		configuration.addConfigurationListener(new ConfigurationListener() {

			@Override
			public void configurationChanged(ConfigurationEvent event) {
				onConfigurationChanged(event);
			}
		});
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
	 * @param event
	 */
	protected void onConfigurationChanged(ConfigurationEvent event) {
		this.editorTheme = null;

		this.resourcePrefix = null;
		this.viewParameterName = null;
		this.localeAttributeName = null;
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

	public String getTheme() {
		if (theme == null) {
			this.theme = configuration.getString(
					"appearances.ui-theme.default", "redmond").trim();
		}

		return theme;
	}

	public String getEditorTheme() {
		if (editorTheme == null) {
			this.editorTheme = StringUtils.trimToNull(configuration
					.getString("appearances.editor-theme"));
		}

		return editorTheme;
	}

	/**
	 * @return the availableThemes
	 */
	public SortedMap<String, String> getAvailableThemes() {
		synchronized (this) {
			if (availableThemes == null) {
				this.availableThemes = new TreeMap<String, String>();

				List<HierarchicalConfiguration> configurations = configuration
						.configurationsAt("appearances.ui-theme.available-themes.theme");
				for (HierarchicalConfiguration config : configurations) {
					String name = config.getString("[@name]");
					availableThemes.put(StringUtils.capitalize(name), name);
				}
			}
		}

		return availableThemes;
	}

	public String getResourcePrefix() {
		if (resourcePrefix == null) {
			this.resourcePrefix = StringUtils.trimToEmpty(configuration
					.getString("web.resource-prefix"));
		}

		return resourcePrefix;
	}

	public String getViewParameterName() {
		if (viewParameterName == null) {
			this.viewParameterName = configuration.getString(
					"web.view-parameter", "viewId").trim();
		}

		return viewParameterName;
	}

	public String getFileParameterName() {
		if (fileParameterName == null) {
			this.fileParameterName = configuration.getString(
					"web.file-parameter", "fileId").trim();
		}

		return fileParameterName;
	}

	public String getPathParameterName() {
		if (pathParameterName == null) {
			this.pathParameterName = configuration.getString(
					"web.path-parameter", "path").trim();
		}

		return pathParameterName;
	}

	public String getLocaleAttributeName() {
		if (localeAttributeName == null) {
			this.localeAttributeName = configuration.getString(
					"web.locale-attribute", "locale").trim();
		}

		return localeAttributeName;
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
