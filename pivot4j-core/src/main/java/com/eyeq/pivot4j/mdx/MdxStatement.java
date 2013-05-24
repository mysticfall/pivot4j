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
public class MdxStatement extends AbstractExp {

	private static final long serialVersionUID = 8608792548174831908L;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private List<Formula> formulas = new ArrayList<Formula>();

	private List<QueryAxis> axes = new ArrayList<QueryAxis>();

	private Exp slicer;

	private CompoundId cube;

	private List<CompoundId> cellProperties = new ArrayList<CompoundId>();

	private List<SapVariable> sapVariables = new ArrayList<SapVariable>();

	private boolean axesSwapped = false;

	/**
	 * @return the formulas
	 */
	public List<Formula> getFormulas() {
		return formulas;
	}

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
	public CompoundId getCube() {
		return cube;
	}

	/**
	 * Sets the cube.
	 * 
	 * @param cube
	 *            The cube to set
	 */
	public void setCube(CompoundId cube) {
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

		if (!formulas.isEmpty()) {
			mdx.append("WITH");

			for (Formula element : formulas) {
				mdx.append(' ');
				mdx.append(element.toMdx());
			}

			mdx.append(' ');
		}

		mdx.append("SELECT");

		boolean following = false;

		for (QueryAxis qa : axes) {
			if (qa.getExp() == null) {
				continue;
			}

			if (following) {
				mdx.append(", ");
			} else {
				mdx.append(' ');
				following = true;
			}

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

				if (str.equalsIgnoreCase("VALUE")
						|| str.equalsIgnoreCase("FORMATTED_VALUE")) {
					continue;
				}

				mdx.append(" ,");
				mdx.append(str);
			}
		}

		if (!sapVariables.isEmpty()) {
			mdx.append(" SAP VARIABLES ");

			following = false;

			for (SapVariable sapVariable : sapVariables) {
				if (following) {
					mdx.append(", ");
				} else {
					following = true;
				}

				mdx.append(sapVariable.toMdx());
			}
		}

		return mdx.toString();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public MdxStatement clone() {
		MdxStatement clone = new MdxStatement();

		if (!formulas.isEmpty()) {
			clone.formulas = new ArrayList<Formula>(formulas.size());

			for (Formula element : formulas) {
				clone.formulas.add(element.clone());
			}
		}

		clone.axes = new ArrayList<QueryAxis>(axes.size());

		for (QueryAxis axis : axes) {
			clone.axes.add(axis.clone());
		}

		if (cube != null) {
			clone.cube = this.cube.clone();
		}

		if (slicer != null) {
			clone.slicer = this.slicer.clone();
		}

		for (CompoundId property : cellProperties) {
			clone.cellProperties.add(property.clone());
		}

		for (SapVariable variable : sapVariables) {
			clone.sapVariables.add(variable.clone());
		}

		clone.axesSwapped = this.axesSwapped;

		return clone;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitStatement(this);

		for (Formula element : formulas) {
			element.accept(visitor);
		}

		for (QueryAxis axis : axes) {
			axis.accept(visitor);
		}

		if (cube != null) {
			cube.accept(visitor);
		}

		if (slicer != null) {
			slicer.accept(visitor);
		}

		for (CompoundId property : cellProperties) {
			property.accept(visitor);
		}

		for (SapVariable variable : sapVariables) {
			variable.accept(visitor);
		}
	}
}
