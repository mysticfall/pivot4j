/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.LogFactory;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.slf4j.Logger;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.el.EvaluationFailedException;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.ui.aggregator.AggregatorFactory;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;
import com.eyeq.pivot4j.ui.aggregator.DefaultAggregatorFactory;
import com.eyeq.pivot4j.ui.condition.ConditionFactory;
import com.eyeq.pivot4j.ui.condition.DefaultConditionFactory;
import com.eyeq.pivot4j.ui.impl.RenderStrategyImpl;
import com.eyeq.pivot4j.ui.property.PropertySource;
import com.eyeq.pivot4j.ui.property.PropertySupport;

public abstract class AbstractPivotRenderer implements PivotRenderer,
		PivotLayoutCallback {

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

	private PropertyCollector propertyCollector;

	private RenderStrategy renderStrategy;

	private AggregatorFactory aggregatorFactory = new DefaultAggregatorFactory();

	private ConditionFactory conditionFactory = new DefaultConditionFactory();

	private Map<AggregatorKey, List<String>> aggregatorNames = new HashMap<AggregatorKey, List<String>>();

	private PropertySupport cellProperties;

	private PropertySupport headerProperties;

	private boolean suppressPropertyErrors = true;

	public AbstractPivotRenderer() {
		this.renderStrategy = createRenderStrategy();
		initializeProperties();
	}

	protected void initializeProperties() {
		if (conditionFactory == null) {
			this.cellProperties = null;
			this.headerProperties = null;
		} else {
			this.cellProperties = new PropertySupport(conditionFactory);
			this.headerProperties = new PropertySupport(conditionFactory);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		if (renderStrategy == null) {
			throw new IllegalStateException("Renderer was not initialized yet.");
		}

		renderStrategy.render(model, this, this);
	}

	/**
	 * @return the hideSpans
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getHideSpans()
	 */
	public boolean getHideSpans() {
		return hideSpans;
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setHideSpans(boolean)
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * @return the showParentMembers
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowParentMembers()
	 */
	public boolean getShowParentMembers() {
		return showParentMembers;
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowParentMembers(boolean)
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	/**
	 * @return the showDimensionTitle
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowDimensionTitle()
	 */
	public boolean getShowDimensionTitle() {
		return showDimensionTitle;
	}

	/**
	 * @param showDimensionTitle
	 *            the showDimensionTitle to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowDimensionTitle(boolean)
	 */
	public void setShowDimensionTitle(boolean showDimensionTitle) {
		this.showDimensionTitle = showDimensionTitle;
	}

	/**
	 * @return the propertyCollector
	 */
	public PropertyCollector getPropertyCollector() {
		return propertyCollector;
	}

	/**
	 * @param propertyCollector
	 *            the propertyCollector to set
	 */
	public void setPropertyCollector(PropertyCollector propertyCollector) {
		this.propertyCollector = propertyCollector;
	}

	/**
	 * @return renderStrategy
	 */
	protected RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}

	/**
	 * @return
	 */
	protected RenderStrategy createRenderStrategy() {
		return new RenderStrategyImpl();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public void cellContent(RenderContext context) {
		cellContent(context, getCellLabel(context));
	}

	public abstract void cellContent(RenderContext context, String label);

	/**
	 * @param context
	 * @return
	 */
	protected String getCellLabel(RenderContext context) {
		String label;

		Cell cell = context.getCell();

		switch (context.getCellType()) {
		case Header:
			if (context.getProperty() == null) {
				label = context.getMember().getCaption();
			} else {
				try {
					label = context.getMember().getPropertyFormattedValue(
							context.getProperty());
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}
			break;
		case Title:
			if (context.getProperty() != null) {
				label = context.getProperty().getCaption();
			} else if (context.getLevel() != null) {
				label = context.getLevel().getCaption();
			} else if (context.getHierarchy() != null) {
				label = context.getHierarchy().getCaption();
			} else {
				label = null;
			}
			break;
		case Value:
			if (cell == null) {
				Aggregator aggregator = context.getAggregator();

				if (aggregator == null) {
					label = null;
				} else {
					label = aggregator.getFormattedValue(context);
				}
			} else {
				label = cell.getFormattedValue();
			}

			break;
		case Aggregation:
			Aggregator aggregator = context.getAggregator();

			if (aggregator == null && context.getMember() != null) {
				label = context.getMember().getCaption();
			} else {
				label = aggregator.getLabel(context);
			}

			break;
		case None:
		default:
			label = null;
			break;
		}

		PropertySupport properties = getProperties(context);
		if (label != null && properties != null) {
			label = properties.getString("label", label, context);
		}

		return label;
	}

	/**
	 * @return the aggregatorFactory
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getAggregatorFactory()
	 */
	public AggregatorFactory getAggregatorFactory() {
		return aggregatorFactory;
	}

	/**
	 * @param aggregatorFactory
	 *            the aggregatorFactory to set
	 */
	public void setAggregatorFactory(AggregatorFactory aggregatorFactory) {
		this.aggregatorFactory = aggregatorFactory;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getAggregators(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition)
	 */
	@Override
	public List<String> getAggregators(Axis axis, AggregatorPosition position) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		List<String> names = aggregatorNames.get(new AggregatorKey(axis,
				position));

		if (names == null) {
			names = Collections.emptyList();
		}

		return names;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#addAggregator(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.lang.String)
	 */
	@Override
	public void addAggregator(Axis axis, AggregatorPosition position,
			String name) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		List<String> names = aggregatorNames.get(key);

		if (names == null) {
			names = new ArrayList<String>();
			aggregatorNames.put(key, names);
		}

		if (!names.contains(name)) {
			names.add(name);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#removeAggregator(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.lang.String)
	 */
	@Override
	public void removeAggregator(Axis axis, AggregatorPosition position,
			String name) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		List<String> names = aggregatorNames.get(key);

		if (names != null) {
			names.remove(name);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setAggregators(org.olap4j.Axis,
	 *      com.eyeq.pivot4j.ui.aggregator.AggregatorPosition, java.util.List)
	 */
	@Override
	public void setAggregators(Axis axis, AggregatorPosition position,
			List<String> names) {
		if (axis == null) {
			throw new NullArgumentException("axis");
		}

		if (position == null) {
			throw new NullArgumentException("position");
		}

		AggregatorKey key = new AggregatorKey(axis, position);

		if (names == null || names.isEmpty()) {
			aggregatorNames.remove(key);
		} else {
			aggregatorNames.put(key, names);
		}
	}

	/**
	 * @return the conditionFactory
	 */
	public ConditionFactory getConditionFactory() {
		return conditionFactory;
	}

	/**
	 * @param conditionFactory
	 *            the conditionFactory to set
	 */
	public void setConditionFactory(ConditionFactory conditionFactory) {
		this.conditionFactory = conditionFactory;
	}

	/**
	 * @return the cellProperties
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getCellProperties()
	 */
	public PropertySource getCellProperties() {
		return cellProperties;
	}

	/**
	 * @return the headerProperties
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getHeaderProperties()
	 */
	public PropertySource getHeaderProperties() {
		return headerProperties;
	}

	/**
	 * @return the suppressPropertyErrors
	 */
	public boolean getSuppressPropertyErrors() {
		return suppressPropertyErrors;
	}

	/**
	 * @param suppressPropertyErrors
	 *            the suppressPropertyErrors to set
	 */
	public void setSuppressPropertyErrors(boolean suppressPropertyErrors) {
		this.suppressPropertyErrors = suppressPropertyErrors;
	}

	/**
	 * @param axis
	 * @param type
	 * @return
	 */
	protected PropertySupport getProperties(RenderContext context) {
		PropertySupport properties = null;

		if (context.getCellType() == CellType.Header
				|| context.getCellType() == CellType.Title) {
			properties = headerProperties;
		} else {
			properties = cellProperties;
		}

		return properties;
	}

	/**
	 * @param key
	 * @param properties
	 * @param context
	 * @return
	 */
	protected String getPropertyValue(String key, PropertySupport properties,
			RenderContext context) {
		String value = null;

		try {
			value = properties.getString(key, null, context);
		} catch (EvaluationFailedException e) {
			onPropertyEvaluationFailure(key, context, e);
		}

		return value;
	}

	/**
	 * @param key
	 * @param context
	 * @param e
	 */
	protected void onPropertyEvaluationFailure(String key,
			RenderContext context, EvaluationFailedException e) {
		if (suppressPropertyErrors) {
			Logger logger = context.getLogger();

			if (logger.isWarnEnabled()) {
				logger.warn(String.format(
						"Property evaluation failed for '%s' : %s", key,
						e.toString()), e);
			}
		} else {
			throw e;
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#swapAxes()
	 */
	@Override
	public void swapAxes() {
		HashMap<AggregatorKey, List<String>> map = new HashMap<AggregatorKey, List<String>>(
				aggregatorNames.size());

		for (AggregatorKey key : aggregatorNames.keySet()) {
			List<String> names = aggregatorNames.get(key);

			if (key.axis == Axis.ROWS) {
				map.put(new AggregatorKey(Axis.COLUMNS, key.position), names);
			} else {
				map.put(new AggregatorKey(Axis.ROWS, key.position), names);
			}
		}

		this.aggregatorNames = map;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[6];
		states[0] = showDimensionTitle;
		states[1] = showParentMembers;
		states[2] = hideSpans;
		states[3] = (Serializable) aggregatorNames;

		if (cellProperties != null) {
			states[4] = cellProperties.saveState();
		}

		if (headerProperties != null) {
			states[5] = headerProperties.saveState();
		}

		return states;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.PropertySupport#restoreState(java.io.Serializable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		Serializable[] states = (Serializable[]) state;

		this.showDimensionTitle = (Boolean) states[0];
		this.showParentMembers = (Boolean) states[1];
		this.hideSpans = (Boolean) states[2];
		this.aggregatorNames = (Map<AggregatorKey, List<String>>) states[3];

		initializeProperties();

		if (states[4] != null && cellProperties != null) {
			this.cellProperties.restoreState(states[4]);
		}

		if (states[5] != null && headerProperties != null) {
			this.headerProperties.restoreState(states[5]);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.PropertySupport#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		configuration.setDelimiterParsingDisabled(true);

		if (configuration.getLogger() == null) {
			configuration.setLogger(LogFactory.getLog(getClass()));
		}

		configuration.addProperty("showDimensionTitle", showDimensionTitle);
		configuration.addProperty("showParentMembers", showParentMembers);
		configuration.addProperty("hideSpans", hideSpans);

		if (!aggregatorNames.isEmpty()) {
			int index = 0;

			for (AggregatorKey key : aggregatorNames.keySet()) {
				Axis axis = key.getAxis();
				AggregatorPosition position = key.getPosition();

				List<String> names = aggregatorNames.get(key);

				for (String name : names) {
					configuration
							.addProperty(String.format(
									"aggregations.aggregation(%s)[@name]",
									index), name);
					configuration.addProperty(String.format(
							"aggregations.aggregation(%s)[@axis]", index), axis
							.name());
					configuration.addProperty(String.format(
							"aggregations.aggregation(%s)[@position]", index),
							position.name());

					index++;
				}
			}
		}

		if (cellProperties != null) {
			configuration.addProperty("properties.cell", "");

			cellProperties.saveSettings(configuration
					.configurationAt("properties.cell"));
		}

		if (headerProperties != null) {
			configuration.addProperty("properties.header", "");

			headerProperties.saveSettings(configuration
					.configurationAt("properties.header"));
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		this.showDimensionTitle = configuration.getBoolean(
				"showDimensionTitle", true);
		this.showParentMembers = configuration.getBoolean("showParentMembers",
				false);
		this.hideSpans = configuration.getBoolean("hideSpans", false);

		List<HierarchicalConfiguration> aggregationSettings = configuration
				.configurationsAt("aggregations.aggregation");

		this.aggregatorNames.clear();

		for (HierarchicalConfiguration aggConfig : aggregationSettings) {
			String name = aggConfig.getString("[@name]");

			if (name != null) {
				Axis axis = Axis.Standard.valueOf(aggConfig
						.getString("[@axis]"));

				AggregatorPosition position = AggregatorPosition
						.valueOf(aggConfig.getString("[@position]"));

				AggregatorKey key = new AggregatorKey(axis, position);

				List<String> names = aggregatorNames.get(key);

				if (names == null) {
					names = new LinkedList<String>();
					aggregatorNames.put(key, names);
				}

				if (!names.contains(name)) {
					names.add(name);
				}
			}
		}

		initializeProperties();

		if (cellProperties != null) {
			try {
				cellProperties.restoreSettings(configuration
						.configurationAt("properties.cell"));
			} catch (IllegalArgumentException e) {
			}
		}

		if (headerProperties != null) {
			try {
				headerProperties.restoreSettings(configuration
						.configurationAt("properties.header"));
			} catch (IllegalArgumentException e) {
			}
		}
	}

	static class AggregatorKey implements Serializable {

		private static final long serialVersionUID = 4244611391569825053L;

		private Axis axis;

		private AggregatorPosition position;

		AggregatorKey() {
		}

		/**
		 * @param axis
		 * @param position
		 */
		AggregatorKey(Axis axis, AggregatorPosition position) {
			if (axis == null) {
				throw new NullArgumentException("axis");
			}

			if (position == null) {
				throw new NullArgumentException("position");
			}

			this.axis = axis;
			this.position = position;
		}

		/**
		 * @return the axis
		 */
		Axis getAxis() {
			return axis;
		}

		/**
		 * @return the position
		 */
		AggregatorPosition getPosition() {
			return position;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(axis).append(position)
					.toHashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}

			if (getClass() != obj.getClass()) {
				return false;
			}

			AggregatorKey otherKey = (AggregatorKey) obj;

			return axis == otherKey.axis && position == otherKey.position;
		}
	}
}
