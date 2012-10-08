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

	private String tableStyle;

	private String tableStyleClass = "pv-table";

	private String rowStyleClass = "pv-row";

	private String evenRowStyleClass = "pv-row-even";

	private String oddRowStyleClass = "pv-row-odd";

	private String columnHeaderStyleClass = "pv-col-hdr";

	private String rowHeaderStyleClass = "pv-row-hdr";

	private String columnTitleStyleClass = columnHeaderStyleClass;

	private String rowTitleStyleClass = rowHeaderStyleClass;

	private String cellStyleClass = "pv-cell";

	private String cornerStyleClass = "pv-corner";

	private int rowHeaderLevelPadding = 10;

	/**
	 * @return the tableStyle
	 */
	public String getTableStyle() {
		return tableStyle;
	}

	/**
	 * @param tableStyle
	 *            the tableStyle to set
	 */
	public void setTableStyle(String tableStyle) {
		this.tableStyle = tableStyle;
	}

	/**
	 * @return the tableStyleClass
	 */
	public String getTableStyleClass() {
		return tableStyleClass;
	}

	/**
	 * @param tableStyleClass
	 *            the tableStyleClass to set
	 */
	public void setTableStyleClass(String tableStyleClass) {
		this.tableStyleClass = tableStyleClass;
	}

	/**
	 * @return the rowStyleClass
	 */
	public String getRowStyleClass() {
		return rowStyleClass;
	}

	/**
	 * @param rowStyleClass
	 *            the rowStyleClass to set
	 */
	public void setRowStyleClass(String rowStyleClass) {
		this.rowStyleClass = rowStyleClass;
	}

	/**
	 * @return the evenRowStyleClass
	 */
	public String getEvenRowStyleClass() {
		return evenRowStyleClass;
	}

	/**
	 * @param evenRowStyleClass
	 *            the evenRowStyleClass to set
	 */
	public void setEvenRowStyleClass(String evenRowStyleClass) {
		this.evenRowStyleClass = evenRowStyleClass;
	}

	/**
	 * @return the oddRowStyleClass
	 */
	public String getOddRowStyleClass() {
		return oddRowStyleClass;
	}

	/**
	 * @param oddRowStyleClass
	 *            the oddRowStyleClass to set
	 */
	public void setOddRowStyleClass(String oddRowStyleClass) {
		this.oddRowStyleClass = oddRowStyleClass;
	}

	/**
	 * @return the columnHeaderStyleClass
	 */
	public String getColumnHeaderStyleClass() {
		return columnHeaderStyleClass;
	}

	/**
	 * @param columnHeaderStyleClass
	 *            the columnHeaderStyleClass to set
	 */
	public void setColumnHeaderStyleClass(String columnHeaderStyleClass) {
		this.columnHeaderStyleClass = columnHeaderStyleClass;
	}

	/**
	 * @return the rowHeaderStyleClass
	 */
	public String getRowHeaderStyleClass() {
		return rowHeaderStyleClass;
	}

	/**
	 * @param rowHeaderStyleClass
	 *            the rowHeaderStyleClass to set
	 */
	public void setRowHeaderStyleClass(String rowHeaderStyleClass) {
		this.rowHeaderStyleClass = rowHeaderStyleClass;
	}

	/**
	 * @return the columnTitleStyleClass
	 */
	public String getColumnTitleStyleClass() {
		return columnTitleStyleClass;
	}

	/**
	 * @param columnTitleStyleClass
	 *            the columnTitleStyleClass to set
	 */
	public void setColumnTitleStyleClass(String columnTitleStyleClass) {
		this.columnTitleStyleClass = columnTitleStyleClass;
	}

	/**
	 * @return the rowTitleStyleClass
	 */
	public String getRowTitleStyleClass() {
		return rowTitleStyleClass;
	}

	/**
	 * @param rowTitleStyleClass
	 *            the rowTitleStyleClass to set
	 */
	public void setRowTitleStyleClass(String rowTitleStyleClass) {
		this.rowTitleStyleClass = rowTitleStyleClass;
	}

	/**
	 * @return the cellStyleClass
	 */
	public String getCellStyleClass() {
		return cellStyleClass;
	}

	/**
	 * @param cellStyleClass
	 *            the cellStyleClass to set
	 */
	public void setCellStyleClass(String cellStyleClass) {
		this.cellStyleClass = cellStyleClass;
	}

	/**
	 * @return the cornerStyleClass
	 */
	public String getCornerStyleClass() {
		return cornerStyleClass;
	}

	/**
	 * @param cornerStyleClass
	 *            the cornerStyleClass to set
	 */
	public void setCornerStyleClass(String cornerStyleClass) {
		this.cornerStyleClass = cornerStyleClass;
	}

	/**
	 * @return the rowHeaderLevelPadding
	 */
	public int getRowHeaderLevelPadding() {
		return rowHeaderLevelPadding;
	}

	/**
	 * @param rowHeaderLevelPadding
	 *            the rowHeaderLevelPadding to set
	 */
	public void setRowHeaderLevelPadding(int rowHeaderLevelPadding) {
		this.rowHeaderLevelPadding = rowHeaderLevelPadding;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createTable(com.eyeq.pivot4j
	 *      .ui.BuildContext)
	 */
	@Override
	protected HtmlTableModel createTable(BuildContext context) {
		HtmlTableModel table = new HtmlTableModel();
		table.setStyle(tableStyle);
		table.setStyleClass(tableStyleClass);

		return table;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#createRow(com.eyeq.pivot4j.ui
	 *      .BuildContext, com.eyeq.pivot4j.ui.TableModel, int)
	 */
	@Override
	protected HtmlTableRow createRow(BuildContext context,
			HtmlTableModel table, int rowIndex) {
		HtmlTableRow row = new HtmlTableRow();

		StringBuilder builder = new StringBuilder();
		if (rowStyleClass != null) {
			builder.append(rowStyleClass);
		}

		int mod = rowIndex % 2;
		if (evenRowStyleClass != null && mod == 0) {
			if (rowStyleClass != null) {
				builder.append(' ');
			}
			builder.append(evenRowStyleClass);
		} else if (oddRowStyleClass != null && mod == 1) {
			if (rowStyleClass != null) {
				builder.append(' ');
			}
			builder.append(oddRowStyleClass);
		}

		row.setStyleClass(builder.toString());

		return row;
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

		return cell;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractTableBuilder#configureCell(com.eyeq.pivot4j.ui.BuildContext,
	 *      com.eyeq.pivot4j.ui.TableModel, com.eyeq.pivot4j.ui.TableRow, int,
	 *      int, com.eyeq.pivot4j.ui.TableCell)
	 */
	@Override
	protected void configureCell(BuildContext context, HtmlTableModel table,
			HtmlTableRow row, int colIndex, int rowIndex, HtmlTableCell cell) {
		super.configureCell(context, table, row, colIndex, rowIndex, cell);

		CellType type = cell.getType();

		cell.setHeader(type != CellType.Value);

		switch (type) {
		case ColumnHeader:
			cell.setStyleClass(columnHeaderStyleClass);
			break;
		case RowHeader:
			cell.setStyleClass(rowHeaderStyleClass);

			if (rowHeaderLevelPadding > 0) {
				int padding = rowHeaderLevelPadding
						* (1 + context.getMember().getDepth());
				cell.setStyle("padding-left: " + padding + "px;");
			}

			break;
		case ColumnTitle:
			cell.setStyleClass(columnTitleStyleClass);
			break;
		case RowTitle:
			cell.setStyleClass(rowTitleStyleClass);
			break;
		case Value:
			cell.setStyleClass(cellStyleClass);
			break;
		case None:
			cell.setStyleClass(cornerStyleClass);
			break;
		default:
			assert false;
		}
	}
}