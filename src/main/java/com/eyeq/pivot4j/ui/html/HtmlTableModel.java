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
import java.util.ArrayList;
import java.util.List;

import com.eyeq.pivot4j.ui.TableModel;

public class HtmlTableModel extends AbstractHtmlElement implements
		TableModel<HtmlTableRow> {

	private static final long serialVersionUID = 2154755695117603346L;

	private List<HtmlTableRow> headers = new ArrayList<HtmlTableRow>();

	private List<HtmlTableRow> rows = new ArrayList<HtmlTableRow>();

	private Integer border;

	private Integer cellSpacing;

	private Integer cellPadding;

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getHeaders()
	 */
	@Override
	public List<HtmlTableRow> getHeaders() {
		return headers;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableModel#getRows()
	 */
	@Override
	public List<HtmlTableRow> getRows() {
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
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#writeHtml(java.io.PrintWriter,
	 *      int)
	 */
	@Override
	public void writeHtml(PrintWriter writer, int indent) {
		for (int i = 0; i < indent; i++) {
			writer.print('\t');
		}

		writer.print("<table");

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

		if (cellSpacing != null) {
			writer.print(" cellspacing=\"");
			writer.print(cellSpacing);
			writer.print("\"");
		}

		if (cellPadding != null) {
			writer.print(" cellpadding=\"");
			writer.print(cellPadding);
			writer.print("\"");
		}

		if (border != null) {
			writer.print(" border=\"");
			writer.print(border);
			writer.print("\"");
		}

		writer.println('>');

		indent++;

		if (!headers.isEmpty()) {
			for (int i = 0; i < indent; i++) {
				writer.print('\t');
			}

			writer.println("<thead>");

			indent++;

			for (HtmlTableRow row : getHeaders()) {
				row.writeHtml(writer, indent);
			}

			indent--;

			for (int i = 0; i < indent; i++) {
				writer.print('\t');
			}

			writer.println("</thead>");
		}

		if (!rows.isEmpty()) {
			for (int i = 0; i < indent; i++) {
				writer.print('\t');
			}

			writer.println("<tbody>");

			indent++;

			for (HtmlTableRow row : getRows()) {
				row.writeHtml(writer, indent);
			}

			indent--;

			for (int i = 0; i < indent; i++) {
				writer.print('\t');
			}

			writer.println("</tbody>");
		}

		writer.println("</table>");
	}
}