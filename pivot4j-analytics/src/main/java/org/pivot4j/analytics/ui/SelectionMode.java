package org.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;
import org.pivot4j.util.OlapUtils;

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

				List<? extends Member> children = parent.getChildMembers();
				List<Member> selection = new ArrayList<Member>(children.size());

				for (Member child : children) {
					if (OlapUtils.isVisible(child)) {
						selection.add(child);
					}
				}

				return selection;
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
				List<? extends Member> children = member.getChildMembers();

				for (Member child : children) {
					if (OlapUtils.isVisible(child)) {
						selection.add(child);
					}
				}
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
				if (OlapUtils.isVisible(member)) {
					collectDescendants(member, selection);
				}
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
			if (OlapUtils.isVisible(member)) {
				collectDescendants(member, selection);
			}
		}
	}

	public abstract List<Member> getTargetMembers(Member member);
}