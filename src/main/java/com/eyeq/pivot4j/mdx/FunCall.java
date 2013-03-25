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

public class FunCall extends AbstractExp {

	private static final long serialVersionUID = -1747077227822699594L;

	private Syntax type;

	private String function;

	private List<Exp> args = new ArrayList<Exp>();

	public FunCall() {
	}

	/**
	 * @param function
	 */
	public FunCall(String function) {
		this(function, Syntax.Function);
	}

	/**
	 * @param function
	 * @param type
	 */
	public FunCall(String function, Syntax type) {
		this(function, type, null);
	}

	/**
	 * @param function
	 * @param type
	 * @param args
	 */
	public FunCall(String function, Syntax type, List<Exp> args) {
		this.function = function;
		this.type = type;

		if (args != null) {
			this.args.addAll(args);
		}
	}

	/**
	 * @return
	 */
	public String getFunction() {
		return function;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(String function) {
		this.function = function;
	}

	/**
	 * @return the type
	 */
	public Syntax getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Syntax type) {
		this.type = type;
	}

	/**
	 * Returns the args.
	 * 
	 * @return Exp[]
	 */
	public List<Exp> getArgs() {
		return args;
	}

	/**
	 * Format to MDX
	 * 
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	public String toMdx() {
		return type.toMdx(function, args);
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public FunCall clone() {
		List<Exp> cloneArgs = new ArrayList<Exp>(args.size());

		for (Exp arg : args) {
			cloneArgs.add(arg.clone());
		}

		return new FunCall(function, type, cloneArgs);
	}

	/**
	 * compare function name ignoring case
	 * 
	 * @param fName
	 * @return boolean
	 */
	public boolean isCallTo(String fName) {
		return (fName.compareToIgnoreCase(function) == 0);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitFunCall(this);

		for (Exp arg : args) {
			arg.accept(visitor);
		}
	}
}
