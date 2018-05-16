/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import org.olap4j.OlapConnection;
import org.olap4j.metadata.Member;
import org.pivot4j.impl.QueryAdapter;
import org.pivot4j.transform.AbstractTransform;
import org.pivot4j.transform.DrillExpandMember;

public class DrillExpandMemberImpl extends AbstractTransform implements
        DrillExpandMember {

    /**
     * @param queryAdapter
     * @param connection
     */
    public DrillExpandMemberImpl(QueryAdapter queryAdapter,
            OlapConnection connection) {
        super(queryAdapter, connection);
    }

    /**
     * @see
     * org.pivot4j.transform.DrillExpandMember#canExpand(org.olap4j.metadata.Member)
     * @param member the membber to be checked for potential expansion
     * @return true if the member can be expanded
     */
    public boolean canExpand(Member member) {
        return getQueryAdapter().canExpand(member);
    }

    /**
     * @see
     * org.pivot4j.transform.DrillExpandMember#canCollapse(org.olap4j.metadata.Member)
     * @param member member to be expanded
     * @return true if the member can be collapsed
     */
    public boolean canCollapse(Member member) {
        return getQueryAdapter().canCollapse(member);
    }

    /**
     * @see
     * org.pivot4j.transform.DrillExpandMember#expand(org.olap4j.metadata.Member)
     * @param member member to be expanded
     */
    public void expand(Member member) {
        getQueryAdapter().expand(member);
    }

    /**
     * @see
     * org.pivot4j.transform.DrillExpandMember#collapse(org.olap4j.metadata.Member)
     * @param member member to be collapsed
     */
    public void collapse(Member member) {
        getQueryAdapter().collapse(member);
    }
}
