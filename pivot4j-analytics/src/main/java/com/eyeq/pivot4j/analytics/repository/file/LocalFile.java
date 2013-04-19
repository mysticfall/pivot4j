package com.eyeq.pivot4j.analytics.repository.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.eyeq.pivot4j.analytics.repository.RepositoryFile;

public class LocalFile implements RepositoryFile {

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
			path = path.replaceAll(File.separator, SEPARATOR);
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
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#getId()
	 */
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFile#isRoot()
	 */
	@Override
	public boolean isRoot() {
		return root.equals(file);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(file).toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LocalFile)) {
			return false;
		}

		LocalFile other = (LocalFile) obj;

		return ObjectUtils.equals(file, other.getFile());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return file.toString();
	}
}
