/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import java.io.Serializable;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.eyeq.pivot4j.ui.AbstractPivotUIRenderer;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.util.MarkupWriter;

public class HtmlRenderer extends AbstractPivotUIRenderer {

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

	private List<CellCommand<?>> commands;

	private MarkupWriter writer;

	/**
	 * @param writer
	 */
	public HtmlRenderer(Writer writer) {
		this.writer = new MarkupWriter(writer);
	}

	/**
	 * @return writer
	 */
	protected MarkupWriter getWriter() {
		return writer;
	}

	/**
	 * @see com.eyeq.pivot4j.util.MarkupWriter#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		this.commands = null;
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
		writer.startElement("table", getTableAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getTableAttributes(RenderContext context) {
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

		return attributes;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startHeader(com.eyeq.pivot4j.
	 *      ui.RenderContext)
	 */
	@Override
	public void startHeader(RenderContext context) {
		writer.startElement("thead", getHeaderAttributes(context));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endHeader(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void endHeader(RenderContext context) {
		writer.endElement("thead");
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getHeaderAttributes(RenderContext context) {
		return Collections.emptyMap();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startBody(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public void startBody(RenderContext context) {
		writer.startElement("tbody", getBodyAttributes(context));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endBody(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endBody(RenderContext context) {
		writer.endElement("tbody");
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getBodyAttributes(RenderContext context) {
		return Collections.emptyMap();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endTable(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endTable(RenderContext context) {
		writer.endElement("table");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startRow(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void startRow(RenderContext context) {
		writer.startElement("tr", getRowAttributes(context));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endRow(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endRow(RenderContext context) {
		writer.endElement("tr");
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getRowAttributes(RenderContext context) {
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

		return attributes;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#startCell(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.util.Set)
	 */
	@Override
	public void startCell(RenderContext context, List<CellCommand<?>> commands) {
		boolean header;

		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
		case ColumnTitle:
		case RowTitle:
		case None:
			header = true;
			break;
		default:
			header = false;
			break;
		}

		String name = header ? "th" : "td";

		writer.startElement(name, getCellAttributes(context));

		this.commands = commands;

		if (commands != null && !commands.isEmpty()) {
			startCommand(context, commands);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#endCell(com.eyeq.pivot4j.ui.
	 *      RenderContext)
	 */
	@Override
	public void endCell(RenderContext context) {
		if (commands != null && !commands.isEmpty()) {
			endCommand(context, commands);
			this.commands = null;
		}

		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
		case ColumnTitle:
		case RowTitle:
		case None:
			writer.endElement("th");
			break;
		default:
			writer.endElement("td");
			break;
		}
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getCellAttributes(RenderContext context) {
		String styleClass = null;
		String style = null;

		switch (context.getCellType()) {
		case ColumnHeader:
			styleClass = columnHeaderStyleClass;
			break;
		case RowHeader:
			styleClass = rowHeaderStyleClass;

			if (rowHeaderLevelPadding > 0) {
				int padding = rowHeaderLevelPadding
						* (1 + context.getMember().getDepth());
				style = "padding-left: " + padding + "px;";
			}

			break;
		case ColumnTitle:
			styleClass = columnTitleStyleClass;
			break;
		case RowTitle:
			styleClass = rowTitleStyleClass;
			break;
		case Value:
			styleClass = cellStyleClass;
			break;
		case None:
			styleClass = cornerStyleClass;
			break;
		default:
			assert false;
		}

		Map<String, String> attributes = new TreeMap<String, String>();

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

		return attributes;
	}

	/**
	 * @param context
	 * @param commands
	 */
	public void startCommand(RenderContext context,
			List<CellCommand<?>> commands) {
	}

	/**
	 * @param context
	 * @param commands
	 */
	public void endCommand(RenderContext context, List<CellCommand<?>> commands) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void cellContent(RenderContext context, String label) {
		writer.writeContent(label);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getCellLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getCellLabel(RenderContext context) {
		return StringUtils
				.defaultIfEmpty(super.getCellLabel(context), "&nbsp;");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#bookmarkState()
	 */
	@Override
	public Serializable bookmarkState() {
		return new Serializable[] { super.bookmarkState(), tableId, border,
				cellSpacing, cellPadding, tableStyleClass,
				columnHeaderStyleClass, columnTitleStyleClass,
				rowHeaderStyleClass, rowTitleStyleClass, cornerStyleClass,
				rowStyleClass, evenRowStyleClass, oddRowStyleClass,
				cellStyleClass, rowHeaderLevelPadding };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.tableId = (String) states[1];
		this.border = (Integer) states[2];
		this.cellSpacing = (Integer) states[3];
		this.cellPadding = (Integer) states[4];
		this.tableStyleClass = (String) states[5];
		this.columnHeaderStyleClass = (String) states[6];
		this.columnTitleStyleClass = (String) states[7];
		this.rowHeaderStyleClass = (String) states[8];
		this.rowTitleStyleClass = (String) states[9];
		this.cornerStyleClass = (String) states[10];
		this.rowStyleClass = (String) states[11];
		this.evenRowStyleClass = (String) states[12];
		this.oddRowStyleClass = (String) states[13];
		this.cellStyleClass = (String) states[14];
		this.rowHeaderLevelPadding = (Integer) states[15];
	}
}
