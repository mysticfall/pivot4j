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
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.el.ExpressionContext;
import com.eyeq.pivot4j.sort.SortCriteria;
import com.eyeq.pivot4j.state.Bookmarkable;
import com.eyeq.pivot4j.state.Configurable;
import com.eyeq.pivot4j.transform.Transform;

/**
 * Provides access to result and metadata. It does not specify what data are
 * displayed (the query).
 */
public interface PivotModel extends Configurable, Bookmarkable {

	void initialize();

	boolean isInitialized();

	/**
	 * Called once when the not used any longer. E.g. close DB connection
	 * 
	 * @throws NotInitializedException
	 */
	void destroy();

	/**
	 * Returns the database metadata for the active connection.
	 */
	OlapDatabaseMetaData getMetadata();

	/**
	 * Returns the cube for the query.
	 * 
	 * @throws NotInitializedException
	 */
	Cube getCube();

	/**
	 * Runs the query and returns the result.
	 * 
	 * @throws NotInitializedException
	 */
	CellSet getCellSet();

	/**
	 * Flushes the last query result.
	 * 
	 * @throws NotInitializedException
	 */
	void refresh();

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

	/**
	 * Returns the current MDX query after all parameter expressions are
	 * resolved. The result would be identical with {@link #getCurrentMdx()} if
	 * no parameter expressions are present.
	 */
	String getEvaluatedMdx();

	Locale getLocale();

	/**
	 * Sets the locale for messages, data display etc
	 */
	void setLocale(Locale locale);

	String getRoleName();

	void setRoleName(String roleName);

	ExpressionContext getExpressionContext();

	/**
	 * Adds a model change listener
	 */
	void addModelChangeListener(ModelChangeListener listener);

	/**
	 * Removes a model change listener
	 */
	void removeModelChangeListener(ModelChangeListener listener);

	/**
	 * Adds a query listener
	 */
	void addQueryListener(QueryListener listener);

	/**
	 * Removes a query listener
	 */
	void removeQueryListener(QueryListener listener);

	/**
	 * Returns the registered transform instance for the given type.
	 */
	<T extends Transform> T getTransform(Class<T> type);

	/**
	 * Changes current sorting. If <code>mode</code> is <code>TOPCOUNT</code> or
	 * <code>BOTTOMCOUNT</code> the current value of <code>topBottomCount</code>
	 * will be used.
	 * 
	 * @param axisToSort
	 *            the axis to sort. Its ONE of the "other" axes, that do not
	 *            contain position
	 * @param position
	 *            the sort criteria
	 * @throws NotInitializedException
	 */
	void sort(CellSetAxis axisToSort, Position position);

	boolean isSorting();

	/**
	 * Return true if the result is sorted by the members of the position
	 */
	boolean isSorting(Position position);

	void setSorting(boolean enabled);

	/**
	 * Returns true if user may sort by the members. This will be the case when
	 * ONE of the members is a measure. The GUI will paint a "sort button" for
	 * ONE of the members
	 */
	boolean isSortable(Position position);

	SortCriteria getSortCriteria();

	void setSortCriteria(SortCriteria criteria);

	List<Member> getSortPosMembers();

	/**
	 * Number of members for topcount and bottomcount
	 */
	int getTopBottomCount();

	void setTopBottomCount(int topBottomCount);
}
