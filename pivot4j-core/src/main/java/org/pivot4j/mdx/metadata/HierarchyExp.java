/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx.metadata;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.pivot4j.mdx.ExpVisitor;

public class HierarchyExp extends AbstractMetadataExp<Hierarchy> {

    private static final long serialVersionUID = 3116369522934630935L;

    public HierarchyExp() {
    }

    /**
     * @param hierarchy
     */
    public HierarchyExp(Hierarchy hierarchy) {
        super(hierarchy);
    }

    /**
     * @param name
     * @param uniqueName
     */
    public HierarchyExp(String name, String uniqueName) {
        super(name, uniqueName);
    }

    /**
     * @see
     * org.pivot4j.mdx.metadata.AbstractMetadataExp#lookupMetadata(org.olap4j.metadata.Cube)
     */
    @Override
    protected Hierarchy lookupMetadata(Cube cube) {
        return cube.getHierarchies().get(getName());
    }

    /**
     * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
     */
    @Override
    public void accept(ExpVisitor visitor) {
        if (visitor instanceof MetadataExpVisitor) {
            ((MetadataExpVisitor) visitor).visitHierarchy(this);
        }
    }

    /**
     * @see org.pivot4j.mdx.Exp#copy()
     */
    @Override
    public HierarchyExp copy() {
        return new HierarchyExp(getName(), getUniqueName());
    }
}
