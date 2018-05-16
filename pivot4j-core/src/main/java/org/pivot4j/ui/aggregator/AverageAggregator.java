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

public class AverageAggregator extends TotalAggregator {

    public static final String NAME = "AVG";

    /**
     * @param axis
     * @param members
     * @param level
     * @param measure
     */
    public AverageAggregator(Axis axis, List<Member> members, Level level,
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
     * org.pivot4j.ui.aggregator.AbstractAggregator#getValue(org.olap4j.Position)
     */
    @Override
    protected Double getValue(Position position) {
        int count = getCount(position);
        Double value = super.getValue(position);

        if (count == 0 || value == null) {
            return null;
        }

        return value / count;
    }
}
