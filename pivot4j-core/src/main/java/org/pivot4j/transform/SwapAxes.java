/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform;

public interface SwapAxes extends Transform {

    /**
     * @return true if axes can be swapped, i.e. if the result is 2 dimensional
     */
    boolean canSwapAxes();

    /**
     * Swaps the axes
     */
    void setSwapAxes(boolean swap);

    boolean isSwapAxes();
}
