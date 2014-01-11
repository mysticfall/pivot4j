/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.logging;

import javax.servlet.ServletContextEvent;

import org.apache.logging.log4j.core.web.Log4jServletContextListener;

public class Log4jServletContextListenerFallback extends
		Log4jServletContextListener {

	public static final int FALLBACK_MAJOR_VERSION = 3;

	/**
	 * @see org.apache.logging.log4j.core.web.Log4jServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		if (event.getServletContext().getMajorVersion() < FALLBACK_MAJOR_VERSION) {
			super.contextInitialized(event);
		}
	}

	/**
	 * @see org.apache.logging.log4j.core.web.Log4jServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (event.getServletContext().getMajorVersion() < FALLBACK_MAJOR_VERSION) {
			super.contextDestroyed(event);
		}
	}
}
