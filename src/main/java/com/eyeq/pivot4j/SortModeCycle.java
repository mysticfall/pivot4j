/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

import java.io.Serializable;

public abstract class SortModeCycle implements Serializable {

	private static final long serialVersionUID = 3571398856078189701L;

	public static final SortModeCycle BASIC = new SortModeCycle() {

		private static final long serialVersionUID = 3622337346091896703L;

		@Override
		public SortMode nextMode(SortMode mode) {
			if (mode == null) {
				return SortMode.ASC;
			} else if (mode == SortMode.ASC) {
				return SortMode.DESC;
			} else {
				return null;
			}
		}
	};

	public static final SortModeCycle BREAKING = new SortModeCycle() {

		private static final long serialVersionUID = 7472926285648718374L;

		@Override
		public SortMode nextMode(SortMode mode) {
			if (mode == null) {
				return SortMode.BASC;
			} else if (mode == SortMode.BASC) {
				return SortMode.BDESC;
			} else {
				return null;
			}
		}
	};

	public static final SortModeCycle COUNT = new SortModeCycle() {

		private static final long serialVersionUID = -3751113668954135001L;

		@Override
		public SortMode nextMode(SortMode mode) {
			if (mode == null) {
				return SortMode.TOPCOUNT;
			} else if (mode == SortMode.TOPCOUNT) {
				return SortMode.BOTTOMCOUNT;
			} else {
				return null;
			}
		}
	};

	public abstract SortMode nextMode(SortMode mode);

	/**
	 * @param model
	 */
	public void toggleSort(PivotModel model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		SortMode currentMode = null;
		if (model.isSorting()) {
			currentMode = model.getSortMode();
		}

		SortMode mode = nextMode(currentMode);
		if (mode == null) {
			model.setSorting(false);
		} else {
			model.setSorting(true);
			model.setSortMode(mode);
		}
	}
}
