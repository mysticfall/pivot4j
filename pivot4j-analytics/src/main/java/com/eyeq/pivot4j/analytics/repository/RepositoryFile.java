package com.eyeq.pivot4j.analytics.repository;

import java.io.IOException;
import java.util.List;

public interface RepositoryFile {

	String SEPARATOR = "/";

	String getId();

	String getName();

	String getPath();

	RepositoryFile getParent() throws IOException;

	List<RepositoryFile> getAncestors() throws IOException;

	boolean isDirectory();

	boolean isRoot();
}
