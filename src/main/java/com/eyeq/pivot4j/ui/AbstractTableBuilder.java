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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Cell;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.util.TreeNode;
import com.eyeq.pivot4j.util.TreeNodeCallback;

public abstract class AbstractTableBuilder<T extends TableModel<TR>, TR extends TableRow<TC>, TC extends TableCell>
		implements TableBuilder<T> {

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

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

		HeadersInfo columnHeader = createColumnHeaders(context, table);
		if (columnHeader != null) {
			table.getHeaders().addAll(columnHeader.getHeaders());
		}

		HeadersInfo rowHeader = createRowHeaders(context, table);
		if (rowHeader != null) {
			table.getRows().addAll(rowHeader.getHeaders());
		}

		if (columnHeader != null && rowHeader != null) {
			populateCells(context, table, columnHeader, rowHeader);

			addCornerCells(context, table, columnHeader, rowHeader);
		}

		return table;
	}

	/**
	 * @param context
	 * @param table
	 * @return
	 */
	protected HeadersInfo createColumnHeaders(BuildContext context, T table) {
		List<CellSetAxis> axes = context.getCellSet().getAxes();
		if (axes.isEmpty()) {
			return null;
		}

		CellSetAxis axis = axes.get(0);
		context.setAxis(axis);

		HeaderNode axisRoot = createAxisTree(axis);

		HeadersInfo headers = createColumnHeaders(context, table, axisRoot);

		context.setAxis(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @param axisRoot
	 * @return
	 */
	protected HeadersInfo createColumnHeaders(BuildContext context, T table,
			HeaderNode axisRoot) {
		CellSetAxis axis = context.getAxis();

		List<Position> positions = axis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return null;
		}

		List<TR> headers = new ArrayList<TR>();

		List<Member> members = positions.get(0).getMembers();
		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(members.size());
		List<Integer> spans = new ArrayList<Integer>(members.size());

		int headerIndex = 0;
		for (Member member : members) {
			Hierarchy hierarchy = member.getHierarchy();

			hierarchies.add(hierarchy);

			context.setHierarchy(hierarchy);

			List<HeaderNode> nodes = axisRoot.getNodesForHierarchy(hierarchy);

			List<TR> hierarchyHeaders = createColumnHeadersForHierarchy(
					context, table, headerIndex, nodes);
			if (hierarchyHeaders != null) {
				headers.addAll(hierarchyHeaders);

				int span = hierarchyHeaders.size();
				headerIndex += span;
				spans.add(span);
			}
		}

		context.setHierarchy(null);

		return new HeadersInfo(headers, hierarchies, spans);
	}

	/**
	 * @param context
	 * @param table
	 * @param headerIndex
	 * @param children
	 * @return
	 */
	protected List<TR> createColumnHeadersForHierarchy(BuildContext context,
			T table, int headerIndex, List<HeaderNode> children) {
		List<TR> headers = new ArrayList<TR>();

		if (getShowDimensionTitle()) {
			context.setColumnPosition(null);
			context.setMember(null);

			TR row = createRow(context, table, headerIndex);
			headers.add(row);

			Member parent = null;

			int colIndex = 0;
			int colSpan = 0;

			for (HeaderNode node : children) {
				if (parent != null
						&& !ObjectUtils.equals(parent, node.getParent()
								.getReference())) {
					addSpanCells(context, table, row, headerIndex, colIndex,
							colSpan, 1);

					parent = node.getParent().getReference();

					colIndex += colSpan;
					colSpan = 0;
				}

				colSpan += node.getWidth();
				parent = node.getParent().getReference();
			}

			addSpanCells(context, table, row, headerIndex, colIndex, colSpan, 1);

			headerIndex++;
		}

		int minDepth = 0;
		int maxSpan = 1;

		if (showParentMembers) {
			for (HeaderNode node : children) {
				if (node.isSiblingParent()) {
					int depth = node.getMaxSiblingDepth()
							- node.getReference().getDepth() + 1;
					maxSpan = Math.max(maxSpan, depth);
				}

				minDepth = Math.min(minDepth, node.getReference().getDepth());
			}
		}

		int colIndex = 0;

		for (HeaderNode node : children) {
			context.setMember(node.getReference());
			context.setColumnPosition(node.getPosition());

			TR row = null;

			int nodeWidth = node.getWidth();

			int colSpan = nodeWidth;
			int rowSpan;

			int offset;

			if (showParentMembers) {
				offset = node.getReference().getDepth() - minDepth;
				rowSpan = maxSpan - offset;
			} else {
				rowSpan = 1;
				offset = 0;
			}

			int rowIndex = headerIndex + offset;

			if (showDimensionTitle) {
				offset++;
			}

			if (headers.size() > offset) {
				row = headers.get(offset);
			} else {
				row = createRow(context, table, rowIndex);
				headers.add(row);
			}

			if (showParentMembers && node.isSiblingParent()) {
				addSpanCells(context, table, row, rowIndex, colIndex, colSpan,
						rowSpan);

				int siblingWidth = 0;
				for (HeaderNode siblingChild : node.getSiblingChildren()) {
					siblingWidth += siblingChild.getWidth();
					if (siblingChild.isSiblingParent()) {
						siblingWidth += siblingChild.getSiblingWidth();
					}
				}

				addSpanCells(context, table, row, rowIndex, colIndex,
						siblingWidth, 1);
			} else {
				addSpanCells(context, table, row, rowIndex, colIndex, colSpan,
						rowSpan);
			}

			colIndex += colSpan;
		}

		context.setMember(null);
		context.setColumnPosition(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @return
	 */
	protected HeadersInfo createRowHeaders(BuildContext context, T table) {
		List<CellSetAxis> axes = context.getCellSet().getAxes();
		if (axes.isEmpty() || axes.size() < 2) {
			return null;
		}

		CellSetAxis axis = axes.get(1);
		context.setAxis(axis);

		HeaderNode axisRoot = createAxisTree(axis);

		HeadersInfo headers = createRowHeaders(context, table, axisRoot);

		context.setAxis(null);

		return headers;
	}

	/**
	 * @param context
	 * @param table
	 * @param axisRoot
	 * @return
	 */
	protected HeadersInfo createRowHeaders(final BuildContext context,
			final T table, final HeaderNode axisRoot) {
		final List<TR> rows = new ArrayList<TR>();

		CellSetAxis axis = context.getAxis();

		List<Position> positions = axis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return null;
		}

		List<Member> members = positions.get(0).getMembers();
		final List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(
				members.size());
		for (Member member : members) {
			hierarchies.add(member.getHierarchy());
		}

		final List<Integer> spans = new ArrayList<Integer>(members.size());

		final Map<Hierarchy, Integer> maxSpanMap = new HashMap<Hierarchy, Integer>(
				members.size());
		final Map<Hierarchy, Integer> minDepthMap = new HashMap<Hierarchy, Integer>(
				members.size());

		axisRoot.walkChildren(new TreeNodeCallback<Member>() {

			@Override
			public int handleTreeNode(TreeNode<Member> node) {
				HeaderNode memberNode = (HeaderNode) node;
				Hierarchy hierarchy = memberNode.getReference().getHierarchy();

				Integer maxSpan = maxSpanMap.get(hierarchy);
				if (maxSpan == null) {
					maxSpan = 1;
				}

				if (memberNode.isSiblingParent()) {
					int span = memberNode.getMaxSiblingDepth()
							- node.getReference().getDepth() + 1;
					maxSpan = Math.max(maxSpan, span);
				}

				maxSpanMap.put(hierarchy, maxSpan);

				Integer minDepth = minDepthMap.get(hierarchy);
				if (minDepth == null) {
					minDepth = 0;
				}

				minDepthMap.put(hierarchy,
						Math.min(minDepth, node.getReference().getDepth()));

				return CONTINUE;
			}
		});

		axisRoot.walkChildren(new TreeNodeCallback<Member>() {

			int colIndex = 0;
			int rowIndex = 0;

			@Override
			public int handleTreeNode(TreeNode<Member> node) {
				HeaderNode memberNode = (HeaderNode) node;

				Member member = node.getReference();
				Hierarchy hierarchy = member.getHierarchy();
				Position position = memberNode.getPosition();

				context.setMember(member);
				context.setRowPosition(position);

				int maxSpan = maxSpanMap.get(hierarchy);
				int minDepth = minDepthMap.get(hierarchy);

				int hierarchyIndex = node.getLevel() - 1;

				if (spans.size() <= hierarchyIndex) {
					spans.add(maxSpan);
				}

				int rowSpan = memberNode.getWidth();
				int colSpan;

				if (showParentMembers) {
					int baseColIndex = 0;

					for (Hierarchy hier : hierarchies) {
						if (hier.equals(hierarchy)) {
							break;
						}
						baseColIndex += maxSpanMap.get(hier);
					}

					colIndex = baseColIndex + member.getDepth() - minDepth;
					colSpan = maxSpan - member.getDepth() + minDepth;
				} else {
					colSpan = 1;
					colIndex = hierarchyIndex;
				}

				addSpanCells(context, table, rows, rowIndex, colIndex, colSpan,
						rowSpan);

				if (showParentMembers && memberNode.isSiblingParent()) {
					int siblingWidth = 0;
					for (HeaderNode siblingChild : memberNode
							.getSiblingChildren()) {
						siblingWidth += siblingChild.getWidth();
						if (siblingChild.isSiblingParent()) {
							siblingWidth += siblingChild.getSiblingWidth();
						}
					}

					while (rows.size() <= rowIndex + rowSpan) {
						TR row = createRow(context, table, rows.size());
						rows.add(row);
					}

					addSpanCells(context, table, rows, rowIndex + rowSpan,
							colIndex, 1, siblingWidth);
				}

				if (node.getChildren() == null || node.getChildren().isEmpty()) {
					colIndex = 0;
					rowIndex++;
				}

				context.setMember(null);
				context.setRowPosition(null);

				return CONTINUE;
			}
		});

		context.setHierarchy(null);

		return new HeadersInfo(rows, hierarchies, spans);
	}

	/**
	 * @param context
	 * @param table
	 * @param columnHeader
	 * @param rowHeader
	 */
	protected void addCornerCells(BuildContext context, T table,
			HeadersInfo columnHeader, HeadersInfo rowHeader) {
		TR firstHeader = columnHeader.getHeaders().get(0);

		int width = rowHeader.getWidth();
		int height = columnHeader.getHeight();

		if (showDimensionTitle) {
			height--;
		}

		TC corner = createCell(context, table, firstHeader, 0, 0, width, height);
		firstHeader.getCells().add(0, corner);

		if (showDimensionTitle) {
			context.setAxis(context.getCellSet().getAxes().get(1));

			int colIndex = 0;
			int hierarchyIndex = 0;

			List<Hierarchy> hierarchies = rowHeader.getHierarchies();

			List<TC> cells = new ArrayList<TC>(hierarchies.size());
			for (Hierarchy hierarchy : hierarchies) {
				context.setHierarchy(hierarchy);

				int span;

				if (showParentMembers) {
					span = rowHeader.getSpans().get(hierarchyIndex);
				} else {
					span = 1;
				}

				TC header = createCell(context, table, firstHeader, colIndex,
						height - 1, span, 1);
				cells.add(header);

				hierarchyIndex++;
				colIndex += span;
			}

			Collections.reverse(cells);

			TR lastHeader = columnHeader.getHeaders().get(
					columnHeader.getHeaders().size() - 1);

			for (TC cell : cells) {
				lastHeader.getCells().add(0, cell);
			}

			context.setHierarchy(null);
			context.setAxis(null);
		}
	}

	/**
	 * @param context
	 * @param table
	 * @param columnHeader
	 * @param rowHeader
	 */
	protected void populateCells(BuildContext context, T table,
			HeadersInfo columnHeader, HeadersInfo rowHeader) {
		int width = columnHeader.getWidth();
		int height = rowHeader.getHeight();

		List<TR> headers = rowHeader.getHeaders();

		int ordinal = 0;
		for (int rowIndex = 0; rowIndex < height; rowIndex++) {
			TR row = headers.get(rowIndex);

			for (int colIndex = 0; colIndex < width; colIndex++) {
				Cell cell = context.getCellSet().getCell(ordinal++);
				context.setCell(cell);

				TC tableCell = createCell(context, table, row, colIndex,
						rowIndex);
				row.getCells().add(tableCell);
			}
		}

		context.setCell(null);
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
	 * @param row
	 * @param colIndex
	 * @param rowIndex
	 * @return
	 */
	protected TC createCell(BuildContext context, T table, TR row,
			int colIndex, int rowIndex) {
		return createCell(context, table, row, colIndex, rowIndex, 1, 1);
	}

	/**
	 * @param context
	 * @param table
	 * @param row
	 * @param colIndex
	 * @param rowIndex
	 * @param colSpan
	 * @param rowSpan
	 * @return
	 */
	protected abstract TC createCell(BuildContext context, T table, TR row,
			int colIndex, int rowIndex, int colSpan, int rowSpan);

	/**
	 * @param context
	 * @param table
	 * @param rowIndex
	 * @param row
	 * @param colIndex
	 * @param colSpan
	 * @param rowSpan
	 */
	private void addSpanCells(BuildContext context, T table, TR row,
			int rowIndex, int colIndex, int colSpan, int rowSpan) {
		if (hideSpans) {
			for (int i = 0; i < colSpan; i++) {
				TC cell = createCell(context, table, row, colIndex + i,
						rowIndex, 1, rowSpan);
				row.getCells().add(cell);
			}
		} else {
			TC cell = createCell(context, table, row, colIndex, rowIndex,
					colSpan, rowSpan);
			row.getCells().add(cell);
		}
	}

	/**
	 * @param context
	 * @param table
	 * @param rows
	 * @param rowIndex
	 * @param colIndex
	 * @param colSpan
	 * @param rowSpan
	 */
	private void addSpanCells(BuildContext context, T table, List<TR> rows,
			int rowIndex, int colIndex, int colSpan, int rowSpan) {
		if (hideSpans) {
			for (int i = 0; i < rowSpan; i++) {
				int index = rowIndex + i;

				TR row;

				if (rows.size() > index) {
					row = rows.get(index);
				} else {
					row = createRow(context, table, rows.size());
					rows.add(row);
				}

				TC cell = createCell(context, table, row, colIndex, index,
						colSpan, 1);
				row.getCells().add(cell);
			}
		} else {
			TR row;

			if (rows.size() > rowIndex) {
				row = rows.get(rowIndex);
			} else {
				row = createRow(context, table, rows.size());
				rows.add(row);
			}
			TC cell = createCell(context, table, row, colIndex, rowIndex,
					colSpan, rowSpan);
			row.getCells().add(cell);
		}
	}

	/**
	 * @param axis
	 * @return
	 */
	protected HeaderNode createAxisTree(CellSetAxis axis) {
		HeaderNode root = new HeaderNode(null);

		List<Position> positions = axis.getPositions();

		for (Position position : positions) {
			HeaderNode parent = root;

			for (Member member : position.getMembers()) {
				parent = parent.addChildIfNeeded(member, position);
			}
		}

		return root;
	}

	protected class HeadersInfo {

		private List<TR> headers;

		private List<Hierarchy> hierarchies;

		private List<Integer> spans;

		private Integer width;

		private Integer height;

		/**
		 * @param headers
		 * @param hierarchies
		 * @param spans
		 */
		protected HeadersInfo(List<TR> headers, List<Hierarchy> hierarchies,
				List<Integer> spans) {
			this.headers = headers;
			this.hierarchies = hierarchies;
			this.spans = spans;

			this.width = 0;
			if (headers != null && !headers.isEmpty()) {
				TR firstRow = headers.get(0);
				for (TC cell : firstRow.getCells()) {
					width += cell.getColSpan();
				}
			}

			this.height = 0;
			if (headers != null && !headers.isEmpty()) {
				int i = 0;
				int rowCount = headers.size();

				while (i < rowCount) {
					TR row = headers.get(i);

					TC firstCell = row.getCells().get(0);

					i += firstCell.getRowSpan();
					height += firstCell.getRowSpan();
				}
			}
		}

		/**
		 * @return the headers
		 */
		protected List<TR> getHeaders() {
			return headers;
		}

		/**
		 * @return the hierarchies
		 */
		protected List<Hierarchy> getHierarchies() {
			return hierarchies;
		}

		/**
		 * @return the spans
		 */
		protected List<Integer> getSpans() {
			return spans;
		}

		/**
		 * @return the width
		 */
		protected int getWidth() {
			return width;
		}

		/**
		 * @return the height
		 */
		protected int getHeight() {
			return height;
		}
	}

	protected static class HeaderNode extends TreeNode<Member> {

		private HeaderNode siblingParent;

		private Position position;

		private Integer width = null;

		private Integer siblingWidth = null;

		private Integer siblingMaxDepth = null;

		private List<HeaderNode> siblingChildren = new ArrayList<HeaderNode>();

		/**
		 * @param position
		 */
		protected HeaderNode(Position position) {
			super(null);
			this.position = position;
		}

		/**
		 * @param member
		 * @param position
		 */
		protected HeaderNode(Member member, Position position) {
			super(member);
			this.position = position;
		}

		protected void addSiblingChild(HeaderNode child) {
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

		protected HeaderNode getSiblingParent() {
			return siblingParent;
		}

		protected boolean isSiblingParent() {
			return siblingChildren != null && !siblingChildren.isEmpty();
		}

		protected HeaderNode getSiblingRoot() {
			if (siblingParent == null && !isSiblingParent()) {
				return null;
			}

			if (siblingParent == null) {
				return this;
			} else {
				return siblingParent.getSiblingRoot();
			}
		}

		protected List<HeaderNode> getSiblingChildren() {
			return siblingChildren;
		}

		protected int getWidth() {
			if (width == null) {
				int count = 0;
				if (getChildren() != null) {
					for (TreeNode<Member> child : getChildren()) {
						HeaderNode node = (HeaderNode) child;
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
					for (HeaderNode sibling : siblingChildren) {
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

					for (HeaderNode sibling : siblingChildren) {
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
		protected HeaderNode addChildIfNeeded(Member member, Position position) {
			HeaderNode child = null;

			for (TreeNode<Member> node : getChildren()) {
				if (ObjectUtils.equals(member, node.getReference())) {
					child = (HeaderNode) node;
					break;
				}
			}

			if (child == null) {
				child = new HeaderNode(member, position);

				if (getChildren() != null && !getChildren().isEmpty()) {
					for (TreeNode<Member> node : getChildren()) {
						if (ObjectUtils.equals(node.getReference(),
								member.getParentMember())) {
							((HeaderNode) node).addSiblingChild(child);
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
		protected List<HeaderNode> getNodesForHierarchy(Hierarchy hierarchy) {
			List<HeaderNode> nodes = new ArrayList<HeaderNode>();
			collectNodesForHierarchy(nodes, hierarchy);
			return nodes;
		}

		/**
		 * @param nodes
		 * @param hierarchy
		 */
		private void collectNodesForHierarchy(List<HeaderNode> nodes,
				Hierarchy hierarchy) {
			if (getChildren() != null) {
				for (TreeNode<Member> child : getChildren()) {
					Member member = child.getReference();
					if (member != null
							&& hierarchy.equals(member.getHierarchy())) {
						nodes.add((HeaderNode) child);
					} else if (child.getChildren() != null) {
						((HeaderNode) child).collectNodesForHierarchy(nodes,
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
				((HeaderNode) getParent()).clearWidthCache();
			}
		}
	}
}
