package com.eyeq.pivot4j.ui.command;

import java.io.Serializable;

public class CellParameters implements Serializable {

	private static final long serialVersionUID = 5176340826717067158L;

	private int axisOrdinal;

	private int positionOrdinal;

	private String memberUniqueName;

	private String hierarchyUniqueName;

	/**
	 * @return the axisOrdinal
	 */
	public int getAxisOrdinal() {
		return axisOrdinal;
	}

	/**
	 * @param axisOrdinal
	 *            the axisOrdinal to set
	 */
	public void setAxisOrdinal(int axisOrdinal) {
		this.axisOrdinal = axisOrdinal;
	}

	/**
	 * @return the positionOrdinal
	 */
	public int getPositionOrdinal() {
		return positionOrdinal;
	}

	/**
	 * @param positionOrdinal
	 *            the positionOrdinal to set
	 */
	public void setPositionOrdinal(int positionOrdinal) {
		this.positionOrdinal = positionOrdinal;
	}

	/**
	 * @return the memberUniqueName
	 */
	public String getMemberUniqueName() {
		return memberUniqueName;
	}

	/**
	 * @param memberUniqueName
	 *            the memberUniqueName to set
	 */
	public void setMemberUniqueName(String memberUniqueName) {
		this.memberUniqueName = memberUniqueName;
	}

	/**
	 * @return the hierarchyUniqueName
	 */
	public String getHierarchyUniqueName() {
		return hierarchyUniqueName;
	}

	/**
	 * @param hierarchyUniqueName
	 *            the hierarchyUniqueName to set
	 */
	public void setHierarchyUniqueName(String hierarchyUniqueName) {
		this.hierarchyUniqueName = hierarchyUniqueName;
	}
}
