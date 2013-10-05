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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.sort.SortMode;
import com.eyeq.pivot4j.ui.aggregator.AggregatorFactory;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;
import com.eyeq.pivot4j.ui.aggregator.DefaultAggregatorFactory;
import com.eyeq.pivot4j.ui.collector.PropertyCollector;
import com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapseMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapsePositionCommand;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.ui.command.DrillDownReplaceCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandPositionCommand;
import com.eyeq.pivot4j.ui.command.DrillUpReplaceCommand;
import com.eyeq.pivot4j.ui.command.ToggleSortCommand;
import com.eyeq.pivot4j.ui.command.UICommand;
import com.eyeq.pivot4j.ui.condition.ConditionFactory;
import com.eyeq.pivot4j.ui.condition.DefaultConditionFactory;
import com.eyeq.pivot4j.ui.property.DefaultRenderPropertyList;
import com.eyeq.pivot4j.ui.property.RenderPropertyList;

public abstract class AbstractPivotRenderer<T1 extends RenderContext, T2 extends RenderCallback<T1>>
		implements PivotRenderer<T2> {

	private Map<String, UICommand<?>> commands = new HashMap<String, UICommand<?>>();

	private boolean enableSort = true;

	private SortMode sortMode = SortMode.BASIC;

	private boolean enableDrillDown = true;

	private String drillDownMode = DrillDownCommand.MODE_POSITION;

	private boolean enableDrillThrough = false;

	private boolean renderSlicer = false;

	private PropertyCollector propertyCollector;

	private ConditionFactory conditionFactory;

	private AggregatorFactory aggregatorFactory;

	private Map<AggregatorKey, List<String>> aggregatorNames;

	private Map<String, RenderPropertyList> renderProperties;

	public AbstractPivotRenderer() {
		this.conditionFactory = new DefaultConditionFactory();
		this.aggregatorFactory = new DefaultAggregatorFactory();
		this.aggregatorNames = new HashMap<AggregatorKey, List<String>>();
		this.renderProperties = new HashMap<String, RenderPropertyList>();

		registerCommands();
		initializeRenderProperties();
	}

	protected abstract String getLabel(T1 context);

	protected void registerCommands() {
		addCommand(new DrillExpandPositionCommand(this));
		addCommand(new DrillCollapsePositionCommand(this));
		addCommand(new DrillExpandMemberCommand(this));
		addCommand(new DrillCollapseMemberCommand(this));
		addCommand(new DrillDownReplaceCommand(this));
		addCommand(new DrillUpReplaceCommand(this));
		addCommand(new ToggleSortCommand(this));
		addCommand(new BasicDrillThroughCommand(this));
	}

	protected List<String> getRenderPropertyCategories() {
		return Collections.emptyList();
	}

	protected void initializeRenderProperties() {
		renderProperties.clear();

		if (conditionFactory != null) {
			for (String category : getRenderPropertyCategories()) {
				renderProperties.put(category, new DefaultRenderPropertyList(
						conditionFactory));
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getCommand(java.lang.String)
	 */
	@Override
	public UICommand<?> getCommand(String name) {
		return commands.get(name);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#addCommand(com.eyeq.pivot4j.ui.command.UICommand)
	 */
	@Override
	public void addCommand(UICommand<?> command) {
		commands.put(command.getName(), command);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#removeCommand(java.lang.String)
	 */
	@Override
	public void removeCommand(String name) {
		commands.remove(name);
	}

	/**
	 * @param context
	 * @return
	 */
	protected List<UICommand<?>> getCommands(T1 context) {
		List<UICommand<?>> availableCommands = new ArrayList<UICommand<?>>(
				commands.size());
		for (UICommand<?> command : commands.values()) {
			if (command.canExecute(context)) {
				availableCommands.add(command);
			}
		}

		return availableCommands;
	}

	/**
	 * @return
	 */
	@Override
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @param sortMode
	 *            the sortMode to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setSortMode(com.eyeq.pivot4j.sort.SortMode)
	 */
	@Override
	public void setSortMode(SortMode sortMode) {
		this.sortMode = sortMode;
	}

	/**
	 * @return the drillDownMode
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getDrillDownMode()
	 */
	@Override
	public String getDrillDownMode() {
		return drillDownMode;
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setDrillDownMode(java.lang.String)
	 */
	@Override
	public void setDrillDownMode(String drillDownMode) {
		this.drillDownMode = drillDownMode;
	}

	/**
	 * @return the enableDrillDown
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableDrillDown()
	 */
	@Override
	public boolean getEnableDrillDown() {
		return enableDrillDown;
	}

	/**
	 * @param enableDrillDown
	 *            the enableDrillDown to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableDrillDown(boolean)
	 */
	@Override
	public void setEnableDrillDown(boolean enableDrillDown) {
		this.enableDrillDown = enableDrillDown;
	}

	/**
	 * @return the enableSort
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableSort()
	 */
	@Override
	public boolean getEnableSort() {
		return enableSort;
	}

	/**
	 * @param enableSort
	 *            the enableSort to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableSort(boolean)
	 */
	@Override
	public void setEnableSort(boolean enableSort) {
		this.enableSort = enableSort;
	}

	/**
	 * @return the enableDrillThrough
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableDrillThrough()
	 */
	@Override
	public boolean getEnableDrillThrough() {
		return enableDrillThrough;
	}

	/**
	 * @param enableDrillThrough
	 *            the enableDrillThrough to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableDrillThrough(boolean)
	 */
	@Override
	public void setEnableDrillThrough(boolean enableDrillThrough) {
		this.enableDrillThrough = enableDrillThrough;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getRenderSlicer()
	 */
	@Override
	public boolean getRenderSlicer() {
		return renderSlicer;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setRenderSlicer(boolean)
	 */
	@Override
	public void setRenderSlicer(boolean renderSlicer) {
		this.renderSlicer = renderSlicer;
	}

	/**
	 * @return the propertyCollector
	 */
	@Override
	public PropertyCollector getPropertyCollector() {
		return propertyCollector;
	}

	/**
	 * @param propertyCollector
	 *            the propertyCollector to set
	 */
	@Override
	public void setPropertyCollector(PropertyCollector propertyCollector) {
		this.propertyCollector = propertyCollector;
	}

	/**
	 * @return the aggregatorFactory
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getAggregatorFactory()
	 */
	@Override
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
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getRenderProperties()
	 */
	@Override
	public Map<String, RenderPropertyList> getRenderProperties() {
		return renderProperties;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getRenderPropertyCategory(T1 context) {
		return null;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[8];

		int index = 0;

		states[index++] = enableDrillDown;
		states[index++] = drillDownMode;
		states[index++] = enableSort;
		states[index++] = sortMode;
		states[index++] = enableDrillThrough;
		states[index++] = renderSlicer;
		states[index++] = (Serializable) aggregatorNames;

		HashMap<String, Serializable> propertyState = new HashMap<String, Serializable>(
				renderProperties.size());

		for (String category : getRenderPropertyCategories()) {
			RenderPropertyList properties = renderProperties.get(category);

			if (properties != null) {
				propertyState.put(category, properties.saveState());
			}
		}

		states[index++] = propertyState;

		return states;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.DefaultRenderPropertyList#restoreState(java.io.Serializable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		Serializable[] states = (Serializable[]) state;

		int index = 0;

		this.enableDrillDown = (Boolean) states[index++];
		this.drillDownMode = (String) states[index++];
		this.enableSort = (Boolean) states[index++];
		this.sortMode = (SortMode) states[index++];
		this.enableDrillThrough = (Boolean) states[index++];
		this.renderSlicer = (Boolean) states[index++];
		this.aggregatorNames = (Map<AggregatorKey, List<String>>) states[index++];

		initializeRenderProperties();

		Map<String, Serializable> propertyStates = (Map<String, Serializable>) states[index++];

		for (String category : getRenderPropertyCategories()) {
			RenderPropertyList properties = renderProperties.get(category);
			Serializable propertyState = propertyStates.get(category);

			if (properties != null && propertyState != null) {
				properties.restoreState(propertyState);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.DefaultRenderPropertyList#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
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

		configuration.addProperty("drillDown[@mode]", drillDownMode);
		configuration.addProperty("drillDown[@enabled]", enableDrillDown);

		configuration.addProperty("sort[@enabled]", enableSort);

		if (sortMode != null) {
			configuration.addProperty("sort[@mode]", sortMode.getName());
		}

		configuration.addProperty("drillThrough[@enabled]", enableDrillThrough);

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

		for (String category : renderProperties.keySet()) {
			RenderPropertyList properties = renderProperties.get(category);

			if (properties != null) {
				String propertyConfigName = "properties." + category;

				configuration.addProperty(propertyConfigName, "");

				properties.saveSettings(configuration
						.configurationAt(propertyConfigName));
			}
		}

		configuration.addProperty("filter[@visible]", renderSlicer);
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new NullArgumentException("configuration");
		}

		this.drillDownMode = configuration.getString("drillDown[@mode]",
				DrillDownCommand.MODE_POSITION);
		this.enableDrillDown = configuration.getBoolean("drillDown[@enabled]",
				true);
		this.enableSort = configuration.getBoolean("sort[@enabled]", true);

		// TODO Need to support a custom implementation.
		String sortModeName = configuration.getString("sort[@mode]",
				SortMode.BASIC.getName());

		this.sortMode = SortMode.fromName(sortModeName);

		if (sortMode == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			if (logger.isWarnEnabled()) {
				logger.warn("Ignoring unknown sort mode name : " + sortModeName);
			}

			this.sortMode = SortMode.BASIC;
		}

		this.enableDrillThrough = configuration.getBoolean(
				"drillThrough[@enabled]", false);

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

		initializeRenderProperties();

		for (String category : getRenderPropertyCategories()) {
			RenderPropertyList properties = renderProperties.get(category);

			if (properties != null) {
				try {
					properties.restoreSettings(configuration
							.configurationAt("properties." + category));
				} catch (IllegalArgumentException e) {
				}
			}
		}

		this.renderSlicer = configuration.getBoolean("filter[@visible]", false);
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
