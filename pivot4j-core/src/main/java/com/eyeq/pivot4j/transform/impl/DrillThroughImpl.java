/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.DrillThrough;

public class DrillThroughImpl extends AbstractTransform implements DrillThrough {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @param queryAdapter
	 * @param connection
	 */
	public DrillThroughImpl(QueryAdapter queryAdapter, OlapConnection connection) {
		super(queryAdapter, connection);
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillThrough#drillThrough(org.olap4j.Cell)
	 */
	@Override
	public ResultSet drillThrough(Cell cell) {
		return drillThrough(cell, null, 0);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.DrillThrough#drillThrough(org.olap4j.Cell,
	 *      java.util.List, int)
	 */
	@Override
	public ResultSet drillThrough(Cell cell, List<MetadataElement> selection,
			int maximumRows) {
		if (cell == null) {
			throw new NullArgumentException("cell");
		}

		ResultSet result;

		if (selection != null && !selection.isEmpty() || maximumRows > 0) {
			result = performDrillThroughMdx(cell, selection, maximumRows);
		} else {
			result = performDrillThrough(cell);
		}

		return result;
	}

	/**
	 * @param cell
	 * @return
	 */
	protected ResultSet performDrillThrough(Cell cell) {
		try {
			return cell.drillThrough();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @param cell
	 * @param selection
	 * @param maximumRows
	 * @return
	 */
	protected ResultSet performDrillThroughMdx(Cell cell,
			List<MetadataElement> selection, int maximumRows) {
		PivotModel model = getModel();
		QueryAdapter query = getQueryAdapter();

		StringBuilder builder = new StringBuilder();

		builder.append("DRILLTHROUGH");

		if (maximumRows > 0) {
			builder.append(" MAXROWS ");
			builder.append(maximumRows);
		}

		builder.append(" SELECT (");

		boolean isFirst = true;

		List<Integer> coords = cell.getCoordinateList();

		CellSet cellSet = cell.getCellSet();
		List<CellSetAxis> axes = cellSet.getAxes();

		int axisOrdinal = 0;
		for (int ordinal : coords) {
			Position position = axes.get(axisOrdinal++).getPositions()
					.get(ordinal);

			for (Member member : position.getMembers()) {
				if (isFirst) {
					isFirst = false;
				} else {
					builder.append(", ");
				}

				builder.append(member.getUniqueName());
			}
		}

		builder.append(") ON COLUMNS FROM ");
		builder.append(model.getCube().getUniqueName());

		Exp slicer = query.getParsedQuery().getSlicer();

		if (slicer != null) {
			builder.append(" WHERE ");
			builder.append(slicer.toMdx());
		}

		if (selection != null && !selection.isEmpty()) {
			builder.append(" RETURN ");

			isFirst = true;

			for (MetadataElement elem : selection) {
				if (isFirst) {
					isFirst = false;
				} else {
					builder.append(", ");
				}

				builder.append(elem.getUniqueName());
			}
		}

		String mdx = builder.toString();

		if (logger.isDebugEnabled()) {
			logger.debug("Drill through MDX : {}", mdx);
		}

		ResultSet result;

		try {
			Statement stmt = getConnection().createStatement();
			result = stmt.executeQuery(mdx);
		} catch (SQLException e) {
			throw new PivotException(e);
		}

		return result;
	}
}
