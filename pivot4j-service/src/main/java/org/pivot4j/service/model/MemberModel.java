/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.model;

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;

public class MemberModel extends MetadataModel {

	private static final long serialVersionUID = 4655816736526836558L;

	private int depth;

	private int childMemberCount;

	/**
	 * @param member
	 */
	public MemberModel(Member member) {
		super(member);

		this.depth = member.getDepth();

		try {
			this.childMemberCount = member.getChildMemberCount();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the childMemberCount
	 */
	public int getChildMemberCount() {
		return childMemberCount;
	}

	/**
	 * @param childMemberCount
	 *            the childMemberCount to set
	 */
	public void setChildMemberCount(int childMemberCount) {
		this.childMemberCount = childMemberCount;
	}
}
