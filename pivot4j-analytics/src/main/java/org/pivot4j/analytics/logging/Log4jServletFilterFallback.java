/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.logging;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.core.web.Log4jServletFilter;

public class Log4jServletFilterFallback extends Log4jServletFilter {

	public static final int FALLBACK_MAJOR_VERSION = 3;

	private ServletContext servletContext;

	/**
	 * @see org.apache.logging.log4j.core.web.Log4jServletFilter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.servletContext = filterConfig.getServletContext();

		if (servletContext.getMajorVersion() < FALLBACK_MAJOR_VERSION) {
			super.init(filterConfig);
		}
	}

	/**
	 * @see org.apache.logging.log4j.core.web.Log4jServletFilter#doFilter(javax.servlet
	 *      .ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (servletContext.getMajorVersion() < FALLBACK_MAJOR_VERSION) {
			super.doFilter(request, response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * @see org.apache.logging.log4j.core.web.Log4jServletFilter#destroy()
	 */
	@Override
	public void destroy() {
		if (servletContext.getMajorVersion() < FALLBACK_MAJOR_VERSION) {
			super.destroy();
		}

		this.servletContext = null;
	}
}
