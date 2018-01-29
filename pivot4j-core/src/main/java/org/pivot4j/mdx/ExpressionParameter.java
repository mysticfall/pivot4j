/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

public abstract class ExpressionParameter extends AbstractExp {

    private static final long serialVersionUID = -8720548361608653946L;

    private String expression;

    private String result;

    private boolean evaluated = false;

    public ExpressionParameter() {
    }

    /**
     * @param expression
     */
    public ExpressionParameter(String expression) {
        this.expression = expression;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the evaluated
     */
    public boolean isEvaluated() {
        return evaluated;
    }

    /**
     * @param evaluated the evaluated to set
     */
    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    /**
     * @see org.pivot4j.mdx.Exp#toMdx()
     */
    @Override
    public String toMdx() {
        if (evaluated) {
            return result == null ? "" : result;
        } else {
            return toMdx(expression);
        }
    }

    /**
     * @param expression
     * @return
     */
    protected abstract String toMdx(String expression);
}
