/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui;

import java.util.List;
import java.util.Map;

import org.olap4j.Axis;
import org.pivot4j.PivotModel;
import org.pivot4j.sort.SortMode;
import org.pivot4j.state.Bookmarkable;
import org.pivot4j.state.Configurable;
import org.pivot4j.ui.aggregator.AggregatorFactory;
import org.pivot4j.ui.aggregator.AggregatorPosition;
import org.pivot4j.ui.collector.PropertyCollector;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.property.RenderPropertyList;

public interface PivotRenderer<T extends RenderCallback<?>> extends
		Configurable, Bookmarkable {

	/**
	 * @param model
	 * @param callback
	 */
	void render(PivotModel model, T callback);

	/**
	 * @param name
	 * @return
	 */
	UICommand<?> getCommand(String name);

	/**
	 * @param command
	 */
	void addCommand(UICommand<?> command);

	/**
	 * @param name
	 */
	void removeCommand(String name);

	/**
	 * @return
	 */
	boolean getEnableDrillDown();

	/**
	 * @param enableDrillDown
	 */
	void setEnableDrillDown(boolean enableDrillDown);

	/**
	 * @return
	 */
	String getDrillDownMode();

	/**
	 * @param mode
	 */
	void setDrillDownMode(String mode);

	/**
	 * @return
	 */
	boolean getEnableSort();

	/**
	 * @param enableSort
	 */
	void setEnableSort(boolean enableSort);

	/**
	 * @return
	 */
	boolean getEnableDrillThrough();

	/**
	 * @param enableDrillThrough
	 */
	void setEnableDrillThrough(boolean enableDrillThrough);

	/**
	 * @return
	 */
	SortMode getSortMode();

	/**
	 * @param mode
	 */
	void setSortMode(SortMode mode);

	/**
	 * @return
	 */
	boolean getRenderSlicer();

	/**
	 * @param renderSlicer
	 */
	void setRenderSlicer(boolean renderSlicer);

	/**
	 * @return
	 */
	PropertyCollector getPropertyCollector();

	/**
	 * @param collector
	 */
	void setPropertyCollector(PropertyCollector collector);

	/**
	 * @return
	 */
	AggregatorFactory getAggregatorFactory();

	/**
	 * @param axis
	 * @param position
	 * @return
	 */
	List<String> getAggregators(Axis axis, AggregatorPosition position);

	/**
	 * @param axis
	 * @param position
	 * @param name
	 */
	void addAggregator(Axis axis, AggregatorPosition position, String name);

	/**
	 * @param axis
	 * @param position
	 * @param name
	 */
	void removeAggregator(Axis axis, AggregatorPosition position, String name);

	/**
	 * @param axis
	 * @param position
	 * @param names
	 */
	void setAggregators(Axis axis, AggregatorPosition position,
			List<String> names);

	/**
	 * @return
	 */
	Map<String, RenderPropertyList> getRenderProperties();
}
