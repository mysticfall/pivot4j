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

import org.olap4j.Axis;

/**
 * MDX query axis
 */
public class QueryAxis extends AbstractExp {

	private static final long serialVersionUID = 9064412375948950770L;

	private Axis axis;

	private Exp exp;

	private boolean nonEmpty;

	private List<CompoundId> dimensionProperties = new ArrayList<CompoundId>();

	public QueryAxis() {
	}

	/**
	 * @param axis
	 * @param exp
	 */
	public QueryAxis(Axis axis, Exp exp) {
		this(axis, exp, false);
	}

	/**
	 * @param axis
	 * @param exp
	 * @param nonEmpty
	 */
	public QueryAxis(Axis axis, Exp exp, boolean nonEmpty) {
		this.axis = axis;
		this.exp = exp;
		this.nonEmpty = nonEmpty;
	}

	/**
	 * Returns the exp.
	 * 
	 * @return Exp
	 */
	public Exp getExp() {
		return exp;
	}

	/**
	 * Returns the axis.
	 * 
	 * @return Axis
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * Returns the nonEmpty.
	 * 
	 * @return boolean
	 */
	public boolean isNonEmpty() {
		return nonEmpty;
	}

	/**
	 * Sets the exp.
	 * 
	 * @param exp
	 *            The exp to set
	 */
	public void setExp(Exp exp) {
		this.exp = exp;
	}

	/**
	 * Sets the axis.
	 * 
	 * @param axis
	 *            The axis to set
	 */
	public void setAxis(Axis axis) {
		this.axis = axis;
	}

	/**
	 * Sets the nonEmpty.
	 * 
	 * @param nonEmpty
	 *            The nonEmpty to set
	 */
	public void setNonEmpty(boolean nonEmpty) {
		this.nonEmpty = nonEmpty;
	}

	/**
	 * @return
	 */
	public List<CompoundId> getDimensionProperties() {
		return dimensionProperties;
	}

	/**
	 * format to MDX
	 */
	public String toMdx() {
		StringBuilder sb = new StringBuilder();

		if (exp == null) {
			sb.append("{}");
		} else {
			if (nonEmpty) {
				sb.append("NON EMPTY ");
			}

			sb.append(exp.toMdx());

			if (!dimensionProperties.isEmpty()) {
				sb.append(" DIMENSION PROPERTIES ");

				for (int i = 0; i < dimensionProperties.size(); i++) {
					if (i > 0) {
						sb.append(',');
					}

					sb.append(dimensionProperties.get(i).toMdx());
				}
			}
		}

		sb.append(" ON ");
		sb.append(axis.name());

		return sb.toString();
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public QueryAxis clone() {
		QueryAxis qa = new QueryAxis(axis, exp.clone(), nonEmpty);

		for (CompoundId property : dimensionProperties) {
			qa.dimensionProperties.add(property.clone());
		}

		return qa;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitQueryAxis(this);
	}
}