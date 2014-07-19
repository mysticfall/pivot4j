package org.pivot4j.analytics.repository.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.analytics.repository.AbstractReportFile;
import org.pivot4j.analytics.repository.ReportFile;

public class LocalFile extends AbstractReportFile {

	private File root;

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
	 * @see org.pivot4j.analytics.repository.ReportFile#getName()
	 */
	@Override
	public String getName() {
		return file.getName();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @throws IOException
	 * @see org.pivot4j.analytics.repository.ReportFile#getParent()
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
	 * @see org.pivot4j.analytics.repository.ReportFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @see org.pivot4j.analytics.repository.AbstractReportFile#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return root.equals(file);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() {
		return new Date(file.lastModified());
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#getSize()
	 */
	@Override
	public long getSize() {
		return file.length();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#canRead()
	 */
	@Override
	public boolean canRead() {
		return file.canRead();
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportFile#canWrite()
	 */
	@Override
	public boolean canWrite() {
		return file.canWrite();
	}
}
