package com.eyeq.pivot4j.analytics.repository.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.FacesException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.analytics.config.Settings;
import com.eyeq.pivot4j.analytics.repository.ReportContent;
import com.eyeq.pivot4j.analytics.repository.ReportRepository;
import com.eyeq.pivot4j.analytics.repository.RepositoryFile;
import com.eyeq.pivot4j.analytics.repository.RepositoryFileComparator;

@ManagedBean(name = "reportRepository")
@ApplicationScoped
public class LocalFileSystemRepository implements ReportRepository {

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	private Logger log = LoggerFactory.getLogger(getClass());

	private LocalFile root;

	@PostConstruct
	protected void initialize() {
		String path = getRootPath();

		if (log.isInfoEnabled()) {
			log.info("Root repository path : " + path);
		}

		try {
			File file = new File(path);

			if (!file.exists() && !file.mkdirs()) {
				throw new IOException(
						"Creating repository direcoty failed with unknown reason : "
								+ path);
			}

			this.root = new LocalFile(file, file);
		} catch (IOException e) {
			throw new FacesException(e);
		}
	}

	protected String getRootPath() {
		File home = settings.getApplicationHome();
		return home.getPath() + File.separator + "repository";
	};

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getRoot()
	 */
	@Override
	public RepositoryFile getRoot() {
		return root;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) throws IOException {
		return getSystemFile(path).exists();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFile(java.lang.String)
	 */
	@Override
	public RepositoryFile getFile(String path) throws IOException {
		File file = getSystemFile(path);

		if (!file.exists()) {
			return null;
		}

		return new LocalFile(file, root.getRoot());
	}

	/**
	 * @throws IOException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFiles(com.eyeq.pivot4j.analytics.repository.RepositoryFile)
	 */
	@Override
	public List<RepositoryFile> getFiles(RepositoryFile parent)
			throws IOException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		LocalFile directory = getLocalFile(parent);

		File[] children = directory.getFile().listFiles();

		if (children == null) {
			return Collections.emptyList();
		}

		List<RepositoryFile> files = new ArrayList<RepositoryFile>(
				children.length);

		for (File child : children) {
			files.add(new LocalFile(child, root.getRoot()));
		}

		Collections.sort(files, new RepositoryFileComparator());

		return files;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#createDirectory(com.eyeq.pivot4j.analytics.repository.RepositoryFile,
	 *      java.lang.String)
	 */
	@Override
	public RepositoryFile createDirectory(RepositoryFile parent, String name)
			throws IOException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		LocalFile directory = getLocalFile(parent);

		String path = directory.getFile().getCanonicalPath() + File.separator
				+ name;

		File file = new File(path);
		if (!file.mkdir()) {
			throw new IOException(
					"Creating a direcoty failed with unknown reason : " + path);
		}

		return new LocalFile(file, root.getFile());
	}

	/**
	 * @throws IOException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#createFile(com.eyeq.pivot4j.analytics.repository.RepositoryFile,
	 *      java.lang.String,
	 *      com.eyeq.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public RepositoryFile createFile(RepositoryFile parent, String name,
			ReportContent content) throws IOException, ConfigurationException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (content == null) {
			throw new NullArgumentException("content");
		}

		LocalFile directory = getLocalFile(parent);

		String path = directory.getFile().getCanonicalPath() + File.separator
				+ name;

		File file = new File(path);

		RepositoryFile localFile = new LocalFile(file, root.getFile());

		setContent(localFile, content);

		return localFile;
	}

	/**
	 * @throws IOException
	 * @throws ConfigurationException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getContent(com.eyeq.pivot4j.analytics.repository.RepositoryFile)
	 */
	@Override
	public ReportContent getContent(RepositoryFile file) throws IOException,
			ConfigurationException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		ReportContent content = null;

		InputStream in = null;

		try {
			in = new FileInputStream(getLocalFile(file).getFile());

			content = new ReportContent(in);
		} catch (IOException e) {
			IOUtils.closeQuietly(in);
		}

		return content;
	}

	/**
	 * @throws ConfigurationException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#setContent(com.eyeq.pivot4j.analytics.repository.RepositoryFile,
	 *      com.eyeq.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public void setContent(RepositoryFile file, ReportContent content)
			throws IOException, ConfigurationException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		if (content == null) {
			throw new NullArgumentException("content");
		}

		LocalFile localFile = getLocalFile(file);

		OutputStream out = null;

		try {
			out = new BufferedOutputStream(new FileOutputStream(
					localFile.getFile(), false));

			content.write(out);

			out.flush();
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#renameFile(com.eyeq.pivot4j.analytics.repository.RepositoryFile,
	 *      java.lang.String)
	 */
	@Override
	public RepositoryFile renameFile(RepositoryFile file, String newName)
			throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		if (newName == null) {
			throw new NullArgumentException("newName");
		}

		File localFile = getLocalFile(file).getFile();

		File newFile = new File(localFile.getParent() + File.separator
				+ newName);

		if (!localFile.renameTo(newFile)) {
			throw new IOException(
					"Renaming a file failed with unknown reason : "
							+ newFile.getPath());
		}

		return new LocalFile(newFile, root.getFile());
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#deleteFile(com.eyeq.pivot4j.analytics.repository.RepositoryFile)
	 */
	@Override
	public void deleteFile(RepositoryFile file) throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		File localFile = getLocalFile(file).getFile();

		if (localFile.isDirectory()) {
			FileUtils.deleteDirectory(localFile);
		} else {
			if (!localFile.delete()) {
				throw new IOException(
						"Deleting a file failed with unknown reason : "
								+ localFile.getPath());
			}
		}
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	protected File getSystemFile(String path) throws IOException {
		if (path == null) {
			throw new NullArgumentException("file");
		}

		if (path.equals(RepositoryFile.SEPARATOR)) {
			return root.getFile();
		}

		String filePath = root.getFile().getCanonicalPath()
				+ path.replaceAll(RepositoryFile.SEPARATOR, File.separator);

		return new File(filePath);
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected LocalFile getLocalFile(RepositoryFile file) throws IOException {
		LocalFile localFile;

		if (file instanceof LocalFile) {
			localFile = (LocalFile) file;
		} else {
			localFile = (LocalFile) getFile(file.getPath());
		}

		return localFile;
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
}
