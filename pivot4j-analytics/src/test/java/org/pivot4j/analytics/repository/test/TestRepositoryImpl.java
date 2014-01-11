/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.repository.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.ConfigurationException;
import org.pivot4j.analytics.repository.AbstractFileSystemRepository;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.RepositoryFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRepositoryImpl extends AbstractFileSystemRepository implements
		TestRepository {

	private static final String RESOURCE_PREFIX = "/org/pivot4j/analytics/repository/test";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private TestFile root;

	private Map<String, TestFile> files;

	private Map<String, ReportContent> contents;

	/**
	 * @see org.pivot4j.analytics.repository.test.TestRepository#initialize()
	 */
	@Override
	@PostConstruct
	public void initialize() {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing test repository content.");
		}

		this.root = new TestFile();

		this.files = new HashMap<String, TestFile>();
		this.contents = new HashMap<String, ReportContent>();

		addFile(root);

		TestFile basic = new TestFile("Basic Tests", root, true);
		TestFile advanced = new TestFile("Advanced Tests", root, true);
		TestFile aggregation = new TestFile("AGG_VALUE", advanced, true);
		TestFile ragged = new TestFile("Ragged Dimension", advanced, true);
		TestFile properties = new TestFile("Properties", advanced, true);

		addFile(basic);
		addFile(advanced);
		addFile(aggregation);
		addFile(properties);
		addFile(ragged);

		addFile(new TestFile("Simple", basic, false));
		addFile(new TestFile("Simple Properties", properties, false));
		addFile(new TestFile("Ragged Test", ragged, false));
	}

	/**
	 * @param file
	 */
	protected synchronized void addFile(TestFile file) {
		if (logger.isInfoEnabled()) {
			logger.info("Adding file to repository : " + file);
		}

		files.put(file.getPath(), file);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#getRoot()
	 */
	@Override
	public ReportFile getRoot() {
		return root;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#getFile(java.lang.String)
	 */
	@Override
	public ReportFile getFile(String path) {
		return files.get(path);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#getFileById(java.lang.String)
	 */
	@Override
	public ReportFile getFileById(String id) throws IOException {
		return getFile(id);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) {
		return files.containsKey(path);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#fileWithIdExists(java.lang.String)
	 */
	@Override
	public boolean fileWithIdExists(String id) throws IOException {
		return exists(id);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#getFiles(org.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public synchronized List<ReportFile> getFiles(ReportFile parent)
			throws IOException {
		List<ReportFile> children = new LinkedList<ReportFile>();

		for (ReportFile child : files.values()) {
			if (parent.equals(child.getParent())) {
				children.add(child);
			}
		}

		Collections.sort(children, new RepositoryFileComparator());

		return children;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#createDirectory(org.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String)
	 */
	@Override
	public synchronized ReportFile createDirectory(ReportFile parent,
			String name) throws IOException {
		TestFile directory = new TestFile(name, (TestFile) parent, true);

		addFile(directory);

		return directory;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#createFile(org.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String,
	 *      org.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public synchronized ReportFile createFile(ReportFile parent, String name,
			ReportContent content) throws IOException, ConfigurationException {
		TestFile file = new TestFile(name, (TestFile) parent, false);

		String path = file.getPath();

		if (content == null) {
			contents.remove(path);
		} else {
			contents.put(path, content);
		}

		files.put(path, file);

		return file;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#renameFile(org.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String)
	 */
	@Override
	public synchronized ReportFile renameFile(ReportFile file, String newName)
			throws IOException {
		TestFile newFile = new TestFile(newName, (TestFile) file.getParent(),
				file.isDirectory());

		String oldPath = file.getPath();
		String newPath = newFile.getPath();

		if (file.isDirectory()) {
			List<String> names = new LinkedList<String>(files.keySet());

			String parentPath = file.getPath();

			for (String path : names) {
				if (path.startsWith(parentPath)) {
					TestFile child = files.remove(path);
					ReportContent content = contents.remove(path);

					if (child.equals(newFile)) {
						child.setName(newName);
					}

					for (ReportFile ancestor : child.getAncestors()) {
						if (ancestor.equals(newFile)) {
							((TestFile) ancestor).setName(newName);
							break;
						}
					}

					files.put(child.getPath(), child);

					if (content != null) {
						contents.put(child.getPath(), content);
					}
				}
			}
		} else {
			files.remove(oldPath);
			files.put(newPath, newFile);

			ReportContent content = contents.remove(oldPath);

			if (content != null) {
				contents.put(newPath, content);
			}
		}

		return newFile;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#getReportContent(org.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public synchronized ReportContent getReportContent(ReportFile file)
			throws IOException, ConfigurationException {
		String path = RESOURCE_PREFIX + file.getPath();

		if (logger.isInfoEnabled()) {
			logger.info("Opening file : " + path);
		}

		ReportContent content = contents.get(path);

		if (content == null) {
			content = super.getReportContent(file);
		}

		return content;
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#readContent(org.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public InputStream readContent(ReportFile file) throws IOException {
		return getClass().getResourceAsStream(RESOURCE_PREFIX + file.getPath());
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#setReportContent(org.pivot4j.analytics.repository.ReportFile,
	 *      org.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public synchronized void setReportContent(ReportFile file,
			ReportContent content) throws IOException, ConfigurationException {
		contents.put(file.getPath(), content);
	}

	/**
	 * @see org.pivot4j.analytics.repository.ReportRepository#deleteFile(org.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public synchronized void deleteFile(ReportFile file) throws IOException {
		List<String> names = new LinkedList<String>(files.keySet());

		String parentPath = file.getPath();

		for (String path : names) {
			if (path.startsWith(parentPath)) {
				files.remove(path);
				contents.remove(path);
			}
		}
	}
}
