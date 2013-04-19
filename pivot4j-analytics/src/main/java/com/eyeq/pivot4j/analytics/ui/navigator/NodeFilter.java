package com.eyeq.pivot4j.analytics.ui.navigator;

import org.olap4j.metadata.MetadataElement;

public interface NodeFilter {

	/**
	 * @param element
	 * @return
	 */
	<T extends MetadataElement> boolean isSelected(T element);

	/**
	 * @param element
	 * @return
	 */
	<T extends MetadataElement> boolean isSelectable(T element);

	/**
	 * @param element
	 * @return
	 */
	<T extends MetadataElement> boolean isVisible(T element);

	/**
	 * @param element
	 * @return
	 */
	<T extends MetadataElement> boolean isExpanded(T element);

	/**
	 * @param element
	 * @return
	 */
	<T extends MetadataElement> boolean isActive(T element);
}
