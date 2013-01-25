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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the result of parsing an MDX query.
 */
public class ParsedQuery extends AbstractExp {

	private static final long serialVersionUID = 8608792548174831908L;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<Formula> formulas = new ArrayList<Formula>();

	private QueryAxis[] axes = new QueryAxis[0];

	private Exp slicer;

	private String cube;

	private List<CompoundId> cellProperties = new ArrayList<CompoundId>();

	private List<SapVariable> sapVariables = new ArrayList<SapVariable>();

	private boolean axesSwapped = false;

	/**
	 * 
	 * @return QueryAxis[]
	 */
	public QueryAxis[] getAxes() {
		return this.axes;
	}

	/**
	 * 
	 * @param axes
	 */
	public void setAxes(QueryAxis[] axes) {
		this.axes = axes;
	}

	/**
	 * @return the axesSwapped
	 */
	public boolean isAxesSwapped() {
		return axesSwapped;
	}

	/**
	 * @param axesSwapped
	 *            the axesSwapped to set
	 */
	public void setAxesSwapped(boolean axesSwapped) {
		this.axesSwapped = axesSwapped;
	}

	/**
	 * Returns the cube.
	 * 
	 * @return String
	 */
	public String getCube() {
		return cube;
	}

	/**
	 * Sets the cube.
	 * 
	 * @param cube
	 *            The cube to set
	 */
	public void setCube(String cube) {
		this.cube = cube;
	}

	/**
	 * get the formulas of this query
	 * 
	 * @return formulars
	 */
	public List<Formula> getFormulas() {
		return formulas;
	}

	/**
	 * @return
	 */
	public List<CompoundId> getCellProperties() {
		return cellProperties;
	}

	/**
	 * @return the sapVariables
	 */
	public List<SapVariable> getSapVariables() {
		return sapVariables;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	public String toMdx() {
		StringBuilder mdx = new StringBuilder();

		boolean isFollow;

		if (!formulas.isEmpty()) {
			mdx.append("WITH ");

			for (Formula form : formulas) {
				mdx.append(' ');
				mdx.append(form.toMdx());
			}

			mdx.append(' ');
		}

		mdx.append("SELECT ");

		isFollow = false;

		for (QueryAxis qa : axes) {
			if (isFollow) {
				mdx.append(", ");
			}

			isFollow = true;
			mdx.append(qa.toMdx());
		}

		mdx.append(" FROM ");
		mdx.append(cube);

		if (slicer != null) {
			mdx.append(" WHERE ");
			mdx.append(slicer.toMdx());
		}

		// add CELL PROPERTIES VALUE, FORMATTED_VALUE, ...
		if (!cellProperties.isEmpty()) {
			mdx.append(" CELL PROPERTIES VALUE, FORMATTED_VALUE");

			for (CompoundId cid : cellProperties) {
				String str = cid.toMdx();

				if (str.equalsIgnoreCase("VALUE")) {
					continue; // default
				} else if (str.equalsIgnoreCase("FORMATTED_VALUE")) {
					continue; // default
				}

				mdx.append(" ,");
				mdx.append(str);
			}
		}

		if (!sapVariables.isEmpty()) {
			mdx.append(" SAP VARIABLES ");

			isFollow = false;

			for (SapVariable sapVariable : sapVariables) {
				if (isFollow) {
					mdx.append(", ");
				} else {
					isFollow = true;
				}

				mdx.append(sapVariable.toMdx());
			}
		}

		return mdx.toString();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public ParsedQuery clone() {
		ParsedQuery cloned = new ParsedQuery();

		if (!formulas.isEmpty()) {
			List<Formula> clonedFormulas = new ArrayList<Formula>();

			for (Formula form : formulas) {
				clonedFormulas.add(form.clone());
			}

			cloned.formulas = clonedFormulas;
		}

		if (axes.length > 0) {
			QueryAxis[] clonedAxes = new QueryAxis[axes.length];

			for (int i = 0; i < clonedAxes.length; i++) {
				clonedAxes[i] = axes[i].clone();
			}
			cloned.setAxes(clonedAxes);
		}

		if (slicer != null) {
			cloned.slicer = this.slicer.clone();
		}

		cloned.setCube(this.getCube());

		return cloned;
	}

	/**
	 * @return sliecer exp
	 */
	public Exp getSlicer() {
		return slicer;
	}

	/**
	 * set the slicer exp
	 * 
	 * @param exp
	 */
	public void setSlicer(Exp exp) {
		this.slicer = exp;
	}

	/**
	 * add a formula for a member
	 */
	public void addFormula(String[] names, Exp exp,
			MemberProperty[] memberProperties) {
		Formula newFormula = new Formula(names, exp, memberProperties);
		formulas.add(newFormula);
	}

	/**
	 * add a formula for a set
	 */
	public void addFormula(String[] names, Exp exp) {
		Formula newFormula = new Formula(names, exp);
		formulas.add(newFormula);
	}

	/**
	 * remove a formula
	 */
	public void removeFormula(String uniqueName) {
		for (Iterator<Formula> iter = formulas.iterator(); iter.hasNext();) {
			Formula formula = iter.next();

			if (uniqueName.equals(formula.getUniqeName())) {
				iter.remove();
			}
		}
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitParsedQuery(this);
	}
}
