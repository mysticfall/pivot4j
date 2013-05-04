/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.el.ExpressionEvaluatorFactory;
import com.eyeq.pivot4j.el.freemarker.FreeMarkerExpressionEvaluatorFactory;
import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.PivotLayoutCallback;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.RenderStrategy;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.ui.aggregator.AggregatorFactory;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;
import com.eyeq.pivot4j.util.OlapUtils;
import com.eyeq.pivot4j.util.TreeNode;
import com.eyeq.pivot4j.util.TreeNodeCallback;

public class RenderStrategyImpl implements RenderStrategy {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ExpressionEvaluatorFactory expressionEvaluatorFactory;

	public RenderStrategyImpl() {
		this.expressionEvaluatorFactory = createExpressionEvaluatorFactory();
	}

	/**
	 * @param model
	 * @param renderer
	 * @param callback
	 * @see com.eyeq.pivot4j.ui.RenderStrategy#render(com.eyeq.pivot4j.PivotModel,
	 *      com.eyeq.pivot4j.ui.PivotRenderer,
	 *      com.eyeq.pivot4j.ui.PivotLayoutCallback)
	 */
	public void render(PivotModel model, PivotRenderer renderer,
			PivotLayoutCallback callback) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (renderer == null) {
			throw new NullArgumentException("renderer");
		}

		List<CellSetAxis> axes = model.getCellSet().getAxes();
		if (axes.isEmpty()) {
			return;
		}

		TableHeaderNode columnRoot = createAxisTree(model, renderer,
				Axis.COLUMNS);
		if (columnRoot == null) {
			return;
		}

		TableHeaderNode rowRoot = createAxisTree(model, renderer, Axis.ROWS);
		if (rowRoot == null) {
			return;
		}

		configureAxisTree(model, renderer, Axis.COLUMNS, columnRoot);
		configureAxisTree(model, renderer, Axis.ROWS, rowRoot);

		invalidateAxisTree(model, Axis.COLUMNS, columnRoot);
		invalidateAxisTree(model, Axis.ROWS, rowRoot);

		RenderContext context = createRenderContext(model, renderer,
				columnRoot, rowRoot);

		callback.startTable(context);

		renderHeader(context, columnRoot, rowRoot, callback);
		renderBody(context, columnRoot, rowRoot, callback);

