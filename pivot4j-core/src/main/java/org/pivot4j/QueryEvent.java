/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j;

import java.util.Date;
import java.util.EventObject;

import org.olap4j.CellSet;

/**
 * Informs a listener that the quey has been executed.
 */
public class QueryEvent extends EventObject {

    private static final long serialVersionUID = -900118951700557408L;

    private Date start;

    private long duration;

    private String mdx;

    private transient CellSet cellSet;

    /**
     * Constructor for QueryEvent.
     *
     * @param source
     * @param start
     * @param duration
     * @param mdx
     * @param cellSet
     */
    public QueryEvent(PivotModel source, Date start, long duration, String mdx, CellSet cellSet) {
        super(source);

        this.start = start;
        this.duration = duration;
        this.mdx = mdx;
        this.cellSet = cellSet;
    }

    public PivotModel getModel() {
        return (PivotModel) getSource();
    }

    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the mdx
     */
    public String getMdx() {
        return mdx;
    }

    /**
     * @param mdx the mdx to set
     */
    public void setMdx(String mdx) {
        this.mdx = mdx;
    }

    /**
     * @return the cellSet
     */
    public CellSet getCellSet() {
        return cellSet;
    }

    /**
     * @param cellSet the cellSet to set
     */
    public void setCellSet(CellSet cellSet) {
        this.cellSet = cellSet;
    }
}
