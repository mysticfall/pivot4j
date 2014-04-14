/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.olap4j.OlapException;
import org.olap4j.impl.IdentifierParser;
import org.olap4j.impl.Named;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;
import org.pivot4j.PivotException;

/**
 * Mondrian specific workaround for fixing various problems regarding ragged
 * hierarchy members.
 * 
 * https://github.com/mysticfall/pivot4j/issues/76
 */
public class RaggedMemberWrapper implements Member, Named {

	private Member parentMember;

	private Member baseMember;

	private List<IdentifierSegment> nameSegments;

	private Member topMember;

	private NamedList<RaggedMemberWrapper> children;

	private Level level;

	private RaggedMemberWrapper() {
	}

	/**
	 * @param member
	 * @param cube
	 */
	public RaggedMemberWrapper(Member member, Cube cube) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		int baseDepth = member.getDepth();

		this.baseMember = member;
		this.nameSegments = Collections.unmodifiableList(IdentifierParser
				.parseIdentifier(member.getUniqueName()));

		while (topMember == null && baseDepth > 0) {
			try {
				this.topMember = cube.lookupMember(nameSegments.subList(0,
						baseDepth));
			} catch (OlapException e) {
				throw new PivotException(e);
			}

			if (topMember != null) {
				break;
			}

			baseDepth--;
		}

		if (topMember == null) {
			throw new IllegalArgumentException(
					"Unable to find a valid parent of the specified member : "
							+ member);
		}

