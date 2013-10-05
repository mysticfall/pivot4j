/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import java.util.List;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.metadata.Hierarchy;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillReplace;
import com.eyeq.pivot4j.transform.PlaceHierarchiesOnAxes;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public class DrillUpReplaceCommand extends AbstractDrillDownCommand {

	public static final String NAME = "drillUp";

	/**
	 * @param renderer
	 */
	public DrillUpReplaceCommand(PivotRenderer<?> renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.UICommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.AbstractUICommand#getMode(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		return MODE_REPLACE;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.UICommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		if (!super.canExecute(context)) {
			return false;
		}

		PivotModel model = context.getModel();

		DrillReplace transform = model.getTransform(DrillReplace.class);

		Hierarchy hierarchy = context.getHierarchy();
		if (hierarchy == null) {
			return false;
		}

		return transform.canDrillUp(hierarchy);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.UICommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public UICommandParameters createParameters(RenderContext context) {
		PlaceHierarchiesOnAxes transform = context.getModel().getTransform(
				PlaceHierarchiesOnAxes.class);

		List<Hierarchy> hierarchies = transform.findVisibleHierarchies(context
				.getAxis());

		UICommandParameters parameters = new UICommandParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setHierarchyOrdinal(hierarchies.indexOf(context
				.getHierarchy()));

		return parameters;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.UICommand#execute(com.eyeq.pivot4j.PivotModel
	 *      , com.eyeq.pivot4j.ui.command.UICommandParameters)
	 */
	@Override
	public Void execute(PivotModel model, UICommandParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());

		PlaceHierarchiesOnAxes transform = model
				.getTransform(PlaceHierarchiesOnAxes.class);

		List<Hierarchy> hierarchies = transform.findVisibleHierarchies(axis
				.getAxisOrdinal());

		Hierarchy hierarchy = hierarchies.get(parameters.getHierarchyOrdinal());

		DrillReplace drillTransform = model.getTransform(DrillReplace.class);
		drillTransform.drillUp(hierarchy);

		return null;
	}
}
