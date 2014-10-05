/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.datasource;

import java.io.Serializable;

import org.pivot4j.state.Configurable;

public interface DataSourceInfo extends Serializable, Configurable {

	String getName();

	String getDescription();
}
