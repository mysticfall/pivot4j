/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.DrillExpandPosition;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.RenderContext;

public class DrillCollapsePositionCommand extends AbstractDrillDownCommand {

	public static final String NAME = "collapsePosition";

	/**
	 * @param renderer
	 */
	public DrillCollapsePositionCommand(PivotRenderer<?> renderer) {
		super(renderer);
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.ui.command.AbstractUICommand#getMode(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		return MODE_POSITION;
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#canExecute(org.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		if (!super.canExecute(context)) {
			return false;
		}

		PivotModel model = context.getModel();

		DrillExpandPosition transform = model
				.getTransform(DrillExpandPosition.class);

		Position position = context.getPosition();
		Member member = context.getMember();

		if (position == null || member == null || context.getProperty() != null) {
			return false;
		}

		return transform.canCollapse(position, member);
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#createParameters(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public UICommandParameters createParameters(RenderContext context) {
		UICommandParameters parameters = new UICommandParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setPositionOrdinal(context.getPosition().getOrdinal());
		parameters.setMemberOrdinal(context.getPosition().getMembers()
				.indexOf(context.getMember()));

		return parameters;
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#execute(org.pivot4j.PivotModel ,
	 *      org.pivot4j.ui.command.UICommandParameters)
	 */
	@Override
	public Void execute(PivotModel model, UICommandParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());
		Position position = axis.getPositions().get(
				parameters.getPositionOrdinal());

		Member member = position.getMembers()
				.get(parameters.getMemberOrdinal());

		DrillExpandPosition transform = model
				.getTransform(DrillExpandPosition.class);
		transform.collapse(position, member);

		return null;
	}
}
