package com.eyeq.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;

public abstract class AbstractFileSystemRepository implements ReportRepository {

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFiles(com.eyeq.pivot4j.analytics.repository.ReportFile,
	 *      com.eyeq.pivot4j.analytics.repository.RepositoryFileFilter)
	 */
	@Override
	public List<ReportFile> getFiles(ReportFile parent,
			RepositoryFileFilter filter) throws IOException {
		List<ReportFile> files = getFiles(parent);

		List<ReportFile> result;

		if (filter == null) {
			result = files;
		} else {
			result = new LinkedList<ReportFile>();

			for (ReportFile file : files) {
				if (filter.accept(file)) {
					result.add(file);
				}
			}
		}

		return result;
	}

	/**
	 * @throws IOException
	 * @throws ConfigurationException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getReportContent(com.eyeq.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public ReportContent getReportContent(ReportFile file)
			throws IOException, ConfigurationException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		ReportContent content = null;

		InputStream in = null;

		try {
			in = readContent(file);

			content = new ReportContent(in);
		} catch (IOException e) {
			IOUtils.closeQuietly(in);
		}

		return content;
	}
}
