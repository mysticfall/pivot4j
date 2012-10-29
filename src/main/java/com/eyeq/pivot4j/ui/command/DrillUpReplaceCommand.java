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
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillReplace;
import com.eyeq.pivot4j.ui.RenderContext;

public class DrillUpReplaceCommand implements DrillDownCommand {

	public static final String NAME = "drillUp";

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		PivotModel model = context.getModel();

		DrillReplace transform = model.getTransform(DrillReplace.class);

		Hierarchy hierarchy = context.getHierarchy();
		if (hierarchy == null) {
			return false;
		}

		return transform.canDrillUp(hierarchy);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public CellParameters createParameters(RenderContext context) {
		CellParameters parameters = new CellParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setHierarchyUniqueName(context.getHierarchy()
				.getUniqueName());

		return parameters;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#execute(com.eyeq.pivot4j.PivotModel
	 *      , com.eyeq.pivot4j.ui.command.CellParameters)
	 */
	@Override
	public void execute(PivotModel model, CellParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());

		List<Member> members = axis.getPositions().get(0).getMembers();

		Hierarchy hierarchy = null;
		for (Member m : members) {
			if (m.getHierarchy().getUniqueName()
					.equals(parameters.getHierarchyUniqueName())) {
				hierarchy = m.getHierarchy();
				break;
			}
		}

		DrillReplace transform = model.getTransform(DrillReplace.class);
		transform.drillUp(hierarchy);
	}
}
