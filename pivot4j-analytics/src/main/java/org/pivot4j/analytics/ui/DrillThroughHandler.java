package org.pivot4j.analytics.ui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.view.facelets.FaceletException;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.CubeNode;
import org.pivot4j.analytics.ui.navigator.MetadataNode;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "drillThroughHandler")
@RequestScoped
public class DrillThroughHandler implements NodeFilter {

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	@ManagedProperty(value = "#{drillThroughData}")
	private DrillThroughDataModel data;

	private CubeNode cubeNode;

	private TreeNode[] selection;

	private int maximumRows = 0;

	private DataTable table;

	public void update() {
		update(data.getCell());
	}

	/**
	 * @param cell
	 */
	public void update(Cell cell) {
		if (cell == null) {
			throw new NullArgumentException("cell");
		}

		List<MetadataElement> elements = new LinkedList<MetadataElement>();

		if (selection != null) {
			for (TreeNode node : selection) {
				MetadataElement elem = ((MetadataNode<?>) node).getObject();
				elements.add(elem);
			}
		}

		data.setRowIndex(-1);
		data.initialize(cell, elements, maximumRows);

		table.setFirst(0);
	}

	/**
	 * @return the cubeNode
	 */
	public CubeNode getCubeNode() {
		if (cubeNode != null) {
			return cubeNode;
		}

		PivotModel model = stateManager.getModel();

		if (model != null && model.isInitialized() && data.getCell() != null) {
			this.cubeNode = new CubeNode(model.getCube());
			cubeNode.setNodeFilter(this);
		}

		return cubeNode;
	}

	/**
	 * @param cubeNode
	 *            the cubeNode to set
	 */
	public void setCubeNode(CubeNode cubeNode) {
		this.cubeNode = cubeNode;
	}

	/**
	 * @return the data
	 */
	public DrillThroughDataModel getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(DrillThroughDataModel data) {
		this.data = data;
	}

	/**
	 * @return the selection
	 */
	public TreeNode[] getSelection() {
		return selection;
	}

	/**
	 * @param newSelection
	 *            the newSelection to set
	 */
	public void setSelection(TreeNode[] newSelection) {
		if (newSelection == null) {
			this.selection = null;
		} else {
			this.selection = Arrays.copyOf(newSelection, newSelection.length);
		}
	}

	/**
	 * @return the maximumRows
	 */
	public int getMaximumRows() {
		return maximumRows;
	}

	/**
	 * @param maximumRows
	 *            the maximumRows to set
	 */
	public void setMaximumRows(int maximumRows) {
		this.maximumRows = maximumRows;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isSelected(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isSelected(T element) {
		return data.getSelection().contains(element);
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isSelectable(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isSelectable(T element) {
		return element instanceof Level || element instanceof Measure;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isVisible(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isVisible(T element) {
		if (element instanceof Level) {
			Level level = (Level) element;

			if (level.getLevelType() == Level.Type.ALL || level.isCalculated()) {
				return false;
			}
		} else if (element instanceof Measure) {
			return !((Measure) element).isCalculated();
		}

		return true;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isExpanded(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isExpanded(T element) {
		Dimension dimension = null;

		if (element instanceof Cube) {
			return true;
		} else if (element instanceof Dimension) {
			dimension = (Dimension) element;
		} else if (element instanceof Hierarchy) {
			dimension = ((Hierarchy) element).getDimension();
		}

		if (dimension != null) {
			try {
				if (dimension.getDimensionType() == Dimension.Type.MEASURE) {
					return true;
				}
			} catch (OlapException e) {
				throw new FaceletException(e);
			}
		}

		return false;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isActive(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isActive(T element) {
		return false;
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

	/**
	 * @return the table
	 */
	public DataTable getTable() {
		return table;
	}

	/**
	 * @param table
	 *            the table to set
	 */
	public void setTable(DataTable table) {
		this.table = table;
	}
}
