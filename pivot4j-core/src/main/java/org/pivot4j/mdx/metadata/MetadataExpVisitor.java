/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx.metadata;

import org.pivot4j.mdx.ExpVisitor;

/**
 * Visitor for metadata expressions
 */
public interface MetadataExpVisitor extends ExpVisitor {

    void visitDimension(DimensionExp exp);

    void visitHierarchy(HierarchyExp exp);

    void visitLevel(LevelExp exp);

    void visitMember(MemberExp exp);
}
