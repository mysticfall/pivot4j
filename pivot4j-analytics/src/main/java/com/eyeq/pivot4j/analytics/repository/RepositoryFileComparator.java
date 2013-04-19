package com.eyeq.pivot4j.analytics.repository;

import java.util.Comparator;

public class RepositoryFileComparator implements Comparator<RepositoryFile> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(RepositoryFile f1, RepositoryFile f2) {
		int result = 0;

		if (f1 == null) {
			if (f2 != null) {
				result = 1;
			}
		} else if (f2 != null) {
			if (f1.isDirectory()) {
				if (f2.isDirectory()) {
					result = f1.getPath().compareTo(f2.getPath());
				} else {
					result = -1;
				}
			} else if (f2.isDirectory()) {
				result = 1;
			} else {
				result = f1.getPath().compareTo(f2.getPath());
			}
		}

		return result;
	}
}
