/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Cell;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.util.TreeNode;

public abstract class AbstractTableBuilder<T extends TableModel<TR>, TR extends TableRow<TC>, TC extends TableCell>
		implements TableBuilder<T> {

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimension = true;

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
	 * @return the showDimension
	 */
	public boolean getShowDimension() {
		return showDimension;
	}

	/**
	 * @param showDimension
	 *            the showDimension to set
	 */
	public void setShowDimension(boolean showDimension) {
		this.showDimension = showDimension;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.TableBuilder#build(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public T build(PivotModel model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		BuildContext context = createBuildContext(model);

		T table = createTable(context);

		List<TR> columnHeaders = createColumnHeaders(context, table);

		int columnHeaderWidth = 0;
		int columnHeaderHeight = 0;

		if (columnHeaders != null && !columnHeaders.isEmpty()) {
			table.getHeaders().addAll(columnHeaders);

			TR firstRow = columnHeaders.get(0);
			for (TC cell : firstRow.getCells()) {
				columnHeaderWidth += cell.getColSpan();
			}

			int i = 0;
			int rowCount = columnHeaders.size();

			while (i < rowCount) {
				TR row = columnHeaders.get(i);

				TC firstCell = row.getCells().get(0);

				i += firstCell.getRowSpan();
				columnHeaderHeight += firstCell.getRowSpan();
			}
		}

		List<TR> rowHeaders = createRowHeaders(context, table);

		int rowHeaderWidth = 0;
		int rowHeaderHeight = 0;

		if (rowHeaders != null && !rowHeaders.isEmpty()) {
			table.getRows().addAll(rowHeaders);

			TR firstRow = rowHeaders.get(0);
			for (TC cell : firstRow.getCells()) {
				rowHeaderWidth += cell.getColSpan();
			}

			int i = 0;
			int rowCount = rowHeaders.size();

			while (i < rowCount) {
				TR row = rowHeaders.get(i);

				TC firstCell = row.getCells().get(0);

				i += firstCell.getRowSpan();
				rowHeaderHeight += firstCell.getRowSpan();
			}

			populateCells(context, table, columnHeaderWidth,
					columnHeaderHeight, rowHeaderWidth, rowHeaderHeight,
					rowHeaders);
		}

		if (columnHeaderHeight > 1 && rowHeaderWidth > 1) {
			TC corner = createCell(context, table, 0, columnHeaders.get(0), 0,
					rowHeaderWidth, columnHeaderHeight);
			columnHeaders.get(0).getCells().add(0, corner);
		}

		return table;
	}

	/**
	 * @param context
	 * @param table
	 * @param columnHeaderWidth
	 * @param columnHeaderHeight
	 * @param rowHeaderWidth
	 * @param rowHeaderHeight
	 * @param rowHeaders
	 */
	protected void populateCells(BuildContext context, T table,
			int columnHeaderWidth, int columnHeaderHeight, int rowHeaderWidth,
			int rowHeaderHeight, List<TR> rowHeaders) {
		int ordinal = 0;
		for (int rowIndex = 0; rowIndex < rowHeaderHeight; rowIndex++) {
			TR row = rowHeaders.get(rowIndex);

			for (int colIndex = 0; colIndex < columnHeaderWidth; colIndex++) {

				Cell cell = context.getCellSet().getCell(ordinal++);
				context.setCell(cell);

				TC tableCell = createCell(context, table, rowIndex, row,
						colIndex);
				row.getCells().add(tableCell);
			}
		}

		context.setCell(null);
	}

	/**
	 * @param context
	 * @param table
	 * @return
	 */
	protected List<TR> createColumnHeaders(BuildContext context, T table) {
		List<CellSetAxis> axes = context.getCellSet().getAxes();
		if (axes.isEmpty()) {
			return Collections.emptyList();
		}

		CellSetAxis axis = axes.get(0);
		context.setAxis(axis);

		MemberNode axisRoot = createAxisTree(axis);

		List<TR> headers = createColumnHeaders(context, table, axisRoot);

		context.setAxis(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @param axisRoot
	 * @return
	 */
	protected List<TR> createColumnHeaders(BuildContext context, T table,
			MemberNode axisRoot) {
		CellSetAxis axis = context.getAxis();

		List<Position> positions = axis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return Collections.emptyList();
		}

		List<TR> headers = new ArrayList<TR>();

		List<Member> members = positions.get(0).getMembers();

		int headerIndex = 0;
		for (Member member : members) {
			Hierarchy hierarchy = member.getHierarchy();

			context.setHierarchy(hierarchy);

			List<MemberNode> nodes = axisRoot.getNodesForHierarchy(hierarchy);

			List<TR> hierarchyHeaders = createColumnHeadersForHierarchy(
					context, table, headerIndex, nodes);
			if (hierarchyHeaders != null) {
				headers.addAll(hierarchyHeaders);
				headerIndex += hierarchyHeaders.size();
			}
		}

		context.setHierarchy(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @param headerIndex
	 * @param children
	 * @return
	 */
	protected List<TR> createColumnHeadersForHierarchy(BuildContext context,
			T table, int headerIndex, List<MemberNode> children) {
		List<TR> headers = new ArrayList<TR>(3);

		int rowIndex = headerIndex;
		if (getShowDimension()) {
			rowIndex++;
		}

		int colIndex = 0;

		for (MemberNode node : children) {
			context.setMember(node.getReference());
			context.setColumnPosition(node.getPosition());

			int nodeWidth = node.getWidth();

			int colSpan = nodeWidth;
			int rowSpan = 1;

			TR row = null;

			if (headers.isEmpty()) {
				row = createRow(context, table, rowIndex);
				headers.add(row);
			} else {
				row = headers.get(0);
			}

			if (getShowParentMembers()) {
				List<MemberNode> siblingChildren = node.getSiblingChildren();
				if (siblingChildren != null && !siblingChildren.isEmpty()) {
					rowSpan = 2;
				} else if (node.getSiblingParent() != null) {
					if (headers.size() < 2) {
						row = createRow(context, table, rowIndex + 1);
						headers.add(row);
					} else {
						row = headers.get(1);
					}
				}
			}

			TC cell = createCell(context, table, rowIndex, row, colIndex,
					colSpan, rowSpan);
			row.getCells().add(cell);

			if (rowSpan > 1) {
				int siblingWidth = 0;
				for (MemberNode siblingChild : node.getSiblingChildren()) {
					siblingWidth += siblingChild.getWidth();
				}

				TC parentCell = createCell(context, table, rowIndex, row,
						colIndex, siblingWidth, 1);
				row.getCells().add(parentCell);
			}

			colIndex += colSpan;
		}

		context.setMember(null);

		if (getShowDimension()) {
			context.setColumnPosition(null);
			context.setMember(null);

			TR row = createRow(context, table, headerIndex);

			Member parent = null;

			colIndex = 0;
			int colSpan = 0;

			for (MemberNode node : children) {
				if (parent != null
						&& !ObjectUtils.equals(parent, node.getParent()
								.getReference())) {
					TC cell = createCell(context, table, headerIndex, row,
							colIndex, colSpan, 1);
					row.getCells().add(cell);

					parent = node.getParent().getReference();

					colIndex += colSpan;
					colSpan = 0;
				}

				colSpan += node.getWidth();
				parent = node.getParent().getReference();
			}

			TC cell = createCell(context, table, headerIndex, row, colIndex,
					colSpan, 1);
			row.getCells().add(cell);

			headers.add(0, row);

			rowIndex++;
		}

		context.setColumnPosition(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @return
	 */
	protected List<TR> createRowHeaders(BuildContext context, T table) {
		List<CellSetAxis> axes = context.getCellSet().getAxes();
		if (axes.isEmpty() || axes.size() < 2) {
			return Collections.emptyList();
		}

		CellSetAxis axis = axes.get(1);
		context.setAxis(axis);

		MemberNode axisRoot = createAxisTree(axis);

		List<TR> headers = createRowHeaders(context, table, axisRoot);

		context.setAxis(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @param axisRoot
	 * @return
	 */
	protected List<TR> createRowHeaders(BuildContext context, T table,
			MemberNode axisRoot) {
		List<TR> rows = new ArrayList<TR>();

		CellSetAxis axis = context.getAxis();

		List<Position> positions = axis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return Collections.emptyList();
		}

		List<Member> members = positions.get(0).getMembers();

		int headerIndex = 0;
		for (Member member : members) {
			Hierarchy hierarchy = member.getHierarchy();

			context.setHierarchy(hierarchy);

			List<MemberNode> nodes = axisRoot.getNodesForHierarchy(hierarchy);

			populateRowHeadersForHierarchy(context, table, headerIndex, nodes,
					rows);
		}

		context.setHierarchy(null);

		return rows;
	}

	/**
	 * @param context
	 * @param table
	 * @param headerIndex
	 * @param nodes
	 * @param rows
	 */
	protected void populateRowHeadersForHierarchy(BuildContext context,
			T table, int headerIndex, List<MemberNode> nodes, List<TR> rows) {
		if (showParentMembers) {
			int rowIndex = 0;

			int maxDepth = 0;
			for (MemberNode node : nodes) {
				maxDepth = Math.max(maxDepth, node.getReference().getDepth());
			}

			for (MemberNode node : nodes) {
				context.setMember(node.getReference());
				context.setRowPosition(node.getPosition());

				TR row;

				if (rows.size() > rowIndex) {
					row = rows.get(rowIndex);
				} else {
					row = createRow(context, table, rowIndex);
					rows.add(row);
				}

				if (node.getSiblingChildren() != null
						&& !node.getSiblingChildren().isEmpty()) {
					TC cell = createCell(context, table, rowIndex, row,
							node.getLevel() - 1, 1, node.getSiblingWidth() + 1);
					row.getCells().add(cell);

					int colSpan = maxDepth - node.getReference().getDepth();

					TC totalCell = createCell(context, table, rowIndex, row,
							node.getLevel(), colSpan, 1);
					row.getCells().add(totalCell);
				} else {
					int colSpan = maxDepth - node.getReference().getDepth() + 1;
					TC cell = createCell(context, table, rowIndex, row,
							node.getLevel() - 1, colSpan, node.getWidth());
					row.getCells().add(cell);
				}

				rowIndex++;
			}
		} else {
			int rowIndex = 0;
			for (MemberNode node : nodes) {
				context.setMember(node.getReference());
				context.setRowPosition(node.getPosition());

				TR row;

				if (rows.size() > rowIndex) {
					row = rows.get(rowIndex);
				} else {
					row = createRow(context, table, rowIndex);
					rows.add(row);
				}

				TC cell = createCell(context, table, rowIndex, row,
						node.getLevel() - 1, 1, node.getWidth());

				row.getCells().add(cell);

				rowIndex++;
			}
		}

		context.setMember(null);
		context.setRowPosition(null);
	}

	/**
	 * @param model
	 * @return
	 */
	protected BuildContext createBuildContext(PivotModel model) {
		return new BuildContext(model);
	}

	/**
	 * @param context
	 * @return
	 */
	protected abstract T createTable(BuildContext context);

	/**
	 * @param context
	 * @param table
	 * @param rowIndex
	 * @return
	 */
	protected abstract TR createRow(BuildContext context, T table, int rowIndex);

	/**
	 * @param context
	 * @param table
	 * @param rowIndex
	 * @param row
	 * @param colIndex
	 * @return
	 */
	protected TC createCell(BuildContext context, T table, int rowIndex,
			TR row, int colIndex) {
		return createCell(context, table, rowIndex, row, colIndex, 1, 1);
	}

	/**
	 * @param context
	 * @param table
	 * @param rowIndex
	 * @param row
	 * @param colIndex
	 * @param colSpan
	 * @param rowSpan
	 * @return
	 */
	protected abstract TC createCell(BuildContext context, T table,
			int rowIndex, TR row, int colIndex, int colSpan, int rowSpan);

	/**
	 * @param axis
	 * @return
	 */
	protected MemberNode createAxisTree(CellSetAxis axis) {
		MemberNode root = new MemberNode(null);

		List<Position> positions = axis.getPositions();

		for (Position position : positions) {
			MemberNode parent = root;

			for (Member member : position.getMembers()) {
				parent = parent.addChildIfNeeded(member, position);
			}
		}

		return root;
	}

	protected static class MemberNode extends TreeNode<Member> {

		private MemberNode siblingParent;

		private Position position;

		private Integer width = null;

		private Integer siblingWidth = null;

		private Integer siblingMaxDepth = null;

		private List<MemberNode> siblingChildren = new ArrayList<MemberNode>();

		/**
		 * @param position
		 */
		protected MemberNode(Position position) {
			super(null);
			this.position = position;
		}

		/**
		 * @param member
		 * @param position
		 */
		protected MemberNode(Member member, Position position) {
			super(member);
			this.position = position;
		}

		protected void addSiblingChild(MemberNode child) {
			child.siblingParent = this;

			if (!siblingChildren.contains(child)) {
				siblingChildren.add(child);
			}

			this.siblingWidth = null;
			this.siblingMaxDepth = null;
		}

		protected Position getPosition() {
			return position;
		}

		protected MemberNode getSiblingParent() {
			return siblingParent;
		}

		protected MemberNode getSiblingRoot() {
			if (siblingParent == null
					&& (siblingChildren == null || siblingChildren.isEmpty())) {
				return null;
			}

			if (siblingParent == null) {
				return this;
			} else {
				return siblingParent.getSiblingRoot();
			}
		}

		protected List<MemberNode> getSiblingChildren() {
			return siblingChildren;
		}

		protected int getWidth() {
			if (width == null) {
				int count = 0;
				if (getChildren() != null) {
					for (TreeNode<Member> child : getChildren()) {
						MemberNode node = (MemberNode) child;
						count += node.getWidth();
					}
				}
				width = count;
			}

			return Math.max(1, width);
		}

		protected Integer getSiblingWidth() {
			if (siblingWidth == null) {
				siblingWidth = 0;

				if (siblingChildren != null) {
					for (MemberNode sibling : siblingChildren) {
						siblingWidth += sibling.getWidth()
								+ sibling.getSiblingWidth();
					}
				}
			}

			return siblingWidth;
		}

		protected Integer getMaxSiblingDepth() {
			if (siblingMaxDepth == null) {
				siblingMaxDepth = 0;

				if (siblingMaxDepth != null) {
					siblingMaxDepth = getReference().getDepth();

					for (MemberNode sibling : siblingChildren) {
						siblingMaxDepth = Math.max(siblingMaxDepth,
								sibling.getMaxSiblingDepth());
					}
				}
			}

			return siblingMaxDepth;
		}

		/**
		 * @param member
		 * @param position
		 * @return
		 */
		protected MemberNode addChildIfNeeded(Member member, Position position) {
			MemberNode child = null;

			for (TreeNode<Member> node : getChildren()) {
				if (ObjectUtils.equals(member, node.getReference())) {
					child = (MemberNode) node;
					break;
				}
			}

			if (child == null) {
				child = new MemberNode(member, position);

				if (getChildren() != null && !getChildren().isEmpty()) {
					for (TreeNode<Member> node : getChildren()) {
						if (ObjectUtils.equals(node.getReference(),
								member.getParentMember())) {
							((MemberNode) node).addSiblingChild(child);
							break;
						}
					}
				}

				addChild(child);
			}

			return child;
		}

		/**
		 * @param hierarchy
		 * @return
		 */
		protected List<MemberNode> getNodesForHierarchy(Hierarchy hierarchy) {
			List<MemberNode> nodes = new ArrayList<MemberNode>();
			collectNodesForHierarchy(nodes, hierarchy);
			return nodes;
		}

		/**
		 * @param nodes
		 * @param hierarchy
		 */
		private void collectNodesForHierarchy(List<MemberNode> nodes,
				Hierarchy hierarchy) {
			if (getChildren() != null) {
				for (TreeNode<Member> child : getChildren()) {
					Member member = child.getReference();
					if (member != null
							&& hierarchy.equals(member.getHierarchy())) {
						nodes.add((MemberNode) child);
					} else if (child.getChildren() != null) {
						((MemberNode) child).collectNodesForHierarchy(nodes,
								hierarchy);
					}
				}
			}
		}

		/**
		 * @see com.eyeq.pivot4j.util.TreeNode#addChild(com.eyeq.pivot4j.util.TreeNode)
		 */
		@Override
		public void addChild(TreeNode<Member> child) {
			super.addChild(child);
			clearWidthCache();
		}

		private void clearWidthCache() {
			this.width = null;
			this.siblingWidth = null;

			if (getParent() != null) {
				((MemberNode) getParent()).clearWidthCache();
			}
		}
	}
}
