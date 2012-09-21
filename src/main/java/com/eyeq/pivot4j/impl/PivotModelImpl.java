/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.Position;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.ModelChangeEvent;
import com.eyeq.pivot4j.ModelChangeListener;
import com.eyeq.pivot4j.NotInitializedException;
import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.SortMode;
import com.eyeq.pivot4j.StateHolder;
import com.eyeq.pivot4j.mdx.ParsedQuery;
import com.eyeq.pivot4j.query.Quax;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.query.QueryChangeEvent;
import com.eyeq.pivot4j.query.QueryChangeListener;

/**
 * The pivot model represents all (meta-)data for an MDX query.
 */
public class PivotModelImpl implements PivotModel, StateHolder {

	protected static Logger logger = LoggerFactory
			.getLogger(PivotModelImpl.class);

	private OlapDataSource dataSource;

	private OlapConnection connection;

	private String schemaName;

	private String cubeName;

	private String roleName;

	private Locale locale;

	private boolean initialized = false;

	private Collection<ModelChangeListener> listeners = new ArrayList<ModelChangeListener>();

	private QueryAdapter queryAdapter;

	private int topBottomCount = 10;

	private SortMode sortMode = SortMode.ASC;

	private boolean sorting = false;

	private List<Member> sortPosMembers;

	private String mdxQuery;

	private String currentMdx;

	private CellSet cellSet;

	private QueryChangeListener queryChangeListener = new QueryChangeListener() {

		public void queryChanged(QueryChangeEvent e) {
			fireModelChanged();
		}
	};

