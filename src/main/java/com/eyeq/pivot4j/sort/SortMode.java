/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.sort;

import java.io.Serializable;
import java.lang.reflect.Field;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;

public abstract class SortMode implements Serializable {

	private static final long serialVersionUID = 3571398856078189701L;

	public static final SortMode BASIC = new SortMode() {

		private static final long serialVersionUID = 3622337346091896703L;

		@Override
		public String getName() {
			return "basic";
		}

		@Override
		public SortCriteria nextMode(SortCriteria mode) {
			if (mode == null) {
				return SortCriteria.ASC;
			} else if (mode == SortCriteria.ASC) {
				return SortCriteria.DESC;
			} else {
				return null;
			}
		}
	};

	public static final SortMode BREAKING = new SortMode() {

		private static final long serialVersionUID = 7472926285648718374L;

		@Override
		public String getName() {
			return "breaking";
		}

		@Override
		public SortCriteria nextMode(SortCriteria mode) {
			if (mode == null) {
				return SortCriteria.BASC;
			} else if (mode == SortCriteria.BASC) {
				return SortCriteria.BDESC;
			} else {
				return null;
			}
		}
	};

	public static final SortMode COUNT = new SortMode() {

		private static final long serialVersionUID = -3751113668954135001L;

		@Override
		public String getName() {
			return "count";
		}

		@Override
		public SortCriteria nextMode(SortCriteria mode) {
			if (mode == null) {
				return SortCriteria.TOPCOUNT;
			} else if (mode == SortCriteria.TOPCOUNT) {
				return SortCriteria.BOTTOMCOUNT;
			} else {
				return null;
			}
		}
	};

	/**
	 * @param name
	 * @return
	 */
	public static SortMode fromName(String name) {
		Field[] fields = SortMode.class.getFields();

		for (Field field : fields) {
			Object value;

			try {
				value = field.get(null);
			} catch (Exception e) {
				throw new PivotException(e);
			}

			if (value instanceof SortMode) {
				SortMode mode = (SortMode) value;

				if (name.equals(mode.getName())) {
					return mode;
				}
			}
		}

		return null;
	}

	public abstract String getName();

	public abstract SortCriteria nextMode(SortCriteria mode);

	/**
	 * @param model
	 */
	public void toggleSort(PivotModel model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		SortCriteria currentCriteria = null;
		if (model.isSorting()) {
			currentCriteria = model.getSortCriteria();
		}

		SortCriteria criteria = nextMode(currentCriteria);
		if (criteria == null) {
			model.setSorting(false);
		} else {
			model.setSorting(true);
			model.setSortCriteria(criteria);
		}
	}
}
