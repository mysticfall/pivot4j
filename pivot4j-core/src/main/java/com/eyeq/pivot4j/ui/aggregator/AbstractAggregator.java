/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.aggregator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property.StandardCellProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.util.OlapUtils;

public abstract class AbstractAggregator implements Aggregator {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Axis axis;

	private Level level;

	private Measure measure;

	private List<Member> members;

	private Map<Position, Double> values = new HashMap<Position, Double>();

	private Map<Position, Integer> counts = new HashMap<Position, Integer>();

	private Map<Measure, NumberFormat> formats = new HashMap<Measure, NumberFormat>();

	/**
	 * @param axis
	 * @param members
	 * @param level
	 * @param measure
	 */
	public AbstractAggregator(Axis axis, List<Member> members, Level level,
			Measure measure) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		this.axis = axis;

		if (members == null) {
			this.members = Collections.emptyList();
		} else {
			this.members = Collections.unmodifiableList(members);
		}

		this.level = level;
		this.measure = measure;
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getAxis()
	 */
	@Override
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getMembers()
	 */
	@Override
	public List<Member> getMembers() {
		return members;
	}

	/**
	 * @return the level
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getLevel()
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @return the measure
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getMeasure()
	 */
	public Measure getMeasure() {
		return measure;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#aggregate(com.eyeq.pivot4j.
	 *      ui.RenderContext)
	 */
	@Override
	public void aggregate(RenderContext context) {
		Position targetPosition = context.getAggregationTarget(getAxis());

		Double cellValue = null;

		if (context.getCell() == null) {
			cellValue = context.getAggregator().getValue(context);
		} else if (context.getCell().isEmpty()) {
			cellValue = null;
		} else {
			try {
				cellValue = context.getCell().getDoubleValue();
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		}

		if (cellValue == null) {
			return;
		}

		Double value = values.get(targetPosition);
		Double newValue = calculate(cellValue, value, targetPosition, context);

		values.put(targetPosition, newValue);

		int count = getCount(targetPosition);
		counts.put(targetPosition, ++count);

		Measure targetMeasure = getMeasure(targetPosition);

		if (targetMeasure != null && context.getCell() != null
				&& !formats.containsKey(targetMeasure)) {
			formats.put(targetMeasure, getNumberFormat(context.getCell()));
		}

		if (members.isEmpty() && logger.isTraceEnabled()) {
			logger.trace("Calculation result : ");
			logger.trace("	- count : " + count);
			logger.trace("	- value : " + cellValue);
			logger.trace("	- old value : " + value);
			logger.trace("	- new value : " + newValue);
		}
	}

	/**
	 * @param position
	 * @return
	 */
	protected Measure getMeasure(Position position) {
		if (this.measure != null) {
			return this.measure;
		}

		Measure targetMeasure = null;

		int size = position.getMembers().size();
		for (int i = size - 1; i > -1; i--) {
			Member member = position.getMembers().get(i);

			if (member instanceof Measure) {
				targetMeasure = (Measure) member;
				break;
			}
		}

		return targetMeasure;
	}

	/**
	 * @param cell
	 * @return
	 */
	protected NumberFormat getNumberFormat(Cell cell) {
		NumberFormat format = null;

		String pattern = ObjectUtils.toString(cell
				.getPropertyValue(StandardCellProperty.FORMAT_STRING));
		if (pattern != null && !"Standard".equals(pattern)) {
			try {
				format = new DecimalFormat(pattern);
			} catch (IllegalArgumentException e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Illegal number format : " + pattern);
				}
			}
		}

		if (format == null) {
			format = DecimalFormat.getNumberInstance();
		}

		return format;
	}

	/**
	 * @param position
	 * @return
	 */
	protected NumberFormat getNumberFormat(Position position) {
		Measure measureAtPosition = getMeasure(position);
		if (measureAtPosition == null) {
			return null;
		}

		return formats.get(measureAtPosition);
	}

	/**
	 * @param position
	 * @return
	 */
	protected int getCount(Position position) {
		Integer count = counts.get(position);

		if (count == null) {
			count = 0;
		}

		return count;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getValue(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public Double getValue(RenderContext context) {
		Position targetPosition = context.getAggregationTarget(getAxis());
		return getValue(targetPosition);
	}

	/**
	 * @param position
	 * @return
	 */
	protected Double getValue(Position position) {
		return values.get(position);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getFormattedValue(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public String getFormattedValue(RenderContext context) {
		Position position = context.getAggregationTarget(getAxis());

		Double value = getValue(position);

		NumberFormat format = getNumberFormat(position);

		if (value == null) {
			return null;
		} else if (format == null) {
			return Double.toString(value);
		} else {
			return format.format(value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#getLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public String getLabel(RenderContext context) {
		if (measure != null && OlapUtils.equals(measure, context.getMember())) {
			return measure.getCaption();
		}

		return getAggregationLabel(context);
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getAggregationLabel(RenderContext context) {
		ResourceBundle resources = context.getResourceBundle();

		String label = null;

		if (resources != null) {
			label = resources.getString("label.aggregation.type." + getName());
		}

		return label;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.Aggregator#reset()
	 */
	@Override
	public void reset() {
		values.clear();
		counts.clear();
	}

	/**
	 * @param value
	 * @param aggregation
	 * @param position
	 * @param context
	 * @return
	 */
	protected abstract Double calculate(Double value, Double aggregation,
			Position position, RenderContext context);
}
