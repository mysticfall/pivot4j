/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.pivot4j.util.TreeNode;

public class ExpNode extends TreeNode<Exp> implements Serializable {

    private static final long serialVersionUID = 1956521185377274271L;

    /**
     * Default constructor used for serialization
     */
    protected ExpNode() {
    }

    /**
     * @param exp
     */
    public ExpNode(Exp exp) {
        super(exp);
    }

    /**
     * @see org.pivot4j.util.TreeNode#addChild(org.pivot4j.util.TreeNode)
     */
    @Override
    public void addChild(TreeNode<Exp> child) {
        if (!(child instanceof ExpNode)) {
            throw new IllegalArgumentException(
                    "Only ExpNode instance can be added as a child node.");
        }

        super.addChild(child);
    }

    /**
     * deep copy (clone)
     *
     * @return copy of TreeNode
     * @see org.pivot4j.util.TreeNode#deepCopy()
     */
    @Override
    public ExpNode deepCopy() {
        ExpNode newNode = new ExpNode(getReference());
        for (TreeNode<Exp> child : getChildren()) {
            newNode.addChild(child.deepCopy());
        }
        return newNode;
    }

    /**
     * deep copy (clone) and prune
     *
     * @param depth - number of child levels to be copied
     * @return copy of TreeNode
     * @see org.pivot4j.util.TreeNode#deepCopyPrune(int)
     */
    @Override
    public ExpNode deepCopyPrune(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth is negative");
        }

        ExpNode newNode = new ExpNode(getReference());
        if (depth == 0) {
            return newNode;
        }

        for (TreeNode<Exp> child : getChildren()) {
            newNode.addChild(child.deepCopyPrune(depth - 1));
        }
        return newNode;
    }

    /**
     * @see org.pivot4j.util.TreeNode#getParent()
     */
    @Override
    public ExpNode getParent() {
        return (ExpNode) super.getParent();
    }

    /**
     * @see org.pivot4j.util.TreeNode#getRoot()
     */
    @Override
    public ExpNode getRoot() {
        return (ExpNode) super.getRoot();
    }

    /**
     * @param in
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        setReference((Exp) in.readObject());

        @SuppressWarnings("unchecked")
        List<TreeNode<Exp>> children = (List<TreeNode<Exp>>) in.readObject();
        for (TreeNode<Exp> child : children) {
            addChild(child);
        }
    }

    /**
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getReference());
        out.writeObject(getChildren());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getReference().toMdx();
    }
}
