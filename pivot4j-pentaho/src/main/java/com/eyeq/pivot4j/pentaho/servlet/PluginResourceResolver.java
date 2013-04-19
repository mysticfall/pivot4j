package com.eyeq.pivot4j.pentaho.servlet;

import java.net.URL;

import org.apache.myfaces.view.facelets.impl.DefaultResourceResolver;

public class PluginResourceResolver extends DefaultResourceResolver {

	public static final String RESOURCE_PREFIX = "./webapp";

	/**
	 * @see org.apache.myfaces.view.facelets.impl.DefaultResourceResolver#resolveUrl(java.lang.String)
	 */
	@Override
	public URL resolveUrl(String path) {
		return getClass().getClassLoader().getResource(RESOURCE_PREFIX + path);
	}
}
