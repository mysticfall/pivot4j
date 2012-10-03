/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.base;

import com.eyeq.pivot4j.ui.AbstractTableBuilder;
import com.eyeq.pivot4j.ui.BuildContext;

public class BaseTableBuilder extends
		AbstractTableBuilder<BaseTableModel, BaseTableRow, BaseTableCell> {

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createTable(com.eyeq.pivot4j
	 *      .ui.BuildContext)
	 */
	@Override
	protected BaseTableModel createTable(BuildContext context) {
		return new BaseTableModel();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createRow(com.eyeq.pivot4j.ui
	 *      .BuildContext, com.eyeq.pivot4j.ui.TableModel, int)
	 */
	@Override
	protected BaseTableRow createRow(BuildContext context,
			BaseTableModel table, int rowIndex) {
		return new BaseTableRow();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createCell(com.eyeq.pivot4j.
	 *      ui.BuildContext, com.eyeq.pivot4j.ui.TableModel, int,
	 *      com.eyeq.pivot4j.ui.TableRow, int, int, int)
	 */
	@Override
	protected BaseTableCell createCell(BuildContext context,
			BaseTableModel table, int rowIndex, BaseTableRow row, int colIndex,
			int colSpan, int rowSpan) {
		String label = null;

		if (context.getCell() != null) {
			label = context.getCell().getFormattedValue();
		} else if (context.getMember() != null) {
			label = context.getMember().getCaption();
		} else if (context.getHierarchy() != null) {
			label = context.getHierarchy().getDimension().getCaption();
		}

		BaseTableCell cell = new BaseTableCell();
		cell.setLabel(label);
		cell.setColSpan(colSpan);
		cell.setRowSpan(rowSpan);

		return cell;
	}
}
