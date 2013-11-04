/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.Position;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.el.ExpressionContext;

public abstract class CartesianRenderContext extends RenderContext {

	private static List<Axis> AXES = Collections.unmodifiableList(Arrays
			.asList(new Axis[] { Axis.COLUMNS, Axis.ROWS }));

	private Position columnPosition;

	private Position rowPosition;

	/**
	 * @param model
	 * @param renderer
	 */
	public CartesianRenderContext(PivotModel model, PivotRenderer<?> renderer) {
		super(model, renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderContext#getAxes()
	 */
	@Override
	public List<Axis> getAxes() {
		return AXES;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderContext#getAggregationTarget(org.olap4j.Axis)
	 */
	@Override
	public Position getAggregationTarget(Axis axis) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (axis.equals(Axis.COLUMNS)) {
			return rowPosition;
		} else if (axis.equals(Axis.ROWS)) {
			return columnPosition;
		} else {
			return null;
		}
	}

	/**
	 * @return the columnPosition
	 */
	public Position getColumnPosition() {
		return columnPosition;
	}

	/**
	 * @param columnPosition
	 *            the columnPosition to set
	 */
	public void setColumnPosition(Position columnPosition) {
		this.columnPosition = columnPosition;
	}

	/**
	 * @return the rowPosition
	 */
	public Position getRowPosition() {
		return rowPosition;
	}

	/**
	 * @param rowPosition
	 *            the rowPosition to set
	 */
	public void setRowPosition(Position rowPosition) {
		this.rowPosition = rowPosition;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderContext#getPosition(org.olap4j.Axis)
	 */
	@Override
	public Position getPosition(Axis axis) {
		if (axis == Axis.COLUMNS) {
			return columnPosition;
		} else if (axis == Axis.ROWS) {
			return rowPosition;
		}

		return null;
	}

	/**
	 * @param model
	 * @return
	 * @see com.eyeq.pivot4j.ui.RenderContext#createExpressionContext(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	protected ExpressionContext createExpressionContext(PivotModel model) {
		ExpressionContext context = super.createExpressionContext(model);

		context.put("columnPosition",
				new ExpressionContext.ValueBinding<Position>() {

					@Override
					public Position getValue() {
						return getColumnPosition();
					}
				});

		context.put("rowPosition",
				new ExpressionContext.ValueBinding<Position>() {

					@Override
					public Position getValue() {
						return getRowPosition();
					}
				});

		return context;
	}
}
