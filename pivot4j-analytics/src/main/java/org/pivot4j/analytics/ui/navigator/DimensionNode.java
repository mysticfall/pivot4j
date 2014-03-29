package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.primefaces.model.TreeNode;

public class DimensionNode extends MetadataNode<Dimension> {

	/**
	 * @param dimension
	 */
	public DimensionNode(Dimension dimension) {
		super(dimension);
	}

	/**
	 * @see org.primefaces.model.TreeNode#getType()
	 */
	@Override
	public String getType() {
		return "dimension";
	}

	/**
	 * @see org.primefaces.model.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return false;
	}

	/**
	 * @see org.pivot4j.analytics.ui.navigator.MetadataNode#createChildren()
	 */
	@Override
	protected List<TreeNode> createChildren() {
		List<Hierarchy> hierarchies = getObject().getHierarchies();
		List<TreeNode> children = new ArrayList<TreeNode>(hierarchies.size());

		for (Hierarchy hierarchy : hierarchies) {
			if (!hierarchy.isVisible()) {
				continue;
			}

			HierarchyNode node = new HierarchyNode(hierarchy);

			if (configureChildNode(hierarchy, node)) {
				node.setParent(this);

				children.add(node);
			}
		}

		return children;
	}
}
