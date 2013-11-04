/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import static com.eyeq.pivot4j.ui.CellTypes.AGG_VALUE;
import static com.eyeq.pivot4j.ui.CellTypes.LABEL;
import static com.eyeq.pivot4j.ui.CellTypes.VALUE;
import static com.eyeq.pivot4j.ui.table.TableCellTypes.FILL;
import static com.eyeq.pivot4j.ui.table.TableCellTypes.TITLE;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;

import com.eyeq.pivot4j.el.ExpressionContext;
import com.eyeq.pivot4j.ui.AbstractMarkupRenderCallback;
import com.eyeq.pivot4j.ui.command.UICommand;
import com.eyeq.pivot4j.ui.table.TableRenderCallback;
import com.eyeq.pivot4j.ui.table.TableRenderContext;
import com.eyeq.pivot4j.util.CssWriter;
import com.eyeq.pivot4j.util.RenderPropertyUtils;

public class HtmlRenderCallback extends
		AbstractMarkupRenderCallback<TableRenderContext> implements
		TableRenderCallback {

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
	public HtmlRenderCallback(Writer writer) {
		super(writer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractRenderCallback#getContentType()
	 */
	@Override
	public String getContentType() {
		return "text/html";
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
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startTable(TableRenderContext context) {
		startElement("table", getTableAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getTableAttributes(TableRenderContext context) {
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
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startHeader(TableRenderContext context) {
		startElement("thead", getHeaderAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getHeaderAttributes(TableRenderContext context) {
		return Collections.emptyMap();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endHeader(TableRenderContext context) {
		endElement("thead");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startBody(TableRenderContext context) {
		startElement("tbody", getBodyAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getBodyAttributes(TableRenderContext context) {
		return Collections.emptyMap();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startRow(TableRenderContext context) {
		startElement("tr", getRowAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getRowAttributes(TableRenderContext context) {
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
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startCell(TableRenderContext context) {
		String tagName;

		if (VALUE.equals(context.getCellType())
				|| AGG_VALUE.equals(context.getCellType())) {
			tagName = "td";
		} else {
			tagName = "th";
		}

		startElement(tagName, getCellAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected Map<String, String> getCellAttributes(TableRenderContext context) {
		String styleClass = null;

		StringWriter writer = new StringWriter();
		CssWriter cssWriter = new CssWriter(writer);

		if (LABEL.equals(context.getCellType())) {
			if (context.getAxis() == Axis.COLUMNS) {
				styleClass = columnHeaderStyleClass;
			} else {
				styleClass = rowHeaderStyleClass;

				if (rowHeaderLevelPadding > 0) {
					int padding = rowHeaderLevelPadding
							* (1 + context.getMember().getDepth());
					cssWriter.writeStyle("padding-left", padding + "px");
				}
			}
		} else if (TITLE.equals(context.getCellType())
				|| AGG_VALUE.equals(context.getCellType())) {
			if (context.getAxis() == Axis.COLUMNS) {
				styleClass = columnTitleStyleClass;
			} else if (context.getAxis() == Axis.ROWS) {
				styleClass = rowTitleStyleClass;
			}
		} else if (VALUE.equals(context.getCellType())) {
			styleClass = cellStyleClass;
		} else if (FILL.equals(context.getCellType())) {
			styleClass = cornerStyleClass;
		}

		Map<String, String> attributes = new TreeMap<String, String>();

		String propertyCategory = context.getRenderPropertyCategory();

		RenderPropertyUtils propertyUtils = getRenderPropertyUtils();

		cssWriter.writeStyle("color",
				propertyUtils.getString("fgColor", propertyCategory, null));

		String bgColor = propertyUtils.getString("bgColor", propertyCategory,
				null);

		if (bgColor != null) {
			cssWriter.writeStyle("background-color", bgColor);
			cssWriter.writeStyle("background-image", "none");
		}

		cssWriter.writeStyle("font-family",
				propertyUtils.getString("fontFamily", propertyCategory, null));
		cssWriter.writeStyle("font-size",
				propertyUtils.getString("fontSize", propertyCategory, null));

		String fontStyle = propertyUtils.getString("fontStyle",
				propertyCategory, null);

		if (fontStyle != null) {
			if (fontStyle.contains("bold")) {
				cssWriter.writeStyle("font-weight", "bold");
			}

			if (fontStyle.contains("italic")) {
				cssWriter.writeStyle("font-style", "oblique");
			}
		}

		String styleClassValue = getRenderPropertyUtils().getString(
				"styleClass", context.getRenderPropertyCategory(), null);

		if (styleClassValue != null) {
			if (styleClass == null) {
				styleClass = styleClassValue;
			} else {
				styleClass += " " + styleClassValue;
			}
		}

		if (styleClass != null) {
			attributes.put("class", styleClass);
		}

		writer.flush();
		IOUtils.closeQuietly(writer);

		String style = writer.toString();

		if (StringUtils.isNotEmpty(style)) {
			attributes.put("style", style);
		}

		if (context.getColumnSpan() > 1) {
			attributes
					.put("colspan", Integer.toString(context.getColumnSpan()));
		}

		if (context.getRowSpan() > 1) {
			attributes.put("rowspan", Integer.toString(context.getRowSpan()));
		}

		return attributes;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#renderCommands(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.util.List)
	 */
	@Override
	public void renderCommands(TableRenderContext context,
			List<UICommand<?>> commands) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#renderContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String, java.lang.Double)
	 */
	@Override
	public void renderContent(TableRenderContext context, String label,
			Double value) {
		ExpressionContext elContext = context.getExpressionContext();

		elContext.put("label", label);
		elContext.put("value", value);

		String propertyCategory = context.getRenderPropertyCategory();

		RenderPropertyUtils propertyUtils = getRenderPropertyUtils();

		String text;

		try {
			text = StringUtils.defaultIfEmpty(
					propertyUtils.getString("label", propertyCategory, label),
					"&nbsp;");
		} finally {
			elContext.remove("label");
			elContext.remove("value");
		}

		String link = propertyUtils.getString("link", propertyCategory, null);

		if (link == null) {
			writeContent(text);
		} else {
			Map<String, String> attributes = new HashMap<String, String>(1);
			attributes.put("href", link);

			startElement("a", attributes);
			writeContent(text);
			endElement("a");
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endCell(TableRenderContext context) {
		String tagName;

		if (VALUE.equals(context.getCellType())
				|| AGG_VALUE.equals(context.getCellType())) {
			tagName = "td";
		} else {
			tagName = "th";
		}

		endElement(tagName);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endRow(TableRenderContext context) {
		endElement("tr");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endBody(TableRenderContext context) {
		endElement("tbody");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endTable(TableRenderContext context) {
		endElement("table");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractRenderCallback#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { super.saveState(), tableId, border,
				cellSpacing, cellPadding, tableStyleClass,
				columnHeaderStyleClass, columnTitleStyleClass,
				rowHeaderStyleClass, rowTitleStyleClass, cornerStyleClass,
				rowStyleClass, evenRowStyleClass, oddRowStyleClass,
				cellStyleClass, rowHeaderLevelPadding };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractRenderCallback#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		int index = 0;

		super.restoreState(states[index++]);

		this.tableId = (String) states[index++];
		this.border = (Integer) states[index++];
		this.cellSpacing = (Integer) states[index++];
		this.cellPadding = (Integer) states[index++];
		this.tableStyleClass = (String) states[index++];
		this.columnHeaderStyleClass = (String) states[index++];
		this.columnTitleStyleClass = (String) states[index++];
		this.rowHeaderStyleClass = (String) states[index++];
		this.rowTitleStyleClass = (String) states[index++];
		this.cornerStyleClass = (String) states[index++];
		this.rowStyleClass = (String) states[index++];
		this.evenRowStyleClass = (String) states[index++];
		this.oddRowStyleClass = (String) states[index++];
		this.cellStyleClass = (String) states[index++];
		this.rowHeaderLevelPadding = (Integer) states[index++];
	}
}
