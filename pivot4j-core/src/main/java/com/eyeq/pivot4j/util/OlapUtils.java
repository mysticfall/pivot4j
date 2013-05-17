/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
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
		return lookupMember(cube, identifier);
	}

	/**
	 * @param cube
	 * @param identifier
	 * @return
	 */
	public static Member lookupMember(Cube cube, String identifier) {
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
}
