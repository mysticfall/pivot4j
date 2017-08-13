/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.table;

import static org.pivot4j.ui.CellTypes.AGG_VALUE;
import static org.pivot4j.ui.CellTypes.LABEL;
import static org.pivot4j.ui.CellTypes.VALUE;
import static org.pivot4j.ui.table.TableCellTypes.FILL;
import static org.pivot4j.ui.table.TableCellTypes.TITLE;
import static org.pivot4j.ui.table.TablePropertyCategories.CELL;
import static org.pivot4j.ui.table.TablePropertyCategories.HEADER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;
import org.pivot4j.PivotException;
import org.pivot4j.PivotModel;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.ui.AbstractPivotRenderer;
import org.pivot4j.ui.aggregator.Aggregator;
import org.pivot4j.ui.aggregator.AggregatorFactory;
import org.pivot4j.ui.aggregator.AggregatorPosition;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.MemberSelection;
import org.pivot4j.util.OlapUtils;
import org.pivot4j.util.TreeNode;
import org.pivot4j.util.TreeNodeCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableRenderer extends AbstractPivotRenderer<TableRenderContext, TableRenderCallback> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

	private boolean showSlicerMembersInline = true;

	private Comparator<Level> levelComparator = new Comparator<Level>() {

		@Override
		public int compare(Level l1, Level l2) {
			Integer d1 = l1.getDepth();
			Integer d2 = l2.getDepth();
			return d1.compareTo(d2);
		}
	};

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#getRenderPropertyCategories()
	 */
	@Override
	protected List<String> getRenderPropertyCategories() {
		List<String> categories = new LinkedList<String>(super.getRenderPropertyCategories());

		categories.add(CELL);
		categories.add(HEADER);

		return categories;
	}

	/**
	 * @return the hideSpans
	 */
	public boolean getHideSpans() {
		return hideSpans;
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * @return the showParentMembers
	 */
	public boolean getShowParentMembers() {
		return showParentMembers;
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	/**
	 * @return the showDimensionTitle
	 */
	public boolean getShowDimensionTitle() {
		return showDimensionTitle;
	}

	/**
	 * @param showDimensionTitle
	 *            the showDimensionTitle to set
	 */
	public void setShowDimensionTitle(boolean showDimensionTitle) {
		this.showDimensionTitle = showDimensionTitle;
	}

	/**
	 * @return the showSlicerMembersInline
	 */
	public boolean getShowSlicerMembersInline() {
		return showSlicerMembersInline;
	}

	/**
	 * @param showSlicerMembersInline
	 *            the showSlicerMembersInline to set
	 */
	public void setShowSlicerMembersInline(boolean showSlicerMembersInline) {
		this.showSlicerMembersInline = showSlicerMembersInline;
	}

	public void swapAxes() {
		for (AggregatorPosition position : AggregatorPosition.values()) {
			swapAggregators(position);
		}
	}

	/**
	 * @param position
	 */
	private void swapAggregators(AggregatorPosition position) {
		List<String> aggregators = getAggregators(Axis.COLUMNS, position);

		setAggregators(Axis.COLUMNS, position, getAggregators(Axis.ROWS, position));
		setAggregators(Axis.ROWS, position, aggregators);
	}

	/**
	 * @param context
	 * @return
	 * @see org.pivot4j.ui.AbstractPivotRenderer#getLabel(org.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getLabel(TableRenderContext context) {
		String label;

		if (LABEL.equals(context.getCellType())) {
			label = getHeaderLabel(context);
		} else if (TITLE.equals(context.getCellType())) {
			label = getTitleLabel(context);
		} else if (VALUE.equals(context.getCellType())) {
			label = getValueLabel(context);
		} else if (AGG_VALUE.equals(context.getCellType())) {
			label = getAggregationLabel(context);
		} else {
			label = null;
		}

		return label;
	}

	/**
	 * @param context
	 * @return
	 * @see org.pivot4j.ui.AbstractPivotRenderer#getValue(org.pivot4j.ui.RenderContext)
	 */
	@Override
	protected Double getValue(TableRenderContext context) {
		Double value = null;
		boolean nonNumericValue = false;

		Aggregator aggregator = context.getAggregator();

		Cell cell = context.getCell();
		

		try {
			if (aggregator == null) {
				if (cell != null && !cell.isEmpty()) {
					try {
						value = cell.getDoubleValue();
					} catch (OlapException e) {
						nonNumericValue = true;
						//#218 do nothing: cell.getDoubleValue() throws OlapException if this cell does not have a numeric value
					}
				}
			} else {
				value = aggregator.getValue(context);
			}
		} catch (NumberFormatException e) {
			nonNumericValue = true;
			//#147 do nothing: XmlaOlap4jCell.getDoubleValue throws NumberFormatException if this cell does not have a numeric value
		}
		
		if (nonNumericValue && logger.isTraceEnabled()) {
			logger.trace("Non-numeric cell value : {}", cell.getValue());
		}


		return value;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getHeaderLabel(TableRenderContext context) {
		String label;

		if (context.getProperty() == null) {
			if (context.getMember() == null) {
				label = context.getHierarchy().getCaption();
			} else {
				label = context.getMember().getCaption();
			}
		} else if (context.getMember() != null) {
			try {
				label = context.getMember().getPropertyFormattedValue(context.getProperty());
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		} else {
			label = "";
		}

		return label;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getTitleLabel(TableRenderContext context) {
		String label = null;

		if (context.getProperty() != null) {
			label = context.getProperty().getCaption();
		} else if (context.getLevel() != null) {
			label = context.getLevel().getCaption();
		} else if (context.getHierarchy() != null) {
			label = context.getHierarchy().getCaption();
		}

		return label;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getValueLabel(TableRenderContext context) {
		String label;

		Cell cell = context.getCell();

		if (cell == null) {
			if (context.getAxis() == Axis.FILTER) {
				if (context.getMember() != null) {
					label = context.getMember().getCaption();
				} else {
					label = context.getHierarchy().getCaption();
				}
			} else {
				Aggregator aggregator = context.getAggregator();

				if (aggregator == null) {
					label = null;
				} else {
					label = aggregator.getFormattedValue(context);
				}
			}
		} else {
			label = cell.getFormattedValue();
		}

		return label;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getAggregationLabel(TableRenderContext context) {
		String label;

		Aggregator aggregator = context.getAggregator();

		if (aggregator == null && context.getMember() != null) {
			label = context.getMember().getCaption();
		} else {
			label = aggregator.getLabel(context);
		}

		return label;
	}

	/**
	 * @param context
	 * @return
	 */
	@Override
	protected String getRenderPropertyCategory(TableRenderContext context) {
		String category;

		if (VALUE.equals(context.getCellType()) || AGG_VALUE.equals(context.getCellType())) {
			category = CELL;
		} else {
			category = HEADER;
		}

		return category;
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[7];

		int index = 0;

		states[index++] = super.saveState();
		states[index++] = showParentMembers;
		states[index++] = showDimensionTitle;
		states[index++] = hideSpans;
		states[index++] = showSlicerMembersInline;

		return states;
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		Serializable[] states = (Serializable[]) state;

		int index = 0;

		super.restoreState(states[index++]);

		this.showParentMembers = (Boolean) states[index++];
		this.showDimensionTitle = (Boolean) states[index++];
		this.hideSpans = (Boolean) states[index++];
		this.showSlicerMembersInline = (Boolean) states[index++];
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		configuration.addProperty("showDimensionTitle", showDimensionTitle);
		configuration.addProperty("showParentMembers", showParentMembers);
		configuration.addProperty("hideSpans", hideSpans);
		configuration.addProperty("filter[@inline]", showSlicerMembersInline);
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.showDimensionTitle = configuration.getBoolean("showDimensionTitle", true);
		this.showParentMembers = configuration.getBoolean("showParentMembers", false);
		this.hideSpans = configuration.getBoolean("hideSpans", false);
		this.showSlicerMembersInline = configuration.getBoolean("filter[@inline]", true);
	}

	/**
	 * @see org.pivot4j.ui.PivotRenderer#render(org.pivot4j.PivotModel,
	 *      org.pivot4j.ui.RenderCallback)
	 */
	@Override
	public void render(PivotModel model, TableRenderCallback callback) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (callback == null) {
			throw new NullArgumentException("callback");
		}

		CellSet cellSet = model.getCellSet();

		if (cellSet == null) {
			return;
		}

		List<CellSetAxis> axes = cellSet.getAxes();
		if (axes.isEmpty()) {
			return;
		}

		MemberHierarchyCache cache;

		if (model instanceof PivotModelImpl) {
			cache = ((PivotModelImpl) model).getMemberHierarchyCache();
		} else {
			cache = new MemberHierarchyCache(model.getCube());
		}

		TableHeaderNode columnRoot = createAxisTree(model, Axis.COLUMNS, cache);
		if (columnRoot == null) {
			return;
		}

		TableHeaderNode rowRoot = createAxisTree(model, Axis.ROWS, cache);
		if (rowRoot == null) {
			return;
		}

		List<TableHeaderNode> filterRoots;

		if (getRenderSlicer()) {
			filterRoots = createFilterAxisTrees(model, cache);
		} else {
			filterRoots = Collections.emptyList();
		}

		configureAxisTree(model, Axis.COLUMNS, columnRoot);
		configureAxisTree(model, Axis.ROWS, rowRoot);

		for (TableHeaderNode node : filterRoots) {
			configureAxisTree(model, Axis.FILTER, node);
		}

		invalidateAxisTree(model, Axis.COLUMNS, columnRoot);
		invalidateAxisTree(model, Axis.ROWS, rowRoot);

		for (TableHeaderNode node : filterRoots) {
			invalidateAxisTree(model, Axis.FILTER, node);
		}

		TableRenderContext context = createRenderContext(model, columnRoot, rowRoot);

		callback.startRender(context);
		callback.startTable(context);

		renderHeader(context, columnRoot, rowRoot, callback);
		renderBody(context, columnRoot, rowRoot, callback);

		callback.endTable(context);

		for (TableHeaderNode node : filterRoots) {
			renderFilter(context, node, callback);
		}

		callback.endRender(context);
	}

	/**
	 * @param model
	 * @param columnRoot
	 * @param rowRoot
	 * @return
	 */
	protected TableRenderContext createRenderContext(PivotModel model, TableHeaderNode columnRoot,
			TableHeaderNode rowRoot) {
		int columnHeaderCount = columnRoot.getMaxRowIndex();
		int rowHeaderCount = rowRoot.getMaxRowIndex();

		int columnCount = columnRoot.getWidth();
		int rowCount = rowRoot.getWidth();

		TableRenderContext context = new TableRenderContext(model, this, columnCount, rowCount, columnHeaderCount,
				rowHeaderCount);

		return context;
	}

	/**
	 * @param context
	 * @param columnRoot
	 * @param rowRoot
	 * @param callback
	 */
	protected void renderHeader(final TableRenderContext context, final TableHeaderNode columnRoot,
			final TableHeaderNode rowRoot, final TableRenderCallback callback) {

		callback.startHeader(context);
		context.setRenderPropertyCategory(HEADER);

		int count = context.getColumnHeaderCount();

		for (int rowIndex = 0; rowIndex < count; rowIndex++) {
			context.setAxis(Axis.COLUMNS);
			context.setColIndex(0);
			context.setRowIndex(rowIndex);

			callback.startRow(context);

			renderHeaderCorner(context, columnRoot, rowRoot, callback);

			// invoking renderHeaderCorner method resets the axis property.
			context.setAxis(Axis.COLUMNS);

			columnRoot.walkChildrenAtRowIndex(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode headerNode = (TableHeaderNode) node;

					context.setColIndex(headerNode.getColIndex() + context.getRowHeaderCount());
					context.setColSpan(headerNode.getColSpan());
					context.setRowSpan(headerNode.getRowSpan());

					context.setMember(headerNode.getMember());
					context.setProperty(headerNode.getProperty());
					context.setHierarchy(headerNode.getHierarchy());
					context.setPosition(headerNode.getPosition());
					context.setColumnPosition(headerNode.getPosition());
					context.setAggregator(headerNode.getAggregator());
					context.setCell(null);

					if (headerNode.isAggregation()) {
						context.setCellType(AGG_VALUE);
					} else if (context.getMember() == null) {
						if (context.getHierarchy() == null) {
							context.setCellType(FILL);
						} else {
							context.setCellType(TITLE);
						}
					} else {
						context.setCellType(LABEL);
					}

					callback.startCell(context);
					callback.renderCommands(context, getCommands(context));
					callback.renderContent(context, getLabel(context), getValue(context));
					callback.endCell(context);

					return TreeNodeCallback.CONTINUE;
				}
			}, rowIndex + 1);

			callback.endRow(context);
		}

		callback.endHeader(context);
	}

	/**
	 * @param context
	 * @param rowRoot
	 * @param columnRoot
	 * @param callback
	 */
	protected void renderBody(final TableRenderContext context, final TableHeaderNode columnRoot,
			final TableHeaderNode rowRoot, final TableRenderCallback callback) {
		callback.startBody(context);

		int count = rowRoot.getColSpan();

		for (int rowIndex = 0; rowIndex < count; rowIndex++) {
			context.setAxis(Axis.ROWS);
			context.setColIndex(0);
			context.setRowIndex(rowIndex + context.getColumnHeaderCount());

			callback.startRow(context);

			rowRoot.walkChildrenAtColIndex(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode headerNode = (TableHeaderNode) node;

					if (headerNode.getRowIndex() == 0) {
						return TreeNodeCallback.CONTINUE;
					}

					context.setColIndex(headerNode.getRowIndex() - 1);
					context.setColSpan(headerNode.getRowSpan());
					context.setRowSpan(headerNode.getColSpan());
					context.setMember(headerNode.getMember());
					context.setLevel(headerNode.getMemberLevel());
					context.setProperty(headerNode.getProperty());
					context.setHierarchy(headerNode.getHierarchy());
					context.setPosition(headerNode.getPosition());
					context.setRowPosition(headerNode.getPosition());
					context.setAggregator(headerNode.getAggregator());
					context.setCell(null);
					context.setRenderPropertyCategory(HEADER);

					if (headerNode.isAggregation()) {
						context.setCellType(AGG_VALUE);
					} else if (context.getMember() == null && context.getProperty() == null) {
						if (context.getHierarchy() == null) {
							context.setCellType(FILL);
						} else {
							context.setCellType(TITLE);
						}
					} else {
						context.setCellType(LABEL);
					}

					callback.startCell(context);
					callback.renderCommands(context, getCommands(context));
					callback.renderContent(context, getLabel(context), getValue(context));
					callback.endCell(context);

					if (headerNode.getChildCount() == 0) {
						renderDataRow(context, columnRoot, rowRoot, (TableHeaderNode) node, callback);
					}

					return TreeNodeCallback.CONTINUE;
				}
			}, rowIndex);

			callback.endRow(context);
		}

		callback.endBody(context);
	}

	/**
	 * @param context
	 * @param columnRoot
	 * @param rowRoot
	 * @param rowNode
	 * @param callback
	 */
	protected void renderDataRow(TableRenderContext context, TableHeaderNode columnRoot, TableHeaderNode rowRoot,
			TableHeaderNode rowNode, TableRenderCallback callback) {
		context.setCellType(VALUE);
		context.setRenderPropertyCategory(CELL);

		for (int i = 0; i < context.getColumnCount(); i++) {
			Cell cell = null;

			TableHeaderNode columnNode = columnRoot.getLeafNodeAtColIndex(i);

			if (columnNode != null && columnNode.getPosition() != null && columnNode.getPosition().getOrdinal() != -1
					&& rowNode.getPosition() != null && rowNode.getPosition().getOrdinal() != -1) {
				cell = context.getCellSet().getCell(columnNode.getPosition(), rowNode.getPosition());
			}

			context.setColIndex(context.getRowHeaderCount() + i);
			context.setColSpan(1);
			context.setRowSpan(1);
			context.setAggregator(null);

			context.setAxis(null);
			context.setHierarchy(null);
			context.setLevel(null);
			context.setMember(null);
			context.setCell(cell);

			context.setPosition(null);
			context.setColumnPosition(columnNode.getPosition());
			context.setRowPosition(rowNode.getPosition());

			if (columnNode.getAggregator() == null) {
				if (rowNode.getAggregator() != null) {
					context.setAggregator(rowNode.getAggregator());
					context.setAxis(Axis.ROWS);
				}
			} else if (rowNode.getAggregator() == null || columnNode.getAggregator().getMeasure() != null) {
				context.setAggregator(columnNode.getAggregator());
				context.setAxis(Axis.COLUMNS);
			} else if (rowNode.getAggregator().getMeasure() != null) {
				context.setAggregator(rowNode.getAggregator());
				context.setAxis(Axis.ROWS);
			}

			callback.startCell(context);
			callback.renderCommands(context, getCommands(context));
			callback.renderContent(context, getLabel(context), getValue(context));
			callback.endCell(context);

			context.setPosition(context.getRowPosition());

			for (AggregatorPosition position : AggregatorPosition.values()) {
				for (Aggregator aggregator : rowRoot.getReference().getAggregators(position)) {
					aggregate(context, rowNode, aggregator, position);
				}
			}

			context.setPosition(context.getColumnPosition());

			for (AggregatorPosition position : AggregatorPosition.values()) {
				for (Aggregator aggregator : columnRoot.getReference().getAggregators(position)) {
					aggregate(context, columnNode, aggregator, position);
				}
			}
		}

		context.setAggregator(null);
	}

	/**
	 * @param context
	 * @param node
	 * @param aggregator
	 * @param position
	 */
	protected void aggregate(TableRenderContext context, TableHeaderNode node, Aggregator aggregator,
			AggregatorPosition position) {
		Measure measure = aggregator.getMeasure();

		List<Member> members = aggregator.getMembers();

		if (context.getCell() == null && (measure == null || context.getAggregator() == null)) {
			return;
		}

		if (context.getAggregator() != null && (context.getAggregator().getAxis() == aggregator.getAxis())) {
			return;
		}

		MemberHierarchyCache cache = node.getReference().getMemberHierarchyCache();

		List<Member> positionMembers = context.getPosition().getMembers();

		OlapUtils utils = new OlapUtils(context.getModel().getCube());
		utils.setMemberHierarchyCache(cache);

		int index = 0;
		for (Member member : members) {
			if (positionMembers.size() <= index) {
				return;
			}

			Member positionMember = utils.wrapRaggedIfNecessary(positionMembers.get(index));

			if (!OlapUtils.equals(member, positionMember) && (member.getDepth() >= positionMember.getDepth()
					|| !cache.getAncestorMembers(positionMember).contains(member))) {
				return;
			}

			index++;
		}

		if (measure != null && !positionMembers.isEmpty()) {
			Member member = positionMembers.get(positionMembers.size() - 1);

			if (!measure.equals(member)) {
				return;
			}
		}

		TableHeaderNode parent = node;

		while (parent != null) {
			if (parent.getHierarchyDescendents() == 1 && parent.getMemberChildren() > 0) {
				switch (position) {
				case Grand:
					return;
				case Hierarchy:
					if (!members.contains(parent.getMember())) {
						return;
					}
					break;
				case Member:
					if (node == parent || members.lastIndexOf(parent.getMember()) == members.size() - 1) {
						return;
					}
					break;
				default:
					assert false;
				}
			}

			parent = (TableHeaderNode) parent.getParent();
		}

		aggregator.aggregate(context);
	}

	/**
	 * @param context
	 * @param columnRoot
	 * @param rowRoot
	 * @param callback
	 */
	protected void renderHeaderCorner(TableRenderContext context, TableHeaderNode columnRoot, TableHeaderNode rowRoot,
			TableRenderCallback callback) {
		int offset = 0;

		if (getShowDimensionTitle()) {
			offset = showParentMembers ? 2 : 1;
		}

		context.setAxis(null);

		context.setHierarchy(null);
		context.setLevel(null);
		context.setMember(null);
		context.setProperty(null);

		context.setCell(null);
		context.setCellType(FILL);

		context.setPosition(null);
		context.setColumnPosition(null);
		context.setRowPosition(null);

		boolean renderDimensionTitle = showDimensionTitle
				&& (context.getRowIndex() == context.getColumnHeaderCount() - offset);
		boolean renderLevelTitle = showDimensionTitle && showParentMembers
				&& (context.getRowIndex() == context.getColumnHeaderCount() - 1);

		if (context.getRowIndex() == 0 && !renderDimensionTitle && !renderLevelTitle) {
			context.setColSpan(context.getRowHeaderCount());
			context.setRowSpan(context.getColumnHeaderCount() - offset);

			callback.startCell(context);
			callback.renderCommands(context, getCommands(context));
			callback.renderContent(context, getLabel(context), getValue(context));
			callback.endCell(context);
		} else if (renderDimensionTitle) {
			final Map<Hierarchy, Integer> spans = new HashMap<Hierarchy, Integer>();
			final Map<Hierarchy, List<Property>> propertyMap = new HashMap<Hierarchy, List<Property>>();

			rowRoot.walkChildrenAtColIndex(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode headerNode = (TableHeaderNode) node;
					if (headerNode.getHierarchy() == null) {
						return TreeNodeCallback.CONTINUE;
					}

					Hierarchy hierarchy = headerNode.getHierarchy();

					if (headerNode.getProperty() == null) {
						Integer span = spans.get(hierarchy);
						if (span == null) {
							span = 0;
						}

						span += headerNode.getRowSpan();
						spans.put(headerNode.getHierarchy(), span);
					} else {
						List<Property> properties = propertyMap.get(hierarchy);
						if (properties == null) {
							properties = new ArrayList<Property>();
							propertyMap.put(hierarchy, properties);
						}

						properties.add(headerNode.getProperty());
					}

					return TreeNodeCallback.CONTINUE;
				}
			}, 0);

			context.setAxis(Axis.ROWS);
			context.setRowSpan(1);
			context.setCellType(TITLE);

			for (Hierarchy hierarchy : rowRoot.getReference().getHierarchies()) {
				Integer span = spans.get(hierarchy);
				if (span == null) {
					span = 1;
				}

				context.setColSpan(span);
				context.setHierarchy(hierarchy);

				callback.startCell(context);
				callback.renderCommands(context, getCommands(context));
				callback.renderContent(context, getLabel(context), getValue(context));
				callback.endCell(context);

				context.setColIndex(context.getColumnIndex() + span);

				List<Property> properties = propertyMap.get(hierarchy);
				if (properties != null) {
					for (Property property : properties) {
						context.setColSpan(1);
						context.setColIndex(context.getColumnIndex() + 1);
						context.setProperty(property);

						callback.startCell(context);
						callback.renderCommands(context, getCommands(context));
						callback.renderContent(context, getLabel(context), getValue(context));
						callback.endCell(context);
					}
				}
			}
		} else if (renderLevelTitle) {
			final Map<Integer, Level> levels = new HashMap<Integer, Level>();
			final Map<Integer, Property> properties = new HashMap<Integer, Property>();

			rowRoot.walkChildren(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode headerNode = (TableHeaderNode) node;
					int colIndex = headerNode.getRowIndex() - 1;

					if (headerNode.getMember() != null && !levels.containsKey(colIndex)) {
						levels.put(colIndex, headerNode.getMember().getLevel());
					}

					if (headerNode.getProperty() != null && !properties.containsKey(colIndex)) {
						properties.put(colIndex, headerNode.getProperty());
					}

					return TreeNodeCallback.CONTINUE;
				}
			});

			context.setAxis(Axis.ROWS);
			context.setColSpan(1);
			context.setRowSpan(1);
			context.setCellType(TITLE);

			for (int i = 0; i < context.getRowHeaderCount(); i++) {
				context.setColIndex(i);

				Level level = levels.get(i);

				if (level == null) {
					context.setHierarchy(null);
					context.setLevel(null);
				} else {
					context.setHierarchy(level.getHierarchy());
					context.setLevel(level);
				}

				context.setProperty(properties.get(i));

				callback.startCell(context);
				callback.renderCommands(context, getCommands(context));
				callback.renderContent(context, getLabel(context), getValue(context));
				callback.endCell(context);
			}
		}

		context.setHierarchy(null);
	}

	/**
	 * @param model
	 * @param axis
	 * @param cache
	 * @return
	 */
	protected TableHeaderNode createAxisTree(PivotModel model, Axis axis, MemberHierarchyCache cache) {
		List<CellSetAxis> axes = model.getCellSet().getAxes();

		if (axes.size() < 2) {
			return null;
		}

		CellSetAxis cellSetAxis = axes.get(axis.axisOrdinal());

		List<Position> positions = cellSetAxis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return null;
		}

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();

		List<Aggregator> aggregators = new ArrayList<Aggregator>();
		List<Aggregator> hierarchyAggregators = new ArrayList<Aggregator>();
		List<Aggregator> memberAggregators = new ArrayList<Aggregator>();

		Map<AggregatorPosition, List<Aggregator>> aggregatorMap = new HashMap<AggregatorPosition, List<Aggregator>>();
		aggregatorMap.put(AggregatorPosition.Grand, aggregators);
		aggregatorMap.put(AggregatorPosition.Hierarchy, hierarchyAggregators);
		aggregatorMap.put(AggregatorPosition.Member, memberAggregators);

		Map<Hierarchy, List<Level>> levelsMap = new HashMap<Hierarchy, List<Level>>();

		boolean containsMeasure = false;

		List<Member> firstMembers = positions.get(0).getMembers();

		int index = 0;
		for (Member member : firstMembers) {
			if (member instanceof Measure) {
				containsMeasure = true;
				break;
			}

			index++;
		}

		AggregatorFactory aggregatorFactory = getAggregatorFactory();

		List<String> aggregatorNames = null;
		List<String> hierarchyAggregatorNames = null;
		List<String> memberAggregatorNames = null;

		if (aggregatorFactory != null && (!containsMeasure || index == firstMembers.size() - 1)) {
			aggregatorNames = getAggregators(axis, AggregatorPosition.Grand);
			hierarchyAggregatorNames = getAggregators(axis, AggregatorPosition.Hierarchy);
			memberAggregatorNames = getAggregators(axis, AggregatorPosition.Member);
		}

		if (aggregatorNames == null) {
			aggregatorNames = Collections.emptyList();
		}

		if (hierarchyAggregatorNames == null) {
			hierarchyAggregatorNames = Collections.emptyList();
		}

		if (memberAggregatorNames == null) {
			memberAggregatorNames = Collections.emptyList();
		}

		TableAxisContext nodeContext = new TableAxisContext(model.getCube(), axis, hierarchies, levelsMap,
				aggregatorMap, cache, this);

		TableHeaderNode axisRoot = new TableHeaderNode(nodeContext);

		Set<Measure> grandTotalMeasures = new LinkedHashSet<Measure>();
		Set<Measure> totalMeasures = new LinkedHashSet<Measure>();

		Map<Hierarchy, List<AggregationTarget>> memberParentMap = new HashMap<Hierarchy, List<AggregationTarget>>();

		Position lastPosition = null;

		OlapUtils utils = new OlapUtils(model.getCube());
		utils.setMemberHierarchyCache(cache);

		for (Position position : positions) {
			TableHeaderNode lastChild = null;

			List<Member> members = position.getMembers();

			int memberCount = members.size();

			for (int i = memberCount - 1; i >= 0; i--) {
				Member member = members.get(i);

				if (member instanceof Measure) {
					Measure measure = (Measure) member;

					if (!totalMeasures.contains(measure)) {
						totalMeasures.add(measure);
					}

					if (!grandTotalMeasures.contains(measure)) {
						grandTotalMeasures.add(measure);
					}
				} else {
					member = utils.wrapRaggedIfNecessary(member);
				}

				if (hierarchies.size() < memberCount) {
					hierarchies.add(0, member.getHierarchy());
				}

				List<Level> levels = levelsMap.get(member.getHierarchy());

				if (levels == null) {
					levels = new ArrayList<Level>();
					levelsMap.put(member.getHierarchy(), levels);
				}

				if (!levels.contains(member.getLevel())) {
					levels.add(0, member.getLevel());
					Collections.sort(levels, levelComparator);
				}

				TableHeaderNode childNode = new TableHeaderNode(nodeContext);

				childNode.setMember(member);
				childNode.setHierarchy(member.getHierarchy());
				childNode.setPosition(position);

				if (lastChild != null) {
					childNode.addChild(lastChild);
				}

				lastChild = childNode;

				if (!hierarchyAggregatorNames.isEmpty() && lastPosition != null) {
					int start = memberCount - 1;

					if (containsMeasure) {
						start--;
					}

					if (i < start) {
						Member lastMember = lastPosition.getMembers().get(i);

						if (OlapUtils.equals(lastMember.getHierarchy(), member.getHierarchy())
								&& !OlapUtils.equals(lastMember, member)
								|| !OlapUtils.equals(position, lastPosition, i)) {
							for (String aggregatorName : hierarchyAggregatorNames) {
								createAggregators(aggregatorName, nodeContext, hierarchyAggregators, axisRoot, null,
										lastPosition.getMembers().subList(0, i + 1), totalMeasures);
							}
						}
					}
				}

				if (!memberAggregatorNames.isEmpty()) {
					List<AggregationTarget> memberParents = memberParentMap.get(member.getHierarchy());

					if (memberParents == null) {
						memberParents = new ArrayList<AggregationTarget>();
						memberParentMap.put(member.getHierarchy(), memberParents);
					}

					AggregationTarget lastSibling = null;

					if (!memberParents.isEmpty()) {
						lastSibling = memberParents.get(memberParents.size() - 1);
					}

					Member parentMember = utils.getTopLevelRaggedMember(member);

					if (parentMember != null) {
						if (lastSibling == null
								|| cache.getAncestorMembers(parentMember).contains(lastSibling.getParent())) {
							memberParents.add(new AggregationTarget(parentMember, member.getLevel()));
						} else if (!OlapUtils.equals(parentMember, lastSibling.getParent())) {
							while (!memberParents.isEmpty()) {
								int lastIndex = memberParents.size() - 1;

								AggregationTarget lastParent = memberParents.get(lastIndex);

								if (OlapUtils.equals(lastParent.getParent(), parentMember)) {
									break;
								}

								memberParents.remove(lastIndex);

								List<Member> path = new ArrayList<Member>(lastPosition.getMembers().subList(0, i));
								path.add(lastParent.getParent());

								Level parentLevel = lastParent.getParent().getLevel();
								if (!levels.contains(parentLevel)) {
									levels.add(0, parentLevel);
									Collections.sort(levels, levelComparator);
								}

								for (String aggregatorName : memberAggregatorNames) {
									createAggregators(aggregatorName, nodeContext, memberAggregators, axisRoot,
											lastParent.getLevel(), path, totalMeasures);
								}
							}
						}
					}

					if (lastPosition != null && !OlapUtils.equals(position, lastPosition, i)) {
						Hierarchy hierarchy = nodeContext.getHierarchies().get(i);

						Level rootLevel = nodeContext.getLevels(hierarchy).get(0);

						for (AggregationTarget target : memberParents) {
							Member memberParent = target.getParent();

							if (memberParent.getLevel().getDepth() < rootLevel.getDepth()) {
								continue;
							}

							List<Member> path = new ArrayList<Member>(lastPosition.getMembers().subList(0, i));
							path.add(memberParent);

							for (String aggregatorName : memberAggregatorNames) {
								createAggregators(aggregatorName, nodeContext, memberAggregators, axisRoot,
										target.getLevel(), path, totalMeasures);
							}
						}

						memberParents.clear();
					}
				}
			}

			if (lastChild != null) {
				axisRoot.addChild(lastChild);
			}

			lastPosition = position;
		}

		if (lastPosition != null) {
			int memberCount = lastPosition.getMembers().size();

			int start = memberCount - 1;

			if (containsMeasure) {
				start--;
			}

			for (int i = start; i >= 0; i--) {
				if (!memberAggregatorNames.isEmpty()) {
					Hierarchy hierarchy = nodeContext.getHierarchies().get(i);

					Level rootLevel = nodeContext.getLevels(hierarchy).get(0);

					List<AggregationTarget> memberParents = memberParentMap.get(hierarchy);

					for (AggregationTarget target : memberParents) {
						Member member = target.getParent();

						if (member.getLevel().getDepth() < rootLevel.getDepth()) {
							continue;
						}

						List<Member> path = new ArrayList<Member>(lastPosition.getMembers().subList(0, i));
						path.add(member);

						for (String aggregatorName : memberAggregatorNames) {
							createAggregators(aggregatorName, nodeContext, memberAggregators, axisRoot,
									target.getLevel(), path, totalMeasures);
						}
					}
				}

				if (!hierarchyAggregatorNames.isEmpty()) {
					for (String aggregatorName : hierarchyAggregatorNames) {
						createAggregators(aggregatorName, nodeContext, hierarchyAggregators, axisRoot, null,
								lastPosition.getMembers().subList(0, i), totalMeasures);
					}
				}
			}

			if (!aggregatorNames.isEmpty()) {
				List<Member> members = Collections.emptyList();

				for (String aggregatorName : aggregatorNames) {
					createAggregators(aggregatorName, nodeContext, aggregators, axisRoot, null, members,
							grandTotalMeasures);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Original axis tree root for {}", axis);

			axisRoot.walkTree(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					String label = node.toString();
					logger.trace(StringUtils.leftPad(label, node.getLevel() + label.length(), ' '));

					return CONTINUE;
				}
			});
		}

		return axisRoot;
	}

	/**
	 * @param model
	 * @param cache
	 * @return
	 */
	protected List<TableHeaderNode> createFilterAxisTrees(PivotModel model, MemberHierarchyCache cache) {
		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);

		List<Hierarchy> hierarchies = transform.getHierarchies();

		List<TableHeaderNode> nodes = new ArrayList<TableHeaderNode>(hierarchies.size());

		for (Hierarchy hierarchy : hierarchies) {
			nodes.add(createFilterAxisTree(model, hierarchy, cache));
		}

		return nodes;
	}

	/**
	 * @param model
	 * @param cache
	 * @return
	 */
	protected TableHeaderNode createFilterAxisTree(PivotModel model, Hierarchy hierarchy, MemberHierarchyCache cache) {
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(1);
		hierarchies.add(hierarchy);

		Map<Hierarchy, List<Level>> levelsMap = new HashMap<Hierarchy, List<Level>>(1);

		List<Level> levels = new ArrayList<Level>(hierarchy.getLevels().size());
		levelsMap.put(hierarchy, levels);

		TableAxisContext nodeContext = new TableAxisContext(model.getCube(), Axis.FILTER, hierarchies, levelsMap, null,
				cache, this);

		TableHeaderNode axisRoot = new TableHeaderNode(nodeContext);
		axisRoot.setHierarchy(hierarchy);

		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);
		List<Member> members = transform.getSlicer(hierarchy);

		MemberSelection selection = new MemberSelection(model.getCube());
		selection.setMemberHierarchyCache(cache);
		selection.addMembers(members);

		for (Member member : selection.getMembers()) {
			TableHeaderNode childNode = new TableHeaderNode(nodeContext);

			childNode.setMember(member);
			childNode.setHierarchy(hierarchy);

			axisRoot.addChild(childNode);

			if (!levels.contains(member.getLevel())) {
				levels.add(0, member.getLevel());
			}
		}

		Collections.sort(levels, levelComparator);

		return axisRoot;
	}

	/**
	 * @param aggregatorName
	 * @param context
	 * @param aggregators
	 * @param axisRoot
	 * @param level
	 * @param members
	 * @param measures
	 */
	private void createAggregators(String aggregatorName, TableAxisContext context, List<Aggregator> aggregators,
			TableHeaderNode axisRoot, Level level, List<Member> members, Set<Measure> measures) {
		AggregatorFactory factory = getAggregatorFactory();

		if (measures.isEmpty()) {
			Aggregator aggregator = factory.createAggregator(aggregatorName, context.getAxis(), members, level, null);
			if (aggregator != null) {
				aggregators.add(aggregator);

				TableHeaderNode aggNode = createAggregationNode(context, aggregator);
				axisRoot.addChild(aggNode);
			}
		} else {
			for (Measure measure : measures) {
				Aggregator aggregator = factory.createAggregator(aggregatorName, context.getAxis(), members, level,
						measure);

				if (aggregator != null) {
					aggregators.add(aggregator);

					TableHeaderNode aggNode = createAggregationNode(context, aggregator);
					axisRoot.addChild(aggNode);
				}
			}
		}
	}

	/**
	 * @param nodeContext
	 * @param aggregator
	 * @return
	 */
	protected TableHeaderNode createAggregationNode(TableAxisContext nodeContext, Aggregator aggregator) {
		TableHeaderNode result = null;

		TableHeaderNode parent = null;

		List<Member> members = new ArrayList<Member>(aggregator.getMembers());

		Position position = new AggregatePosition(members);

		for (Member member : aggregator.getMembers()) {
			TableHeaderNode node = new TableHeaderNode(nodeContext);

			node.setAggregation(true);
			node.setMember(member);
			node.setPosition(position);
			node.setHierarchy(member.getHierarchy());

			if (result == null) {
				result = node;
			}

			if (parent != null) {
				parent.addChild(node);
			}

			parent = node;
		}

		if (parent != null && aggregator.getLevel() != null && !getShowParentMembers()) {
			parent.setAggregator(aggregator);
		}

		int index = Math.min(nodeContext.getHierarchies().size() - 1, members.size());

		Hierarchy hierarchy;

		if (aggregator.getLevel() == null) {
			hierarchy = nodeContext.getHierarchies().get(index);
		} else {
			hierarchy = aggregator.getLevel().getHierarchy();
		}

		if (aggregator.getLevel() == null || getShowParentMembers()) {
			TableHeaderNode node = new TableHeaderNode(nodeContext);
			node.setAggregation(true);
			node.setAggregator(aggregator);
			node.setPosition(position);
			node.setHierarchy(hierarchy);

			if (parent != null) {
				parent.addChild(node);
			}

			if (result == null) {
				result = node;
			}

			parent = node;
		}

		Measure measure = aggregator.getMeasure();

		if (measure != null) {
			TableHeaderNode measureNode = new TableHeaderNode(nodeContext);

			measureNode.setAggregation(true);
			measureNode.setAggregator(aggregator);
			measureNode.setPosition(position);
			measureNode.setMember(measure);
			measureNode.setHierarchy(measure.getHierarchy());

			parent.addChild(measureNode);

			members.add(measure);
		}

		return result;
	}

	/**
	 * @param model
	 * @param axis
	 * @param node
	 */
	protected void configureAxisTree(PivotModel model, Axis axis, TableHeaderNode node) {
		if (getShowDimensionTitle() && axis == Axis.COLUMNS) {
			node.addHierarhcyHeaders();
		}

		if (getShowParentMembers() || axis == Axis.FILTER) {
			node.addParentMemberHeaders();
		}

		if (!getHideSpans() || axis == Axis.FILTER) {
			node.mergeChildren();
		}

		if (getPropertyCollector() != null) {
			node.addMemberProperties();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Configured axis tree root for {}", axis);

			node.walkTree(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					String label = node.toString();
					logger.debug(StringUtils.leftPad(label, node.getLevel() + label.length(), ' '));

					return CONTINUE;
				}
			});
		}
	}

	/**
	 * @param model
	 * @param axis
	 * @param node
	 */
	protected void invalidateAxisTree(PivotModel model, Axis axis, TableHeaderNode node) {
		node.walkChildren(new TreeNodeCallback<TableAxisContext>() {

			@Override
			public int handleTreeNode(TreeNode<TableAxisContext> node) {
				TableHeaderNode headerNode = (TableHeaderNode) node;
				headerNode.clearCache();
				return TreeNodeCallback.CONTINUE;
			}
		});

		node.getReference().getRowSpanCache().clear();
	}

	static class AggregationTarget {

		private Member parent;

		private Level level;

		/**
		 * @param parent
		 * @param level
		 */
		AggregationTarget(Member parent, Level level) {
			this.parent = parent;
			this.level = level;
		}

		/**
		 * @return the parent
		 */
		public Member getParent() {
			return parent;
		}

		/**
		 * @return the level
		 */
		public Level getLevel() {
			return level;
		}
	}

	static class AggregatePosition implements Position {

		private List<Member> members;

		/**
		 * @param members
		 */
		AggregatePosition(List<Member> members) {
			this.members = members;
		}

		/**
		 * @see org.olap4j.Position#getMembers()
		 */
		@Override
		public List<Member> getMembers() {
			return members;
		}

		/**
		 * @see org.olap4j.Position#getOrdinal()
		 */
		@Override
		public int getOrdinal() {
			return -1;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return 31 + members.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj == null) {
				return false;
			} else if (getClass() != obj.getClass()) {
				return false;
			}

			AggregatePosition other = (AggregatePosition) obj;
			return members.equals(other.members);
		}
	}

	/**
	 * @param context
	 * @param filterRoot
	 * @param callback
	 */
	protected void renderFilter(final TableRenderContext context, TableHeaderNode filterRoot,
			final TableRenderCallback callback) {
		Hierarchy hierarchy = filterRoot.getHierarchy();

		context.setAxis(Axis.FILTER);
		context.setHierarchy(hierarchy);
		context.setLevel(null);
		context.setMember(null);
		context.setCell(null);
		context.setAggregator(null);
		context.setPosition(null);
		context.setProperty(null);

		callback.startTable(context);

		final Map<Integer, Level> levels = new HashMap<Integer, Level>();

		filterRoot.walkChildren(new TreeNodeCallback<TableAxisContext>() {

			@Override
			public int handleTreeNode(TreeNode<TableAxisContext> node) {
				TableHeaderNode headerNode = (TableHeaderNode) node;
				levels.put(node.getLevel(), headerNode.getMember().getLevel());

				return TreeNodeCallback.CONTINUE;
			}
		});

		int rows = levels.size();
		int columns = filterRoot.getWidth() + 1;

		context.setColIndex(0);
		context.setRowIndex(0);

		context.setCellType(TITLE);
		context.setRenderPropertyCategory(TITLE);

		context.setColSpan(columns);
		context.setRowSpan(1);

		callback.startHeader(context);
		callback.startRow(context);

		callback.startCell(context);
		callback.renderContent(context, getLabel(context), getValue(context));
		callback.endCell(context);

		callback.endRow(context);
		callback.endHeader(context);

		callback.startBody(context);

		for (int rowIndex = 1; rowIndex <= rows; rowIndex++) {
			context.setCellType(TITLE);
			context.setRenderPropertyCategory(TITLE);

			context.setRowIndex(rowIndex);
			context.setColIndex(0);

			context.setColSpan(1);
			context.setRowSpan(1);

			callback.startRow(context);

			context.setLevel(levels.get(rowIndex));

			callback.startCell(context);
			callback.renderContent(context, getLabel(context), getValue(context));
			callback.endCell(context);

			context.setCellType(LABEL);
			context.setRenderPropertyCategory(LABEL);

			filterRoot.walkChildrenAtRowIndex(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					TableHeaderNode headerNode = (TableHeaderNode) node;

					context.setColIndex(headerNode.getColIndex() + 1);
					context.setColSpan(headerNode.getColSpan());
					context.setRowSpan(headerNode.getRowSpan());

					context.setLevel(headerNode.getMember().getLevel());
					context.setMember(headerNode.getMember());

					callback.startCell(context);
					callback.renderContent(context, getLabel(context), getValue(context));
					callback.endCell(context);

					return TreeNodeCallback.CONTINUE;
				}
			}, rowIndex);

			callback.endRow(context);
		}

		callback.endBody(context);
		callback.endTable(context);
	}
}
