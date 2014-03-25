package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractReportFile implements ReportFile {

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getId()
	 */
	@Override
	public Serializable getId() {
		return getPath();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getAncestors()
	 */
	@Override
	public List<ReportFile> getAncestors() throws IOException {
		List<ReportFile> ancestors = new ArrayList<ReportFile>();

		ReportFile parent = this;

		while ((parent = parent.getParent()) != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#isRoot()
	 */
	@Override
	public boolean isRoot() {
		try {
			return getParent() == null;
		} catch (IOException e) {
			throw new UnhandledException(e);
		}
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getExtension()
	 */
	@Override
	public String getExtension() {
		String name = getName();

		int index = name.lastIndexOf('.');

		if (index != -1) {
			return name.substring(index + 1);
		}

		return null;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getPath()).build();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ReportFile)) {
			return false;
		}

		ReportFile other = (ReportFile) obj;

		return new EqualsBuilder().append(getClass(), other.getClass())
				.append(getPath(), other.getPath()).build();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getPath();
	}
}
