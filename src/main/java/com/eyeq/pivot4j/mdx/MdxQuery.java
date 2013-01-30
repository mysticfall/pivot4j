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

import org.olap4j.Axis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the result of parsing an MDX query.
 */
public class MdxQuery extends AbstractExp {

	private static final long serialVersionUID = 8608792548174831908L;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<Formula> formulas = new ArrayList<Formula>();

	private List<QueryAxis> axes = new ArrayList<QueryAxis>();

	private Exp slicer;

	private String cube;

	private List<CompoundId> cellProperties = new ArrayList<CompoundId>();

	private List<SapVariable> sapVariables = new ArrayList<SapVariable>();

	private boolean axesSwapped = false;

	/**
	 * 
	 * @return axes
	 */
	public List<QueryAxis> getAxes() {
		return this.axes;
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
	 * @param axis
	 * @return
	 */
	public QueryAxis getAxis(Axis axis) {
		QueryAxis result = null;

		for (QueryAxis ax : axes) {
			if (axis == ax.getAxis()) {
				result = ax;
				break;
			}
		}

		return result;
	}

	/**
	 * @param axis
	 * @return
	 */
	public void setAxis(QueryAxis axis) {
		removeAxis(axis.getAxis());
		axes.add(axis);
	}

	/**
	 * @param axis
	 */
	public void removeAxis(Axis axis) {
		Iterator<QueryAxis> it = axes.iterator();
		while (it.hasNext()) {
			QueryAxis ax = it.next();
			if (ax.getAxis() == axis) {
				it.remove();
			}
		}
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

		mdx.append("SELECT");

		isFollow = false;

		for (QueryAxis qa : axes) {
			if (isFollow) {
				mdx.append(", ");
			} else {
				mdx.append(' ');
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
	public MdxQuery clone() {
		MdxQuery clone = new MdxQuery();

		clone.axes = new ArrayList<QueryAxis>(axes.size());

		for (QueryAxis axis : axes) {
			clone.axes.add(axis);
		}

		clone.cube = this.cube;

		if (slicer != null) {
			clone.slicer = this.slicer.clone();
		}

		if (!formulas.isEmpty()) {
			clone.formulas = new ArrayList<Formula>(formulas.size());

			for (Formula form : formulas) {
				clone.formulas.add(form.clone());
			}
		}

		if (!cellProperties.isEmpty()) {
			clone.cellProperties = new ArrayList<CompoundId>(
					cellProperties.size());

			for (CompoundId property : cellProperties) {
				clone.cellProperties.add(property.clone());
			}
		}

		if (!sapVariables.isEmpty()) {
			clone.sapVariables = new ArrayList<SapVariable>(sapVariables.size());

			for (SapVariable variable : sapVariables) {
				clone.sapVariables.add(variable.clone());
			}
		}

		clone.axesSwapped = this.axesSwapped;

		return clone;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitMdxQuery(this);

		for (QueryAxis axis : axes) {
			axis.accept(visitor);
		}

		if (slicer != null) {
			slicer.accept(visitor);
		}
	}
}
