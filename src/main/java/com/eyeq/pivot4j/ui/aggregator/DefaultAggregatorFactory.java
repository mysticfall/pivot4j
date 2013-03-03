/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregatorFactory implements AggregatorFactory {

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AggregatorFactory#getAvailableAggregations()
	 */
	@Override
	public List<String> getAvailableAggregations() {
		List<String> names = new ArrayList<String>();

		names.add(TotalAggregator.NAME);
		names.add(AverageAggregator.NAME);
		names.add(MinimumAggregator.NAME);
		names.add(MaximumAggregator.NAME);
		names.add(CountAggregator.NAME);

		return names;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AggregatorFactory#createAggregator(java.lang.String,
	 *      org.olap4j.Axis, java.util.List, org.olap4j.metadata.Level,
	 *      org.olap4j.metadata.Measure)
	 */
	@Override
	public Aggregator createAggregator(String name, Axis axis,
			List<Member> members, Level level, Measure measure) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		Aggregator aggregator = null;

		if (TotalAggregator.NAME.equals(name)) {
			aggregator = new TotalAggregator(axis, members, level, measure);
		} else if (AverageAggregator.NAME.equals(name)) {
			aggregator = new AverageAggregator(axis, members, level, measure);
		} else if (MinimumAggregator.NAME.equals(name)) {
			aggregator = new MinimumAggregator(axis, members, level, measure);
		} else if (MaximumAggregator.NAME.equals(name)) {
			aggregator = new MaximumAggregator(axis, members, level, measure);
		} else if (CountAggregator.NAME.equals(name)) {
			aggregator = new CountAggregator(axis, members, level, measure);
		}

		if (aggregator == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			if (logger.isWarnEnabled()) {
				logger.warn("Unknown aggregator name : " + name);
			}
		}

		return aggregator;
	}
}
