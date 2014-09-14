/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.repository.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.service.repository.AbstractReportFile;
import org.pivot4j.service.repository.ReportFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocalFile extends AbstractReportFile {

	public static final String ID_SEPARATOR = "|";

	@JsonIgnore
	private File root;

	@JsonIgnore
	private File file;

	private String path;

	/**
	 * @param file
	 * @param root
	 * @throws IOException
	 */
	public LocalFile(File file, File root) throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		if (root == null) {
			throw new NullArgumentException("root");
		}

		String rootPath = root.getCanonicalPath();
		String filePath = file.getCanonicalPath();

		if (!filePath.startsWith(rootPath)) {
			throw new IllegalArgumentException(
					"The specified file path does not begin with the root path.");
		}

		this.file = file;
		this.root = root;

		this.path = filePath.substring(rootPath.length());

		if (path.length() == 0) {
			path = SEPARATOR;
		} else {
			path = StringUtils.replaceChars(path, File.separator, SEPARATOR);
		}
	}

	/**
	 * @return the root
	 */
	protected File getRoot() {
		return root;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#getId()
	 */
	@Override
	public String getId() {
		return getPath().replaceAll(SEPARATOR, ID_SEPARATOR);
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#getName()
	 */
	@Override
	public String getName() {
		return file.getName();
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @throws IOException
	 * @see org.pivot4j.service.repository.ReportFile#getParent()
	 */
	@Override
	public ReportFile getParent() throws IOException {
		if (isRoot()) {
			return null;
		}

		File parent = file.getParentFile();

		if (parent == null) {
			return null;
		}

		return new LocalFile(parent, root);
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @see org.pivot4j.service.repository.AbstractReportFile#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return root.equals(file);
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() {
		return new Date(file.lastModified());
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#getSize()
	 */
	@Override
	public long getSize() {
		return file.length();
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#canRead()
	 */
	@Override
	public boolean canRead() {
		return file.canRead();
	}

	/**
	 * @see org.pivot4j.service.repository.ReportFile#canWrite()
	 */
	@Override
	public boolean canWrite() {
		return file.canWrite();
	}
}
