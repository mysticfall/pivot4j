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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Property;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.el.ExpressionContext;
import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;

public class RenderContext {

	private PivotModel model;

	private PivotRenderer renderer;

	private Axis axis;

	private Position columnPosition;

	private Position rowPosition;

	private Hierarchy hierarchy;

	private Member member;

	private Property property;

	private Level level;

	private Cell cell;

	private CellType cellType;

	private Aggregator aggregator;

	private int columnCount;

	private int rowCount;

	private int columnHeaderCount;

	private int rowHeaderCount;

	private int colIndex;

	private int rowIndex;

	private int colSpan = 1;

	private int rowSpan = 1;

	private ExpressionContext expressionContext;

	private ExpressionEvaluator expressionEvaluator;

	private Map<String, Member> cachedParents;

	private Map<String, Object> attributes;

	/**
	 * @param model
	 * @param renderer
	 * @param columnCount
	 * @param rowCount
	 * @param columnHeaderCount
	 * @param rowHeaderCount
	 * @param expressionEvaluator
	 * @param cachedParents
	 */
	public RenderContext(PivotModel model, PivotRenderer renderer,
			int columnCount, int rowCount, int columnHeaderCount,
			int rowHeaderCount, ExpressionEvaluator expressionEvaluator,
			Map<String, Member> cachedParents) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (renderer == null) {
			throw new NullArgumentException("renderer");
		}

		if (expressionEvaluator == null) {
			throw new NullArgumentException("expressionEvaluator");
		}

		if (columnCount < 0) {
			throw new IllegalArgumentException(
					"Column count should be ZERO or positive integer.");
		}

		if (rowCount < 0) {
			throw new IllegalArgumentException(
					"Row count should be ZERO or positive integer.");
		}

		if (columnHeaderCount < 0) {
			throw new IllegalArgumentException(
					"Column header count should be ZERO or positive integer.");
		}

		if (rowHeaderCount < 0) {
			throw new IllegalArgumentException(
					"Row header count should be ZERO or positive integer.");
		}

		this.model = model;
		this.renderer = renderer;
		this.columnCount = columnCount;
		this.rowCount = rowCount;
		this.columnHeaderCount = columnHeaderCount;
		this.rowHeaderCount = rowHeaderCount;

		this.expressionContext = createExpressionContext(model);
		this.expressionEvaluator = expressionEvaluator;

		if (cachedParents == null) {
			this.cachedParents = new HashMap<String, Member>();
		} else {
			this.cachedParents = cachedParents;
		}

