package org.pivot4j.analytics.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.transform.DrillThrough;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@ManagedBean(name = "drillThroughData")
@ViewScoped
public class DrillThroughDataModel extends LazyDataModel<Map<String, Object>> {

	private static final long serialVersionUID = 2554173601960871316L;

	private static final String ROW_KEY = "_id";

	public static final int DEFAULT_PAGE_SIZE = 15;

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	private Cell cell;

	private int maximumRows = 0;

	private List<MetadataElement> selection = Collections.emptyList();

	private List<DataColumn> columns = Collections.emptyList();

	public DrillThroughDataModel() {
		setPageSize(DEFAULT_PAGE_SIZE);
	}

	public List<DataColumn> getColumns() {
		return columns;
	}

	/**
	 * @see org.primefaces.model.LazyDataModel#getRowKey(java.lang.Object)
	 */
	@Override
	public Object getRowKey(Map<String, Object> row) {
		return row.get(ROW_KEY);
	}

	/**
	 * @param cell
	 */
	public void initialize(Cell cell) {
		initialize(cell, null, 0);
	}

	/**
	 * @param cell
	 * @param selection
	 * @param maximumRows
	 */
	public void initialize(Cell cell, List<MetadataElement> selection,
			int maximumRows) {
		if (cell == null) {
			throw new NullArgumentException("cell");
		}

		this.cell = cell;

		if (selection == null) {
			this.selection = Collections.emptyList();
		} else {
			this.selection = Collections.unmodifiableList(selection);
		}

		this.maximumRows = maximumRows;

		ResultSet result = null;
		Statement stmt = null;

		try {
			result = execute();
			stmt = result.getStatement();

			boolean scrollable = (result.getStatement().getResultSetType() == ResultSet.TYPE_SCROLL_SENSITIVE);

			int rowCount = 0;

			if (scrollable && result.last()) {
				rowCount = result.getRow();

				result.beforeFirst();
			} else {
				while (result.next()) {
					rowCount++;

					if (maximumRows > 0 && rowCount >= maximumRows) {
						break;
					}
				}
			}

			if (maximumRows > 0) {
				rowCount = Math.min(rowCount, maximumRows);
			}

			setRowCount(rowCount);

			ResultSetMetaData metadata = result.getMetaData();

			int count = metadata.getColumnCount();

			this.columns = new LinkedList<DataColumn>();

			for (int i = 1; i <= count; i++) {
				columns.add(new DataColumn(metadata.getColumnLabel(i), metadata
						.getColumnName(i)));
			}
		} catch (SQLException e) {
			throw new FacesException(e);
		} finally {
			DbUtils.closeQuietly(result);
			DbUtils.closeQuietly(stmt);
		}
	}

	public void reset() {
		this.cell = null;
		this.maximumRows = 0;
		this.columns = Collections.emptyList();
		this.selection = Collections.emptyList();

		setRowCount(0);
		setRowIndex(-1);
	}

	protected ResultSet execute() {
		if (cell == null) {
			throw new IllegalStateException(
					"The model has not been initialized.");
		}

		DrillThrough transform = stateManager.getModel().getTransform(
				DrillThrough.class);

		if (selection.isEmpty()) {
			return transform.drillThrough(cell);
		} else {
			return transform.drillThrough(cell, selection, maximumRows);
		}
	}

	/**
	 * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String,
	 *      org.primefaces.model.SortOrder, java.util.Map)
	 */
	public List<Map<String, Object>> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		if (columns.isEmpty()) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
				pageSize);

		ResultSet result = null;
		Statement stmt = null;

		try {
			result = execute();
			stmt = result.getStatement();

			boolean scrollable = (result.getStatement().getResultSetType() == ResultSet.TYPE_SCROLL_SENSITIVE);

			int rowIndex = 0;

			if (scrollable) {
				rowIndex = first;

				result.absolute(rowIndex + 1);
			} else {
				while (rowIndex < first) {
					if (!result.next()) {
						return Collections.<Map<String, Object>> emptyList();
					}

					rowIndex++;
				}
			}

			List<DataColumn> columnList = getColumns();

			for (int i = 0; i < pageSize; i++) {
				if (result.next()) {
					Map<String, Object> row = new HashMap<String, Object>(
							columnList.size() + 1);

					for (DataColumn column : columnList) {
						if (ROW_KEY.equals(column.getName())) {
							row.put(ROW_KEY, rowIndex + i + 1);
						} else {
							row.put(column.getName(),
									result.getObject(column.getName()));
						}
					}

					data.add(row);
				} else {
					break;
				}
			}
		} catch (SQLException e) {
			throw new FacesException(e);
		} finally {
			DbUtils.closeQuietly(result);
			DbUtils.closeQuietly(stmt);
		}

		return data;
	}

	/**
	 * @return the maximumRows
	 */
	public int getMaximumRows() {
		return maximumRows;
	}

	/**
	 * @return the cell
	 */
	public Cell getCell() {
		return cell;
	}

	/**
	 * @return the selection
	 */
	public List<MetadataElement> getSelection() {
		return selection;
	}

	/**
	 * @return the stateManager
	 */
	public PivotStateManager getStateManager() {
		return stateManager;
	}

	/**
	 * @param stateManager
	 *            the stateManager to set
	 */
	public void setStateManager(PivotStateManager stateManager) {
		this.stateManager = stateManager;
	}

	public static class DataColumn {

		private String label;

		private String name;

		/**
		 * @param label
		 * @param name
		 */
		DataColumn(String label, String name) {
			this.label = label;
			this.name = name;
		}

		/**
		 * @return label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}
	}
}
