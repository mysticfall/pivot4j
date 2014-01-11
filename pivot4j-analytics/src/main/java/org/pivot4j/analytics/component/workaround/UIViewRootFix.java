/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.component.workaround;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * Temporary workaround for a MyFaces issue.
 * 
 * @see https://issues.apache.org/jira/browse/MYFACES-3840
 */
public class UIViewRootFix extends UIViewRoot {

	/**
	 * @see javax.faces.component.UIViewRoot#getClientId(javax.faces.context.FacesContext)
	 */
	@Override
	public String getClientId(FacesContext context) {
		return getViewId();
	}
}
