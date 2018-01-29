/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.collector;

import java.util.List;

import org.olap4j.metadata.Level;
import org.olap4j.metadata.Property;

public interface PropertyCollector {

    /**
     * @param level
     * @return
     */
    List<Property> getProperties(Level level);
}
