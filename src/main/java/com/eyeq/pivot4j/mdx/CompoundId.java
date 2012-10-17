/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * can be any MDX object
 */
public class CompoundId extends AbstractExp {

	private static final long serialVersionUID = 697157412160301933L;

	private List<NamePart> names = new ArrayList<NamePart>();

	private CompoundId() {
	}

	/**
	 * @param name
	 * @param isKey
	 */
	public CompoundId(String name, boolean isKey) {
		names.add(new NamePart(name, isKey));
	}

	public CompoundId(String name) {
		this(name, false);
	}

	public void append(String name, boolean isKey) {
		names.add(new NamePart(name, isKey));
	}

	public void append(String name) {
		names.add(new NamePart(name, false));
	}

	public String[] toStringArray() {
		String[] ret = new String[names.size()];
		int i = 0;
		for (NamePart np : names) {
			ret[i++] = np.name;
		}

		return ret;
	}

	private class NamePart implements Serializable {

		private static final long serialVersionUID = 8583427269370241977L;

		private String name;
		private boolean isKey;

		protected NamePart(String name, boolean isKey) {
			this.name = name;
			this.isKey = isKey;
		}
	}

	/**
	 * format to MDX
	 * 
	 * @see interface Exp
	 */
	public String toMdx() {
		String str = "";
		boolean isFollow = false;
		for (NamePart np : names) {
			if (isFollow)
				str += ".";
			isFollow = true;
			str += np.name;
		}

		return str;
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public CompoundId clone() {
		CompoundId cloned = new CompoundId();
		for (NamePart np : names) {
			cloned.append(np.name, np.isKey);
		}
		return cloned;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitCompoundId(this);
	}
}
