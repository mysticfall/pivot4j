package org.pivot4j.analytics.repository;

public interface RepositoryFileFilter {

	/**
	 * @param file
	 * @return
	 */
	boolean accept(ReportFile file);
}
