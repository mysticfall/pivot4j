/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.sort;

public enum SortCriteria {

    /**
     * sort hierarchically ascending
     */
    ASC,
    /**
     * sort hierarchically descending
     */
    DESC,
    /**
     * sort ascending breaking hierarchy
     */
    BASC,
    /**
     * sort descending breaking hierarchy
     */
    BDESC,
    /**
     * perform topcount
     */
    TOPCOUNT,
    /**
     * perform bottomcount
     */
    BOTTOMCOUNT;
}
