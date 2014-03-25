/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.condition;

import org.olap4j.metadata.Member;

public class MemberCondition extends AbstractMetadataCondition {

	public static final String NAME = "member";

	/**
	 * @param conditionFactory
	 */
	public MemberCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param uniqueName
	 */
	public MemberCondition(ConditionFactory conditionFactory, String uniqueName) {
		super(conditionFactory, uniqueName);
	}

	/**
	 * @see org.pivot4j.ui.condition.Condition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.ui.condition.AbstractMetadataCondition#matches(org.olap4j.metadata.Member)
	 */
	@Override
	protected boolean matches(Member member) {
		return member != null && member.getUniqueName().equals(getUniqueName());
	}
}
