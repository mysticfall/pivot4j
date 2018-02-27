/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.metadata.MetadataElement;

/**
 * Tree Node for the for a general tree of Objects
 */
public class TreeNode<T> {

    private TreeNode<T> parent;

    private List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();
    private List<TreeNode<T>> unmodifiableChildren = Collections.unmodifiableList(children);

    private static final int UNKNOWN = -1;
    private int width = UNKNOWN;
    private int maxDescendantLevel = UNKNOWN;

    private T reference;

    public TreeNode() {
    }

    /**
     * @param obj referenced object
     */
    public TreeNode(T obj) {
        this.reference = obj;
    }

    /**
     * remove node from tree
     */
    public void remove() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    /**
     * remove child node
     *
     * @param child
     */
    public void removeChild(TreeNode<T> child) {
        if (children.contains(child)) {
            children.remove(child);
            invalidateChildrenDerivatives();
        }
    }

    public void clear() {
        children.clear();
        invalidateChildrenDerivatives();
    }

    /**
     * add child node
     *
     * @param child node to be added
     */
    public void addChild(TreeNode<T> child) {
        if (!children.contains(child)) {
            child.parent = this;
            children.add(child);
            invalidateChildrenDerivatives();
        }
    }

    /**
     * add child node
     *
     * @param index
     * @param child node to be added
     */
    public void addChild(int index, TreeNode<T> child) {
        if (!children.contains(child)) {
            child.parent = this;
            children.add(index, child);
            invalidateChildrenDerivatives();
        }
    }

    private void invalidateChildrenDerivatives() {
        width = UNKNOWN;
        maxDescendantLevel = UNKNOWN;
        if (parent != null) {
            parent.invalidateChildrenDerivatives();
        }
    }

    /**
     * deep copy (clone)
     *
     * @return copy of TreeNode
     */
    public TreeNode<T> deepCopy() {
        TreeNode<T> newNode = new TreeNode<T>(reference);
        for (TreeNode<T> child : children) {
            newNode.addChild(child.deepCopy());
        }
        return newNode;
    }

    /**
     * deep copy (clone) and prune
     *
     * @param depth - number of child levels to be copied
     * @return copy of TreeNode
     */
    public TreeNode<T> deepCopyPrune(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth is negative");
        }

        TreeNode<T> newNode = new TreeNode<T>(reference);
        if (depth == 0) {
            return newNode;
        }

        for (TreeNode<T> child : children) {
            newNode.addChild(child.deepCopyPrune(depth - 1));
        }
        return newNode;
    }

    /**
     * @return level = distance from root
     */
    public int getLevel() {
        int level = 0;
        TreeNode<T> p = parent;
        while (p != null) {
            ++level;
            p = p.parent;
        }
        return level;
    }

    public int getMaxDescendantLevel() {
        if (maxDescendantLevel == UNKNOWN) {
            if (getChildCount() == 0) {
                maxDescendantLevel = getLevel();
            } else {
                maxDescendantLevel = 0;
                for (TreeNode<T> child : getChildren()) {
                    maxDescendantLevel = Math.max(maxDescendantLevel, child.getMaxDescendantLevel());
                }
            }
        }

        return maxDescendantLevel;
    }

    public int getWidth() {
        if (width == UNKNOWN) {
            width = 0;
            if (getChildCount() > 0) {
                for (TreeNode<T> child : getChildren()) {
                    width += child.getWidth();
                }
            }

            width = Math.max(1, width);
        }
        return width;
    }

    /**
     * walk through subtree of this node
     *
     * @param callbackHandler function called on iteration
     */
    public int walkTree(TreeNodeCallback<T> callbackHandler) {
        int code = 0;
        code = callbackHandler.handleTreeNode(this);
        if (code != TreeNodeCallback.CONTINUE) {
            return code;
        }

        for (TreeNode<T> child : children) {
            code = child.walkTree(callbackHandler);
            if (code >= TreeNodeCallback.CONTINUE_PARENT) {
                return code;
            }
        }
        return code;
    }

    /**
     * walk through children subtrees of this node
     *
     * @param callbackHandler function called on iteration
     */
    public int walkChildren(TreeNodeCallback<T> callbackHandler) {
        int code = 0;
        for (TreeNode<T> child : children) {
            code = callbackHandler.handleTreeNode(child);
            if (code >= TreeNodeCallback.CONTINUE_PARENT) {
                return code;
            }
            if (code == TreeNodeCallback.CONTINUE) {
                code = child.walkChildren(callbackHandler);
                if (code > TreeNodeCallback.CONTINUE_PARENT) {
                    return code;
                }
            }
        }
        return code;
    }

    /**
     * @return List of children
     */
    public List<TreeNode<T>> getChildren() {
        return unmodifiableChildren;
    }

    public int getChildCount() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * @return parent node
     */
    public TreeNode<T> getParent() {
        return parent;
    }

    public TreeNode<T> getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }

    /**
     * @return reference object
     */
    public T getReference() {
        return reference;
    }

    /**
     * set reference object
     *
     * @param object reference
     */
    public void setReference(T object) {
        reference = object;
    }

    /**
     * @param reference
     * @return
     */
    public TreeNode<T> findNode(T reference) {
        TreeNode<T> node = null;

        if (isEquals(getReference(), reference)) {
            node = this;
        } else {
            for (TreeNode<T> child : getChildren()) {
                node = child.findNode(reference);

                if (node != null) {
                    break;
                }
            }
        }

        return node;
    }

    /**
     * @param reference
     * @param otherReference
     * @return
     */
    private boolean isEquals(T reference, T otherReference) {
        if (reference instanceof MetadataElement
                && otherReference instanceof MetadataElement) {
            return OlapUtils.equals((MetadataElement) reference,
                    (MetadataElement) otherReference);
        } else {
            return ObjectUtils.equals(reference, otherReference);
        }
    }

    /**
     * @param reference
     * @return
     */
    public TreeNode<T> findChild(T reference) {
        TreeNode<T> node = null;

        for (TreeNode<T> child : getChildren()) {
            node = child.findNode(reference);

            if (node != null) {
                break;
            }
        }

        return node;
    }
}
