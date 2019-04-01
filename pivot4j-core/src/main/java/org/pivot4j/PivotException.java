/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j;

public class PivotException extends RuntimeException {

    private static final long serialVersionUID = -453439015227731937L;

    /**
     * Constructor for PivotException.
     */
    public PivotException() {
    }

    /**
     * Constructor for PivotException.
     *
     * @param msg
     */
    public PivotException(String msg) {
        super(msg);
    }

    /**
     * Constructor for PivotException.
     *
     * @param msg
     * @param cause
     */
    public PivotException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructor for PivotException.
     *
     * @param cause
     */
    public PivotException(Throwable cause) {
        super(cause);
    }
}
