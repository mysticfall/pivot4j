package org.pivot4j.pentaho.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.view.facelets.ResourceResolver;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.myfaces.webapp.StartupServletContextListener;

import edu.emory.mathcs.backport.java.util.Collections;

public class PluginServletContext implements ServletContext {

	private ServletContext wrappedContext;

	private StartupServletContextListener listener;

	private Map<String, String> initParameters;

	private ResourceResolver resourceResolver;

	/**
	 * @param context
	 * @param initParameters
	 */
	public PluginServletContext(ServletContext context,
			Map<String, String> initParameters) {
		this.wrappedContext = context;
		this.initParameters = initParameters;

		this.listener = new StartupServletContextListener();
		this.resourceResolver = new PluginResourceResolver();
	}

	/**
	 * @return the wrappedContext
	 */
	protected ServletContext getWrappedContext() {
		return wrappedContext;
	}

	/**
	 * @return the resourceResolver
	 */
	protected ResourceResolver getResourceResolver() {
		return resourceResolver;
	}

	/**
	 * @return the listener
	 */
	public StartupServletContextListener getListener() {
		return listener;
	}

	public void initialize() {
		setAttribute("org.apache.myfaces.DYNAMICALLY_ADDED_FACES_SERVLET", true);

		listener.contextInitialized(new ServletContextEvent(this));
	}

	public void destroy() {
		listener.contextDestroyed(new ServletContextEvent(this));
	}

	/**
	 * @param name
	 * @return
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return wrappedContext.getAttribute(name);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	public Enumeration<?> getAttributeNames() {
		return wrappedContext.getAttributeNames();
	}

	/**
	 * @param uripath
	 * @return
	 * @see javax.servlet.ServletContext#getContext(java.lang.String)
	 */
	public ServletContext getContext(String uripath) {
		return wrappedContext.getContext(uripath);
	}

	/**
	 * @return
	 */
	public String getContextPath() {
		return wrappedContext.getContextPath();
	}

	/**
	 * @param name
	 * @return
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String name) {
		if (initParameters != null && initParameters.containsKey(name)) {
			return initParameters.get(name);
		}

		return wrappedContext.getInitParameter(name);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 */
	public Enumeration<?> getInitParameterNames() {
		Enumeration<?> e = wrappedContext.getInitParameterNames();

		if (initParameters == null) {
			return e;
		}

		List<String> names = new LinkedList<String>();

		for (String name : initParameters.keySet()) {
			names.add(name);
		}

		while (e.hasMoreElements()) {
			names.add(e.nextElement().toString());
		}

		return Collections.enumeration(names);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getMajorVersion()
	 */
	public int getMajorVersion() {
		return wrappedContext.getMajorVersion();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getMinorVersion()
	 */
	public int getMinorVersion() {
		return wrappedContext.getMinorVersion();
	}

	/**
	 * @param file
	 * @return
	 * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(String file) {
		return wrappedContext.getMimeType(file);
	}

	/**
	 * @param name
	 * @return
	 * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
	 */
	public RequestDispatcher getNamedDispatcher(String name) {
		return wrappedContext.getNamedDispatcher(name);
	}

	/**
	 * @param path
	 * @return
	 * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
	 */
	public String getRealPath(String path) {
		return wrappedContext.getRealPath(path);
	}

	/**
	 * @param path
	 * @return
	 * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String path) {
		return wrappedContext.getRequestDispatcher(path);
	}

	/**
	 * @param path
	 * @return
	 * @throws MalformedURLException
	 * @see javax.servlet.ServletContext#getResource(java.lang.String)
	 */
	public URL getResource(String path) throws MalformedURLException {
		return resourceResolver.resolveUrl(path);
	}

	/**
	 * @param path
	 * @return
	 * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String path) {
		InputStream in = null;

		try {
			URL url = getResource(path);

			if (url != null) {
				in = url.openStream();
			}
		} catch (IOException e) {
			throw new UnhandledException(e);
		}

		return in;
	}

	/**
	 * @param path
	 * @return
	 * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
	 */
	public Set<?> getResourcePaths(String path) {
		return wrappedContext.getResourcePaths(path);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	public String getServerInfo() {
		return wrappedContext.getServerInfo();
	}

	/**
	 * @param name
	 * @return
	 * @throws ServletException
	 * @deprecated
	 * @see javax.servlet.ServletContext#getServlet(java.lang.String)
	 */
	public Servlet getServlet(String name) throws ServletException {
		return wrappedContext.getServlet(name);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	public String getServletContextName() {
		return wrappedContext.getServletContextName();
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.ServletContext#getServletNames()
	 */
	public Enumeration<?> getServletNames() {
		return wrappedContext.getServletNames();
	}

	/**
	 * @return
	 * @deprecated
	 * @see javax.servlet.ServletContext#getServlets()
	 */
	public Enumeration<?> getServlets() {
		return wrappedContext.getServlets();
	}

	/**
	 * @param throwable
	 * @param msg
	 * @deprecated
	 * @see javax.servlet.ServletContext#log(java.lang.Exception,
	 *      java.lang.String)
	 */
	public void log(Exception throwable, String msg) {
		wrappedContext.log(throwable, msg);
	}

	/**
	 * @param msg
	 * @param throwable
	 * @see javax.servlet.ServletContext#log(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public void log(String msg, Throwable throwable) {
		wrappedContext.log(msg, throwable);
	}

	/**
	 * @param msg
	 * @see javax.servlet.ServletContext#log(java.lang.String)
	 */
	public void log(String msg) {
		wrappedContext.log(msg);
	}

	/**
	 * @param name
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		Object value = getAttribute(name);

		wrappedContext.removeAttribute(name);

		listener.attributeRemoved(new ServletContextAttributeEvent(this, name,
				value));
	}

	/**
	 * @param name
	 * @param value
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		Object oldValue = getAttribute(name);

		wrappedContext.setAttribute(name, value);

		if (oldValue == null) {
			listener.attributeAdded(new ServletContextAttributeEvent(this,
					name, value));
		} else if (!ObjectUtils.equals(oldValue, value)) {
			listener.attributeReplaced(new ServletContextAttributeEvent(this,
					name, value));
		}
	}
}
