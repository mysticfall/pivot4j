/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.aggregator;

import java.util.List;

import org.olap4j.Axis;
import org.olap4j.Position;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.pivot4j.ui.RenderContext;

public class MaximumAggregator extends AbstractAggregator {

    public static final String NAME = "MAX";

    /**
     * @param axis
     * @param members
     * @param level
     * @param measure
     */
    public MaximumAggregator(Axis axis, List<Member> members, Level level,
            Measure measure) {
        super(axis, members, level, measure);
    }

    /**
     * @see org.pivot4j.ui.aggregator.Aggregator#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * @see
     * org.pivot4j.ui.aggregator.AbstractAggregator#calculate(java.lang.Double,
     * java.lang.Double, org.olap4j.Position, org.pivot4j.ui.RenderContext)
     */
    @Override
    protected Double calculate(Double value, Double aggregation,
            Position position, RenderContext context) {
        if (value == null) {
            return aggregation;
        }

        if (aggregation == null) {
            return value;
        }

        return Math.max(value, aggregation);
    }
}
