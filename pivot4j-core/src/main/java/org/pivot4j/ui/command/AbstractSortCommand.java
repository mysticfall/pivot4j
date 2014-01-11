/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import org.pivot4j.sort.SortMode;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.RenderContext;

public abstract class AbstractSortCommand extends AbstractUICommand<Void>
		implements SortCommand {

	/**
	 * @param renderer
	 */
	public AbstractSortCommand(PivotRenderer<?> renderer) {
		super(renderer);
	}

	/**
	 * @see org.pivot4j.ui.command.AbstractUICommand#getMode(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		StringBuilder builder = new StringBuilder();

		SortMode mode = getRenderer().getSortMode();
		if (mode != null) {
			builder.append(mode.getName());
			builder.append('-');
		}

		if (context.getModel().isSorting()) {
			if (context.getPosition() == null
					|| !context.getModel().isSorting(context.getPosition())) {
				builder.append("other");
			} else {
				builder.append("current");
			}

			builder.append('-');

			switch (context.getModel().getSortCriteria()) {
			case ASC:
			case BASC:
			case TOPCOUNT:
				builder.append("up");
				break;
			case DESC:
			case BDESC:
			case BOTTOMCOUNT:
				builder.append("down");
				break;
			default:
				assert false;
			}
		} else {
			builder.append("natural");
		}

		return builder.toString();
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
	 * @see org.pivot4j.ui.command.UICommand#canExecute(org.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		return getRenderer().getEnableSort()
				&& context.getPosition() != null
				&& context.getMember() != null
				&& context.getAggregator() == null
				&& context.getPosition().getMembers()
						.indexOf(context.getMember()) == context.getPosition()
						.getMembers().size() - 1
				&& context.getModel().isSortable(context.getPosition());
	}
}
