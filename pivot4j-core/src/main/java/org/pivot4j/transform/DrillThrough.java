/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform;

import java.sql.ResultSet;
import java.util.List;

import org.olap4j.Cell;
import org.olap4j.metadata.MetadataElement;

public interface DrillThrough extends Transform {

    /**
     * Note that it is caller's responsibility to close the returned ResultSet
     * object and also the Statement instance which is associated with it.
     *
     * @param cell
     * @return
     */
    ResultSet drillThrough(Cell cell);

    /**
     * Note that it is caller's responsibility to close the returned ResultSet
     * object and also the Statement instance which is associated with it.
     *
     * @param cell
     * @param selection
     * @param maximumRows
     * @return
     */
    ResultSet drillThrough(Cell cell, List<MetadataElement> selection,
            int maximumRows);
}
