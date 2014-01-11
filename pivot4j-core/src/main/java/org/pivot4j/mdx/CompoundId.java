/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Can be any MDX object
 */
public class CompoundId extends AbstractExp {

	private static final long serialVersionUID = 697157412160301933L;

	private List<NamePart> names = new ArrayList<NamePart>();

	public CompoundId() {
	}

	/**
	 * @param name
	 */
	public CompoundId(String name) {
		this(name, false);
	}

	/**
	 * @param name
	 * @param isKey
	 */
	public CompoundId(String name, boolean isKey) {
		names.add(new NamePart(name, isKey));
	}

	/**
	 * @return the names
	 */
	public List<NamePart> getNames() {
		return names;
	}

	/**
	 * @param name
	 */
	public CompoundId append(String name) {
		names.add(new NamePart(name, false));
		return this;
	}

	/**
	 * @param name
	 * @param isKey
	 */
	public CompoundId append(String name, boolean isKey) {
		names.add(new NamePart(name, isKey));
		return this;
	}

	public String[] toStringArray() {
		String[] ret = new String[names.size()];

		int i = 0;
		for (NamePart part : names) {
			ret[i++] = part.name;
		}

		return ret;
	}

	public static class NamePart implements Serializable {

		private static final long serialVersionUID = 8583427269370241977L;

		private String name;

		private boolean key;

		/**
		 * @param name
		 * @param key
		 */
		protected NamePart(String name, boolean key) {
			this.name = name;
			this.key = key;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		public String getQuotedName() {
			if (name == null || isQuoted()) {
				return name;
			} else {
				return name = "[" + name + "]";
			}
		}

		public String getUnquotedName() {
			if (name == null || !isQuoted()) {
				return name;
			} else {
				return name.substring(1, name.length() - 1);
			}
		}

		/**
		 * @return the key
		 */
		public boolean isKey() {
			return key;
		}

		/**
		 * @param key
		 *            the isKey to set
		 */
		public void setKey(boolean key) {
			this.key = key;
		}

		public boolean isQuoted() {
			return name != null && name.startsWith("[") && name.endsWith("]");
		}
	}

	/**
	 * format to MDX
	 * 
	 * @see Exp
	 */
	public String toMdx() {
		StringBuilder sb = new StringBuilder();

		boolean isFollow = false;

		for (NamePart part : names) {
			if (isFollow) {
				sb.append('.');
			} else {
				isFollow = true;
			}

			if (part.key) {
				sb.append('&');
			}

			sb.append(part.name);
		}

		return sb.toString();
	}

	/**
	 * @see org.pivot4j.mdx.Exp#copy()
	 */
	public CompoundId copy() {
		CompoundId cloned = new CompoundId();

		for (NamePart part : names) {
			cloned.append(part.name, part.key);
		}

		return cloned;
	}

	/**
	 * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitCompoundId(this);
	}
}
