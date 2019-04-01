/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import static org.pivot4j.ui.CellTypes.AGG_VALUE;

import org.apache.commons.lang.ObjectUtils;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.RenderContext;

public abstract class AbstractDrillDownCommand extends AbstractUICommand<Void>
        implements DrillDownCommand {

    /**
     * @param renderer
     */
    public AbstractDrillDownCommand(PivotRenderer<?> renderer) {
        super(renderer);
    }

    /**
     * @see org.pivot4j.ui.command.UICommand#canExecute(org.pivot4j.ui
     * .RenderContext)
     */
    @Override
    public boolean canExecute(RenderContext context) {
        boolean enabled = getRenderer().getEnableDrillDown()
                && ObjectUtils.equals(getMode(context), getRenderer()
                        .getDrillDownMode()) && context.getAxis() != null
                && !AGG_VALUE.equals(context.getCellType());

        return enabled;
    }
}
