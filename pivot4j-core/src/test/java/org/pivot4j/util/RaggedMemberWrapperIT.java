/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;
import org.pivot4j.AbstractIntegrationTestCase;
import org.pivot4j.util.OlapUtils;
import org.pivot4j.util.RaggedMemberWrapper;

public class RaggedMemberWrapperIT extends AbstractIntegrationTestCase {

	private Cube cube;

	private Member member;

	/**
	 * @see org.pivot4j.AbstractIntegrationTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		this.cube = getDataSource().getConnection().getOlapSchema().getCubes()
				.get("Sales Ragged");
		this.member = OlapUtils.lookupMember("[Geography].[Israel].[Tel Aviv]",
				cube);
	}

	/**
	 * @see org.pivot4j.AbstractIntegrationTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		this.cube = null;
		this.member = null;
	}

	/**
	 * @return the cube
	 */
	protected Cube getCube() {
		return cube;
	}

	/**
	 * @return the member
	 */
	protected Member getMember() {
		return member;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitializeWithNonRaggedMember() {
		Member nonRaggedMember = OlapUtils.lookupMember(
				"[Geography].[USA].[CA]", cube);

		new RaggedMemberWrapper(nonRaggedMember, cube);
	}

	@Test
	public void testBaseMember() {
		Member raggedMember = new RaggedMemberWrapper(member, cube);

		assertThat("Invalid base member name.", raggedMember.getName(),
				is("Tel Aviv"));

		assertThat("Invalid base member unique name.",
				raggedMember.getUniqueName(),
				is("[Geography].[Israel].[Tel Aviv]"));
		assertThat("Invalid member depth.", raggedMember.getDepth(), is(3));
	}

	@Test
	public void testParentMember() {
		Member raggedMember = new RaggedMemberWrapper(member, cube);

		Member parent = raggedMember.getParentMember();

		assertThat("Parent member is null.", parent, is(notNullValue()));
		assertThat("Parent member level is null.", parent.getLevel(),
				is(notNullValue()));
		assertThat("Invalid parent member unique name.",
				parent.getUniqueName(),
				is(equalTo("[Geography].[Israel].[Israel]")));
		assertThat("Invalid member depth.", parent.getDepth(), is(equalTo(2)));
	}

	@Test
	public void testTopMember() {
		Member raggedMember = new RaggedMemberWrapper(member, cube);

		Member parent = raggedMember.getParentMember().getParentMember();

		assertThat("Top member is null.", parent, is(notNullValue()));
		assertThat("Top member level is null.", parent.getLevel(),
				is(notNullValue()));
		assertThat("Invalid top member unique name.", parent.getUniqueName(),
				is("[Geography].[Israel]"));
		assertThat("Invalid member depth.", parent.getDepth(), is(equalTo(1)));
	}

	@Test
	public void testAncestorMembers() {
		Member raggedMember = new RaggedMemberWrapper(member, cube);

		List<Member> ancestors = raggedMember.getAncestorMembers();

		assertThat("Ancestor member list is null.", ancestors,
				is(notNullValue()));
		assertThat("Invalid number of ancestors.", ancestors.size(),
				is(equalTo(3)));

		assertThat("Top level ancestor should be an all member.", ancestors
				.get(2).isAll(), is(true));
		assertThat("Second level ancestor depth should be 1.", ancestors.get(1)
				.getDepth(), is(equalTo(1)));
		assertThat("Third level ancestor depth should be 2.", ancestors.get(0)
				.getDepth(), is(equalTo(2)));
	}

	@Test
	public void testEquals() {
		Member raggedMember = new RaggedMemberWrapper(member, cube);
		Member anotherRaggedMember = new RaggedMemberWrapper(member, cube);

		assertThat("Wrapped member should be equal to the original.",
				raggedMember.equals(member), is(true));
		assertThat("Members should be equal to each other.",
				raggedMember.equals(anotherRaggedMember), is(true));
		assertThat(
				"Parent members should be equal to each other.",
				raggedMember.getParentMember().equals(
						anotherRaggedMember.getParentMember()), is(true));
	}
}
