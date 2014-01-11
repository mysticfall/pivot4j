package org.pivot4j.analytics.ui.navigator;

import org.olap4j.metadata.MetadataElement;

public abstract class MetadataNode<T extends MetadataElement> extends
		NavigatorNode<T> {

	private static final long serialVersionUID = -4131026137106022312L;

	/**
	 * @param object
	 */
	public MetadataNode(T object) {
		super(object);
	}

	/**
	 * @see org.pivot4j.analytics.ui.navigator.NavigatorNode#createData(java.lang.Object)
	 */
	@Override
	protected NodeData createData(T object) {
		return new NodeData(object.getUniqueName(), object.getCaption());
	}
}
