/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import java.util.ArrayList;
import java.util.List;

public class SapVariable extends AbstractExp {

	private static final long serialVersionUID = 6766264897810191295L;

	private CompoundId name;

	private List<Value> values = new ArrayList<Value>();

	public SapVariable() {
	}

	/**
	 * @param name
	 * @param value
	 * @param including
	 */
	public SapVariable(CompoundId name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public CompoundId getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(CompoundId name) {
		this.name = name;
	}

	/**
	 * @return the values
	 */
	public List<Value> getValues() {
		return values;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#toMdx()
	 */
	public String toMdx() {
		if (values.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		if (name != null) {
			builder.append(name.toMdx());
			builder.append(' ');
		}

		boolean first = true;

		for (Value value : values) {
			if (first) {
				first = false;
			} else {
				builder.append(' ');
			}

			builder.append(value.toMdx());
		}

		return builder.toString();
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public SapVariable clone() {
		SapVariable clone = new SapVariable();

		if (name != null) {
			clone.name = name.clone();
		}

		for (Value value : values) {
			clone.values.add(value.clone());
		}

		return clone;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitSapVariable(this);

		if (name != null) {
			name.accept(visitor);
		}

		for (Value value : values) {
			value.accept(visitor);
		}
	}

	public static class Value extends AbstractExp {

		private static final long serialVersionUID = -5532029488482311594L;

		private Exp highValue;

		private Exp lowValue;

		private boolean including = true;

		private boolean interval = false;

		private String option;

		public Value() {
		}

		/**
		 * @param value
		 * @param including
		 * @param option
		 */
		public Value(Exp value, boolean including, String option) {
			this.lowValue = value;
			this.including = including;
			this.interval = false;
			this.option = option;
		}

		/**
		 * @param values
		 * @param including
		 * @param interval
		 * @param option
		 */
		public Value(Exp lowValue, Exp highValue, boolean including,
				String option) {
			this.lowValue = lowValue;
			this.highValue = highValue;
			this.including = including;
			this.interval = true;
			this.option = option;
		}

		/**
		 * @return the value
		 */
		public Exp getValue() {
			return getLowValue();
		}

		/**
		 * @return the highValue
		 */
		public Exp getHighValue() {
			return highValue;
		}

		/**
		 * @return the lowValue
		 */
		public Exp getLowValue() {
			return lowValue;
		}

		/**
		 * @return the including
		 */
		public boolean isIncluding() {
			return including;
		}

		/**
		 * @return the interval
		 */
		public boolean isInterval() {
			return interval;
		}

		/**
		 * @return the option
		 */
		public String getOption() {
			return option;
		}

		/**
		 * 
		 * @see java.lang.Object#clone()
		 */
		public Value clone() {
			Value clone = new Value();

			if (highValue != null) {
				clone.highValue = highValue.clone();
			}

			if (lowValue != null) {
				clone.lowValue = lowValue.clone();
			}

			clone.including = including;
			clone.interval = interval;
			clone.option = option;

			return clone;
		}

		/**
		 * @param visitor
		 */
		public void accept(ExpVisitor visitor) {
			visitor.visitSapVariableValue(this);

			if (highValue != null) {
				highValue.accept(visitor);
			}

			if (lowValue != null) {
				lowValue.accept(visitor);
			}
		}

		/**
		 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#toMdx()
		 */
		public String toMdx() {
			if (lowValue == null || isInterval() && highValue == null) {
				return "";
			}

			StringBuilder builder = new StringBuilder();

			if (isIncluding()) {
				builder.append("INCLUDING ");
			} else {
				builder.append("EXCLUDING ");
			}

			if (option != null) {
				builder.append(option);
				builder.append(' ');
			}

			builder.append(lowValue.toMdx());

			if (isInterval()) {
				builder.append(':');
				builder.append(highValue.toMdx());
			}

			return builder.toString();
		}
	}
}
