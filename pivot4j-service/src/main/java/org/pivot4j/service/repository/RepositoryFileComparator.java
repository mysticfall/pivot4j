/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.repository;

import java.util.Comparator;

public class RepositoryFileComparator implements Comparator<ReportFile> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ReportFile f1, ReportFile f2) {
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