		this.attributes = new HashMap<String, Object>();
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		return model;
	}

	/**
	 * @return the renderer
	 */
	public PivotRenderer getRenderer() {
		return renderer;
	}

	/**
	 * @return the cellSet
	 */
	public CellSet getCellSet() {
		return model.getCellSet();
	}

	/**
	 * @return the axis
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            the axis to set
	 */
	public void setAxis(Axis axis) {
		this.axis = axis;
	}

	/**
	 * @return the hierarchy
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(Member member) {
		this.member = member;
	}

	/**
	 * @return the property
	 */
	public Property getProperty() {
		return property;
	}

	/**
	 * @param property
	 *            the property to set
	 */
	public void setProperty(Property property) {
		this.property = property;
	}

	/**
	 * @return the level
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(Level level) {
		this.level = level;
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

	public Position getPosition() {
		if (axis == null) {
			return null;
		}

		if (axis.equals(Axis.COLUMNS)) {
			return columnPosition;
		} else if (axis.equals(Axis.ROWS)) {
			return rowPosition;
		} else {
			return null;
		}
	}

	/**
	 * @return the columnPosition
	 */
	public Position getColumnPosition() {
		return columnPosition;
	}

	/**
	 * @param columnPosition
	 *            the columnPosition to set
	 */
	public void setColumnPosition(Position columnPosition) {
		this.columnPosition = columnPosition;
	}

	/**
	 * @return the rowPosition
	 */
	public Position getRowPosition() {
		return rowPosition;
	}

	/**
	 * @param rowPosition
	 *            the rowPosition to set
	 */
	public void setRowPosition(Position rowPosition) {
		this.rowPosition = rowPosition;
	}

	/**
	 * @return the cellType
	 */
	public CellType getCellType() {
		return cellType;
	}

	/**
	 * @param cellType
	 *            the cellType to set
	 */
	public void setCellType(CellType cellType) {
		this.cellType = cellType;
	}

	/**
	 * @return the columnCount
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * @return the columnHeaderCount
	 */
	public int getColumnHeaderCount() {
		return columnHeaderCount;
	}

	/**
	 * @return the rowHeaderCount
	 */
	public int getRowHeaderCount() {
		return rowHeaderCount;
	}

	/**
	 * @return the colIndex
	 */
	public int getColIndex() {
		return colIndex;
	}

	/**
	 * @param colIndex
	 *            the colIndex to set
	 */
	public void setColIndex(int colIndex) {
		this.colIndex = colIndex;
	}

	/**
	 * @return the rowIndex
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * @param rowIndex
	 *            the rowIndex to set
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 * @return the colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * @param colSpan
	 *            the colSpan to set
	 */
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}

	/**
	 * @return the rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * @param rowSpan
	 *            the rowSpan to set
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	/**
	 * @return the aggregator
	 */
	public Aggregator getAggregator() {
		return aggregator;
	}

	/**
	 * @param aggregator
	 *            the aggregator to set
	 */
	public void setAggregator(Aggregator aggregator) {
		this.aggregator = aggregator;
	}

	/**
	 * @return
	 */
	public CellSetAxis getCellSetAxis() {
		if (axis == null) {
			return null;
		}

		return getCellSet().getAxes().get(axis.axisOrdinal());
	}

	/**
	 * Temporary workaround for performance issue.
	 * 
	 * See http://jira.pentaho.com/browse/MONDRIAN-1292
	 * 
	 * @param member
	 * @return
	 */
	public Member getParentMember(Member member) {
		Member parent = cachedParents.get(member.getUniqueName());

		if (parent == null) {
			parent = member.getParentMember();
			cachedParents.put(member.getUniqueName(), parent);
		}

		return parent;
	}

	/**
	 * Temporary workaround for performance issue.
	 * 
	 * See http://jira.pentaho.com/browse/MONDRIAN-1292
	 * 
	 * @param member
	 * @return
	 */
	public List<Member> getAncestorMembers(Member member) {
		List<Member> ancestors = new ArrayList<Member>();

		Member parent = member;

		while ((parent = getParentMember(parent)) != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}

	/**
	 * @return the expressionContext
	 */
	public ExpressionContext getExpressionContext() {
		return expressionContext;
	}

	/**
	 * @return the expressionEvaluator
	 */
	public ExpressionEvaluator getExpressionEvaluator() {
		return expressionEvaluator;
	}

	/**
	 * @param name
	 * @return
	 */
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	/**
	 * @param name
	 * @return
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	/**
	 * @param name
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	/**
	 * @param model
	 * @return
	 */
	protected ExpressionContext createExpressionContext(PivotModel model) {
		ExpressionContext context = new ExpressionContext(
				model.getExpressionContext());

		context.put("axis", new ExpressionContext.ValueBinding<Axis>() {

			@Override
			public Axis getValue() {
				return getAxis();
			}
		});

		context.put("axisName", new ExpressionContext.ValueBinding<String>() {

			@Override
			public String getValue() {
				Axis theAxis = getAxis();

				if (theAxis == null) {
					return null;
				}

				return theAxis.name();
			}
		});

		context.put("axisOridinal",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						Axis theAxis = getAxis();

						if (theAxis == null) {
							return null;
						}

						return theAxis.axisOrdinal();
					}
				});

		context.put("hierarchy",
				new ExpressionContext.ValueBinding<Hierarchy>() {

					@Override
					public Hierarchy getValue() {
						return getHierarchy();
					}
				});

		context.put("level", new ExpressionContext.ValueBinding<Level>() {

			@Override
			public Level getValue() {
				return getLevel();
			}
		});

		context.put("member", new ExpressionContext.ValueBinding<Member>() {

			@Override
			public Member getValue() {
				return getMember();
			}
		});

		context.put("cell", new ExpressionContext.ValueBinding<Cell>() {

			@Override
			public Cell getValue() {
				return getCell();
			}
		});

		context.put("cellType", new ExpressionContext.ValueBinding<CellType>() {

			@Override
			public CellType getValue() {
				return getCellType();
			}
		});

		context.put("position", new ExpressionContext.ValueBinding<Position>() {

			@Override
			public Position getValue() {
				return getPosition();
			}
		});

		context.put("columnPosition",
				new ExpressionContext.ValueBinding<Position>() {

					@Override
					public Position getValue() {
						return getColumnPosition();
					}
				});

		context.put("rowPosition",
				new ExpressionContext.ValueBinding<Position>() {

					@Override
					public Position getValue() {
						return getRowPosition();
					}
				});

		context.put("property", new ExpressionContext.ValueBinding<Property>() {

			@Override
			public Property getValue() {
				return getProperty();
			}
		});

		context.put("columnCount",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return getColumnCount();
					}
				});

		context.put("rowCount", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return getRowCount();
			}
		});

		context.put("columnHeaderCount",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return getColumnHeaderCount();
					}
				});

		context.put("rowHeaderCount",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return getRowHeaderCount();
					}
				});

		context.put("colIndex", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return getColIndex();
			}
		});

		context.put("rowIndex", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return getRowIndex();
			}
		});

		context.put("colSpan", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return getColSpan();
			}
		});

		context.put("rowSpan", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return getRowSpan();
			}
		});

		context.put("aggregator",
				new ExpressionContext.ValueBinding<Aggregator>() {

					@Override
					public Aggregator getValue() {
						return getAggregator();
					}
				});

		context.put("attributes",
				new ExpressionContext.ValueBinding<Map<String, Object>>() {

					@Override
					public Map<String, Object> getValue() {
						return attributes;
					}
				});

		return context;
	}
}
