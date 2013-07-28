/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

public class CssWriter {

	private PrintWriter writer;

	/**
	 * @param writer
	 */
	public CssWriter(Writer writer) {
		if (writer == null) {
			throw new NullArgumentException("writer");
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
	 * @param name
	 * @param value
	 */
	public void writeStyle(String name, String value) {
		if (StringUtils.isNotBlank(value)) {
			writer.print(name);
			writer.print(": ");
			writer.print(value);
			writer.print(";");
		}
	}
}
