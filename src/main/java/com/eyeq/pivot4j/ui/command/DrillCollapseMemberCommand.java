/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillExpandMember;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public class DrillCollapseMemberCommand extends AbstractDrillDownCommand {

	public static final String NAME = "collapseMember";

	/**
	 * @param renderer
	 */
	public DrillCollapseMemberCommand(PivotRenderer renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.AbstractCellCommand#getMode()
	 */
	@Override
	public String getMode() {
		return MODE_MEMBER;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		if (!super.canExecute(context)) {
			return false;
		}

		PivotModel model = context.getModel();

		DrillExpandMember transform = model
				.getTransform(DrillExpandMember.class);

		Member member = context.getMember();
		if (member == null) {
			return false;
		}

		return transform.canCollapse(member);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public CellParameters createParameters(RenderContext context) {
		CellParameters parameters = new CellParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setMemberUniqueName(context.getMember().getUniqueName());
		parameters.setPositionOrdinal(context.getPosition().getOrdinal());

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
		Position position = axis.getPositions().get(
				parameters.getPositionOrdinal());

		Member member = null;

		for (Member m : position.getMembers()) {
			if (m.getUniqueName().equals(parameters.getMemberUniqueName())) {
				member = m;
				break;
			}
		}

		DrillExpandMember transform = model
				.getTransform(DrillExpandMember.class);
		transform.collapse(member);
	}
}
