package com.eyeq.pivot4j.analytics.repository.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.analytics.repository.AbstractRepositoryFile;
import com.eyeq.pivot4j.analytics.repository.RepositoryFile;

public class LocalFile extends AbstractRepositoryFile {

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
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getName()
	 */
	@Override
	public String getName() {
		return file.getName();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @throws IOException
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getParent()
	 */
	@Override
	public RepositoryFile getParent() throws IOException {
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
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.AbstractRepositoryFile#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return root.equals(file);
	}
}
