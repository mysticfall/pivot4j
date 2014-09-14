/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

public interface ReportRepository {

	ReportFile getRoot() throws IOException;

	ReportFile getFile(String path) throws IOException;

	ReportFile getFileById(String id) throws IOException;

	boolean exists(String path) throws IOException;

	boolean fileWithIdExists(String id) throws IOException;

	List<ReportFile> getFiles(ReportFile parent) throws IOException;

	List<ReportFile> getFiles(ReportFile parent, RepositoryFileFilter filter)
			throws IOException;

	ReportFile createDirectory(ReportFile parent, String name)
			throws IOException;

	ReportFile createFile(ReportFile parent, String name, ReportContent content)
			throws IOException, ConfigurationException;

	ReportFile renameFile(ReportFile file, String newName) throws IOException;

	void deleteFile(ReportFile file) throws IOException;

	ReportContent getReportContent(ReportFile file) throws IOException,
			ConfigurationException;

	void setReportContent(ReportFile file, ReportContent content)
			throws IOException, ConfigurationException;

	InputStream readContent(ReportFile file) throws IOException;
}
