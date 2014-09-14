package org.pivot4j.service.repository;

public interface RepositoryFileFilter {

	/**
	 * @param file
	 * @return
	 */
	boolean accept(ReportFile file);
}
