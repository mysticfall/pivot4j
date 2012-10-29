/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;

public class RenderContext {

	private PivotModel model;

	private Axis axis;

	private Position columnPosition;

	private Position rowPosition;

	private Hierarchy hierarchy;

	private Member member;

	private Level level;

	private Cell cell;

	private CellType cellType;

	private int columnCount;

	private int rowCount;

	private int columnHeaderCount;

	private int rowHeaderCount;

	private int colIndex;

	private int rowIndex;

	private int colSpan = 1;

	private int rowSpan = 1;

	/**
	 * @param model
	 * @param columnCount
	 * @param rowCount
	 * @param columnHeaderCount
	 * @param rowHeaderCount
	 */
	public RenderContext(PivotModel model, int columnCount, int rowCount,
			int columnHeaderCount, int rowHeaderCount) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		if (columnCount < 0) {
			throw new IllegalArgumentException(
					"Column count should be zero or positive integer.");
		}

		if (rowCount < 0) {
			throw new IllegalArgumentException(
					"Row count should be zero or positive integer.");
		}

		if (columnHeaderCount < 0) {
			throw new IllegalArgumentException(
					"Column header count should be zero or positive integer.");
		}

		if (rowHeaderCount < 0) {
			throw new IllegalArgumentException(
					"Row header count should be zero or positive integer.");
		}

		this.model = model;
		this.columnCount = columnCount;
		this.rowCount = rowCount;
		this.columnHeaderCount = columnHeaderCount;
		this.rowHeaderCount = rowHeaderCount;
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
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            the axis to set
	 */
	public void setAxis(Axis axis) {
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
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
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

	public Position getPosition() {
		if (axis == null) {
			return null;
		}

		if (axis.equals(Axis.COLUMNS)) {
			return columnPosition;
		} else if (axis.equals(Axis.ROWS)) {
			return rowPosition;
		} else {
			return null;
		}
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

	/**
	 * @return the cellType
	 */
	public CellType getCellType() {
		return cellType;
	}

	/**
	 * @param cellType
	 *            the cellType to set
	 */
	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}

	/**
	 * @return the columnCount
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * @return the columnHeaderCount
	 */
	public int getColumnHeaderCount() {
		return columnHeaderCount;
	}

	/**
	 * @return the rowHeaderCount
	 */
	public int getRowHeaderCount() {
		return rowHeaderCount;
	}

	/**
	 * @return the colIndex
	 */
	public int getColIndex() {
		return colIndex;
	}

	/**
	 * @param colIndex
	 *            the colIndex to set
	 */
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	/**
	 * @return the rowIndex
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * @param rowIndex
	 *            the rowIndex to set
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 * @return the colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * @param colSpan
	 *            the colSpan to set
	 */
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	/**
	 * @return the rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * @param rowSpan
	 *            the rowSpan to set
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	/**
	 * @return
	 */
	public CellSetAxis getCellSetAxis() {
		if (axis == null) {
			return null;
		}

		return getCellSet().getAxes().get(axis.axisOrdinal());
	}
}
