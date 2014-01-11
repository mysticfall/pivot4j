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
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.mdx.AbstractExp;

public abstract class AbstractMetadataExp<T extends MetadataElement> extends
		AbstractExp implements MetadataExp<T> {

	private static final long serialVersionUID = 2577401208308436530L;

	private String name;

	private String uniqueName;

	private transient T element;

	public AbstractMetadataExp() {
	}

	/**
	 * @param name
	 * @param uniqueName
	 */
	public AbstractMetadataExp(String name, String uniqueName) {
		this.name = name;
		this.uniqueName = uniqueName;
	}

	/**
	 * @param element
	 */
	public AbstractMetadataExp(T element) {
		setMetadata(element);
	}

	/**
	 * @see org.pivot4j.mdx.metadata.MetadataExp#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see org.pivot4j.mdx.metadata.MetadataExp#getUniqueName()
	 */
	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @see org.pivot4j.mdx.metadata.MetadataExp#getMetadata(org.olap4j.metadata.Cube)
	 */
	@Override
	public T getMetadata(Cube cube) {
		if (element == null && uniqueName != null) {
			this.element = lookupMetadata(cube);
		}

		return element;
	}

	/**
	 * @param element
	 */
	public void setMetadata(T element) {
		this.element = element;

		if (element == null) {
			this.name = null;
			this.uniqueName = null;
		} else {
			this.name = element.getName();
			this.uniqueName = element.getUniqueName();
		}
	}

	/**
	 * @param cube
	 * @return
	 */
	protected abstract T lookupMetadata(Cube cube);

	/**
	 * @see org.pivot4j.mdx.Exp#toMdx()
	 */
	@Override
	public String toMdx() {
		if (uniqueName == null) {
			return "";
		}

		return uniqueName;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (uniqueName == null) {
			return super.hashCode();
		}

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

		AbstractMetadataExp<?> other = (AbstractMetadataExp<?>) obj;

		if (uniqueName == null) {
			return other.uniqueName == null;
		} else {
			return uniqueName.equals(other.uniqueName);
		}
	}
}
