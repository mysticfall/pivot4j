package com.eyeq.pivot4j.pentaho.servlet;

import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.webapp.StartupServletContextListener;

public class PluginServletRequest extends HttpServletRequestWrapper {

	public static final String PLUGIN_PREFIX = "/plugin";

	public static final String SERVLET_PATH = "/pivot4j/faces";

	private PluginServletContext servletContext;

	/**
	 * @param servletContext
	 * @param request
	 */
	public PluginServletRequest(PluginServletContext servletContext,
			HttpServletRequest request) {
		super(request);

		this.servletContext = servletContext;
	}

	/**
	 * @return the servletContext
	 */
	protected PluginServletContext getServletContext() {
		return servletContext;
	}

	public void initialize() {
		StartupServletContextListener listener = servletContext.getListener();
		listener.requestInitialized(new ServletRequestEvent(servletContext,
				this));
	}

	public void destroy() {
		StartupServletContextListener listener = servletContext.getListener();
		listener.requestDestroyed(new ServletRequestEvent(servletContext, this));
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
	 */
	@Override
	public String getServletPath() {
		return PLUGIN_PREFIX + SERVLET_PATH;
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
	 */
	@Override
	public String getPathInfo() {
		String pathInfo = super.getPathInfo();

		if (pathInfo.startsWith(SERVLET_PATH)) {
			pathInfo = pathInfo.substring(SERVLET_PATH.length());
		}

		return pathInfo;
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		Object oldValue = getAttribute(name);

		super.setAttribute(name, value);

		StartupServletContextListener listener = servletContext.getListener();

		if (oldValue == null) {
			listener.attributeAdded(new ServletRequestAttributeEvent(
					servletContext, this, name, value));
		} else if (!ObjectUtils.equals(oldValue, value)) {
			listener.attributeReplaced(new ServletRequestAttributeEvent(
					servletContext, this, name, value));
		}
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#removeAttribute(java.lang.String)
	 */
	@Override
	public void removeAttribute(String name) {
		Object value = getAttribute(name);

		super.removeAttribute(name);

		StartupServletContextListener listener = servletContext.getListener();
		listener.attributeRemoved(new ServletRequestAttributeEvent(
				servletContext, this, name, value));
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getSession()
	 */
	@Override
	public HttpSession getSession() {
		HttpSession session = super.getSession();

		if (!(session instanceof PluginServletSession)) {
			session = new PluginServletSession(servletContext, session);
		}

		return session;
	}

	/**
	 * @see javax.servlet.http.HttpServletRequestWrapper#getSession(boolean)
	 */
	@Override
	public HttpSession getSession(boolean create) {
		HttpSession session = super.getSession(create);

		if (!(session instanceof PluginServletSession)) {
			session = new PluginServletSession(servletContext, session);
		}

		return session;
	}
}