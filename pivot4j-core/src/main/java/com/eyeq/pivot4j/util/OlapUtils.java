/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import java.sql.SQLException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;

import com.eyeq.pivot4j.PivotException;

public class OlapUtils {

	private Cube cube;

	/**
	 * @param cube
	 */
	public OlapUtils(Cube cube) {
		if (cube == null) {
			throw new NullArgumentException("cube");
		}

		this.cube = cube;
	}

	/**
	 * @return the cube
	 */
	public Cube getCube() {
		return cube;
	}

	/**
	 * @param identifier
	 * @return
	 */
	public Member lookupMember(String identifier) {
		return lookupMember(identifier, cube);
	}

	/**
	 * @param identifier
	 * @param cube
	 * @return
	 */
	public static Member lookupMember(String identifier, Cube cube) {
		try {
			return cube.lookupMember(IdentifierNode.parseIdentifier(identifier)
					.getSegmentList());
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @param elem
	 * @param otherElem
	 * @return
	 */
	public static boolean equals(MetadataElement elem, MetadataElement otherElem) {
		if (elem == null) {
			return otherElem == null;
		} else if (otherElem == null) {
			return false;
		}

		String uniqueName = elem.getUniqueName();
		String otherUniqueName = otherElem.getUniqueName();

		return ObjectUtils.equals(uniqueName, otherUniqueName);
	}

	/**
	 * @param position
	 * @param otherPosition
	 * @return
	 */
	public static boolean equals(Position position, Position otherPosition) {
		return equals(position, otherPosition, -1);
	}

	/**
	 * @param position
	 * @param otherPosition
	 * @param memberIndex
	 * @return
	 */
	public static boolean equals(Position position, Position otherPosition,
			int memberIndex) {
		if (position == null) {
			throw new NullArgumentException("position");
		}

		if (otherPosition == null) {
			throw new NullArgumentException("lastPosition");
		}

		if (position == otherPosition) {
			return true;
		}

		int size = position.getMembers().size();

		if (memberIndex < 0) {
			memberIndex = size;

			if (size != otherPosition.getMembers().size()) {
				return false;
			}
		}

		for (int i = 0; i < memberIndex; i++) {
			Member member = position.getMembers().get(i);
			Member lastMember = otherPosition.getMembers().get(i);

			if (!equals(member, lastMember)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param member
	 * @return
	 */
	public static boolean isRaggedMember(Member member) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		return member.getDepth() > 1 && member.getParentMember() == null;
	}

	/**
	 * @param member
	 * @return
	 */
	public Member wrapRaggedIfNecessary(Member member) {
		return wrapRaggedIfNecessary(member, cube);
	}

	/**
	 * @param member
	 * @param cube
	 * @return
	 */
	public static Member wrapRaggedIfNecessary(Member member, Cube cube) {
		if (member == null) {
			throw new NullArgumentException("member");
		}

		if (cube == null) {
			throw new NullArgumentException("cube");
		}

		if (isRaggedMember(member)) {
			return new RaggedMemberWrapper(member, cube);
		}

		return member;
	}

	/**
	 * Check to see if an empty set expression is supported by the backend
	 * provider.
	 * 
	 * See : http://jira.pentaho.com/browse/MONDRIAN-1597
	 * 
	 * @param metadata
	 * @return
	 */
	public static boolean isEmptySetSupported(OlapDatabaseMetaData metadata) {
		try {
			String driverName = metadata.getDriverName().toLowerCase();
			if (driverName.contains("xmla") || driverName.contains("xml/a")) {
				return !metadata.getDatabaseProductName().toLowerCase()
						.contains("mondrian");
			}
		} catch (SQLException e) {
			throw new PivotException(e);
		}

		return true;
	}
}
