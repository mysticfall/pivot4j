/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.chart;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.AbstractPivotRenderer;
import com.eyeq.pivot4j.util.OlapUtils;
import com.eyeq.pivot4j.util.TreeNode;
import com.eyeq.pivot4j.util.TreeNodeCallback;

public class ChartRenderer extends
		AbstractPivotRenderer<ChartRenderContext, ChartRenderCallback> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String DEFAULT_MEMBER_SEPARATOR = " / ";

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
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#render(com.eyeq.pivot4j.PivotModel,
	 *      com.eyeq.pivot4j.ui.RenderCallback)
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

		PageSizeCalculator calculator = new PageSizeCalculator();

		columnRoot.walkChildrenAtLevel(calculator,
				Math.max(0, maxColumnLevel - 1));

		final ChartRenderContext context = new ChartRenderContext(model, this,
				calculator.getMaxPageSize());

		callback.startRender(context);

		final TreeNodeCallback<Member> rowNodeHandler = new TreeNodeCallback<Member>() {

			@Override
			public int handleTreeNode(TreeNode<Member> node) {
				PlotNode plotNode = (PlotNode) node;

				if (callback.renderSeries()) {
					context.setSeriesPath(plotNode.getPath());

					callback.startSeries(context);
				}

				List<TreeNode<Member>> children = node.getChildren();

				for (TreeNode<Member> child : children) {
					PlotNode childNode = (PlotNode) child;

					Position columnPosition = context.getColumnPosition();
					Position rowPosition = childNode.getPosition();

					CellSet cellSet = model.getCellSet();

					Cell cell = cellSet.getCell(columnPosition, rowPosition);

					Member member = childNode.getReference();
					Hierarchy hierarchy = member.getHierarchy();

					context.setPlotIndex(context.getPlotIndex() + 1);

					context.setHierarchy(hierarchy);
					context.setMember(member);
					context.setPosition(rowPosition);
					context.setRowPosition(rowPosition);
					context.setCell(cell);

					String label;

					if (callback.renderSeries()) {
						label = getLabel(context);
					} else {
						label = getLabel(childNode.getPath());
					}

					callback.renderCommands(context, getCommands(context));
					callback.renderContent(context, label, getValue(context));

					context.setRowPosition(plotNode.getPosition());

					context.setMember(node.getReference());
				}

				if (callback.renderSeries()) {
					callback.endSeries(context);

					context.setSeriesIndex(context.getSeriesIndex() + 1);
					context.setSeriesPath(null);
				}

				return CONTINUE;
			}
		};

		final int rowLevel = Math.max(0, maxRowLevel - 1);

		TreeNodeCallback<Member> columnNodeHandler = new TreeNodeCallback<Member>() {

			@Override
			public int handleTreeNode(TreeNode<Member> node) {
				PlotNode plotNode = (PlotNode) node;

				context.setChartIndex(0);
				context.setColumnPosition(plotNode.getPosition());
				context.setPagePath(plotNode.getPath());

				callback.startPage(context);

				List<TreeNode<Member>> children = node.getChildren();

				for (TreeNode<Member> child : children) {
					PlotNode childNode = (PlotNode) child;

					context.setPlotIndex(0);
					context.setSeriesIndex(0);
					context.setColumnPosition(childNode.getPosition());

					context.setPosition(null);
					context.setRowPosition(null);
					context.setHierarchy(null);
					context.setMember(null);
					context.setCell(null);

					callback.startChart(context);

					rowRoot.walkChildrenAtLevel(rowNodeHandler, rowLevel);

					callback.endChart(context);

					context.setChartIndex(context.getChartIndex() + 1);
				}

				context.setColumnPosition(plotNode.getPosition());

				callback.endPage(context);

				context.setPageIndex(context.getPageIndex() + 1);
				context.setColumnPosition(null);
				context.setPagePath(null);

				return CONTINUE;
			}
		};

		columnRoot.walkChildrenAtLevel(columnNodeHandler,
				Math.max(0, maxColumnLevel - 1));

		context.setPosition(null);
		context.setColumnPosition(null);
		context.setRowPosition(null);
		context.setHierarchy(null);
		context.setMember(null);
		context.setCell(null);

		callback.endRender(context);
	}

	/**
	 * @param members
	 * @return
	 */
	protected String getLabel(List<Member> members) {
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
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getLabel(ChartRenderContext context) {
		return getLabel(context.getMember());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getValue(com.eyeq.pivot4j.ui.RenderContext)
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

	static class PageSizeCalculator implements TreeNodeCallback<Member> {

		private int maxPageSize = 0;

		public int getMaxPageSize() {
			return maxPageSize;
		}

		/**
		 * @see com.eyeq.pivot4j.util.TreeNodeCallback#handleTreeNode(com.eyeq.pivot4j.util.TreeNode)
		 */
		@Override
		public int handleTreeNode(TreeNode<Member> node) {
			this.maxPageSize = Math.max(node.getChildCount(), maxPageSize);

			return CONTINUE;
		}
	}
}