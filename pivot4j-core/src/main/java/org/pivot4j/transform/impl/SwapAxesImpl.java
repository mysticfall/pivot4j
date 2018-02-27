/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import org.olap4j.OlapConnection;
import org.pivot4j.impl.QueryAdapter;
import org.pivot4j.transform.AbstractTransform;
import org.pivot4j.transform.SwapAxes;

/**
 * Implementation of the SwapAxesf transform
 */
public class SwapAxesImpl extends AbstractTransform implements SwapAxes {

    /**
     * @param queryAdapter
     * @param connection
     */
    public SwapAxesImpl(QueryAdapter queryAdapter, OlapConnection connection) {
        super(queryAdapter, connection);
    }

    /**
     * @see org.pivot4j.transform.SwapAxes#canSwapAxes()
     */
    public boolean canSwapAxes() {
        return getQueryAdapter().getAxes().size() > 1;
    }

    /**
     * @see org.pivot4j.transform.SwapAxes#isSwapAxes()
     */
    public boolean isSwapAxes() {
        return getQueryAdapter().isAxesSwapped();
    }

    /**
     * @see org.pivot4j.transform.SwapAxes#setSwapAxes(boolean)
     */
    public void setSwapAxes(boolean swap) {
        if (getQueryAdapter().isAxesSwapped() != swap) {
            getQueryAdapter().swapAxes();
        }
    }
}
