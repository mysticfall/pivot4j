/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import org.apache.commons.lang.NullArgumentException;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.RenderContext;

public abstract class AbstractUICommand<T> implements UICommand<T> {

    private PivotRenderer<?> renderer;

    /**
     * @param renderer
     */
    public AbstractUICommand(PivotRenderer<?> renderer) {
        if (renderer == null) {
            throw new NullArgumentException("renderer");
        }

        this.renderer = renderer;
    }

    /**
     * @return the renderer
     */
    protected PivotRenderer<?> getRenderer() {
        return renderer;
    }

    /**
     * @see org.pivot4j.ui.command.UICommand#getDescription()
     */
    @Override
    public String getDescription() {
        // TODO Do i18n here
        return null;
    }

    /**
     * @see
     * org.pivot4j.ui.command.UICommand#getMode(org.pivot4j.ui.RenderContext)
     */
    @Override
    public String getMode(RenderContext context) {
        return null;
    }
}
