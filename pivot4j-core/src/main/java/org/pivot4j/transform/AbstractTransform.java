/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.pivot4j.PivotModel;
import org.pivot4j.query.QueryAdapter;

public abstract class AbstractTransform implements Transform {

	private QueryAdapter queryAdapter;

	private OlapConnection connection;

	/**
	 * @param queryAdapter
	 * @param connection
	 */
	public AbstractTransform(QueryAdapter queryAdapter,
			OlapConnection connection) {
		if (queryAdapter == null) {
			throw new NullArgumentException("queryAdapter");
		}

		if (connection == null) {
			throw new NullArgumentException("connection");
		}

		this.queryAdapter = queryAdapter;
		this.connection = connection;
	}

	/**
	 * @return the model
	 * @see org.pivot4j.transform.Transform#getModel()
	 */
	public PivotModel getModel() {
		return getQueryAdapter().getModel();
	}

	/**
	 * @return the queryAdapter
	 */
	protected QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	/**
	 * @return the connection
	 */
	protected OlapConnection getConnection() {
		return connection;
	}

	/**
	 * @param cellSet
	 * @param axis
	 * @return
	 */
	protected CellSetAxis getCellSetAxis(CellSet cellSet, Axis axis) {
		Axis targetAxis = axis;

		if (queryAdapter.isAxesSwapped()) {
			if (axis == Axis.COLUMNS) {
				targetAxis = Axis.ROWS;
			} else if (axis == Axis.ROWS) {
				targetAxis = Axis.COLUMNS;
			}
		}

		CellSetAxis cellAxis = null;

		if (cellSet != null) {
			for (CellSetAxis item : cellSet.getAxes()) {
				if (item.getAxisOrdinal() == targetAxis) {
					cellAxis = item;
					break;
				}
			}
		}

		return cellAxis;
	}
}
