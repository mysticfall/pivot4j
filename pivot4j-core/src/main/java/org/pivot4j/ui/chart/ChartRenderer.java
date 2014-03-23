/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.chart;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.AbstractPivotRenderer;
import org.pivot4j.util.OlapUtils;
import org.pivot4j.util.TreeNode;
import org.pivot4j.util.TreeNodeCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChartRenderer extends
		AbstractPivotRenderer<ChartRenderContext, ChartRenderCallback> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String DEFAULT_MEMBER_SEPARATOR = " / ";

	private static final String UNUSED_AXIS = "none";

	private Axis pageAxis = Axis.COLUMNS;

	private Axis chartAxis = Axis.COLUMNS;

	private Axis seriesAxis = Axis.ROWS;

	private Axis plotAxis = Axis.ROWS;

	private String memberSeparator = DEFAULT_MEMBER_SEPARATOR;

	/**
	 * @return the memberSeparator
	 */
	public String getMemberSeparator() {
		return memberSeparator;
	}

	/**
	 * @param memberSeparator
	 *            the memberSeparator to set
	 */
	public void setMemberSeparator(String memberSeparator) {
		this.memberSeparator = memberSeparator;
	}

	/**
	 * @return the pageAxis
	 */
	public Axis getPageAxis() {
		return pageAxis;
	}

	/**
	 * @param pageAxis
	 *            the pageAxis to set
	 */
	public void setPageAxis(Axis pageAxis) {
		this.pageAxis = pageAxis;
	}

	/**
	 * @return the chartAxis
	 */
	public Axis getChartAxis() {
		return chartAxis;
	}

	/**
	 * @param chartAxis
	 *            the chartAxis to set
	 */
	public void setChartAxis(Axis chartAxis) {
		this.chartAxis = chartAxis;
	}

	/**
	 * @return the seriesAxis
	 */
	public Axis getSeriesAxis() {
		return seriesAxis;
	}

	/**
	 * @param seriesAxis
	 *            the seriesAxis to set
	 */
	public void setSeriesAxis(Axis seriesAxis) {
		this.seriesAxis = seriesAxis;
	}

	/**
	 * @return the plotAxis
	 */
	public Axis getPlotAxis() {
		return plotAxis;
	}

	/**
	 * @param plotAxis
	 *            the plotAxis to set
	 */
	public void setPlotAxis(Axis plotAxis) {
		this.plotAxis = plotAxis;
	}

	/**
	 * @see org.pivot4j.ui.PivotRenderer#render(org.pivot4j.PivotModel,
	 *      org.pivot4j.ui.RenderCallback)
	 */
	@Override
	public void render(final PivotModel model,
			final ChartRenderCallback callback) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (callback == null) {
			throw new NullArgumentException("callback");
		}

		CellSet cellSet = model.getCellSet();

		if (cellSet == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cell set is empty.");
			}

			return;
		}

		List<CellSetAxis> axes = cellSet.getAxes();
		if (axes.size() != 2) {
			if (logger.isWarnEnabled()) {
				logger.warn("Not enough axes.");
			}

			return;
		}

		CellSetAxis columnAxis = axes.get(Axis.COLUMNS.axisOrdinal());
		CellSetAxis rowAxis = axes.get(Axis.ROWS.axisOrdinal());

		if (columnAxis == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Column axis is missing.");
			}

			return;
		}

		if (rowAxis == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Row axis is missing.");
			}

			return;
		}

		List<Position> columnPositions = columnAxis.getPositions();
		List<Position> rowPositions = rowAxis.getPositions();

		PlotNode columnRoot = new PlotNode();

		int maxColumnLevel = 0;

		for (Position columnPosition : columnPositions) {
			PlotNode node = columnRoot.addPositionNodes(columnPosition);

			maxColumnLevel = Math.max(maxColumnLevel, node.getLevel());
		}

		final PlotNode rowRoot = new PlotNode();

		int maxRowLevel = 0;

		for (Position rowPosition : rowPositions) {
			PlotNode node = rowRoot.addPositionNodes(rowPosition);

			maxRowLevel = Math.max(maxRowLevel, node.getLevel());
		}

		final ChartRenderContext context = new ChartRenderContext(model, this);

		callback.startRender(context);

		PartRenderer pagePart = new PagePartRenderer(pageAxis, callback);
		PartRenderer chartPart = new ChartPartRenderer(chartAxis, callback);
		PartRenderer seriesPart = new SeriesPartRenderer(seriesAxis, callback);
		PartRenderer plotPart = new PlotPartRenderer(plotAxis, callback);

		pagePart.setChildRenderer(chartPart);
		chartPart.setChildRenderer(seriesPart);
		seriesPart.setChildRenderer(plotPart);

		Map<Axis, PlotNode> nodeContext = new HashMap<Axis, PlotNode>(2);
		nodeContext.put(Axis.ROWS, rowRoot);
		nodeContext.put(Axis.COLUMNS, columnRoot);

		pagePart.render(context, nodeContext);

		pagePart.setDryRun(false);
		pagePart.render(context, nodeContext);

		context.setAxis(null);
		context.setPosition(null);
		context.setColumnPosition(null);
		context.setRowPosition(null);
		context.setHierarchy(null);
		context.setMember(null);
		context.setCell(null);

		context.setPagePath(Collections.<Member> emptyList());
		context.setChartPath(Collections.<Member> emptyList());
		context.setSeriesPath(Collections.<Member> emptyList());
		context.setPlotPath(Collections.<Member> emptyList());

		callback.endRender(context);
	}

	/**
	 * @param members
	 * @return
	 */
	protected String getLabel(List<Member> members) {
		int size = members.size();

		if (size == 0) {
			return null;
		} else if (size == 1) {
			return getLabel(members.get(0));
		}

		StringBuilder builder = new StringBuilder();

		boolean first = true;
		for (Member member : members) {
			if (first) {
				first = false;
			} else {
				builder.append(memberSeparator);
			}

			builder.append(getLabel(member));
		}

		return builder.toString();
	}

	/**
	 * @param member
	 * @return
	 */
	protected String getLabel(Member member) {
		return member.getCaption();
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#getLabel(org.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getLabel(ChartRenderContext context) {
		return getLabel(context.getPlotPath());
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#getValue(org.pivot4j.ui.RenderContext)
	 */
	@Override
	protected Double getValue(ChartRenderContext context) {
		Double value = null;

		Cell cell = context.getCell();

		if (cell != null && !cell.isEmpty()) {
			try {
				value = cell.getDoubleValue();
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		}

		return value;
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[6];

		int index = 0;

		states[index++] = super.saveState();

		if (pageAxis == null) {
			states[index++] = -1;
		} else {
			states[index++] = pageAxis.axisOrdinal();
		}

		if (chartAxis == null) {
			states[index++] = -1;
		} else {
			states[index++] = chartAxis.axisOrdinal();
		}

		if (seriesAxis == null) {
			states[index++] = -1;
		} else {
			states[index++] = seriesAxis.axisOrdinal();
		}

		if (plotAxis == null) {
			states[index++] = -1;
		} else {
			states[index++] = plotAxis.axisOrdinal();
		}

		states[index++] = memberSeparator;

		return states;
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		int index = 0;

		super.restoreState(states[index++]);

		int ordinal = (Integer) states[index++];
		if (ordinal > -1) {
			this.pageAxis = Axis.Factory.forOrdinal(ordinal);
		} else {
			this.pageAxis = null;
		}

		ordinal = (Integer) states[index++];
		if (ordinal > -1) {
			this.chartAxis = Axis.Factory.forOrdinal(ordinal);
		} else {
			this.chartAxis = null;
		}

		ordinal = (Integer) states[index++];
		if (ordinal > -1) {
			this.seriesAxis = Axis.Factory.forOrdinal(ordinal);
		} else {
			this.seriesAxis = null;
		}

		ordinal = (Integer) states[index++];
		if (ordinal > -1) {
			this.plotAxis = Axis.Factory.forOrdinal(ordinal);
		} else {
			this.plotAxis = null;
		}

		this.memberSeparator = (String) states[index++];
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#saveSettings(org.apache.commons.
	 *      configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		configuration.addProperty("mappings.page[@axis]",
				pageAxis == null ? UNUSED_AXIS : pageAxis.name());
		configuration.addProperty("mappings.chart[@axis]",
				chartAxis == null ? UNUSED_AXIS : chartAxis.name());
		configuration.addProperty("mappings.series[@axis]",
				seriesAxis == null ? UNUSED_AXIS : seriesAxis.name());
		configuration.addProperty("mappings.plot[@axis]",
				plotAxis == null ? UNUSED_AXIS : plotAxis.name());

		if (!DEFAULT_MEMBER_SEPARATOR.equals(memberSeparator)) {
			configuration.addProperty("member-separator", memberSeparator);
		}
	}

	/**
	 * @see org.pivot4j.ui.AbstractPivotRenderer#restoreSettings(org.apache.commons
	 *      .configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.pageAxis = readAxisMapping("page", configuration, Axis.COLUMNS);
		this.chartAxis = readAxisMapping("chart", configuration, Axis.COLUMNS);
		this.seriesAxis = readAxisMapping("series", configuration, Axis.ROWS);
		this.plotAxis = readAxisMapping("plot", configuration, Axis.ROWS);

		this.memberSeparator = configuration.getString("member-separator",
				DEFAULT_MEMBER_SEPARATOR);
	}

	/**
	 * @param name
	 * @param configuration
	 * @param defaultValue
	 * @return
	 */
	private Axis readAxisMapping(String name,
			HierarchicalConfiguration configuration, Axis defaultValue) {
		Axis axis = defaultValue;

		String axisName = configuration.getString("mappings." + name
				+ "[@axis]");

		if (UNUSED_AXIS.equals(axisName)) {
			axis = null;
		} else if (axisName != null) {
			axis = Axis.Factory.forOrdinal(Axis.Standard.valueOf(axisName)
					.axisOrdinal());
		}

		return axis;
	}

	static class PlotNode extends TreeNode<Member> {

		private Position position;

		private Cell cell;

		public PlotNode() {
		}

		public PlotNode(Member member, Position position) {
			super(member);

			this.position = position;
		}

		/**
		 * @param position
		 * @return
		 */
		public PlotNode addPositionNodes(Position position) {
			PlotNode targetNode = null;

			int depth = getLevel() - 1;

			if (depth > -1) {
				Member member = position.getMembers().get(depth);

				if (!OlapUtils.equals(member, getReference())) {
					return null;
				} else if (depth == position.getMembers().size() - 1) {
					return this;
				}
			}

			for (TreeNode<Member> child : getChildren()) {
				PlotNode node = (PlotNode) child;

				targetNode = node.addPositionNodes(position);

				if (targetNode != null) {
					break;
				}
			}

			if (targetNode == null) {
				PlotNode child = new PlotNode(position.getMembers().get(
						depth + 1), position);
				addChild(child);

				targetNode = child.addPositionNodes(position);
			}

			return targetNode;
		}

		/**
		 * @return the position
		 */
		public Position getPosition() {
			return position;
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

		public Double getValue() {
			Double value = 0d;

			if (cell != null && !cell.isEmpty()) {
				try {
					value = cell.getDoubleValue();
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}

			for (TreeNode<Member> child : getChildren()) {
				PlotNode node = (PlotNode) child;

				Double nodeValue = node.getValue();

				if (nodeValue != null) {
					value += nodeValue;
				}
			}

			return value;
		}

		public List<Member> getPath() {
			PlotNode parent = (PlotNode) getParent();

			if (parent == null) {
				return new LinkedList<Member>();
			}

			List<Member> path = parent.getPath();
			path.add(getReference());

			return path;
		}

		/**
		 * @param callbackHandler
		 * @param level
		 * @return
		 */
		public int walkChildrenAtLevel(
				TreeNodeCallback<Member> callbackHandler, int level) {
			int code;

			int nodeLevel = getLevel();

			if (nodeLevel == level) {
				callbackHandler.handleTreeNode(this);
				code = TreeNodeCallback.CONTINUE_SIBLING;
			} else if (nodeLevel < level) {
				for (TreeNode<Member> child : getChildren()) {
					PlotNode childNode = (PlotNode) child;

					childNode.walkChildrenAtLevel(callbackHandler, level);
				}

				code = TreeNodeCallback.CONTINUE_SIBLING;
			} else {
				code = TreeNodeCallback.CONTINUE_PARENT;
			}

			return code;
		}
	}

	static abstract class PartRenderer {

		private Axis axis;

		private ChartRenderCallback callback;

		private PartRenderer childRenderer;

		private boolean dryRun = true;

		private int renderIndex = 0;

		private int renderCount = 0;

		/**
		 * @param axis
		 * @param callback
		 */
		PartRenderer(Axis axis, ChartRenderCallback callback) {
			this.axis = axis;
			this.callback = callback;
		}

		/**
		 * @return the axis
		 */
		public Axis getAxis() {
			return axis;
		}

		/**
		 * @return the callback
		 */
		public ChartRenderCallback getCallback() {
			return callback;
		}

		/**
		 * @return the childRenderer
		 */
		public PartRenderer getChildRenderer() {
			return childRenderer;
		}

		/**
		 * @param childRenderer
		 *            the childRenderer to set
		 */
		public void setChildRenderer(PartRenderer childRenderer) {
			this.childRenderer = childRenderer;
		}

		/**
		 * @return the dryRun
		 */
		public boolean isDryRun() {
			return dryRun;
		}

		/**
		 * @param dryRun
		 *            the dryRun to set
		 */
		public void setDryRun(boolean dryRun) {
			this.dryRun = dryRun;

			if (childRenderer != null) {
				childRenderer.setDryRun(dryRun);
			}
		}

		/**
		 * @return the renderIndex
		 */
		public int getRenderIndex() {
			return renderIndex;
		}

		/**
		 * @return the renderCount
		 */
		public int getRenderCount() {
			return renderCount;
		}

		public void reset() {
			this.renderIndex = 0;
			this.renderCount = 0;
		}

		/**
		 * @param axis
		 * @return
		 */
		protected int getRendererCount(Axis axis) {
			int count = 0;

			if (OlapUtils.equals(axis, this.axis)) {
				count++;
			}

			if (childRenderer != null) {
				count += childRenderer.getRendererCount(axis);
			}

			return count;
		}

		/**
		 * @param context
		 * @param nodeContext
		 */
		public void render(final ChartRenderContext context,
				final Map<Axis, PlotNode> nodeContext) {
			resetContext(context);

			context.setAxis(axis);

			PlotNode contextNode = null;

			if (axis != null) {
				contextNode = nodeContext.get(axis);
			}

			int level = 1;
			boolean contextAvailable;

			if (contextNode == null) {
				contextAvailable = false;
			} else {
				int count = getRendererCount(axis);
				int maxDepth = contextNode.getMaxDescendantLevel();

				level = maxDepth - count + 1;
				contextAvailable = (maxDepth >= count);
			}

			if (contextAvailable) {
				TreeNodeCallback<Member> handler = new TreeNodeCallback<Member>() {

					@Override
					public int handleTreeNode(TreeNode<Member> node) {
						PlotNode plotNode = (PlotNode) node;

						PlotNode originalContext = nodeContext.get(axis);

						try {
							nodeContext.put(axis, plotNode);

							updateContext(context, nodeContext, plotNode);
							renderStart(context, nodeContext);

							renderContent(context, nodeContext);

							updateContext(context, nodeContext, plotNode);
							renderEnd(context, nodeContext);

							updateContext(context, nodeContext, null);
						} finally {
							nodeContext.put(axis, originalContext);
						}

						return CONTINUE;
					}
				};

				if (contextNode != null) {
					contextNode.walkChildrenAtLevel(handler, level);
				}
			} else {
				updateContext(context, nodeContext, null);
				renderStart(context, nodeContext);

				renderContent(context, nodeContext);

				updateContext(context, nodeContext, null);
				renderEnd(context, nodeContext);
			}

			context.setAxis(null);
		}

		/**
		 * @param context
		 * @param nodeContext
		 * @param contextNode
		 */
		protected void updateContext(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext, PlotNode contextNode) {
			if (getAxis() == null) {
				return;
			}

			Position position = null;

			if (contextNode != null) {
				position = contextNode.getPosition();
			}

			if (OlapUtils.equals(getAxis(), Axis.COLUMNS)) {
				context.setColumnPosition(position);
			} else if (OlapUtils.equals(getAxis(), Axis.ROWS)) {
				context.setRowPosition(position);
			}
		}

		/**
		 * @param context
		 */
		protected void resetContext(ChartRenderContext context) {
			this.renderIndex = 0;
		}

		/**
		 * @param context
		 * @param nodeContext
		 */
		protected void renderStart(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			if (childRenderer != null) {
				childRenderer.resetContext(context);
			}
		}

		/**
		 * @param context
		 * @param nodeContext
		 */
		protected void renderContent(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			PartRenderer child = getChildRenderer();

			if (child != null) {
				child.render(context, nodeContext);
			}
		}

		/**
		 * @param context
		 * @param nodeContext
		 */
		protected void renderEnd(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			this.renderIndex++;
			this.renderCount = Math.max(renderCount, renderIndex);
		}
	}

	class PagePartRenderer extends PartRenderer {

		/**
		 * @param axis
		 * @param callback
		 */
		PagePartRenderer(Axis axis, ChartRenderCallback callback) {
			super(axis, callback);
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderStart(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderStart(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderStart(context, nodeContext);

			if (!isDryRun()) {
				getCallback().startPage(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderEnd(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderEnd(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderEnd(context, nodeContext);

			if (!isDryRun()) {
				getCallback().endPage(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#updateContext(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map, org.pivot4j.ui.chart.ChartRenderer.PlotNode)
		 */
		@Override
		protected void updateContext(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext, PlotNode contextNode) {
			super.updateContext(context, nodeContext, contextNode);

			context.setPageIndex(getRenderIndex());

			if (isDryRun()) {
				context.setPageCount(getRenderCount());
				return;
			}

			if (contextNode == null) {
				context.setPagePath(Collections.<Member> emptyList());
			} else {
				context.setPagePath(contextNode.getPath());
			}
		}
	}

	class ChartPartRenderer extends PartRenderer {

		/**
		 * @param axis
		 * @param callback
		 */
		ChartPartRenderer(Axis axis, ChartRenderCallback callback) {
			super(axis, callback);
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderStart(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderStart(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderStart(context, nodeContext);

			if (!isDryRun()) {
				getCallback().startChart(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderEnd(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderEnd(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderEnd(context, nodeContext);

			if (!isDryRun()) {
				getCallback().endChart(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#updateContext(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map, org.pivot4j.ui.chart.ChartRenderer.PlotNode)
		 */
		@Override
		protected void updateContext(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext, PlotNode contextNode) {
			super.updateContext(context, nodeContext, contextNode);

			context.setChartIndex(getRenderIndex());

			if (isDryRun()) {
				context.setChartCount(getRenderCount());
				return;
			}

			if (contextNode == null) {
				context.setChartPath(Collections.<Member> emptyList());
			} else {
				List<Member> path = new LinkedList<Member>(
						contextNode.getPath());

				for (Member member : context.getPagePath()) {
					path.remove(member);
				}

				context.setChartPath(path);
			}
		}
	}

	class SeriesPartRenderer extends PartRenderer {

		/**
		 * @param axis
		 * @param callback
		 */
		SeriesPartRenderer(Axis axis, ChartRenderCallback callback) {
			super(axis, callback);
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderStart(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderStart(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderStart(context, nodeContext);

			if (!isDryRun()) {
				getCallback().startSeries(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderEnd(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderEnd(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderEnd(context, nodeContext);

			if (!isDryRun()) {
				getCallback().endSeries(context);
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#updateContext(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map, org.pivot4j.ui.chart.ChartRenderer.PlotNode)
		 */
		@Override
		protected void updateContext(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext, PlotNode contextNode) {
			super.updateContext(context, nodeContext, contextNode);

			context.setSeriesIndex(getRenderIndex());

			if (isDryRun()) {
				context.setSeriesCount(getRenderCount());
				return;
			}

			if (contextNode == null) {
				context.setSeriesPath(Collections.<Member> emptyList());
			} else {
				List<Member> path = new LinkedList<Member>(
						contextNode.getPath());

				for (Member member : context.getPagePath()) {
					path.remove(member);
				}

				for (Member member : context.getChartPath()) {
					path.remove(member);
				}

				context.setSeriesPath(path);
			}
		}
	}

	class PlotPartRenderer extends PartRenderer {

		/**
		 * @param axis
		 * @param callback
		 */
		PlotPartRenderer(Axis axis, ChartRenderCallback callback) {
			super(axis, callback);
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#renderContent(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map)
		 */
		@Override
		protected void renderContent(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext) {
			super.renderContent(context, nodeContext);

			if (!isDryRun()) {
				String label = getLabel(context.getPlotPath());

				ChartRenderCallback callback = getCallback();

				callback.renderCommands(context, getCommands(context));
				callback.renderContent(context, label, getValue(context));
			}
		}

		/**
		 * @see org.pivot4j.ui.chart.ChartRenderer.PartRenderer#updateContext(org.pivot4j.ui.chart.ChartRenderContext,
		 *      java.util.Map, org.pivot4j.ui.chart.ChartRenderer.PlotNode)
		 */
		@Override
		protected void updateContext(ChartRenderContext context,
				Map<Axis, PlotNode> nodeContext, PlotNode contextNode) {
			super.updateContext(context, nodeContext, contextNode);

			context.setPlotIndex(getRenderIndex());

			if (isDryRun()) {
				context.setPlotCount(getRenderCount());
				return;
			}

			if (contextNode == null) {
				context.setHierarchy(null);
				context.setMember(null);
				context.setPosition(null);
				context.setColumnPosition(null);
				context.setRowPosition(null);
				context.setCell(null);
				context.setPlotPath(Collections.<Member> emptyList());
			} else {
				Position columnPosition;
				Position rowPosition;

				if (OlapUtils.equals(getAxis(), Axis.COLUMNS)) {
					columnPosition = contextNode.getPosition();
					rowPosition = nodeContext.get(Axis.ROWS).getPosition();
				} else if (OlapUtils.equals(getAxis(), Axis.ROWS)) {
					columnPosition = nodeContext.get(Axis.COLUMNS)
							.getPosition();
					rowPosition = contextNode.getPosition();
				} else {
					return;
				}

				CellSet cellSet = context.getCellSet();

				Cell cell = cellSet.getCell(columnPosition, rowPosition);

				Member member = contextNode.getReference();
				Hierarchy hierarchy = member.getHierarchy();

				context.setHierarchy(hierarchy);
				context.setMember(member);
				context.setPosition(contextNode.getPosition());
				context.setColumnPosition(columnPosition);
				context.setRowPosition(rowPosition);
				context.setCell(cell);

				List<Member> path = new LinkedList<Member>(
						contextNode.getPath());

				for (Member m : context.getPagePath()) {
					path.remove(m);
				}

				for (Member m : context.getChartPath()) {
					path.remove(m);
				}

				for (Member m : context.getSeriesPath()) {
					path.remove(m);
				}

				context.setPlotPath(path);
			}
		}
	}
}