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

public class SapVariable implements Exp {

	private static final long serialVersionUID = 6766264897810191295L;

	private String name;

	private List<Value> values;

	/**
	 * @param name
	 * @param value
	 * @param including
	 */
	public SapVariable(String name, List<Value> values) {
		this.name = name;
		this.values = values;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
		if (values == null || values.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(' ');

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
		List<Value> clonedValues = null;

		if (this.values != null) {
			clonedValues = new ArrayList<Value>();

			for (Value value : values) {
				clonedValues.add((Value) value.clone());
			}
		}

		return new SapVariable(name, clonedValues);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
	}

	public static class Value implements Exp {

		private static final long serialVersionUID = -5532029488482311594L;

		private Exp highValue;

		private Exp lowValue;

		private boolean including = true;

		private boolean interval = false;

		private String option;

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
			if (isInterval()) {
				Exp clonedHighValue = highValue == null ? null
						: (Exp) highValue.clone();
				Exp clonedLowValue = lowValue == null ? null : (Exp) lowValue
						.clone();

				return new Value(clonedLowValue, clonedHighValue,
						isIncluding(), getOption());
			} else {
				Exp clonedLowValue = lowValue == null ? null : (Exp) lowValue
						.clone();

				return new Value(clonedLowValue, isIncluding(), getOption());
			}
		}

		/**
		 * @param visitor
		 */
		public void accept(ExpVisitor visitor) {
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
