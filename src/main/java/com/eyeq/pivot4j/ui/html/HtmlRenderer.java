/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import com.eyeq.pivot4j.ui.AbstractMarkupRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public class HtmlRenderer extends AbstractMarkupRenderer {

	private String tableId;

	private String tableStyleClass = "pv-table";

	private Integer border;

	private Integer cellSpacing;

	private Integer cellPadding;

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
	 * @param writer
	 */
	public HtmlRenderer(Writer writer) {
		super(writer);
	}

	/**
	 * @return the tableId
	 */
	public String getTableId() {
		return tableId;
	}

	/**
	 * @param tableId
	 *            the tableId to set
	 */
	public void setTableId(String tableId) {
		this.tableId = tableId;
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
	 * @return the cellSpacing
	 */
	public Integer getCellSpacing() {
		return cellSpacing;
	}

	/**
	 * @param cellSpacing
	 *            the cellSpacing to set
	 */
	public void setCellSpacing(Integer cellSpacing) {
		this.cellSpacing = cellSpacing;
	}

	/**
	 * @return the cellPadding
	 */
	public Integer getCellPadding() {
		return cellPadding;
	}

	/**
	 * @param cellPadding
	 *            the cellPadding to set
	 */
	public void setCellPadding(Integer cellPadding) {
		this.cellPadding = cellPadding;
	}

	/**
	 * @return the border
	 */
	public Integer getBorder() {
		return border;
	}

	/**
	 * @param border
	 *            the border to set
	 */
	public void setBorder(Integer border) {
		this.border = border;
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
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startTable(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void startTable(RenderContext context) {
		Map<String, String> attributes = new TreeMap<String, String>();

		if (tableId != null) {
			attributes.put("id", tableId);
		}

		if (tableStyleClass != null) {
			attributes.put("class", tableStyleClass);
		}

		if (cellSpacing != null) {
			attributes.put("cellspacing", Integer.toString(cellSpacing));
		}

		if (cellPadding != null) {
			attributes.put("cellpadding", Integer.toString(cellPadding));
		}

		if (border != null) {
			attributes.put("border", Integer.toString(border));
		}

		startElement(context, "table", attributes);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startHeader(com.eyeq.pivot4j.
	 *      ui.RenderContext)
	 */
	@Override
	public void startHeader(RenderContext context) {
		startElement(context, "thead", null);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endHeader(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void endHeader(RenderContext context) {
		endElement(context, "thead");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startBody(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void startBody(RenderContext context) {
		startElement(context, "tbody", null);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endBody(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endBody(RenderContext context) {
		endElement(context, "tbody");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endTable(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endTable(RenderContext context) {
		endElement(context, "table");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startRow(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void startRow(RenderContext context) {
		Map<String, String> attributes = new TreeMap<String, String>();

		int index = context.getRowIndex() - context.getColumnHeaderCount();
		if (index < 0) {
			index = context.getRowIndex();
		}

		if (rowStyleClass != null || evenRowStyleClass != null
				|| oddRowStyleClass != null) {
			boolean first = true;

			StringBuilder builder = new StringBuilder();

			if (rowStyleClass != null) {
				builder.append(rowStyleClass);
				first = false;
			}

			boolean even = index % 2 == 0;
			if (even && evenRowStyleClass != null) {
				if (first) {
					first = false;
				} else {
					builder.append(' ');
				}

				builder.append(evenRowStyleClass);
			} else if (!even && oddRowStyleClass != null) {
				if (first) {
					first = false;
				} else {
					builder.append(' ');
				}

				builder.append(oddRowStyleClass);
			}

			attributes.put("class", builder.toString());
		}

		startElement(context, "tr", attributes);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endRow(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endRow(RenderContext context) {
		endElement(context, "tr");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startCell(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void startCell(RenderContext context) {
		boolean header = false;

		String styleClass = null;
		String style = null;

		switch (context.getCellType()) {
		case ColumnHeader:
			styleClass = columnHeaderStyleClass;
			header = true;
			break;
		case RowHeader:
			styleClass = rowHeaderStyleClass;

			if (rowHeaderLevelPadding > 0) {
				int padding = rowHeaderLevelPadding
						* (1 + context.getMember().getDepth());
				style = "padding-left: " + padding + "px;";
			}

			header = true;
			break;
		case ColumnTitle:
			styleClass = columnTitleStyleClass;
			header = true;
			break;
		case RowTitle:
			styleClass = rowTitleStyleClass;
			header = true;
			break;
		case Value:
			styleClass = cellStyleClass;
			break;
		case None:
			styleClass = cornerStyleClass;
			header = true;
			break;
		default:
			assert false;
		}

		Map<String, String> attributes = new TreeMap<String, String>();

		String name = header ? "th" : "td";

		if (styleClass != null) {
			attributes.put("class", styleClass);
		}

		if (style != null) {
			attributes.put("style", style);
		}

		if (context.getColSpan() > 1) {
			attributes.put("colspan", Integer.toString(context.getColSpan()));
		}

		if (context.getRowSpan() > 1) {
			attributes.put("rowspan", Integer.toString(context.getRowSpan()));
		}

		startElement(context, name, attributes);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void cellContent(RenderContext context) {
		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
			writeContent(context, context.getMember().getCaption());
			break;
		case ColumnTitle:
		case RowTitle:
			if (context.getLevel() != null) {
				writeContent(context, context.getLevel().getCaption());
			} else if (context.getHierarchy() != null) {
				writeContent(context, context.getHierarchy().getCaption());
			} else {
				writeContent(context, "&nbsp;");
			}
			break;
		case Value:
			writeContent(context, context.getCell().getFormattedValue());
			break;
		default:
			writeContent(context, "&nbsp;");
			break;
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endCell(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endCell(RenderContext context) {
		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
		case ColumnTitle:
		case RowTitle:
		case None:
			endElement(context, "th");
			break;
		default:
			endElement(context, "td");
			break;
		}
	}
}
