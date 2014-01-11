package org.pivot4j.analytics.datasource;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class AbstractDataSourceInfo implements DataSourceInfo {

	private static final long serialVersionUID = -1308857219571095791L;

	private String name;

	private String description;

	/**
	 * @see org.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		if (name != null) {
			configuration.setProperty("name", name);
		}

		if (description != null) {
			configuration.setProperty("description", description);
		}
	}

	/**
	 * @see org.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		this.name = configuration.getString("name");
		this.description = configuration.getString("description");
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (!(obj instanceof AbstractDataSourceInfo)) {
			return false;
		}

		AbstractDataSourceInfo other = (AbstractDataSourceInfo) obj;

		return new EqualsBuilder().append(name, other.name).isEquals();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append(name).append(description)
				.build();
	}
}
