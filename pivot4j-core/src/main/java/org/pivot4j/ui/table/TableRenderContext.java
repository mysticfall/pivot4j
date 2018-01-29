/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.table;

import org.pivot4j.PivotModel;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.ui.CartesianRenderContext;

public class TableRenderContext extends CartesianRenderContext {

    private int columnCount;

    private int rowCount;

    private int columnHeaderCount;

    private int rowHeaderCount;

    private int colIndex;

    private int rowIndex;

    private int colSpan = 1;

    private int rowSpan = 1;

    /**
     * @param model
     * @param renderer
     * @param columnCount
     * @param rowCount
     * @param columnHeaderCount
     * @param rowHeaderCount
     */
    public TableRenderContext(PivotModel model, TableRenderer renderer,
            int columnCount, int rowCount, int columnHeaderCount,
            int rowHeaderCount) {
        super(model, renderer);

        if (columnCount < 0) {
            throw new IllegalArgumentException(
                    "Column count should be ZERO or positive integer.");
        }

        if (rowCount < 0) {
            throw new IllegalArgumentException(
                    "Row count should be ZERO or positive integer.");
        }

        if (columnHeaderCount < 0) {
            throw new IllegalArgumentException(
                    "Column header count should be ZERO or positive integer.");
        }

        if (rowHeaderCount < 0) {
            throw new IllegalArgumentException(
                    "Row header count should be ZERO or positive integer.");
        }

        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.columnHeaderCount = columnHeaderCount;
        this.rowHeaderCount = rowHeaderCount;
    }

    /**
     * @see org.pivot4j.ui.RenderContext#getRenderer()
     */
    @Override
    public TableRenderer getRenderer() {
        return (TableRenderer) super.getRenderer();
    }

    /**
     * @return the columnCount
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * @return the rowCount
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * @return the columnHeaderCount
     */
    public int getColumnHeaderCount() {
        return columnHeaderCount;
    }

    /**
     * @return the rowHeaderCount
     */
    public int getRowHeaderCount() {
        return rowHeaderCount;
    }

    /**
     * @return the colIndex
     */
    public int getColumnIndex() {
        return colIndex;
    }

    /**
     * @param colIndex the colIndex to set
     */
    public void setColIndex(int colIndex) {
        this.colIndex = colIndex;
    }

    /**
     * @return the rowIndex
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * @param rowIndex the rowIndex to set
     */
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * @return the colSpan
     */
    public int getColumnSpan() {
        return colSpan;
    }

    /**
     * @param colSpan the colSpan to set
     */
    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    /**
     * @return the rowSpan
     */
    public int getRowSpan() {
        return rowSpan;
    }

    /**
     * @param rowSpan the rowSpan to set
     */
    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    /**
     * @param model
     * @return
     * @see
     * org.pivot4j.ui.CartesianRenderContext#createExpressionContext(org.pivot4j.PivotModel)
     */
    @Override
    protected ExpressionContext createExpressionContext(PivotModel model) {
        ExpressionContext context = super.createExpressionContext(model);

        context.put("columnCount",
                new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getColumnCount();
            }
        });

        context.put("rowCount", new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getRowCount();
            }
        });

        context.put("columnHeaderCount",
                new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getColumnHeaderCount();
            }
        });

        context.put("rowHeaderCount",
                new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getRowHeaderCount();
            }
        });

        context.put("columnIndex",
                new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getColumnIndex();
            }
        });

        context.put("rowIndex", new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getRowIndex();
            }
        });

        context.put("columnSpan",
                new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getColumnSpan();
            }
        });

        context.put("rowSpan", new ExpressionContext.ValueBinding<Integer>() {

            @Override
            public Integer getValue() {
                return getRowSpan();
            }
        });

        return context;
    }
}
