/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.base;

import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.TableCell;

public class BaseTableCell implements TableCell {

	private static final long serialVersionUID = 650349142296925822L;

	private int colSpan = 1;

	private int rowSpan = 1;

	private String label;

	private CellType type;

	/**
	 * @param type
	 */
	public BaseTableCell(CellType type) {
		this.type = type;
	}

	/**
	 * @return the type
	 * @see com.eyeq.pivot4j.ui.TableCell#getType()
	 */
	public CellType getType() {
		return type;
	}

	/**
	 * @return the colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * @param colSpan
	 *            the colSpan to set
	 */
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	/**
	 * @return the rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * @param rowSpan
	 *            the rowSpan to set
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
