/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import java.io.PrintWriter;

import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.TableCell;

public class HtmlTableCell extends AbstractHtmlElement implements TableCell {

	private static final long serialVersionUID = -1896134719355213839L;

	private String label;

	private CellType type;

	private int colSpan = 1;

	private int rowSpan = 1;

	private String title;

	private String width;

	private String height;

	private boolean header = false;

	/**
	 * @param type
	 */
	public HtmlTableCell(CellType type) {
		this.type = type;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableCell#getType()
	 */
	@Override
	public CellType getType() {
		return type;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the header
	 */
	public boolean isHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(boolean header) {
		this.header = header;
	}

	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#writeHtml(java.io.PrintWriter,
	 *      int)
	 */
	@Override
	public void writeHtml(PrintWriter writer, int indent) {
		for (int i = 0; i < indent; i++) {
			writer.print('\t');
		}

		if (header) {
			writer.print("<th");
		} else {
			writer.print("<td");
		}

		writeAttributes(writer);
		writer.print('>');

		writeContent(writer, indent);

		if (header) {
			writer.print("</th>");
		} else {
			writer.print("</td>");
		}

		writer.println();
	}

	/**
	 * @param writer
	 */
	protected void writeAttributes(PrintWriter writer) {
		if (getStyleClass() != null) {
			writer.print(" class=\"");
			writer.print(getStyleClass());
			writer.print("\"");
		}

		if (getStyle() != null) {
			writer.print(" style=\"");
			writer.print(getStyle());
			writer.print("\"");
		}

		if (colSpan > 1) {
			writer.print(" colspan=\"");
			writer.print(Integer.toString(colSpan));
			writer.print("\"");
		}

		if (rowSpan > 1) {
			writer.print(" rowspan=\"");
			writer.print(Integer.toString(rowSpan));
			writer.print("\"");
		}

		if (width != null) {
			writer.print(" width=\"");
			writer.print(width);
			writer.print("\"");
		}

		if (height != null) {
			writer.print(" height=\"");
			writer.print(height);
			writer.print("\"");
		}

		if (title != null) {
			writer.print(" title=\"");
			writer.print(title);
			writer.print("\"");
		}
	}

	/**
	 * @param writer
	 * @param indent
	 */
	protected void writeContent(PrintWriter writer, int indent) {
		if (label == null) {
			writer.print("&nbsp;");
		} else {
			writer.print(label);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return label;
	}
}
