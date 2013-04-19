package com.eyeq.pivot4j.pentaho.servlet;

import java.io.IOException;
import java.util.Map;

import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class FacesDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 9009308748116185472L;

	private FacesServlet facesServlet;

	private PluginServletContext contextWrapper;

	private Map<String, String> initParameters;

	/**
	 * @return the initParameters
	 */
	public Map<String, String> getInitParameters() {
		return initParameters;
	}

	/**
	 * @param initParameters
	 *            the initParameters to set
	 */
	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}

	/**
	 * @return the contextListener
	 */
	protected PluginServletContext getContextWrapper() {
		return contextWrapper;
	}

	/**
	 * @return facesServlet
	 */
	protected FacesServlet getFacesServlet() {
		return facesServlet;
	}

	/**
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());

			this.contextWrapper = new PluginServletContext(
					config.getServletContext(), initParameters);
			contextWrapper.initialize();

			this.facesServlet = new FacesServlet();
			this.facesServlet.init(new PluginServletConfig(contextWrapper));
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		this.contextWrapper.destroy();
		this.contextWrapper = null;

		this.facesServlet.destroy();
		this.facesServlet = null;

		super.destroy();
	}

	/**
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse)
	 */
	@Override
	public void service(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		PluginServletRequest wrappedRequest = new PluginServletRequest(
				contextWrapper, (HttpServletRequest) request);
		try {
			Thread.currentThread().setContextClassLoader(
					getClass().getClassLoader());

			wrappedRequest.initialize();

			facesServlet.service(wrappedRequest, response);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);

			wrappedRequest.destroy();
		}
	}
}