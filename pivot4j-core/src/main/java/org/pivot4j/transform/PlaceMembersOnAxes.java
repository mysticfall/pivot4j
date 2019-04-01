/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform;

import java.util.List;

import org.olap4j.Axis;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

/**
 * Allows to place a set of members on a visible query axis.
 */
public interface PlaceMembersOnAxes extends Transform {

    /**
     * @param hierarchy The target hierarchy
     * @param members a list of Members
     */
    void placeMembers(Hierarchy hierarchy, List<Member> members);

    /**
     * @param axis The target axis
     * @param members a list of Members
     */
    void placeMembers(Axis axis, List<Member> members);

    /**
     * @param member
     * @param position
     */
    void addMember(Member member, int position);

    /**
     * @param hierarchy
     * @param members
     */
    void addMembers(Hierarchy hierarchy, List<Member> members);

    /**
     * @param axis
     * @param member
     * @param position
     */
    void addMember(Axis axis, Member member, int position);

    /**
     * @param axis
     * @param members
     * @param position
     */
    void addMembers(Axis axis, List<Member> members, int position);

    /**
     * @param member
     */
    void removeMember(Member member);

    /**
     * @param hierarchy
     * @param members
     */
    void removeMembers(Hierarchy hierarchy, List<Member> members);

    /**
     * @param member
     * @param position
     */
    void moveMember(Member member, int position);

    /**
     * Collects all members from the visible axes in the result. If no members
     * of the hierarchy are on a visible axis, returns an empty list.
     *
     * @param axis the axis
     * @return A list of Members
     */
    List<Member> findVisibleMembers(Axis axis);

    /**
     * Collects all members from the visible axes in the result. If no members
     * of the hierarchy are on a visible axis, returns an empty list.
     *
     * @param hierarchy the Hierarchy
     * @return A list of Members
     */
    List<Member> findVisibleMembers(Hierarchy hierarchy);
}
