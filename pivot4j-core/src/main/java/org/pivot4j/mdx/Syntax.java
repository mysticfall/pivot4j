/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import java.util.List;

public enum Syntax {

	Function(0) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			sb.append(function);
			sb.append("(");

			boolean isFollow = false;
			for (Exp arg : args) {
				if (isFollow) {
					sb.append(", ");
				} else {
					isFollow = true;
				}

				sb.append(arg.toMdx());
			}

			sb.append(")");

			return sb.toString();
		}
	},

	Property(1) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			if (!args.isEmpty()) {
				sb.append(args.get(0).toMdx());
			}

			sb.append(".");
			sb.append(function);

			return sb.toString();
		}
	},

	Method(2) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			if (!args.isEmpty()) {
				sb.append(args.get(0).toMdx());
			}

			sb.append(".");
			sb.append(function);
			sb.append("(");

			if (args.size() > 1) {
				sb.append(args.get(1).toMdx());
			}

			sb.append(")");

			return sb.toString();
		}
	},

	Infix(3) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			if (!args.isEmpty()) {
				sb.append(args.get(0).toMdx());
				sb.append(" ");
			}

			sb.append(function);

			if (args.size() > 1) {
				sb.append(" ");
				sb.append(args.get(1).toMdx());
			}

			return sb.toString();
		}
	},

	Prefix(4) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			sb.append(function);

			if (!args.isEmpty()) {
				sb.append(" ");
				sb.append(args.get(0).toMdx());
			}

			return sb.toString();
		}
	},

	Braces(5) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			sb.append("{");

			boolean isFollow = false;
			for (Exp arg : args) {
				if (isFollow) {
					sb.append(", ");
				} else {
					isFollow = true;
				}

				sb.append(arg.toMdx());
			}

			sb.append("}");

			return sb.toString();
		}
	},

	Parentheses(6) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			StringBuilder sb = new StringBuilder();

			sb.append("(");

			boolean isFollow = false;
			for (Exp arg : args) {
				if (isFollow) {
					sb.append(", ");
				} else {
					isFollow = true;
				}

				sb.append(arg.toMdx());
			}

			sb.append(")");

			return sb.toString();
		}
	},

	Case(7) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			throw new UnsupportedOperationException(
					"Unsupported operation for this syntatic type : " + name());
		}
	},

	Mask(0xFF) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			throw new UnsupportedOperationException(
					"Unsupported operation for this syntatic type : " + name());
		}
	},

	PropertyQuoted(Property.getCode() | 0x100) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			if (args.isEmpty()) {
				return "";
			}

			StringBuilder sb = new StringBuilder();

			sb.append(args.get(0).toMdx());
			sb.append(".");
			sb.append(function);

			return sb.toString();
		}
	},

	PropertyAmpQuoted(Property.getCode() | 0x200) {
		@Override
		public String toMdx(String function, List<Exp> args) {
			if (args.isEmpty()) {
				return "";
			}

			StringBuilder sb = new StringBuilder();

			sb.append(args.get(0).toMdx());
			sb.append(".");
			sb.append(function);

			return sb.toString();
		}
	};

	private int code;

	/**
	 * @param code
	 */
	Syntax(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	/**
	 * @param function
	 * @param args
	 * @return
	 */
	public abstract String toMdx(String function, List<Exp> args);
}
