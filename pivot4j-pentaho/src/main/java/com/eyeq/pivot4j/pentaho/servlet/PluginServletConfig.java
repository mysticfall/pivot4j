package com.eyeq.pivot4j.pentaho.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class PluginServletConfig implements ServletConfig {

	public static final String SERVLET_NAME = "facesServlet";

	private ServletContext context;

	/**
	 * @param context
	 */
	public PluginServletConfig(ServletContext context) {
		this.context = context;
	}

	/**
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	@Override
	public String getInitParameter(String name) {
		return context.getInitParameter(name);
	}

	/**
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	@Override
	public Enumeration<?> getInitParameterNames() {
		return context.getInitParameterNames();
	}

	/**
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		return context;
	}

	/**
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	@Override
	public String getServletName() {
		return SERVLET_NAME;
	}
}
