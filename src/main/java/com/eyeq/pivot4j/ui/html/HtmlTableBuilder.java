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
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createCell(com.eyeq.pivot4j.
	 *      ui.BuildContext, com.eyeq.pivot4j.ui.TableModel, int,
	 *      com.eyeq.pivot4j.ui.TableRow, int, int, int)
	 */
	@Override
	protected HtmlTableCell createCell(BuildContext context,
			HtmlTableModel table, int rowIndex, HtmlTableRow row, int colIndex,
			int colSpan, int rowSpan) {
		String label = null;

		boolean header = false;

		if (context.getCell() != null) {
			label = context.getCell().getFormattedValue();
		} else if (context.getMember() != null) {
			label = context.getMember().getCaption();
			header = true;
		} else if (context.getHierarchy() != null) {
			label = context.getHierarchy().getDimension().getCaption();
			header = true;
		}

		HtmlTableCell cell = new HtmlTableCell();
		cell.setLabel(colSpan +", " + rowSpan+ " : " +label);
		cell.setColSpan(colSpan);
		cell.setRowSpan(rowSpan);
		cell.setHeader(header);

		return cell;
	}
}
