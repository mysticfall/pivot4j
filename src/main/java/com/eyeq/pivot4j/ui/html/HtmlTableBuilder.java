/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import com.eyeq.pivot4j.ui.AbstractTableBuilder;
import com.eyeq.pivot4j.ui.BuildContext;
import com.eyeq.pivot4j.ui.CellType;

public class HtmlTableBuilder extends
		AbstractTableBuilder<HtmlTableModel, HtmlTableRow, HtmlTableCell> {

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createTable(com.eyeq.pivot4j
	 *      .ui.BuildContext)
	 */
	@Override
	protected HtmlTableModel createTable(BuildContext context) {
		return new HtmlTableModel();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createRow(com.eyeq.pivot4j.ui
	 *      .BuildContext, com.eyeq.pivot4j.ui.TableModel, int)
	 */
	@Override
	protected HtmlTableRow createRow(BuildContext context,
			HtmlTableModel table, int rowIndex) {
		return new HtmlTableRow();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createCell(com.eyeq.pivot4j.ui.BuildContext,
	 *      com.eyeq.pivot4j.ui.TableModel, com.eyeq.pivot4j.ui.TableRow,
	 *      com.eyeq.pivot4j.ui.CellType, int, int, int, int)
	 */
	@Override
	protected HtmlTableCell createCell(BuildContext context,
			HtmlTableModel table, HtmlTableRow row, CellType type,
			int colIndex, int rowIndex, int colSpan, int rowSpan) {
		HtmlTableCell cell = new HtmlTableCell(type);
		cell.setColSpan(colSpan);
		cell.setRowSpan(rowSpan);
		cell.setHeader(type != CellType.Value);

		return cell;
	}
}
