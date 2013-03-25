/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotException;

public class MemberParameter extends ExpressionParameter {

	private static final long serialVersionUID = 7205831600041541541L;

	private transient Member member;

	public MemberParameter() {
	}

	/**
	 * @param namespace
	 * @param expression
	 */
	public MemberParameter(String namespace, String expression) {
		super(namespace, expression);
	}

	/**
	 * @param cube
	 * @return
	 */
	public Member getMember(Cube cube) {
		if (getResult() == null) {
			return null;
		}

		try {
			this.member = cube.lookupMember(IdentifierNode.parseIdentifier(
					getResult()).getSegmentList());
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		return member;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpressionParameter#setResult(java.lang.String)
	 */
	@Override
	public void setResult(String result) {
		boolean changed = !ObjectUtils.equals(getResult(), result);

		super.setResult(result);

		if (changed) {
			this.member = null;
		}
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public MemberParameter clone() {
		MemberParameter clone = new MemberParameter(getNamespace(),
				getExpression());
		clone.setEvaluated(isEvaluated());
		clone.setResult(getResult());
		clone.member = member;

		return clone;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitMemberParameter(this);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpressionParameter#toMdx(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected String toMdx(String namespace, String expression) {
		StringBuilder sb = new StringBuilder();

		sb.append("$[");

		if (namespace != null) {
			sb.append(namespace);
			sb.append(":");
		}

		if (expression != null) {
			sb.append(expression.replaceAll("]", "]]"));
		}

		sb.append("]");

		return sb.toString();
	}
}
