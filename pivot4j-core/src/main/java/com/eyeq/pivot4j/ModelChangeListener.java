/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

import java.util.EventListener;

public interface ModelChangeListener extends EventListener {

	/** Model has initialized */
	void modelInitialized(ModelChangeEvent e);

	/** Model data have changed, e.g. user has navigated */
	void modelChanged(ModelChangeEvent e);

	/** Model change, e.g. extensions added/removed */
	void structureChanged(ModelChangeEvent e);

	/** Model has destroyed */
	void modelDestroyed(ModelChangeEvent e);
}
