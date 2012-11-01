/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public abstract class MarkupPivotRenderer extends AbstractPivotRenderer {

	private PrintWriter writer;

	private boolean formatOutput = true;

	private int indent = 0;

	private int indentSize = 1;

	private char indentCharacter = '\t';

	/**
	 * @param writer
	 */
	public MarkupPivotRenderer(Writer writer) {
		if (writer == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'writer'.");
		}

		this.writer = new PrintWriter(writer);
	}

	/**
	 * @return the writer
	 */
	public PrintWriter getWriter() {
		return writer;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();
		this.indent = 0;
	}

	/**
	 * @return the formatOutput
	 */
	public boolean getFormatOutput() {
		return formatOutput;
	}

	/**
	 * @param formatOutput
	 *            the formatOutput to set
	 */
	public void setFormatOutput(boolean formatOutput) {
		this.formatOutput = formatOutput;
	}

	/**
	 * @return the indent
	 */
	public int getIndent() {
		return indent;
	}

	/**
	 * @param indent
	 *            the indent to set
	 */
	public void setIndent(int indent) {
		this.indent = indent;
	}

	/**
	 * @return the indentSize
	 */
	public int getIndentSize() {
		return indentSize;
	}

	/**
	 * @param indentSize
	 *            the indentSize to set
	 */
	public void setIndentSize(int indentSize) {
		this.indentSize = indentSize;
	}

	/**
	 * @return the indentCharacter
	 */
	public char getIndentCharacter() {
		return indentCharacter;
	}

	/**
	 * @param indentCharacter
	 *            the indentCharacter to set
	 */
	public void setIndentCharacter(char indentCharacter) {
		this.indentCharacter = indentCharacter;
	}

	/**
	 * @param context
	 */
	protected void writeIndent(RenderContext context) {
		for (int i = 0; i < indent * indentSize; i++) {
			writer.print(indentCharacter);
		}
	}

	/**
	 * @param context
	 * @param name
	 * @param attributes
	 */
	protected void startElement(RenderContext context, String name,
			Map<String, String> attributes) {
		if (formatOutput) {
			writeIndent(context);
			indent++;
		}

		writer.print('<');
		writer.print(name);

		if (attributes != null) {
			if (attributes.containsKey("id")) {
				writeAttribute(context, "id", attributes.get("id"));
			}

			for (String attributeName : attributes.keySet()) {
				if (!"id".equals(attributeName)) {
					writeAttribute(context, attributeName,
							attributes.get(attributeName));
				}
			}
		}

		writer.print('>');

		if (formatOutput) {
			writer.println();
		}
	}

	/**
	 * @param context
	 * @param attributeName
	 * @param attributeValue
	 */
	protected void writeAttribute(RenderContext context, String attributeName,
			String attributeValue) {
		writer.print(' ');
		writer.print(attributeName);
		writer.print("=\"");
		writer.print(StringUtils.trimToEmpty(attributeValue));
		writer.print("\"");
	}

	/**
	 * @param context
	 * @param name
	 * @param attributes
	 */
	protected void endElement(RenderContext context, String name) {
		if (formatOutput) {
			indent--;
			writeIndent(context);
		}

		writer.print("</");
		writer.print(name);
		writer.print('>');

		if (formatOutput) {
			writer.println();
		}
	}

	/**
	 * @param context
	 * @param content
	 */
	protected void writeContent(RenderContext context, String content) {
		if (formatOutput) {
			writeIndent(context);
		}

		writer.print(content);

		if (formatOutput) {
			writer.println();
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void cellContent(RenderContext context, String label) {
		writeContent(context, label);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getCellLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getCellLabel(RenderContext context) {
		return StringUtils.trimToEmpty(super.getCellLabel(context));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#bookmarkState()
	 */
	@Override
	public Serializable bookmarkState() {
		return new Serializable[] { super.bookmarkState(), formatOutput,
				indent, indentCharacter, indentSize };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.formatOutput = (Boolean) states[1];
		this.indent = (Integer) states[2];
		this.indentCharacter = (Character) states[3];
		this.indentSize = (Integer) states[4];
	}
}
