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
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;

public class HierarchyDetail extends HierarchyModel {

	private static final long serialVersionUID = 6586940923790684031L;

	private List<MemberModel> rootMembers;

	private List<LevelModel> levels;

	private MemberModel defaultMember;

	/**
	 * @param hierarchy
	 */
	public HierarchyDetail(Hierarchy hierarchy) {
		super(hierarchy);

		try {
			List<MemberModel> rootMemberList = new LinkedList<MemberModel>();

			for (Member member : hierarchy.getRootMembers()) {
				rootMemberList.add(new MemberModel(member));
			}

			this.rootMembers = Collections.unmodifiableList(rootMemberList);

			List<LevelModel> levelList = new LinkedList<LevelModel>();

			for (Level level : hierarchy.getLevels()) {
				levelList.add(new LevelModel(level));
			}

			this.levels = Collections.unmodifiableList(levelList);

			if (hierarchy.getDefaultMember() != null) {
				this.defaultMember = new MemberModel(
						hierarchy.getDefaultMember());
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @return the rootMembers
	 */
	public List<MemberModel> getRootMembers() {
		return rootMembers;
	}

	/**
	 * @return the levels
	 */
	public List<LevelModel> getLevels() {
		return levels;
	}

	/**
	 * @return the defaultMember
	 */
	public MemberModel getDefaultMember() {
		return defaultMember;
	}
}
