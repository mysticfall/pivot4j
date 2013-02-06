/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotException;

public class MemberUtils {

	private Cube cube;

	/**
	 * @param cube
	 */
	public MemberUtils(Cube cube) {
		if (cube == null) {
			throw new IllegalArgumentException(
					"Required argument 'cube' cannot be null.");
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
}
