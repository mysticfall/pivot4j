/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;

public class MemberDetail extends MemberModel {

	private static final long serialVersionUID = -2634883661023294950L;

	private List<MemberModel> childMembers;

	/**
	 * @param member
	 */
	public MemberDetail(Member member) {
		super(member);

		try {
			List<MemberModel> memberList = new LinkedList<MemberModel>();

			for (Member child : member.getChildMembers()) {
				memberList.add(new MemberModel(child));
			}

			this.childMembers = Collections.unmodifiableList(memberList);
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @return the childMembers
	 */
	public List<MemberModel> getChildMembers() {
		return childMembers;
	}
}
