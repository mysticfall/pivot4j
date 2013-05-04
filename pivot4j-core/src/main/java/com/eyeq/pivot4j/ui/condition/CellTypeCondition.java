/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.RenderContext;

public class CellTypeCondition extends AbstractCondition {

	public static final String NAME = "CELL_TYPE";

	private CellType cellType;

	/**
	 * @param conditionFactory
	 */
	public CellTypeCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param cellType
	 */
	public CellTypeCondition(ConditionFactory conditionFactory,
			CellType cellType) {
		super(conditionFactory);

		this.cellType = cellType;
	}

	/**
	 * @see com.eyeq.kona.equation.AbstractCondition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the cellType
	 */
	public CellType getCellType() {
		return cellType;
	}

	/**
	 * @param cellType
	 *            the cellType to set
	 */
	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (cellType == null) {
			throw new IllegalStateException("Cell type was not specified.");
		}

		return cellType == context.getCellType();
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		if (cellType == null) {
			return null;
		}

		return cellType.name();
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			this.cellType = null;
		} else {
			this.cellType = CellType.valueOf((String) state);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (cellType == null) {
			return;
		}

		configuration.setProperty("value", cellType.name());
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		String value = configuration.getString("value");

		if (value == null) {
			this.cellType = null;
		} else {
			this.cellType = CellType.valueOf(value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("cell type == ");

		if (cellType == null) {
			builder.append("[MISSING]");
		} else {
			builder.append(cellType.name());
		}

		return builder.toString();
	}
}
