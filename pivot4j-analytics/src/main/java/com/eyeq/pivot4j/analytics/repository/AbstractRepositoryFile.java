package com.eyeq.pivot4j.analytics.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractRepositoryFile implements RepositoryFile {

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getId()
	 */
	@Override
	public String getId() {
		return getPath();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getAncestors()
	 */
	@Override
	public List<RepositoryFile> getAncestors() throws IOException {
		List<RepositoryFile> ancestors = new ArrayList<RepositoryFile>();

		RepositoryFile parent = this;

		while ((parent = parent.getParent()) != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}

	/**
	 * @throws IOException
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#isRoot()
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
		if (!(obj instanceof RepositoryFile)) {
			return false;
		}

		RepositoryFile other = (RepositoryFile) obj;

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
