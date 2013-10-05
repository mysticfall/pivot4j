/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.aggregator;

import static com.eyeq.pivot4j.ui.CellTypes.AGG_VALUE;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.Position;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.ui.RenderContext;

public class CountAggregator extends AbstractAggregator {

	public static final String NAME = "CNT";

	// TODO Make it locale-aware and configurable.
	private NumberFormat numberFormat = new DecimalFormat("###,###");

	/**
	 * @param axis
	 * @param members
	 * @param level
	 * @param measure
	 */
	public CountAggregator(Axis axis, List<Member> members, Level level,
			Measure measure) {
		super(axis, members, level, measure);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregator#getNumberFormat(org.olap4j.Cell)
	 */
	@Override
	protected NumberFormat getNumberFormat(Cell cell) {
		return numberFormat;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregator#getNumberFormat(org.olap4j.Position)
	 */
	@Override
	protected NumberFormat getNumberFormat(Position position) {
		return numberFormat;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregator#aggregate(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void aggregate(RenderContext context) {
		if (context.getAggregator() != null
				|| AGG_VALUE.equals(context.getCellType())) {
			return;
		}

		super.aggregate(context);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregator#calculate(java.lang.Double,
	 *      java.lang.Double, org.olap4j.Position,
	 *      com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected Double calculate(Double value, Double aggregation,
			Position position, RenderContext context) {
		return null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregator#getValue(org.olap4j.Position)
	 */
	@Override
	protected Double getValue(Position position) {
		if (position.getMembers().isEmpty()) {
			return null;
		}

		return (double) getCount(position);
	}
}
