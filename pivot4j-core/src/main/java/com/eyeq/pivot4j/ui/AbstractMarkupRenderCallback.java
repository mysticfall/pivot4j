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
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

public abstract class AbstractMarkupRenderCallback<T extends RenderContext>
		extends AbstractRenderCallback<T> {

	private PrintWriter writer;

	private boolean formatOutput = true;

	private int indent = 0;

	private int indentSize = 1;

	private char indentCharacter = '\t';

	/**
	 * @param writer
	 */
	public AbstractMarkupRenderCallback(Writer writer) {
		if (writer == null) {
			throw new NullArgumentException("writer");
		}

		this.writer = new PrintWriter(writer);
	}

	/**
	 * @return the writer
	 */
	protected PrintWriter getWriter() {
		return writer;
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

	protected void writeIndent() {
		for (int i = 0; i < indent * indentSize; i++) {
			writer.print(indentCharacter);
		}
	}

	/**
	 * @param name
	 * @param attributes
	 */
	protected void startElement(String name, Map<String, String> attributes) {
		if (formatOutput) {
			writeIndent();
			indent++;
		}

		writer.print('<');
		writer.print(name);

		if (attributes != null) {
			if (attributes.containsKey("id")) {
				writeAttribute("id", attributes.get("id"));
			}

			for (String attributeName : attributes.keySet()) {
				if (!"id".equals(attributeName)) {
					writeAttribute(attributeName, attributes.get(attributeName));
				}
			}
		}

		writer.print('>');

		if (formatOutput) {
			writer.println();
		}
	}

	/**
	 * @param attributeName
	 * @param attributeValue
	 */
	protected void writeAttribute(String attributeName, String attributeValue) {
		writer.print(' ');
		writer.print(attributeName);
		writer.print("=\"");
		writer.print(StringUtils.trimToEmpty(attributeValue));
		writer.print("\"");
	}

	/**
	 * @param content
	 */
	protected void writeContent(String content) {
		if (formatOutput) {
			writeIndent();
		}

		writer.print(StringUtils.trimToEmpty(content));

		if (formatOutput) {
			writer.println();
		}
	}

	/**
	 * @param name
	 */
	protected void endElement(String name) {
		if (formatOutput) {
			indent--;
			writeIndent();
		}

		writer.print("</");
		writer.print(name);
		writer.print('>');

		if (formatOutput) {
			writer.println();
		}
	}
}
