package org.pivot4j.analytics.ui.navigator;

import org.olap4j.metadata.Level;
import org.pivot4j.analytics.component.tree.NodeData;

public class LevelNodeData extends NodeData {

	private static final long serialVersionUID = 1538439140022459634L;

	private int depth;

	public LevelNodeData() {
	}

	/**
	 * @param level
	 */
	public LevelNodeData(Level level) {
		super(level.getUniqueName(), level.getCaption());
		this.depth = level.getDepth();
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
}
