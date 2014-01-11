/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx.metadata;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.mdx.Exp;

public interface MetadataExp<T extends MetadataElement> extends Exp {

	String getName();

	String getUniqueName();

	T getMetadata(Cube cube);

	void setMetadata(T element);
}
