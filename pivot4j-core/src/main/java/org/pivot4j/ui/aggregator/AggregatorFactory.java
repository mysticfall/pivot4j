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
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;

public interface AggregatorFactory {

    List<String> getAvailableAggregations();

    /**
     * @param name
     * @param axis
     * @param members
     * @param level
     * @param measure
     * @return
     */
    Aggregator createAggregator(String name, Axis axis, List<Member> members,
            Level level, Measure measure);
}
