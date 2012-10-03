/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.base;

import java.util.ArrayList;
import java.util.List;

import com.eyeq.pivot4j.ui.TableRow;

public class BaseTableRow implements TableRow<BaseTableCell> {

	private static final long serialVersionUID = -8479509640383802855L;

	private List<BaseTableCell> cells = new ArrayList<BaseTableCell>();

	/**
	 * @see com.eyeq.pivot4j.ui.TableRow#getCells()
	 */
	@Override
	public List<BaseTableCell> getCells() {
		return cells;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableRow#getCellCount()
	 */
	@Override
	public int getCellCount() {
		return cells.size();
	}
}
