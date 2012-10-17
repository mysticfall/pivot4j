/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotException;

public class MemberExp extends AbstractMetadataElementExp<Member> {

	private static final long serialVersionUID = 7794058991628436993L;

	/**
	 * @param member
	 */
	public MemberExp(Member member) {
		super(member);
	}

	/**
	 * @param name
	 * @param uniqueName
	 */
	public MemberExp(String name, String uniqueName) {
		super(name, uniqueName);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	@Override
	public void accept(ExpVisitor visitor) {
		visitor.visitMember(this);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MemberExp clone() {
		return new MemberExp(getName(), getUniqueName());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.AbstractMetadataElementExp#lookupMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	protected Member lookupMetadata(Cube cube) {
		try {
			return cube.lookupMember(IdentifierNode.parseIdentifier(
					getUniqueName()).getSegmentList());
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}
}
