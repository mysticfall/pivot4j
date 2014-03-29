package org.pivot4j.analytics.component.tree;

import java.io.Serializable;

public class NodeData implements Serializable {

	private static final long serialVersionUID = 1504395803569600514L;

	private String id;

	private String name;

	private boolean selected;

	public NodeData() {
	}

	/**
	 * @param id
	 * @param name
	 */
	public NodeData(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
