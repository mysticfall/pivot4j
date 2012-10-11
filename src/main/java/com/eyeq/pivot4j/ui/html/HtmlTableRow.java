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

import com.eyeq.pivot4j.ui.TableRow;

public class HtmlTableRow extends AbstractHtmlElement implements
		TableRow<HtmlTableCell> {

	private static final long serialVersionUID = 4178767392250124494L;

	private List<HtmlTableCell> cells = new ArrayList<HtmlTableCell>();

	/**
	 * @see com.eyeq.pivot4j.ui.TableRow#getCells()
	 */
	@Override
	public List<HtmlTableCell> getCells() {
		return cells;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableRow#getCellCount()
	 */
	@Override
	public int getCellCount() {
		return cells.size();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#writeHtml(java.io.PrintWriter,
	 *      int)
	 */
	@Override
	public void writeHtml(PrintWriter writer, int indent) {
		if (cells.isEmpty()) {
			return;
		}

		for (int i = 0; i < indent; i++) {
			writer.print('\t');
		}

		writer.print("<tr");
		writeAttributes(writer);
		writer.println('>');

		indent++;
		writeCells(writer, indent);
		indent--;

		for (int i = 0; i < indent; i++) {
			writer.print('\t');
		}

		writer.println("</tr>");
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
	}

	/**
	 * @param writer
	 * @param indent
	 */
	protected void writeCells(PrintWriter writer, int indent) {
		for (HtmlTableCell cell : cells) {
			cell.writeHtml(writer, indent);
		}
	}
}
