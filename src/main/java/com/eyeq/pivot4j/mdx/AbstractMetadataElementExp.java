/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.olap4j.metadata.Cube;
import org.olap4j.metadata.MetadataElement;

public abstract class AbstractMetadataElementExp<T extends MetadataElement>
		extends AbstractExp implements MetadataElementExp<T> {

	private static final long serialVersionUID = 2577401208308436530L;

	private String name;

	private String uniqueName;

	private transient T element;

	/**
	 * @param element
	 */
	public AbstractMetadataElementExp(T element) {
		if (element == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'element'.");
		}

		this.element = element;
		this.name = element.getName();
		this.uniqueName = element.getUniqueName();
	}

	/**
	 * @param name
	 * @param uniqueName
	 */
	public AbstractMetadataElementExp(String name, String uniqueName) {
		if (name == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'name'.");
		}

		if (uniqueName == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'uniqueName'.");
		}

		this.name = name;
		this.uniqueName = uniqueName;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.MetadataElementExp#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.MetadataElementExp#getUniqueName()
	 */
	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract AbstractMetadataElementExp<T> clone();

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	@Override
	public String toMdx() {
		return uniqueName;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.MetadataElementExp#getMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	public T getMetadata(Cube cube) {
		if (element == null) {
			this.element = lookupMetadata(cube);
		}
		return element;
	}

	/**
	 * @param cube
	 * @return
	 */
	protected abstract T lookupMetadata(Cube cube);

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uniqueName.hashCode() * 31;
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
		} else if (getClass() != obj.getClass()) {
			return false;
		}

		AbstractMetadataElementExp<?> other = (AbstractMetadataElementExp<?>) obj;

		return uniqueName.equals(other.uniqueName);
	}
}
