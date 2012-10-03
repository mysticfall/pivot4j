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

import com.eyeq.pivot4j.ui.TableModel;

public class BaseTableModel implements TableModel<BaseTableRow> {

	private static final long serialVersionUID = 6161456258495136284L;

	private List<BaseTableRow> headers = new ArrayList<BaseTableRow>();

	private List<BaseTableRow> rows = new ArrayList<BaseTableRow>();

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getHeaders()
	 */
	@Override
	public List<BaseTableRow> getHeaders() {
		return headers;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getRows()
	 */
	@Override
	public List<BaseTableRow> getRows() {
		return rows;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getHeaderCount()
	 */
	@Override
	public int getHeaderCount() {
		return headers.size();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rows.size();
	}
}
