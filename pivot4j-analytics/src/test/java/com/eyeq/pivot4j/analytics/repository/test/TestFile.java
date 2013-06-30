package com.eyeq.pivot4j.analytics.repository.test;

import java.io.IOException;

import com.eyeq.pivot4j.analytics.repository.AbstractRepositoryFile;
import com.eyeq.pivot4j.analytics.repository.RepositoryFile;

public class TestFile extends AbstractRepositoryFile {

	private String name;

	private TestFile parent;

	private boolean directory;

	public TestFile() {
		this.name = "";
		this.directory = true;
	}

	/**
	 * @param name
	 * @param parent
	 * @param directory
	 */
	public TestFile(String name, TestFile parent, boolean directory) {
		this.name = name;
		this.parent = parent;
		this.directory = directory;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getPath()
	 */
	@Override
	public String getPath() {
		if (parent == null) {
			return "";
		}

		return parent.getPath() + "/" + name;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getParent()
	 */
	@Override
	public RepositoryFile getParent() throws IOException {
		return parent;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return directory;
	}
}
