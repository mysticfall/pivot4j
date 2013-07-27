package com.eyeq.pivot4j.analytics.repository.test;

import java.io.IOException;
import java.util.Date;

import com.eyeq.pivot4j.analytics.repository.AbstractReportFile;
import com.eyeq.pivot4j.analytics.repository.ReportFile;

public class TestFile extends AbstractReportFile {

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
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#getName()
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
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#getPath()
	 */
	@Override
	public String getPath() {
		if (parent == null) {
			return "";
		}

		return parent.getPath() + "/" + name;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#getParent()
	 */
	@Override
	public ReportFile getParent() throws IOException {
		return parent;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() {
		return new Date();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportFile#getSize()
	 */
	@Override
	public long getSize() {
		return 0;
	}
}
