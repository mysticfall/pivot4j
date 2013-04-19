package com.eyeq.pivot4j.analytics.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.olap4j.Cell;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@ManagedBean(name = "drillThroughData")
@ViewScoped
public class DrillThroughDataModel extends LazyDataModel<Map<String, Object>> {

	private static final long serialVersionUID = 2554173601960871316L;

	private static final String ROW_KEY = "_id";

	private Cell cell;

	private List<DataColumn> columns;

	public List<DataColumn> getColumns() {
		if (!isDataAvailable()) {
			return Collections.emptyList();
		}

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
	 * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String,
	 *      org.primefaces.model.SortOrder, java.util.Map)
	 */
	public List<Map<String, Object>> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
				pageSize);

		ResultSet rs = null;

		try {
			rs = cell.drillThrough();

			int rowIndex = 0;

			while (rowIndex < first) {
				if (!rs.next()) {
					return Collections.emptyList();
				}

				rowIndex++;
			}

			List<DataColumn> columns = getColumns();

			for (int i = 0; i < pageSize; i++) {
				if (rs.next()) {
					Map<String, Object> row = new HashMap<String, Object>(
							columns.size() + 1);

					for (DataColumn column : columns) {
						if (ROW_KEY.equals(column.getName())) {
							row.put(ROW_KEY, rowIndex + i + 1);
						} else {
							row.put(column.getName(),
									rs.getObject(column.getName()));
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
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}

		return data;
	}

	/**
	 * @param cell
	 */
	public void initialize(Cell cell) {
		reset();

		int rowCount = 0;

		ResultSet rs = null;

		try {
			rs = cell.drillThrough();

			ResultSetMetaData metadata = rs.getMetaData();

			int count = metadata.getColumnCount();

			this.columns = new ArrayList<DataColumn>(count);

			columns.add(new DataColumn("#", ROW_KEY));

			for (int i = 1; i <= count; i++) {
				columns.add(new DataColumn(metadata.getColumnLabel(i), metadata
						.getColumnName(i)));
			}

			while (rs.next()) {
				rowCount++;
			}

			setRowCount(rowCount);
		} catch (SQLException e) {
			throw new FacesException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
		}

		this.cell = cell;
	}

	public boolean isDataAvailable() {
		return cell != null;
	}

	@PreDestroy
	public void reset() {
		this.columns = null;
		this.cell = null;
	}

	public static class DataColumn {

		String label;

		String name;

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
