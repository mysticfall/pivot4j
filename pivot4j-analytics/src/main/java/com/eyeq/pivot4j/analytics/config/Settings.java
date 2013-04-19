package com.eyeq.pivot4j.analytics.config;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "settings", eager = true)
@ApplicationScoped
public class Settings extends AbstractMap<String, String> {

	private Set<Entry<String, String>> entries = new HashSet<Entry<String, String>>();

	public static final String APPLICATION_HOME = "applicationHome";

	public static final String PREFIX = "pivot4j.";

	public static final String RESOURCE_PREFIX = "resourcePrefix";

	public static final String LOCALE_ATTRIBUTE_NAME = "localeAttributeName";

	public static final String CODE_MIRROR_THEME = "codeMirrorTheme";

	public static final String VIEW_PARAMETER_NAME = "viewParameterName";

	private File applicationHome;

	@PostConstruct
	protected void initialize() {
		Logger log = LoggerFactory.getLogger(getClass());

		if (log.isInfoEnabled()) {
			log.info("Reading configuration parameters.");
		}

		ExternalContext context = FacesContext.getCurrentInstance()
				.getExternalContext();

		@SuppressWarnings("unchecked")
		Map<String, String> parameters = context.getInitParameterMap();

		for (String key : parameters.keySet()) {
			if (key.startsWith(PREFIX)) {
				String configKey = key.substring(PREFIX.length());
				String configValue = StringUtils.trim(parameters.get(key));

				SettingEntry entry = new SettingEntry(configKey, configValue);

				if (log.isInfoEnabled()) {
					log.info(configKey + " : " + configValue);
				}

				entries.add(entry);
			}
		}

		String path = StringUtils.trimToNull(get(Settings.APPLICATION_HOME));

		if (path == null) {
			if (log.isInfoEnabled()) {
				log.info("Parameter 'applicationHome' is not set. Using the default path.");
			}

			path = System.getProperty("user.home") + File.separator
					+ ".pivot4j";
		}

		if (log.isInfoEnabled()) {
			log.info("Using application home : " + path);
		}

		this.applicationHome = new File(path);

		if (!applicationHome.exists()) {
			applicationHome.mkdirs();
		}

		if (log.isInfoEnabled()) {
			log.info("Pivot4J web application has been initialized successfully.");
		}
	}

	/**
	 * @return the applicationHome
	 */
	public File getApplicationHome() {
		return applicationHome;
	}

	/**
	 * @see java.util.AbstractMap#entrySet()
	 */
	@Override
	public Set<Entry<String, String>> entrySet() {
		return entries;
	}

	protected static class SettingEntry implements Entry<String, String> {

		private String key;

		private String value;

		/**
		 * @param key
		 * @param value
		 */
		protected SettingEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * @see java.util.Map.Entry#getKey()
		 */
		@Override
		public String getKey() {
			return key;
		}

		/**
		 * @see java.util.Map.Entry#getValue()
		 */
		@Override
		public String getValue() {
			return value;
		}

		/**
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		@Override
		public String setValue(String value) {
			this.value = value;
			return value;
		}
	}
}