		initialize(baseMember, topMember, nameSegments, member.getLevel());
	}

	/**
	 * @param baseMember
	 * @param topMember
	 * @param nameSegments
	 * @param level
	 */
	private void initialize(Member baseMember, Member topMember,
			List<IdentifierSegment> nameSegments, Level level) {
		this.baseMember = baseMember;
		this.topMember = topMember;
		this.nameSegments = nameSegments;
		this.level = level;

		if (level.getDepth() - topMember.getDepth() < 2) {
			this.parentMember = topMember;
		} else {
			List<Level> levels = level.getHierarchy().getLevels();
			int index = levels.indexOf(level);

			RaggedMemberWrapper wrapper = new RaggedMemberWrapper();

			wrapper.initialize(baseMember, topMember, nameSegments,
					levels.get(index - 1));
			wrapper.children = new NamedListImpl<RaggedMemberWrapper>(1);
			wrapper.children.add(this);

			this.parentMember = wrapper;
		}
	}

	/**
	 * @return the baseMember
	 */
	public Member getBaseMember() {
		return baseMember;
	}

	/**
	 * @return the topMember
	 */
	public Member getTopMember() {
		return topMember;
	}

	protected boolean isBaseMember() {
		return level.getDepth() == baseMember.getDepth();
	}

	/**
	 * @see org.olap4j.metadata.MetadataElement#getName()
	 */
	@Override
	public String getName() {
		if (isBaseMember()) {
			return baseMember.getName();
		}

		return "";
	}

	/**
	 * @see org.olap4j.metadata.MetadataElement#getUniqueName()
	 */
	@Override
	public String getUniqueName() {
		if (isBaseMember()) {
			StringBuilder builder = new StringBuilder(topMember.getUniqueName());
			builder.append(".[");
			builder.append(baseMember.getName());
			builder.append("]");

			return builder.toString();
		} else {
			return new IdentifierNode(nameSegments.subList(0, getDepth() + 1))
					.toString();
		}
	}

	/**
	 * @see org.olap4j.metadata.MetadataElement#getCaption()
	 */
	@Override
	public String getCaption() {
		if (isBaseMember()) {
			return baseMember.getCaption();
		}

		return "";
	}

	/**
	 * @see org.olap4j.metadata.MetadataElement#getDescription()
	 */
	@Override
	public String getDescription() {
		if (isBaseMember()) {
			return baseMember.getDescription();
		}

		return "";
	}

	/**
	 * @see org.olap4j.metadata.Member#getChildMembers()
	 */
	@Override
	public NamedList<? extends Member> getChildMembers() throws OlapException {
		if (isBaseMember()) {
			return baseMember.getChildMembers();
		}

		return children;
	}

	/**
	 * @see org.olap4j.metadata.Member#getChildMemberCount()
	 */
	@Override
	public int getChildMemberCount() throws OlapException {
		if (isBaseMember()) {
			return baseMember.getChildMemberCount();
		}

		return 1;
	}

	/**
	 * @see org.olap4j.metadata.Member#getParentMember()
	 */
	@Override
	public Member getParentMember() {
		return parentMember;
	}

	/**
	 * @see org.olap4j.metadata.Member#getAncestorMembers()
	 */
	@Override
	public List<Member> getAncestorMembers() {
		List<Member> ancestors = new LinkedList<Member>(
				parentMember.getAncestorMembers());
		ancestors.add(0, parentMember);

		return ancestors;
	}

	/**
	 * @see org.olap4j.metadata.Member#getLevel()
	 */
	@Override
	public Level getLevel() {
		return level;
	}

	/**
	 * @see org.olap4j.metadata.Member#getHierarchy()
	 */
	@Override
	public Hierarchy getHierarchy() {
		return baseMember.getHierarchy();
	}

	/**
	 * @see org.olap4j.metadata.Member#getDimension()
	 */
	@Override
	public Dimension getDimension() {
		return baseMember.getDimension();
	}

	/**
	 * @see org.olap4j.metadata.Member#getMemberType()
	 */
	@Override
	public Type getMemberType() {
		if (isBaseMember()) {
			return baseMember.getMemberType();
		}

		return Type.UNKNOWN;
	}

	/**
	 * @see org.olap4j.metadata.Member#isAll()
	 */
	@Override
	public boolean isAll() {
		return false;
	}

	/**
	 * @see org.olap4j.metadata.Member#isChildOrEqualTo(org.olap4j.metadata.Member)
	 */
	@Override
	public boolean isChildOrEqualTo(Member member) {
		if (isBaseMember()) {
			return baseMember.isChildOrEqualTo(member);
		}

		if (!(member instanceof RaggedMemberWrapper)) {
			return false;
		}

		RaggedMemberWrapper other = (RaggedMemberWrapper) member;

		return OlapUtils.equals(baseMember, other.baseMember)
				&& level.getDepth() <= other.level.getDepth();
	}

	/**
	 * @see org.olap4j.metadata.Member#getExpression()
	 */
	@Override
	public ParseTreeNode getExpression() {
		if (isBaseMember()) {
			return baseMember.getExpression();
		}

		return null;
	}

	/**
	 * @see org.olap4j.metadata.Member#isCalculated()
	 */
	@Override
	public boolean isCalculated() {
		return false;
	}

	/**
	 * @see org.olap4j.metadata.Member#isCalculatedInQuery()
	 */
	@Override
	public boolean isCalculatedInQuery() {
		return false;
	}

	@Override
	public int getSolveOrder() {
		if (isBaseMember()) {
			return baseMember.getSolveOrder();
		}

		return 0;
	}

	/**
	 * @see org.olap4j.metadata.Member#getPropertyValue(org.olap4j.metadata.Property)
	 */
	@Override
	public Object getPropertyValue(Property property) throws OlapException {
		if (isBaseMember()) {
			return baseMember.getPropertyValue(property);
		}

		return null;
	}

	/**
	 * @see org.olap4j.metadata.Member#getPropertyFormattedValue(org.olap4j.metadata.Property)
	 */
	@Override
	public String getPropertyFormattedValue(Property property)
			throws OlapException {
		if (isBaseMember()) {
			return baseMember.getPropertyFormattedValue(property);
		}

		return null;
	}

	/**
	 * @see org.olap4j.metadata.Member#setProperty(org.olap4j.metadata.Property,
	 *      java.lang.Object)
	 */
	@Override
	public void setProperty(Property property, Object value)
			throws OlapException {
		if (isBaseMember()) {
			baseMember.setProperty(property, value);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @see org.olap4j.metadata.Member#getProperties()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public NamedList<Property> getProperties() {
		if (isBaseMember()) {
			return baseMember.getProperties();
		}

		return new NamedListImpl();
	}

	/**
	 * @see org.olap4j.metadata.Member#getOrdinal()
	 */
	@Override
	public int getOrdinal() {
		if (isBaseMember()) {
			return baseMember.getOrdinal();
		}

		return 0;
	}

	/**
	 * @see org.olap4j.metadata.MetadataElement#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return baseMember.isVisible();
	}

	/**
	 * @see org.olap4j.metadata.Member#isHidden()
	 */
	@Override
	public boolean isHidden() {
		return baseMember.isHidden();
	}

	/**
	 * @see org.olap4j.metadata.Member#getDepth()
	 */
	@Override
	public int getDepth() {
		return level.getDepth();
	}

	/**
	 * @see org.olap4j.metadata.Member#getDataMember()
	 */
	@Override
	public Member getDataMember() {
		return this;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getUniqueName()).toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Member)) {
			return false;
		}

		Member other = (Member) obj;

		if (isBaseMember() && OlapUtils.equals(baseMember, other)) {
			return true;
		}

		return OlapUtils.equals(this, other);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getUniqueName();
	}
}