		callback.endTable(context);
	}

	/**
	 * @param model
	 * @param renderer
	 * @param columnRoot
	 * @param rowRoot
	 * @return
	 */
	protected RenderContext createRenderContext(PivotModel model,
			PivotRenderer renderer, TableHeaderNode columnRoot,
			TableHeaderNode rowRoot) {
		int columnHeaderCount = columnRoot.getMaxRowIndex();
		int rowHeaderCount = rowRoot.getMaxRowIndex();

		int columnCount = columnRoot.getWidth();
		int rowCount = rowRoot.getWidth();

		// TODO Share expression evaluator factory instance with the pivot
		// model.
		ExpressionEvaluator evaluator = expressionEvaluatorFactory
				.createEvaluator();

		Map<String, Member> cachedParents = new HashMap<String, Member>();

		cachedParents.putAll(columnRoot.getReference().getParentMembersCache());
		cachedParents.putAll(rowRoot.getReference().getParentMembersCache());

		return new RenderContext(model, renderer, columnCount, rowCount,
				columnHeaderCount, rowHeaderCount, evaluator, cachedParents);
	}

	/**
	 * @param context
	 * @param columnRoot
	 * @param rowRoot
	 * @param callback
	 */
	protected void renderHeader(final RenderContext context,
			final TableHeaderNode columnRoot, final TableHeaderNode rowRoot,
			final PivotLayoutCallback callback) {

		callback.startHeader(context);

		int count = context.getColumnHeaderCount();

		for (int rowIndex = 0; rowIndex < count; rowIndex++) {
			context.setAxis(Axis.COLUMNS);
			context.setColIndex(0);
			context.setRowIndex(rowIndex);

			callback.startRow(context);

			renderHeaderCorner(context, columnRoot, rowRoot, callback);

			// invoking renderHeaderCorner method resets the axis property.
			context.setAxis(Axis.COLUMNS);

			columnRoot.walkChildrenAtRowIndex(
					new TreeNodeCallback<TableAxisContext>() {

						@Override
						public int handleTreeNode(
								TreeNode<TableAxisContext> node) {
							TableHeaderNode headerNode = (TableHeaderNode) node;

							context.setColIndex(headerNode.getColIndex()
									+ context.getRowHeaderCount());
							context.setColSpan(headerNode.getColSpan());
							context.setRowSpan(headerNode.getRowSpan());

							context.setMember(headerNode.getMember());
							context.setProperty(headerNode.getProperty());
							context.setHierarchy(headerNode.getHierarchy());
							context.setColumnPosition(headerNode.getPosition());
							context.setAggregator(headerNode.getAggregator());
							context.setCell(null);

							if (headerNode.isAggregation()) {
								context.setCellType(CellType.Aggregation);
							} else if (context.getMember() == null) {
								if (context.getHierarchy() == null) {
									context.setCellType(CellType.None);
								} else {
									context.setCellType(CellType.Title);
								}
							} else {
								context.setCellType(CellType.Header);
							}

							callback.startCell(context);
							callback.cellContent(context);
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
	protected void renderBody(final RenderContext context,
			final TableHeaderNode columnRoot, final TableHeaderNode rowRoot,
			final PivotLayoutCallback callback) {
		callback.startBody(context);

		int count = rowRoot.getColSpan();

		for (int rowIndex = 0; rowIndex < count; rowIndex++) {
			context.setAxis(Axis.ROWS);
			context.setColIndex(0);
			context.setRowIndex(rowIndex + context.getColumnHeaderCount());

			callback.startRow(context);

			rowRoot.walkChildrenAtColIndex(
					new TreeNodeCallback<TableAxisContext>() {

						@Override
						public int handleTreeNode(
								TreeNode<TableAxisContext> node) {
							TableHeaderNode headerNode = (TableHeaderNode) node;

							if (headerNode.getRowIndex() == 0) {
								return TreeNodeCallback.CONTINUE;
							}

							context.setColIndex(headerNode.getRowIndex() - 1);
							context.setColSpan(headerNode.getRowSpan());
							context.setRowSpan(headerNode.getColSpan());

							context.setMember(headerNode.getMember());
							context.setProperty(headerNode.getProperty());
							context.setHierarchy(headerNode.getHierarchy());
							context.setRowPosition(headerNode.getPosition());
							context.setCell(null);
							context.setAggregator(headerNode.getAggregator());

							if (headerNode.isAggregation()) {
								context.setCellType(CellType.Aggregation);
							} else if (context.getMember() == null) {
								if (context.getHierarchy() == null) {
									context.setCellType(CellType.None);
								} else {
									context.setCellType(CellType.Title);
								}
							} else {
								context.setCellType(CellType.Header);
							}

							callback.startCell(context);
							callback.cellContent(context);
							callback.endCell(context);

							if (headerNode.getChildCount() == 0) {
								renderDataRow(context, columnRoot, rowRoot,
										(TableHeaderNode) node, callback);
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
	protected void renderDataRow(RenderContext context,
			TableHeaderNode columnRoot, TableHeaderNode rowRoot,
			TableHeaderNode rowNode, PivotLayoutCallback callback) {
		context.setCellType(CellType.Value);

		for (int i = 0; i < context.getColumnCount(); i++) {
			Cell cell = null;

			TableHeaderNode columnNode = columnRoot.getLeafNodeAtColIndex(i);

			if (columnNode != null && columnNode.getPosition() != null
					&& columnNode.getPosition().getOrdinal() != -1
					&& rowNode.getPosition() != null
					&& rowNode.getPosition().getOrdinal() != -1) {
				cell = context.getCellSet().getCell(columnNode.getPosition(),
						rowNode.getPosition());
			}

			context.setCell(cell);
			context.setHierarchy(null);
			context.setMember(null);
			context.setAxis(null);
			context.setColIndex(context.getRowHeaderCount() + i);
			context.setColSpan(1);
			context.setRowSpan(1);

			if (columnNode == null) {
				Aggregator aggregator = rowNode.getAggregator();
				context.setAggregator(aggregator);

				if (aggregator != null) {
					context.setAxis(Axis.ROWS);
				}

				context.setColumnPosition(null);
			} else {
				if (columnNode.getAggregator() == null) {
					Aggregator aggregator = rowNode.getAggregator();
					context.setAggregator(aggregator);

					if (aggregator != null) {
						context.setAxis(Axis.ROWS);
					}
				} else if (rowNode.getAggregator() != null
						&& rowNode.getAggregator().getMeasure() != null) {
					context.setAggregator(rowNode.getAggregator());
				} else {
					Aggregator aggregator = columnNode.getAggregator();
					context.setAggregator(aggregator);

					if (aggregator != null && rowNode.getAggregator() == null) {
						context.setAxis(Axis.COLUMNS);
					}
				}

				context.setColumnPosition(columnNode.getPosition());
			}

			context.setRowPosition(rowNode.getPosition());

			callback.startCell(context);
			callback.cellContent(context);
			callback.endCell(context);

			if (rowNode.getMemberChildren() == 0) {
				List<Aggregator> rowAggregators = rowRoot.getReference()
						.getAggregators();
				for (Aggregator aggregator : rowAggregators) {
					if (context.getAggregator() != aggregator) {
						aggregator.aggregate(context);
					}
				}
			}

			if (columnNode == null || columnNode.getMemberChildren() == 0) {
				List<Aggregator> columnAggregators = columnRoot.getReference()
						.getAggregators();
				for (Aggregator aggregator : columnAggregators) {
					if (context.getAggregator() != aggregator) {
						aggregator.aggregate(context);
					}
				}
			}
		}

		context.setAggregator(null);
	}

	/**
	 * @param context
	 * @param columnRoot
	 * @param rowRoot
	 * @param callback
	 */
	protected void renderHeaderCorner(RenderContext context,
			TableHeaderNode columnRoot, TableHeaderNode rowRoot,
			PivotLayoutCallback callback) {
		boolean showParentMembers = context.getRenderer()
				.getShowParentMembers();
		boolean showDimensionTitle = context.getRenderer()
				.getShowDimensionTitle();

		int offset = 0;

		if (context.getRenderer().getShowDimensionTitle()) {
			offset = showParentMembers ? 2 : 1;
		}

		context.setAxis(null);

		context.setHierarchy(null);
		context.setLevel(null);
		context.setMember(null);
		context.setProperty(null);

		context.setCell(null);
		context.setCellType(CellType.None);

		context.setColumnPosition(null);
		context.setRowPosition(null);

		boolean renderDimensionTitle = showDimensionTitle
				&& (context.getRowIndex() == context.getColumnHeaderCount()
						- offset);
		boolean renderLevelTitle = showDimensionTitle
				&& showParentMembers
				&& (context.getRowIndex() == context.getColumnHeaderCount() - 1);

		if (context.getRowIndex() == 0 && !renderDimensionTitle
				&& !renderLevelTitle) {
			context.setColSpan(context.getRowHeaderCount());
			context.setRowSpan(context.getColumnHeaderCount() - offset);

			callback.startCell(context);
			callback.cellContent(context);
			callback.endCell(context);
		} else if (renderDimensionTitle) {
			final Map<Hierarchy, Integer> spans = new HashMap<Hierarchy, Integer>();
			final Map<Hierarchy, List<Property>> propertyMap = new HashMap<Hierarchy, List<Property>>();

			rowRoot.walkChildrenAtColIndex(
					new TreeNodeCallback<TableAxisContext>() {

						@Override
						public int handleTreeNode(
								TreeNode<TableAxisContext> node) {
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
								List<Property> properties = propertyMap
										.get(hierarchy);
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
			context.setCellType(CellType.Title);

			for (Hierarchy hierarchy : rowRoot.getReference().getHierarchies()) {
				Integer span = spans.get(hierarchy);
				if (span == null) {
					span = 1;
				}

				context.setColSpan(span);
				context.setHierarchy(hierarchy);

				callback.startCell(context);
				callback.cellContent(context);
				callback.endCell(context);

				context.setColIndex(context.getColIndex() + span);

				List<Property> properties = propertyMap.get(hierarchy);
				if (properties != null) {
					for (Property property : properties) {
						context.setColSpan(1);
						context.setColIndex(context.getColIndex() + 1);
						context.setProperty(property);

						callback.startCell(context);
						callback.cellContent(context);
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

					if (headerNode.getMember() != null
							&& !levels.containsKey(colIndex)) {
						levels.put(colIndex, headerNode.getMember().getLevel());
					}

					if (headerNode.getProperty() != null
							&& !properties.containsKey(colIndex)) {
						properties.put(colIndex, headerNode.getProperty());
					}

					return TreeNodeCallback.CONTINUE;
				}
			});

			context.setAxis(Axis.ROWS);
			context.setColSpan(1);
			context.setRowSpan(1);
			context.setCellType(CellType.Title);

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
				callback.cellContent(context);
				callback.endCell(context);
			}
		}

		context.setHierarchy(null);
	}

	/**
	 * @param model
	 * @param renderer
	 * @param axis
	 * @return
	 */
	protected TableHeaderNode createAxisTree(PivotModel model,
			PivotRenderer renderer, Axis axis) {
		CellSetAxis cellSetAxis = model.getCellSet().getAxes()
				.get(axis.axisOrdinal());

		List<Position> positions = cellSetAxis.getPositions();
		if (positions == null || positions.isEmpty()) {
			return null;
		}

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>();
		List<Aggregator> aggregators = new ArrayList<Aggregator>();

		Map<Hierarchy, List<Level>> levelsMap = new HashMap<Hierarchy, List<Level>>();

		Comparator<Level> levelComparator = new Comparator<Level>() {

			@Override
			public int compare(Level l1, Level l2) {
				Integer d1 = l1.getDepth();
				Integer d2 = l2.getDepth();
				return d1.compareTo(d2);
			}
		};

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

		AggregatorFactory aggregatorFactory = renderer.getAggregatorFactory();

		List<String> aggregatorNames = null;
		List<String> hierarchyAggregatorNames = null;
		List<String> memberAggregatorNames = null;

		if (aggregatorFactory != null
				&& (!containsMeasure || index == firstMembers.size() - 1)) {
			aggregatorNames = renderer.getAggregators(axis,
					AggregatorPosition.Grand);
			hierarchyAggregatorNames = renderer.getAggregators(axis,
					AggregatorPosition.Hierarchy);
			memberAggregatorNames = renderer.getAggregators(axis,
					AggregatorPosition.Member);
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

		TableAxisContext nodeContext = new TableAxisContext(axis, hierarchies,
				levelsMap, aggregators, renderer);

		TableHeaderNode axisRoot = new TableHeaderNode(nodeContext);

		Set<Measure> grandTotalMeasures = new LinkedHashSet<Measure>();
		Set<Measure> totalMeasures = new LinkedHashSet<Measure>();

		List<Member> lastMembers = null;

		Map<Hierarchy, List<AggregationTarget>> memberParentMap = new HashMap<Hierarchy, List<AggregationTarget>>();

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

				if (!hierarchyAggregatorNames.isEmpty() && lastMembers != null) {
					int start = memberCount - 1;

					if (containsMeasure) {
						start--;
					}

					if (i < start) {
						Member lastMember = lastMembers.get(i);

						if (OlapUtils.equals(lastMember.getHierarchy(),
								member.getHierarchy())
								&& !OlapUtils.equals(lastMember, member)) {
							for (String aggregatorName : hierarchyAggregatorNames) {
								createAggregators(aggregatorName, nodeContext,
										aggregators, axisRoot, null,
										lastMembers.subList(0, i + 1),
										totalMeasures);
							}
						}
					}
				}

				if (!memberAggregatorNames.isEmpty()) {
					List<AggregationTarget> memberParents = memberParentMap
							.get(member.getHierarchy());

					if (memberParents == null) {
						memberParents = new ArrayList<AggregationTarget>();
						memberParentMap.put(member.getHierarchy(),
								memberParents);
					}

					AggregationTarget lastSibling = null;

					if (!memberParents.isEmpty()) {
						lastSibling = memberParents
								.get(memberParents.size() - 1);
					}

					Member parentMember = axisRoot.getReference()
							.getParentMember(member);

					if (parentMember != null) {
						if (lastSibling == null
								|| axisRoot.getReference()
										.getAncestorMembers(parentMember)
										.contains(lastSibling.getParent())) {
							memberParents.add(new AggregationTarget(
									parentMember, member.getLevel()));
						} else if (!OlapUtils.equals(parentMember,
								lastSibling.getParent())) {
							while (!memberParents.isEmpty()) {
								int lastIndex = memberParents.size() - 1;

								AggregationTarget lastParent = memberParents
										.get(lastIndex);

								if (OlapUtils.equals(lastParent.getParent(),
										parentMember)) {
									break;
								}

								memberParents.remove(lastIndex);

								List<Member> path = new ArrayList<Member>(
										lastMembers.subList(0, i));
								path.add(lastParent.getParent());

								Level parentLevel = lastParent.getParent()
										.getLevel();
								if (!levels.contains(parentLevel)) {
									levels.add(0, parentLevel);
									Collections.sort(levels, levelComparator);
								}

								for (String aggregatorName : memberAggregatorNames) {
									createAggregators(aggregatorName,
											nodeContext, aggregators, axisRoot,
											lastParent.getLevel(), path,
											totalMeasures);
								}
							}
						}
					}
				}
			}

			if (lastChild != null) {
				axisRoot.addChild(lastChild);
			}

			lastMembers = members;
		}

		if (lastMembers != null) {
			int memberCount = lastMembers.size();

			int start = memberCount - 1;

			if (containsMeasure) {
				start--;
			}

			for (int i = start; i >= 0; i--) {
				if (!memberAggregatorNames.isEmpty()) {
					Hierarchy hierarchy = nodeContext.getHierarchies().get(i);

					Level rootLevel = nodeContext.getLevels(hierarchy).get(0);

					List<AggregationTarget> memberParents = memberParentMap
							.get(hierarchy);

					for (AggregationTarget target : memberParents) {
						Member member = target.getParent();

						if (member.getLevel().getDepth() < rootLevel.getDepth()) {
							continue;
						}

						List<Member> path = new ArrayList<Member>(
								lastMembers.subList(0, i));
						path.add(member);

						for (String aggregatorName : memberAggregatorNames) {
							createAggregators(aggregatorName, nodeContext,
									aggregators, axisRoot, target.getLevel(),
									path, totalMeasures);
						}
					}
				}

				if (!hierarchyAggregatorNames.isEmpty()) {
					for (String aggregatorName : hierarchyAggregatorNames) {
						createAggregators(aggregatorName, nodeContext,
								aggregators, axisRoot, null,
								lastMembers.subList(0, i), totalMeasures);
					}
				}
			}

			if (!aggregatorNames.isEmpty()) {
				List<Member> members = Collections.emptyList();

				for (String aggregatorName : aggregatorNames) {
					createAggregators(aggregatorName, nodeContext, aggregators,
							axisRoot, null, members, grandTotalMeasures);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Original axis tree root for " + axis);

			axisRoot.walkTree(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					String label = node.toString();
					logger.debug(StringUtils.leftPad(label, node.getLevel()
							+ label.length(), ' '));

					return CONTINUE;
				}
			});
		}

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
	private void createAggregators(String aggregatorName,
			TableAxisContext context, List<Aggregator> aggregators,
			TableHeaderNode axisRoot, Level level, List<Member> members,
			Set<Measure> measures) {
		AggregatorFactory factory = context.getPivotRenderer()
				.getAggregatorFactory();

		if (measures.isEmpty()) {
			Aggregator aggregator = factory.createAggregator(aggregatorName,
					context.getAxis(), members, level, null);
			if (aggregator != null) {
				aggregators.add(aggregator);

				TableHeaderNode aggNode = createAggregationNode(context,
						aggregator);
				axisRoot.addChild(aggNode);
			}
		} else {
			for (Measure measure : measures) {
				Aggregator aggregator = factory.createAggregator(
						aggregatorName, context.getAxis(), members, level,
						measure);

				if (aggregator != null) {
					aggregators.add(aggregator);

					TableHeaderNode aggNode = createAggregationNode(context,
							aggregator);
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
	protected TableHeaderNode createAggregationNode(
			TableAxisContext nodeContext, Aggregator aggregator) {
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

		if (parent != null && aggregator.getLevel() != null
				&& !nodeContext.getPivotRenderer().getShowParentMembers()) {
			parent.setAggregator(aggregator);
		}

		int index = Math.min(nodeContext.getHierarchies().size() - 1,
				members.size());

		Hierarchy hierarchy;

		if (aggregator.getLevel() == null) {
			hierarchy = nodeContext.getHierarchies().get(index);
		} else {
			hierarchy = aggregator.getLevel().getHierarchy();
		}

		if (aggregator.getLevel() == null
				|| nodeContext.getPivotRenderer().getShowParentMembers()) {
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
	 * @param renderer
	 * @param axis
	 * @param node
	 */
	protected void configureAxisTree(PivotModel model, PivotRenderer renderer,
			Axis axis, TableHeaderNode node) {
		if (renderer.getShowDimensionTitle() && axis.equals(Axis.COLUMNS)) {
			node.addHierarhcyHeaders();
		}

		if (renderer.getShowParentMembers()) {
			node.addParentMemberHeaders();
		}

		if (!renderer.getHideSpans()) {
			node.mergeChildren();
		}

		if (renderer.getPropertyCollector() != null) {
			node.addMemberProperties();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Configured axis tree root for " + axis);

			node.walkTree(new TreeNodeCallback<TableAxisContext>() {

				@Override
				public int handleTreeNode(TreeNode<TableAxisContext> node) {
					String label = node.toString();
					logger.debug(StringUtils.leftPad(label, node.getLevel()
							+ label.length(), ' '));

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
	protected void invalidateAxisTree(PivotModel model, Axis axis,
			TableHeaderNode node) {
		node.walkChildren(new TreeNodeCallback<TableAxisContext>() {

			@Override
			public int handleTreeNode(TreeNode<TableAxisContext> node) {
				TableHeaderNode headerNode = (TableHeaderNode) node;
				headerNode.clearCache();
				return TreeNodeCallback.CONTINUE;
			}
		});
	}

	protected ExpressionEvaluatorFactory createExpressionEvaluatorFactory() {
		return new FreeMarkerExpressionEvaluatorFactory();
	}

	/**
	 * @return the expressionEvaluatorFactory
	 */
	protected ExpressionEvaluatorFactory getExpressionEvaluatorFactory() {
		return expressionEvaluatorFactory;
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
}
