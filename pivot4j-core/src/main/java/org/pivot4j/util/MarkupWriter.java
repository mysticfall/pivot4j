/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

public class MarkupWriter {

    private PrintWriter writer;

    private boolean formatOutput = true;

    private int indent = 0;

    private int indentSize = 1;

    private char indentCharacter = '\t';

    /**
     * @param writer
     */
    public MarkupWriter(Writer writer) {
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
     * @return the formatOutput
     */
    public boolean getFormatOutput() {
        return formatOutput;
    }

    /**
     * @param formatOutput the formatOutput to set
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
     * @param indent the indent to set
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
     * @param indentSize the indentSize to set
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
     * @param indentCharacter the indentCharacter to set
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
    public void startElement(String name, Map<String, String> attributes) {
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
    public void writeAttribute(String attributeName, String attributeValue) {
        writer.print(' ');
        writer.print(attributeName);
        writer.print("=\"");
        writer.print(StringUtils.trimToEmpty(attributeValue));
        writer.print("\"");
    }

    /**
     * @param content
     */
    public void writeContent(String content) {
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
    public void endElement(String name) {
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
