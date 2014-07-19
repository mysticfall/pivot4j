package org.pivot4j.analytics.repository.test;

import java.io.IOException;
import java.util.Date;

import org.pivot4j.analytics.repository.AbstractReportFile;
import org.pivot4j.analytics.repository.ReportFile;

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
	 * @see org.pivot4j.analytics.repository.ReportFile#getName()
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
	 * @see org.pivot4j.analytics.repository.ReportFile#getPath()
	 */
	@Override
	public String getPath() {
		if (parent == null) {
			return "";
		}

		return parent.getPath() + "/" + name;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getParent()
	 */
	@Override
	public ReportFile getParent() throws IOException {
		return parent;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() {
		return new Date();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getSize()
	 */
	@Override
	public long getSize() {
		return 0;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#canRead()
	 */
	@Override
	public boolean canRead() {
		return true;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#canWrite()
	 */
	@Override
	public boolean canWrite() {
		return true;
	}
}
