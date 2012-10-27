/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

import java.util.List;
import java.util.Locale;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.transform.Transform;
import com.eyeq.pivot4j.ui.PivotRenderer;

/**
 * Provides access to result and metadata. It does not specify what data are
 * displayed (the query).
 */
public interface PivotModel {

	void initialize();

	boolean isInitialized();

	/**
	 * Called once when the not used any longer. E.g. close DB connection
	 */
	void destroy() throws NotInitializedException;

	Cube getCube() throws NotInitializedException;

	/**
	 * Runs the query and returns the result.
	 */
	CellSet getCellSet() throws NotInitializedException;

	/**
	 * The initial MDX query. This is never changed except when the user enters
	 * a new MDX query.
	 */
	String getMdx();

	void setMdx(String mdxQuery);

	/**
	 * Returns the current MDX query which this model instance is based on or
	 * null if the model is not initialized.
	 */
	String getCurrentMdx();

	Locale getLocale();

	/**
	 * Sets the locale for messages, data display etc
	 */
	void setLocale(Locale locale);

	String getRoleName();

	void setRoleName(String roleName);

	/**
	 * Adds a model change listener
	 */
	void addModelChangeListener(ModelChangeListener listener);

	/**
	 * Removes a model change listener
	 */
	void removeModelChangeListener(ModelChangeListener listener);

	/**
	 * Returns the registered transform instance for the given type.
	 */
	<T extends Transform> T getTransform(Class<T> type);

	boolean isSorting();

	/**
	 * Return true if the result is sorted by the members of the position
	 */
	boolean isSorting(Position position);

	void setSorting(boolean enabled);

	/**
	 * Returns true if user may sort by the members. This will be the case when
	 * one of the members is a measure. The GUI will paint a "sort button" for
	 * one of the members
	 */
	boolean isSortable(Position position);

	SortMode getSortMode();

	void setSortMode(SortMode mode);

	List<Member> getSortPosMembers();

	/**
	 * Number of members for topcount and bottomcount
	 */
	int getTopBottomCount();

	void setTopBottomCount(int topBottomCount);

	/**
	 * Changes current sorting. If <code>mode</code> is <code>TOPCOUNT</code> or
	 * <code>BOTTOMCOUNT</code> the current value of <code>topBottomCount</code>
	 * will be used.
	 * 
	 * @param axisToSort
	 *            the axis to sort. Its one of the "other" axes, that do not
	 *            contain position
	 * @param position
	 *            the sort criteria
	 */
	void sort(CellSetAxis axisToSort, Position position)
			throws NotInitializedException;

	boolean getHideSpans();

	void setHideSpans(boolean hideSpans);

	boolean getShowParentMembers();

	void setShowParentMembers(boolean showParentMembers);

	boolean getShowDimensionTitle();

	void setShowDimensionTitle(boolean showDimensionTitle);

	/**
	 * Render the pivot model using the specified renderer
	 * 
	 * @param renderer
	 */
	void render(PivotRenderer renderer);
}
