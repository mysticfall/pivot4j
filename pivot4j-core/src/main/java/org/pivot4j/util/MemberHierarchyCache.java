/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import freemarker.template.utility.NullArgumentException;

/**
 * Temporary workaround for performance issue.
 * 
 * See http://jira.pentaho.com/browse/MONDRIAN-1292
 */
public class MemberHierarchyCache extends Cache<String, Member> {

	private Cube cube;

	private OlapUtils util;

	/**
	 * @param cube
	 */
	public MemberHierarchyCache(Cube cube) {
		if (cube == null) {
			throw new NullArgumentException("cube");
		}

		this.cube = cube;
		this.util = new OlapUtils(cube);

		util.setMemberHierarchyCache(this);
	}

	public Cube getCube() {
		return cube;
	}

	/**
	 * @param member
	 * @return
	 */
	public Member getParentMember(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		Member parent = get(member.getUniqueName());

		if (parent == null) {
			parent = member.getParentMember();

			if (parent != null) {
				parent = util.wrapRaggedIfNecessary(parent);
			}

			put(member.getUniqueName(), parent);
		}

		return parent;
	}

	/**
	 * @param member
	 * @return
	 */
	public List<Member> getAncestorMembers(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		List<Member> ancestors = new ArrayList<Member>();

		Member parent = member;

		while ((parent = getParentMember(parent)) != null) {
			ancestors.add(parent);
		}

		return ancestors;
	}
}
