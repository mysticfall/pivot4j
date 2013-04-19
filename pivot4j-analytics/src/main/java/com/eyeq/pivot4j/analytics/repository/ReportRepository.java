package com.eyeq.pivot4j.analytics.repository;

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

public interface ReportRepository {

	RepositoryFile getRoot();

	RepositoryFile getFile(String path) throws IOException;

	List<RepositoryFile> getFiles(RepositoryFile parent) throws IOException;

	RepositoryFile createDirectory(RepositoryFile parent, String name)
			throws IOException;

	RepositoryFile createFile(RepositoryFile parent, String name,
			ReportContent content) throws IOException, ConfigurationException;

	RepositoryFile renameFile(RepositoryFile file, String newName)
			throws IOException;

	ReportContent getContent(RepositoryFile file) throws IOException, ConfigurationException;

	void setContent(RepositoryFile file, ReportContent content)
			throws IOException, ConfigurationException;

	void deleteFile(RepositoryFile file) throws IOException;
}
