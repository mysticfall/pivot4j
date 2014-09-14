package org.pivot4j.service.repository;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ReportFile {

	String SEPARATOR = "/";

	String getId();

	String getName();

	String getPath();

	String getExtension();

	@JsonIgnore
	ReportFile getParent() throws IOException;

	@JsonIgnore
	List<ReportFile> getAncestors() throws IOException;

	boolean isDirectory();

	boolean isRoot();

	Date getLastModifiedDate();

	long getSize();

	boolean canRead();

	boolean canWrite();
}
