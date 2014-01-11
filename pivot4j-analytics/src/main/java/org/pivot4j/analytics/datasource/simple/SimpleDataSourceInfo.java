package org.pivot4j.analytics.datasource.simple;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pivot4j.analytics.datasource.AbstractDataSourceInfo;

public class SimpleDataSourceInfo extends AbstractDataSourceInfo {

	private static final long serialVersionUID = -513787516897344513L;

	private String url;

	private String userName;

	private String password;

	private String driverClass;

	private Properties properties;

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceInfo#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (userName != null) {
			configuration.setProperty("user", userName);
		}

		if (password != null) {
			configuration.setProperty("password", password);
		}

		if (url != null) {
			configuration.setProperty("url", url);
		}

		if (driverClass != null) {
			configuration.setProperty("driverClass", url);
		}

		if (properties != null) {
			int index = 0;

			Enumeration<Object> en = properties.elements();
			while (en.hasMoreElements()) {
				String prefix = String.format("properties.property(%s)", index);

				Object key = en.nextElement();
				Object value = properties.get(key);

				configuration.setProperty(prefix + "[@name]", key);
				configuration.setProperty(prefix, value);

				index++;
			}
		}
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceInfo#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		SubnodeConfiguration connectionConfig = configuration
				.configurationAt("connection-info");

		this.url = connectionConfig.getString("url");
		this.driverClass = connectionConfig.getString("driverClass");
		this.userName = connectionConfig.getString("user");
		this.password = connectionConfig.getString("password");
		this.properties = new Properties();

		List<HierarchicalConfiguration> propertiesConfig = connectionConfig
				.configurationsAt("properties.property");
		for (HierarchicalConfiguration propertyConfig : propertiesConfig) {
			String key = propertyConfig.getString("[@name]");
			String value = propertyConfig.getString("");

			properties.put(key, value);
		}
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the driverClass
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * @param driverClass
	 *            the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceInfo#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getName()).append(driverClass)
				.append(url).toHashCode();
	}

	/**
	 * @see org.pivot4j.analytics.datasource.AbstractDataSourceInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof SimpleDataSourceInfo)) {
			return false;
		}

		SimpleDataSourceInfo other = (SimpleDataSourceInfo) obj;

		return new EqualsBuilder().append(getName(), other.getName())
				.append(driverClass, other.driverClass).append(url, other.url)
				.isEquals();
	}
}
