package org.pivot4j.analytics.component.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.primefaces.model.TreeNode;

/**
 * Workaround for the PrimeFaces issue :
 * https://code.google.com/p/primefaces/issues/detail?id=6100
 */
public class DefaultTreeNode extends AbstractTreeNode<Object> {

	public static final String DEFAULT_TYPE = "default";

	private String type;

	private Object data;

	private List<TreeNode> children;

	public DefaultTreeNode() {
		this(null);
	}

	/**
	 * @param data
	 */
	public DefaultTreeNode(Object data) {
		this.type = DEFAULT_TYPE;
		this.children = new LazyTreeNodeChildren(this);
		this.data = data;
	}

	/**
	 * @param data
	 * @param parent
	 */
	public DefaultTreeNode(Object data, TreeNode parent) {
		this.type = DEFAULT_TYPE;
		this.data = data;
		this.children = new LazyTreeNodeChildren(this);

		if (parent != null) {
			parent.getChildren().add(this);
		}
	}

	/**
	 * @param type
	 * @param data
	 * @param parent
	 */
	public DefaultTreeNode(String type, Object data, TreeNode parent) {
		this.type = type;
		this.data = data;
		this.children = new LazyTreeNodeChildren(this);
		if (parent != null) {
			parent.getChildren().add(this);
		}
	}

	/**
	 * @see org.primefaces.model.TreeNode#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getData()
	 */
	@Override
	public Object getData() {
		return data;
	}

	/**
	 * @param data
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getChildren()
	 */
	@Override
	public List<TreeNode> getChildren() {
		return children;
	}

	/**
	 * @param children
	 */
	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return children.size();
	}

	/**
	 * @see org.primefaces.model.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return CollectionUtils.isEmpty(children);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(data).toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		}

		if (!(obj instanceof DefaultTreeNode)) {
			return false;
		}

		DefaultTreeNode other = (DefaultTreeNode) obj;

		return ObjectUtils.equals(data, other.data);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (data != null) {
			return data.toString();
		} else {
			return super.toString();
		}
	}

	static class LazyTreeNodeChildren extends ArrayList<TreeNode> {

		private static final long serialVersionUID = 1L;

		private TreeNode parent;

		/**
		 * @param parent
		 */
		public LazyTreeNodeChildren(TreeNode parent) {
			this.parent = parent;
		}

		/**
		 * @param node
		 */
		private void eraseParent(TreeNode node) {
			TreeNode parentNode = node.getParent();

			if (parentNode != null) {
				parentNode.getChildren().remove(node);
				node.setParent(null);
			}
		}

		/**
		 * @see java.util.ArrayList#add(java.lang.Object)
		 */
		@Override
		public boolean add(TreeNode node) {
			if (node == null) {
				throw new NullArgumentException("node");
			}

			eraseParent(node);
			boolean result = super.add(node);
			node.setParent(parent);
			updateRowKeys(parent);
			return result;
		}

		/**
		 * @see java.util.ArrayList#add(int, java.lang.Object)
		 */
		@Override
		public void add(int index, TreeNode node) {
			if (node == null) {
				throw new NullArgumentException("node");
			}

			if ((index < 0) || (index > size())) {
				throw new IndexOutOfBoundsException();
			}

			eraseParent(node);
			super.add(index, node);
			node.setParent(parent);
			updateRowKeys(parent);
		}

		/**
		 * @see java.util.ArrayList#addAll(java.util.Collection)
		 */
		@Override
		public boolean addAll(Collection<? extends TreeNode> collection) {
			Iterator<TreeNode> elements = (new ArrayList<TreeNode>(collection))
					.iterator();

			boolean changed = false;
			while (elements.hasNext()) {
				TreeNode node = elements.next();

				eraseParent(node);
				super.add(node);
				node.setParent(parent);
				changed = true;
			}

			if (changed) {
				updateRowKeys(parent);
			}

			return (changed);
		}

		/**
		 * @see java.util.ArrayList#addAll(int, java.util.Collection)
		 */
		@Override
		public boolean addAll(int index,
				Collection<? extends TreeNode> collection) {
			Iterator<TreeNode> elements = (new ArrayList<TreeNode>(collection))
					.iterator();
			boolean changed = false;
			while (elements.hasNext()) {
				TreeNode node = elements.next();
				if (node == null) {
					throw new NullPointerException();
				} else {
					eraseParent(node);
					super.add(index++, node);
					node.setParent(parent);
					changed = true;
				}
			}

			if (changed) {
				updateRowKeys(parent);
			}

			return (changed);
		}

		/**
		 * @see java.util.ArrayList#set(int, java.lang.Object)
		 */
		@Override
		public TreeNode set(int index, TreeNode node) {
			if (node == null) {
				throw new NullArgumentException("node");
			}

			if ((index < 0) || (index >= size())) {
				throw new IndexOutOfBoundsException();
			}

			eraseParent(node);
			TreeNode previous = get(index);
			super.set(index, node);
			previous.setParent(null);
			node.setParent(parent);
			updateRowKeys(parent);

			return previous;
		}

		/**
		 * @see java.util.ArrayList#remove(int)
		 */
		@Override
		public TreeNode remove(int index) {
			TreeNode node = get(index);
			node.setParent(null);
			super.remove(index);
			updateRowKeys(parent);

			return node;
		}

		/**
		 * @see java.util.ArrayList#remove(java.lang.Object)
		 */
		@Override
		public boolean remove(Object object) {
			TreeNode node = (TreeNode) object;
			if (node == null) {
				throw new NullPointerException();
			}

			if (super.indexOf(node) != -1) {
				node.setParent(null);
			}

			if (super.remove(node)) {
				updateRowKeys(parent);
				return true;
			} else {
				return false;
			}
		}

		/**
		 * @param node
		 */
		private void updateRowKeys(TreeNode node) {
			if (!node.isExpanded()) {
				return;
			}

			int count = node.getChildCount();

			List<TreeNode> children = node.getChildren();

			for (int i = 0; i < count; i++) {
				TreeNode childNode = children.get(i);

				String rowKey;

				if (node.getParent() == null) {
					rowKey = String.valueOf(i);
				} else {
					rowKey = node.getRowKey() + "_" + i;
				}

				childNode.setRowKey(rowKey);

				if (childNode.isExpanded()) {
					updateRowKeys(childNode);
				}
			}
		}
	}
}