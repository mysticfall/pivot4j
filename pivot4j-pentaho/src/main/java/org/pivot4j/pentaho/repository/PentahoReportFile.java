package org.pivot4j.pentaho.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pivot4j.analytics.repository.AbstractReportFile;
import org.pivot4j.analytics.repository.ReportFile;

public class PentahoReportFile extends AbstractReportFile {

	public static final String DEFAULT_EXTENSION = "pivot4j";

	private RepositoryFile file;

	private PentahoReportFile parent;

	/**
	 * @param file
	 * @param parent
	 */
	public PentahoReportFile(RepositoryFile file, PentahoReportFile parent) {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		this.file = file;
		this.parent = parent;
	}

	/**
	 * @return the file
	 */
	protected RepositoryFile getFile() {
		return file;
	}

	/**
	 * @see org.pivot4j.analytics.repository.AbstractReportFile#getId()
	 */
	@Override
	public Serializable getId() {
		return file.getId();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getName()
	 */
	@Override
	public String getName() {
		return file.getName();
	}

	public String getTitle() {
		return file.getTitle();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getPath()
	 */
	@Override
	public String getPath() {
		return file.getPath().replaceAll(RepositoryFile.SEPARATOR,
				ReportFile.SEPARATOR);
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
		return file.isFolder();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() {
		return file.getLastModifiedDate();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getSize()
	 */
	@Override
	public long getSize() {
		return file.getFileSize();
	}
}
