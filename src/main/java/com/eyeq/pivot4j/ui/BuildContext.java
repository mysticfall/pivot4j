/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;

public class BuildContext {

	private PivotModel model;

	private CellSetAxis axis;

	private Position columnPosition;

	private Position rowPosition;

	private Hierarchy hierarchy;

	private Member member;

	private Cell cell;

	/**
	 * @param model
	 * @param cellSet
	 */
	BuildContext(PivotModel model) {
		this.model = model;
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		return model;
	}

	/**
	 * @return the cellSet
	 */
	public CellSet getCellSet() {
		return model.getCellSet();
	}

	/**
	 * @return the axis
	 */
	public CellSetAxis getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            the axis to set
	 */
	public void setAxis(CellSetAxis axis) {
		this.axis = axis;
	}

	/**
	 * @return the hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(Member member) {
		this.member = member;
	}

	/**
	 * @return the cell
	 */
	public Cell getCell() {
		return cell;
	}

	/**
	 * @param cell
	 *            the cell to set
	 */
	public void setCell(Cell cell) {
		this.cell = cell;
	}

	/**
	 * @return the columnPosition
	 */
	public Position getColumnPosition() {
		return columnPosition;
	}

	/**
	 * @param columnPosition
	 *            the columnPosition to set
	 */
	public void setColumnPosition(Position columnPosition) {
		this.columnPosition = columnPosition;
	}

	/**
	 * @return the rowPosition
	 */
	public Position getRowPosition() {
		return rowPosition;
	}

	/**
	 * @param rowPosition
	 *            the rowPosition to set
	 */
	public void setRowPosition(Position rowPosition) {
		this.rowPosition = rowPosition;
	}
}