	/**
	 * @param dataSource
	 */
	public PivotModelImpl(OlapDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#initialize()
	 */
	public synchronized void initialize() {
		if (isInitialized()) {
			destroy();
		}

		if (mdxQuery == null) {
			throw new PivotException("Initial MDX query is null.");
		}

		try {
			this.connection = dataSource.getConnection();

			if (roleName != null) {
				connection.setRoleName(roleName);
			}

			if (locale != null) {
				connection.setLocale(locale);
			}
		} catch (SQLException e) {
			throw new PivotException(e);
		}

		this.initialized = true;
	}

	private void checkInitialization() throws NotInitializedException {
		if (!isInitialized()) {
			throw new NotInitializedException(
					"Model has not been initialized yet.");
		}
	}

	/**
	 * Session terminated, closing connections etc
	 */
	public synchronized void destroy() {
		checkInitialization();

		if (queryAdapter != null) {
			queryAdapter.removeChangeListener(queryChangeListener);
			this.queryAdapter = null;
		}

		if (connection != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing OLAP connection " + connection);
			}

			try {
				connection.close();
			} catch (SQLException e) {
				throw new PivotException(e);
			}

			this.connection = null;
		}

		this.cubeName = null;
		this.schemaName = null;
		this.sortPosMembers = null;
		this.sortMode = SortMode.ASC;
		this.currentMdx = null;
		this.sorting = false;
		this.cellSet = null;
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#isInitialized()
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Returns the connection.
	 */
	protected OlapConnection getConnection() {
		return connection;
	}

	public OlapDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#getCatalog()
	 */
	public Catalog getCatalog() throws NotInitializedException {
		checkInitialization();

		try {
			return connection.getOlapCatalog();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel.jpivot.olap.model.OlapModel#getCellSet()
	 * @return Result of Query Execution
	 */
	public synchronized CellSet getCellSet() throws NotInitializedException {
		checkInitialization();

		if (cellSet != null) {
			return cellSet;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(currentMdx);
		}

		long t1 = System.currentTimeMillis();

		ParsedQuery query = queryAdapter.updateQuery();

		this.currentMdx = query.toMdx().replaceAll("\r", "");

		try {
			OlapStatement stmt = connection.createStatement();

			this.cellSet = stmt.executeOlapQuery(currentMdx);

			Cube cube = cellSet.getMetaData().getCube();

			this.cubeName = cube.getName();
			this.schemaName = cube.getSchema().getName();
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		queryAdapter.afterExecute(cellSet);

		if (logger.isInfoEnabled()) {
			long t2 = System.currentTimeMillis();
			logger.info("Query execution time " + (t2 - t1) + " ms");
		}

		return cellSet;
	}

	/**
	 * @return the corresponding mdx
	 * @see com.eyeq.pivot4j.PivotModel#getCurrentMdx()
	 */
	public String getCurrentMdx() {
		if (!isInitialized()) {
			return getMdx();
		}

		return queryAdapter.getParsedQuery().toMdx();
	}

	/**
	 * Returns the mdxQuery.
	 * 
	 * @return String
	 */
	public String getMdx() {
		return mdxQuery;
	}

	/**
	 * Sets the mdxQuery.
	 * 
	 * @param mdxQuery
	 *            The mdxQuery to set
	 */
	public void setMdx(String mdxQuery) {
		if (mdxQuery == null) {
			throw new IllegalArgumentException("MDX query cannot be null.");
		}

		if (logger.isInfoEnabled()) {
			logger.info("setMdx: " + mdxQuery);
		}

		this.mdxQuery = mdxQuery;

		String mdx = mdxQuery.replaceAll("\r", "");
		if (mdx.equals(currentMdx)) {
			return;
		}

		this.cellSet = null;
		this.topBottomCount = 10;
		this.sortMode = SortMode.ASC;
		this.sorting = false;
		this.sortPosMembers = null;

		if (queryAdapter != null) {
			queryAdapter.removeChangeListener(queryChangeListener);
		}

		this.queryAdapter = createQueryAdapter();

		queryAdapter.addChangeListener(queryChangeListener);

		this.currentMdx = mdx;
	}

	protected QueryAdapter createQueryAdapter() {
		return new QueryAdapter(this);
	}

	/**
	 * Returns the current locale.
	 * 
	 * @return Locale
	 * @see com.eyeq.pivot4j.PivotModel#getLocale()
	 */
	public Locale getLocale() {
		if (locale == null) {
			return Locale.getDefault();
		}
		return locale;
	}

	/**
	 * Sets the current locale.
	 * 
	 * @param locale
	 *            The locale to set
	 * @see com.eyeq.pivot4j.PivotModel#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the roleName
	 * @see com.eyeq.pivot4j.PivotModel#getRoleName()
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * @param roleName
	 *            the roleName to set
	 * @see com.eyeq.pivot4j.PivotModel#setRoleName(java.lang.String)
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @see com.eyeq.pivot4j.tonbeller.jpivot.core.Model#addModelChangeListener(ModelChangeListener)
	 */
	public void addModelChangeListener(ModelChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see com.eyeq.pivot4j.tonbeller.jpivot.core.Model#removeModelChangeListener(ModelChangeListener)
	 */
	public void removeModelChangeListener(ModelChangeListener listener) {
		listeners.remove(listener);
	}

	protected void fireModelChanged() {
		this.cellSet = null;

		ModelChangeEvent e = new ModelChangeEvent(this);
		for (ModelChangeListener listener : listeners) {
			listener.modelChanged(e);
		}
	}

	protected void fireStructureChanged() {
		this.cellSet = null;

		ModelChangeEvent e = new ModelChangeEvent(this);
		for (ModelChangeListener listener : listeners) {
			listener.structureChanged(e);
		}
	}

	/**
	 * Returns the queryAdapter.
	 * 
	 * @return MondrianQueryAdapter
	 */
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#isSorting()
	 */
	public boolean isSorting() {
		return sorting;
	}

	/**
	 * @param position
	 *            to be checked
	 * @return true, if position is the current sorting position
	 * @see com.eyeq.pivot4j.PivotModel#isSorting(org.olap4j.Position)
	 */
	public boolean isSorting(Position position) {
		if (!isSortOnQuery()) {
			return false;
		} else {
			if (sortPosMembers.size() != position.getMembers().size()) {
				return false;
			}

			for (int i = 0; i < sortPosMembers.size(); i++) {
				Member member1 = sortPosMembers.get(i);
				Member member2 = position.getMembers().get(i);
				// any null does not compare
				if (member1 == null) {
					return false;
				} else if (!member1.equals(member2)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * @task support for natural sorting
	 * @see com.eyeq.pivot4j.PivotModel#setSorting(boolean)
	 */
	public void setSorting(boolean sorting) {
		if (sorting == this.sorting) {
			return;
		}

		if (logger.isInfoEnabled()) {
			logger.info("change sorting to " + sorting);
		}

		this.sorting = sorting;

		fireModelChanged();
	}

	/**
	 * @return sort mode (ASC,DESC,BASC,BDESC,TOPCOUNT,BOTTOMCOUNT)
	 */
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @param sort
	 *            mode (ASC,DESC,BASC,BDESC)
	 */
	public void setSortMode(SortMode sortMode) {
		if (this.sortMode == sortMode) {
			return;
		}

		if (logger.isInfoEnabled()) {
			logger.info("change topBottomCount from " + this.sortMode + " to "
					+ sortMode);
		}

		this.sortMode = sortMode;

		if (isSortOnQuery()) {
			fireModelChanged();
		}
	}

	/**
	 * returns true, if one of the members is a measure
	 * 
	 * @param position
	 *            the position to check for sortability
	 * @return true, if the position is sortable
	 */
	public boolean isSortable(Position position) {
		try {
			List<Member> members = position.getMembers();
			for (Member member : members) {
				if (member.getLevel().getHierarchy().getDimension()
						.getDimensionType() == Type.MEASURE) {
					return true;
				}
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		return false;
	}

	/**
	 * @return true, if there is a sort for the query
	 */
	protected boolean isSortOnQuery() {
		return sorting && sortPosMembers != null && !sortPosMembers.isEmpty();
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#getSortPosMembers()
	 */
	public List<Member> getSortPosMembers() {
		return Collections.unmodifiableList(sortPosMembers);
	}

	/**
	 * @return top/bottom count
	 */
	public int getTopBottomCount() {
		return topBottomCount;
	}

	/**
	 * @param top
	 *            /bottom count
	 */
	public void setTopBottomCount(int topBottomCount) {
		if (this.topBottomCount == topBottomCount) {
			return;
		}

		if (logger.isInfoEnabled()) {
			logger.info("Change topBottomCount from " + this.topBottomCount
					+ " to " + topBottomCount);
		}

		this.topBottomCount = topBottomCount;

		if (sorting
				&& sortPosMembers != null
				&& (sortMode == SortMode.TOPCOUNT || sortMode == SortMode.BOTTOMCOUNT)) {
			fireModelChanged();
		}
	}

	/**
	 * @param membersToSort
	 *            Axis containing the members to be sorted
	 * @param position
	 *            Position on "other axis" defining the members by which the
	 *            membersToSort are sorted
	 */
	public void sort(CellSetAxis membersToSort, Position position) {
		List<Position> positions = membersToSort.getPositions();

		// if the axis to sort does not contain any positions - sorting is not
		// posssible
		if (positions.isEmpty()) {
			if (logger.isWarnEnabled()) {
				logger.warn("Reject sort, the axis to be sorted is empty.");
			}

			this.sorting = false;
			return;
		}

		this.sortPosMembers = position.getMembers();

		// find the axis to sort
		Dimension dim = positions.get(0).getMembers().get(0).getDimension();

		Quax quaxToSort = getQueryAdapter().findQuax(dim);

		if (quaxToSort == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Reject sort, the Quax is null");
			}
			this.sorting = false;
			return;
		}

		getQueryAdapter().setQuaxToSort(quaxToSort);

		if (logger.isInfoEnabled()) {
			StringBuilder builder = new StringBuilder();
			builder.append("Change Sort Position ");

			boolean first = true;

			List<Member> members = position.getMembers();
			for (Member member : members) {
				if (first) {
					first = false;
				} else {
					builder.append(" ");
				}
				builder.append(member.getUniqueName());
			}
			builder.append(" iAxisToSort=");
			builder.append(Integer.toString(quaxToSort.getOrdinal()));

			logger.info(builder.toString());
		}

		fireModelChanged();
	}

	/**
	 * @see com.eyeq.pivot4j.StateHolder#bookmarkState()
	 */
	public synchronized Serializable bookmarkState() {
		Serializable[] state = new Serializable[5];

		state[0] = currentMdx;
		state[1] = cubeName;
		state[2] = schemaName;

		if (sortPosMembers == null) {
			state[3] = null;
		} else {
			Serializable[] sortState = new Serializable[4];

			String[] uniqueNames = new String[sortPosMembers.size()];
			for (int i = 0; i < uniqueNames.length; i++) {
				uniqueNames[i] = sortPosMembers.get(i).getUniqueName();
			}

			sortState[0] = uniqueNames;
			sortState[1] = getTopBottomCount();
			sortState[2] = getSortMode();
			sortState[3] = isSorting();

			state[3] = sortState;
		}

		state[4] = queryAdapter.bookmarkState();

		return state;
	}

	/**
	 * @see com.eyeq.pivot4j.StateHolder#restoreState(java.io.Serializable)
	 */
	public synchronized void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		setMdx((String) states[0]);

		this.cubeName = (String) states[1];
		this.schemaName = (String) states[2];

		if (!isInitialized()) {
			initialize();
		}

		// sorting
		if (states[3] == null || cubeName == null || schemaName == null) {
			this.sortPosMembers = null;
		} else {
			try {
				Schema schema = getCatalog().getSchemas().get(schemaName);
				Cube cube = schema.getCubes().get(cubeName);

				Serializable[] sortStates = (Serializable[]) states[3];

				String[] sortPosUniqueNames = (String[]) sortStates[0];
				if (sortPosUniqueNames == null) {
					this.sortPosMembers = null;
				} else {
					this.sortPosMembers = new ArrayList<Member>(
							sortPosUniqueNames.length);

					for (int i = 0; i < sortPosUniqueNames.length; i++) {
						Member member = cube.lookupMember(IdentifierNode
								.parseIdentifier(sortPosUniqueNames[i])
								.getSegmentList());
						if (member == null) {
							if (logger.isWarnEnabled()) {
								logger.warn("sort position member not found "
										+ sortPosUniqueNames[i]);
							}

							break;
						}

						sortPosMembers.add(member);
					}

					this.topBottomCount = (Integer) sortStates[1];
					this.sortMode = (SortMode) sortStates[2];
					this.sorting = (Boolean) sortStates[3];
				}
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		}

		this.cellSet = null;

		queryAdapter.restoreState(states[4]);
	}
}
