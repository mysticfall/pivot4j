/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j;

import java.util.EventListener;

public interface QueryListener extends EventListener {

    /**
     * Query has been executed
     */
    void queryExecuted(QueryEvent e);
}
