package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public interface ReportFile {

	String SEPARATOR = "/";

	Serializable getId();

	String getName();

	String getPath();

	String getExtension();

	ReportFile getParent() throws IOException;

	List<ReportFile> getAncestors() throws IOException;

	boolean isDirectory();

	boolean isRoot();
	
	Date getLastModifiedDate();
	
	long getSize();

	boolean canRead();

	boolean canWrite();
}
