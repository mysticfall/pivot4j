package com.eyeq.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;

public enum SelectionMode {

	Single {
		@Override
		public List<Member> getTargetMembers(Member member) {
			List<Member> selection = new ArrayList<Member>(1);
			selection.add(member);

			return selection;
		}
	},

	Level {
		@Override
		public List<Member> getTargetMembers(Member member) {
			try {
				return member.getLevel().getMembers();
			} catch (OlapException e) {
				throw new FacesException(e);
			}
		}
	},

	Sibling {
		@Override
		public List<Member> getTargetMembers(Member member) {
			try {
				Member parent = member.getParentMember();

				if (parent == null) {
					return Single.getTargetMembers(member);
				}

				return new ArrayList<Member>(parent.getChildMembers());
			} catch (OlapException e) {
				throw new FacesException(e);
			}
		}
	},

	Children {
		@Override
		public List<Member> getTargetMembers(Member member) {
			List<Member> selection = new ArrayList<Member>();
			selection.add(member);

			try {
				selection.addAll(member.getChildMembers());
			} catch (OlapException e) {
				throw new FacesException(e);
			}

			return selection;
		}
	},

	Descendants {
		@Override
		public List<Member> getTargetMembers(Member member) {
			List<Member> selection = new ArrayList<Member>();

			try {
				collectDescendants(member, selection);
			} catch (OlapException e) {
				throw new FacesException(e);
			}

			return selection;
		}
	};

	/**
	 * @param parent
	 * @param selection
	 */
	private static void collectDescendants(Member parent, List<Member> selection)
			throws OlapException {
		selection.add(parent);

		for (Member member : parent.getChildMembers()) {
			collectDescendants(member, selection);
		}
	}

	public abstract List<Member> getTargetMembers(Member member);
}