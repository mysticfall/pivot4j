/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
import org.pivot4j.PivotModel;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.ui.aggregator.Aggregator;
import org.pivot4j.ui.property.RenderPropertyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RenderContext {

	public static final String RESOURCE_BUNDLE_NAME = "org.pivot4j.i18n.messages";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private PivotModel model;

	private PivotRenderer<?> renderer;

	private ResourceBundle resourceBundle;

	private ExpressionContext expressionContext;

	private ExpressionEvaluator expressionEvaluator;

	private Axis axis;

	private Hierarchy hierarchy;

	private Level level;

	private Member member;

	private Property property;

	private Position position;

	private Cell cell;

	private String cellType;

	private String renderPropertyCategory;

	private Aggregator aggregator;

	private Map<String, Object> attributes;

	/**
	 * @param model
	 * @param renderer
	 */
	public RenderContext(PivotModel model, PivotRenderer<?> renderer) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (renderer == null) {
			throw new NullArgumentException("renderer");
		}

		this.model = model;
		this.renderer = renderer;

		this.resourceBundle = createDefaultResourceBundle(model);

		this.expressionContext = createExpressionContext(model);
		this.expressionEvaluator = model.getExpressionEvaluatorFactory()
				.createEvaluator();

		this.attributes = new HashMap<String, Object>();
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger
	 *            the logger to set
	 */
	public void setLogger(Logger logger) {
		if (logger == null) {
			throw new NullArgumentException("logger");
		}

		this.logger = logger;
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
	public PivotRenderer<?> getRenderer() {
		return renderer;
	}

	/**
	 * @return the expressionContext
	 */
	public ExpressionContext getExpressionContext() {
		return expressionContext;
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
				return axis;
			}
		});

		context.put("hierarchy",
				new ExpressionContext.ValueBinding<Hierarchy>() {

					@Override
					public Hierarchy getValue() {
						return hierarchy;
					}
				});

		context.put("level", new ExpressionContext.ValueBinding<Level>() {

			@Override
			public Level getValue() {
				return level;
			}
		});

		context.put("member", new ExpressionContext.ValueBinding<Member>() {

			@Override
			public Member getValue() {
				return member;
			}
		});

		context.put("cell", new ExpressionContext.ValueBinding<Cell>() {

			@Override
			public Cell getValue() {
				return cell;
			}
		});

		context.put("cellType", new ExpressionContext.ValueBinding<String>() {

			@Override
			public String getValue() {
				return cellType;
			}
		});

		context.put("renderPropertyCategory",
				new ExpressionContext.ValueBinding<String>() {

					@Override
					public String getValue() {
						return renderPropertyCategory;
					}
				});

		context.put("position", new ExpressionContext.ValueBinding<Position>() {

			@Override
			public Position getValue() {
				return position;
			}
		});

		context.put("property", new ExpressionContext.ValueBinding<Property>() {

			@Override
			public Property getValue() {
				return property;
			}
		});

		context.put("aggregator",
				new ExpressionContext.ValueBinding<Aggregator>() {

					@Override
					public Aggregator getValue() {
						return aggregator;
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

	/**
	 * @return the expressionEvaluator
	 */
	public ExpressionEvaluator getExpressionEvaluator() {
		return expressionEvaluator;
	}

	/**
	 * @return the resourceBundle
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * @param resourceBundle
	 */
	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	/**
	 * @param model
	 * @return
	 */
	protected ResourceBundle createDefaultResourceBundle(PivotModel model) {
		return ResourceBundle
				.getBundle(RESOURCE_BUNDLE_NAME, model.getLocale());
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

	public abstract List<Axis> getAxes();

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

	/**
	 * @return the cellType
	 */
	public String getCellType() {
		return cellType;
	}

	/**
	 * @param cellType
	 *            the cellType to set
	 */
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}

	/**
	 * @return the renderPropertyCategory
	 */
	public String getRenderPropertyCategory() {
		return renderPropertyCategory;
	}

	/**
	 * @param renderPropertyCategory
	 *            the renderPropertyCategory to set
	 */
	public void setRenderPropertyCategory(String renderPropertyCategory) {
		this.renderPropertyCategory = renderPropertyCategory;
	}

	/**
	 * @return the position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Position position) {
		this.position = position;
	}

	/**
	 * @param axis
	 * @return
	 */
	public abstract Position getPosition(Axis axis);

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
	 * @param axis
	 * @return
	 */
	public abstract Position getAggregationTarget(Axis axis);

	/**
	 * @return the cellSetAxis
	 */
	public CellSetAxis getCellSetAxis() {
		if (axis == null) {
			return null;
		}

		return getCellSet().getAxes().get(axis.axisOrdinal());
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
	 * @return
	 */
	public Map<String, RenderPropertyList> getRenderProperties() {
		return renderer.getRenderProperties();
	}
}
