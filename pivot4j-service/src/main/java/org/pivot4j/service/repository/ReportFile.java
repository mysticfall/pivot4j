/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
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
