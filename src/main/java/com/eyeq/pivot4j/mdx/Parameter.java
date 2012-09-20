/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

public class Parameter {

	public static final int TYPE_NUMERIC = 1;
	public static final int TYPE_STRING = 2;
	public static final int TYPE_MEMBER = 3;

	private String name;
	private String description;
	private int type;
	private int iAxis; // negative, if not on axis
	private Object oValue;

	public Parameter(String name, int type, int iAxis) {
		this.name = name;
		this.type = type;
		this.iAxis = iAxis;
	}

	/**
	 * @return the value object
	 */
	public Object getOValue() {
		return oValue;
	}

	/**
	 * set the value object
	 * 
	 * @param object
	 *            value
	 */
	public void setOValue(Object object) {
		oValue = object;
	}

	/**
	 * @return parameter type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return parameter's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return index of axis, negative if not on any axis
	 */
	public int getIAxis() {
		return iAxis;
	}
}
