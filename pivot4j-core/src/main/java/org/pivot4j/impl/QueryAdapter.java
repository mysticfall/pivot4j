/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.mdx.AbstractExpVisitor;
import org.pivot4j.mdx.CompoundId;
import org.pivot4j.mdx.Exp;
import org.pivot4j.mdx.ExpressionParameter;
import org.pivot4j.mdx.FunCall;
import org.pivot4j.mdx.Literal;
import org.pivot4j.mdx.MdxParser;
import org.pivot4j.mdx.MdxStatement;
import org.pivot4j.mdx.MemberParameter;
import org.pivot4j.mdx.QueryAxis;
import org.pivot4j.mdx.Syntax;
import org.pivot4j.mdx.ValueParameter;
import org.pivot4j.mdx.impl.MdxParserImpl;
import org.pivot4j.mdx.metadata.MemberExp;
import org.pivot4j.state.Bookmarkable;
import org.pivot4j.util.OlapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapt the MDX query to the model
 */
public class QueryAdapter implements Bookmarkable {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private PivotModelImpl model;

	// Array of query axis state object
	private Map<Axis, Quax> quaxes;

	private boolean useQuax = false;

	private boolean axesSwapped = false;

	private Quax quaxToSort;

	private MdxStatement parsedQuery;

	private MdxStatement cloneQuery;

	private Collection<QueryChangeListener> listeners = new LinkedList<QueryChangeListener>();

	private QuaxChangeListener quaxListener = new QuaxChangeListener() {

		public void quaxChanged(QuaxChangeEvent e) {
			onQuaxChanged(e.getQuax(), e.isChangedByNavigator());
		}
	};

	/**
	 * @param model
	 */
	public QueryAdapter(PivotModelImpl model) {
		if (model == null) {
			throw new NullArgumentException("model");
		}

		this.model = model;
	}

	public void initialize() {
		this.useQuax = false;
		this.axesSwapped = false;
		this.quaxToSort = null;

		this.parsedQuery = parseQuery(model.getMdx());
		this.cloneQuery = null;

		List<QueryAxis> queryAxes = parsedQuery.getAxes();

		this.quaxes = new LinkedHashMap<Axis, Quax>(queryAxes.size());

		for (QueryAxis queryAxis : queryAxes) {
			Quax quax = new Quax(queryAxis.getAxis().axisOrdinal(), model);
			quax.addChangeListener(quaxListener);

			quaxes.put(queryAxis.getAxis(), quax);
		}
	}

	public boolean isInitialized() {
		if (quaxes == null || quaxes.isEmpty()) {
			return false;
		}

		for (Quax quax : quaxes.values()) {
			if (!quax.isInitialized()) {
				return false;
			}
		}

		return true;
	}

	public boolean isValid() {
		if (parsedQuery == null || parsedQuery.getAxes() == null) {
			return false;
		}

		List<QueryAxis> axes = parsedQuery.getAxes();

		int axisCount = 0;

		for (QueryAxis qa : axes) {
			if (qa.getExp() != null) {
				axisCount++;
			}
		}

		return axisCount >= 2;
	}

	/**
	 * @return the model
	 */
	public PivotModelImpl getModel() {
		return model;
	}

	public String getCubeName() {
		CompoundId cube = parsedQuery.getCube();

		if (cube != null && !cube.getNames().isEmpty()) {
			return cube.getNames().get(0).getUnquotedName();
		}

		return null;
	}

	/**
	 * Register change listener
	 * 
	 * @param listener
	 */
	public void addChangeListener(QueryChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Unregister change listener
	 * 
	 * @param listener
	 */
	public void removeChangeListener(QueryChangeListener listener) {
		listeners.remove(listener);
	}

	protected void fireQueryChanged() {
		fireQueryChanged(true);
	}

	protected void fireQueryChanged(boolean update) {
		if (update) {
			this.useQuax = true;

			updateQuery();
		}

		QueryChangeEvent e = new QueryChangeEvent(this);

		List<QueryChangeListener> copiedListeners = new ArrayList<QueryChangeListener>(
				listeners);
		for (QueryChangeListener listener : copiedListeners) {
			listener.queryChanged(e);
		}
	}

	/**
	 * @return the XMLA Query object
	 */
	public MdxStatement getParsedQuery() {
		return parsedQuery;
	}

	/**
	 * @param evaluated
	 * @return
	 */
	public String getCurrentMdx(final boolean evaluated) {
		MdxStatement stmt = parsedQuery.copy();

		stmt.accept(new AbstractExpVisitor() {

			@Override
			public void visitMemberParameter(MemberParameter exp) {
				exp.setEvaluated(evaluated);
			}

			@Override
			public void visitValueParameter(ValueParameter exp) {
				exp.setEvaluated(evaluated);
			}
		});

		return stmt.toMdx();
	}

	/**
	 * @param evaluator
	 */
	public void evaluate(final ExpressionEvaluator evaluator) {
		parsedQuery.accept(new AbstractExpVisitor() {

			@Override
			public void visitMemberParameter(MemberParameter exp) {
				evaluate(exp, evaluator);
			}

			@Override
			public void visitValueParameter(ValueParameter exp) {
				evaluate(exp, evaluator);
			}
		});
	}

	/**
	 * @param exp
	 * @param evaluator
	 */
	protected void evaluate(ExpressionParameter exp,
			ExpressionEvaluator evaluator) {
		String expression = StringUtils.trimToNull(exp.getExpression());

		if (expression == null) {
			exp.setResult("");
		} else {
			Object result = evaluator.evaluate(
					"${" + exp.getExpression() + "}",
					model.getExpressionContext());

			exp.setResult(ObjectUtils.toString(result));
		}
	}

	/**
	 * @return
	 */
	public Quax getQuax(Axis axis) {
		if (!isInitialized()) {
			getModel().getCellSet();
		}

		return quaxes.get(axis);
	}

	/**
	 * @param axis
	 * @return
	 */
	public Quax createQuax(Axis axis) {
		if (!isInitialized()) {
			getModel().getCellSet();
		}

		Quax quax = new Quax(axis.axisOrdinal(), getModel());
		quax.initialize(new ArrayList<Position>());
		quax.addChangeListener(quaxListener);

		quaxes.put(axis, quax);

		return quax;
	}

	public Set<Axis> getAxes() {
		return Collections.unmodifiableSet(quaxes.keySet());
	}

	/**
	 * @return true if quax is to be used
	 */
	public boolean getUseQuax() {
		return useQuax;
	}

	/**
	 * @return true, if axes are currently swapped
	 */
	public boolean isAxesSwapped() {
		return axesSwapped;
	}

	/**
	 * @param axesSwapped
	 */
	public void setAxesSwapped(boolean axesSwapped) {
		if (axesSwapped != this.axesSwapped) {
			QueryAxis columnAxis = parsedQuery.getAxis(Axis.COLUMNS);
			QueryAxis rowAxis = parsedQuery.getAxis(Axis.ROWS);

			if (columnAxis != null && rowAxis != null) {
				this.axesSwapped = axesSwapped;

				Exp exp = columnAxis.getExp();
				columnAxis.setExp(rowAxis.getExp());
				rowAxis.setExp(exp);

				Quax columnQuax = quaxes.get(Axis.COLUMNS);
				Quax rowQuax = quaxes.get(Axis.ROWS);

				quaxes.put(Axis.COLUMNS, rowQuax);
				quaxes.put(Axis.ROWS, columnQuax);

				fireQueryChanged();
			}
		}
	}

	public boolean isNonEmpty() {
		boolean nonEmpty = true;

		List<QueryAxis> queryAxes = parsedQuery.getAxes();
		for (QueryAxis axis : queryAxes) {
			nonEmpty &= axis.isNonEmpty();
		}

		return !queryAxes.isEmpty() && nonEmpty;
	}

	/**
	 * @param nonEmpty
	 */
	public void setNonEmpty(boolean nonEmpty) {
		boolean changed = nonEmpty != isNonEmpty();

		if (changed) {
			List<QueryAxis> queryAxes = parsedQuery.getAxes();
			for (QueryAxis axis : queryAxes) {
				axis.setNonEmpty(nonEmpty);
			}

			fireQueryChanged(false);
		}
	}

	/**
	 * @return the quaxToSort
	 */
	public Quax getQuaxToSort() {
		return quaxToSort;
	}

	/**
	 * @param quaxToSort
	 *            the quaxToSort to set
	 */
	public void setQuaxToSort(Quax quaxToSort) {
		this.quaxToSort = quaxToSort;
		updateQuery();
	}

	protected boolean isSortOnQuery() {
		return model.isSorting() && model.getSortPosMembers() != null
				&& !model.getSortPosMembers().isEmpty();
	}

	/**
	 * @return ordinal of quax to sort, if sorting is active
	 */
	protected int activeQuaxToSort() {
		if (isSortOnQuery()) {
			return quaxToSort.getOrdinal();
		} else {
			return -1;
		}
	}

	/**
	 * find the Quax for a specific dimension
	 * 
	 * @param dim
	 *            Dimension
	 * @return Quax containg dimension
	 */
	public Quax findQuax(Dimension dim) {
		if (!isInitialized()) {
			getModel().getCellSet();
		}

		for (Quax quax : quaxes.values()) {
			if (quax.dimIdx(dim) >= 0) {
				return quax;
			}
		}
		return null;
	}

	/**
	 * Update the Query Object before Execute. The current query is build from -
	 * the original query - adding the drilldown groups - apply pending swap
	 * axes - apply pending sorts.
	 */
	public MdxStatement updateQuery() {
		// if quax is to be used, generate axes from quax
		if (useQuax) {
			int iQuaxToSort = activeQuaxToSort();

			for (Quax quax : quaxes.values()) {
				if (quax.getPosTreeRoot() == null) {
					continue;
				}

				boolean doHierarchize = false;
				if (quax.isHierarchizeNeeded()
						&& quax.getOrdinal() != iQuaxToSort) {
					doHierarchize = true;

					if (logger.isDebugEnabled()) {
						logger.debug("MDX Generation added Hierarchize()");
					}
				}

				Exp eSet = quax.genExp(doHierarchize);

				Axis axis = Axis.Factory.forOrdinal(quax.getOrdinal());

				if (isAxesSwapped()) {
					if (axis == Axis.COLUMNS) {
						axis = Axis.ROWS;
					} else if (axis == Axis.ROWS) {
						axis = Axis.COLUMNS;
					}
				}

				QueryAxis queryAxis = parsedQuery.getAxis(axis);
				if (queryAxis == null) {
					parsedQuery.setAxis(new QueryAxis(axis, eSet));
				} else {
					queryAxis.setExp(eSet);
				}
			}
		}

		// generate order function if neccessary
		if (!useQuax) {
			// if Quax is used, the axis exp's are re-generated every time.
			// if not -
			// adding a sort to the query must not be permanent.
			// Therefore, we clone the orig state of the query object and
			// use
			// the clone furthermore in order to avoid duplicate "Order"
			// functions.
			if (cloneQuery == null) {
				if (isSortOnQuery()) {
					this.cloneQuery = parsedQuery.copy();
				}
			} else {
				// reset to original state
				if (isSortOnQuery()) {
					this.parsedQuery = cloneQuery.copy();
				} else {
					this.parsedQuery = cloneQuery;
				}
			}
		}

		addSortToQuery();

		return parsedQuery;
	}

	/**
	 * Apply sort to query
	 */
	public void addSortToQuery() {
		if (isSortOnQuery()) {
			switch (model.getSortCriteria()) {
			case ASC:
			case DESC:
			case BASC:
			case BDESC:
				// call sort
				orderAxis(parsedQuery);
				break;
			case TOPCOUNT:
				topBottomAxis(parsedQuery, "TopCount");
				break;
			case BOTTOMCOUNT:
				topBottomAxis(parsedQuery, "BottomCount");
				break;
			default:
				// do nothing
				return;
			}
		}
	}

	/**
	 * Add Order Funcall to QueryAxis
	 * 
	 * @param pq
	 */
	protected void orderAxis(MdxStatement pq) {
		// Order(TopCount) is allowed, Order(Order) is not permitted
		QueryAxis qa = pq.getAxis(Axis.Factory.forOrdinal(quaxToSort
				.getOrdinal()));

		Exp setForAx = qa.getExp();

		// setForAx is the top level Exp of the axis
		// put an Order FunCall around
		List<Exp> args = new ArrayList<Exp>(3);

		// the set to be sorted is the set representing the query axis
		args.add(setForAx);

		// if we got more than 1 position member, generate a tuple for the 2.arg
		Exp sortExp;

		List<Member> sortPosMembers = model.getSortPosMembers();
		if (sortPosMembers == null) {
			return;
		}

		OlapUtils utils = new OlapUtils(getModel().getCube());
		utils.setMemberHierarchyCache(getModel().getMemberHierarchyCache());

		if (sortPosMembers.size() > 1) {
			List<Exp> memberExp = new ArrayList<Exp>(sortPosMembers.size());

			for (Member member : sortPosMembers) {
				memberExp
						.add(new MemberExp(utils.wrapRaggedIfNecessary(member)));
			}

			sortExp = new FunCall("()", Syntax.Parentheses, memberExp);
		} else {
			sortExp = new MemberExp(utils.wrapRaggedIfNecessary(sortPosMembers
					.get(0)));
		}

		args.add(sortExp);
		args.add(Literal.createString(model.getSortCriteria().name()));

		FunCall order = new FunCall("Order", Syntax.Function, args);
		qa.setExp(order);
	}

	/**
	 * Add Top/BottomCount Funcall to QueryAxis
	 * 
	 * @param pq
	 * @param function
	 */
	protected void topBottomAxis(MdxStatement pq, String function) {
		if (quaxToSort == null) {
			return;
		}

		// TopCount(TopCount) and TopCount(Order) is not permitted
		QueryAxis qa = pq.getAxis(Axis.Factory.forOrdinal(quaxToSort
				.getOrdinal()));

		Exp setForAx = qa.getExp();
		Exp sortExp;

		List<Member> sortPosMembers = model.getSortPosMembers();
		if (sortPosMembers == null) {
			return;
		}

		OlapUtils utils = new OlapUtils(getModel().getCube());
		utils.setMemberHierarchyCache(getModel().getMemberHierarchyCache());

		// if we got more than 1 position member, generate a tuple
		if (sortPosMembers.size() > 1) {
			List<Exp> memberExp = new ArrayList<Exp>(sortPosMembers.size());

			for (Member member : sortPosMembers) {
				memberExp
						.add(new MemberExp(utils.wrapRaggedIfNecessary(member)));
			}

			sortExp = new FunCall("()", Syntax.Parentheses, memberExp);
		} else {
			sortExp = new MemberExp(utils.wrapRaggedIfNecessary(sortPosMembers
					.get(0)));
		}

		List<Exp> args = new ArrayList<Exp>(3);

		// the set representing the query axis
		args.add(setForAx);

		args.add(Literal.create(model.getTopBottomCount()));
		args.add(sortExp);

		FunCall topbottom = new FunCall(function, Syntax.Function, args);
		qa.setExp(topbottom);
	}

	/**
	 * @param mdxQuery
	 */
	protected MdxStatement parseQuery(String mdxQuery) {
		MdxParser parser = new MdxParserImpl();

		return parser.parse(mdxQuery);
	}

	/**
	 * After the startup query was run: get the current positions as array of
	 * array of member. Called from Model.getResult after the query was
	 * executed.
	 * 
	 * @param cellSet
	 *            the result which redefines the query axes
	 */
	public void afterExecute(CellSet cellSet) {
		List<CellSetAxis> axes = cellSet.getAxes();

		Map<Axis, CellSetAxis> axisMap = new HashMap<Axis, CellSetAxis>();
		for (CellSetAxis axis : axes) {
			axisMap.put(axis.getAxisOrdinal(), axis);
		}

		// initialization: get the result positions and set it to quax
		// if the quaxes are not yet used to generate the query
		if (!useQuax) {
			for (CellSetAxis axis : axes) {
				List<Position> positions = axis.getPositions();

				Axis targetAxis = axis.getAxisOrdinal();

				if (axesSwapped) {
					if (axis.getAxisOrdinal() == Axis.COLUMNS) {
						targetAxis = Axis.ROWS;
					} else if (axis.getAxisOrdinal() == Axis.ROWS) {
						targetAxis = Axis.COLUMNS;
					}
				}

				Quax quax = quaxes.get(targetAxis);
				if (quax != null) {
					quax.initialize(positions);
				}
			}
		} else {
			// hierarchize result if neccessary
			for (Quax quax : quaxes.values()) {
				Axis targetAxis = Axis.Factory.forOrdinal(quax.getOrdinal());

				if (axesSwapped) {
					if (quax.getOrdinal() == Axis.COLUMNS.axisOrdinal()) {
						targetAxis = Axis.ROWS;
					} else if (quax.getOrdinal() == Axis.ROWS.axisOrdinal()) {
						targetAxis = Axis.COLUMNS;
					}
				}

				CellSetAxis cellSetAxis = axisMap.get(targetAxis);

				if (cellSetAxis != null) {
					List<Position> positions = cellSetAxis.getPositions();

					// after a result for CalcSet.GENERATE was gotten
					// we have to re-initialize the quax,
					// so that we can navigate.
					if (quax.getGenerateMode() == CalcSetMode.Generate) {
						quax.resetGenerate();
						quax.initialize(positions);
					} else {
						// unknown function members are collected
						// - always for a "Sticky generate" unknown function
						// - on first result for any other unknown function
						int nDimension = quax.getNDimension();
						for (int j = 0; j < nDimension; j++) {
							// collect members for unknown functions on quax
							if (quax.isUnknownFunction(j)) {
								List<Member> members = memListForHier(j,
										positions);
								quax.setHierMemberList(j, members);
							}
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			// print the result positions to logger
			for (CellSetAxis axis : axes) {
				List<Position> positions = axis.getPositions();
				logger.debug("Positions of axis "
						+ axis.getAxisOrdinal().axisOrdinal());

				if (positions.size() == 0) {
					// the axis does not have any positions
					logger.debug("0 positions");
				} else {
					int nDimension = positions.get(0).getMembers().size();
					for (Position position : positions) {
						List<Member> members = position.getMembers();

						StringBuilder sb = new StringBuilder();
						for (int j = 0; j < nDimension; j++) {
							if (j > 0) {
								sb.append(" * ");
							}

							List<Member> memsj = new ArrayList<Member>(j + 1);
							for (int k = 0; k <= j; k++) {
								memsj.add(members.get(k));
							}

							if (this.canExpand(memsj)) {
								sb.append("(+)");
							} else if (this.canCollapse(memsj)) {
								sb.append("(-)");
							} else {
								sb.append("   ");
							}

							sb.append(members.get(j).getUniqueName());
						}
						logger.debug(sb.toString());
					}
				}
			}
		}
	}

	/**
	 * Extract members of hier from Result
	 * 
	 * @param hierIndex
	 * @return members of hier
	 */
	protected List<Member> memListForHier(int hierIndex,
			List<Position> positions) {
		List<Member> members = new ArrayList<Member>();
		for (Position position : positions) {
			Member member = position.getMembers().get(hierIndex);
			if (!members.contains(member)) {
				members.add(member);
			}
		}

		return members;
	}

	/**
	 * Create set expression for list of members
	 * 
	 * @param members
	 * @return set expression
	 */
	protected Object createMemberSet(List<Member> members) {
		List<Exp> exps = new ArrayList<Exp>(members.size());

		OlapUtils utils = new OlapUtils(getModel().getCube());
		utils.setMemberHierarchyCache(getModel().getMemberHierarchyCache());

		for (Member member : members) {
			exps.add(new MemberExp(utils.wrapRaggedIfNecessary(member)));
		}

		return new FunCall("{}", Syntax.Braces, exps);
	}

	/**
	 * Find out, whether a member can be expanded. this is true, if - the member
	 * is on an axis and - the member is not yet expanded and - the member has
	 * children
	 * 
	 * @param member
	 *            Member to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canExpand(Member member) {
		// a calculated member cannot be expanded
		if (member.isCalculated()) {
			return false;
		}

		try {
			if (member.getChildMemberCount() <= 0) {
				return false;
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canExpand(member);
	}

	/**
	 * @param pathMembers
	 *            Members to be expanded
	 * @return true if the member can be expanded
	 */
	public boolean canExpand(List<Member> pathMembers) {
		if (pathMembers.isEmpty()) {
			return false;
		}

		Member member = pathMembers.get(pathMembers.size() - 1);
		// a calculated member cannot be expanded
		if (member.isCalculated()) {
			return false;
		}

		try {
			if (member.getChildMemberCount() <= 0) {
				return false;
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canExpand(pathMembers);
	}

	/**
	 * @param member
	 *            Member to be collapsed
	 * @return true if the member can be collapsed
	 */
	public boolean canCollapse(Member member) {
		// a calculated member cannot be collapsed
		if (member.isCalculated()) {
			return false;
		}

		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canCollapse(member);
	}

	/**
	 * @param pathMembers
	 *            positions to be collapsed
	 * @return true if the position can be collapsed
	 */
	public boolean canCollapse(List<Member> pathMembers) {
		if (pathMembers.isEmpty()) {
			return false;
		}

		Member member = pathMembers.get(pathMembers.size() - 1);
		// a calculated member cannot be expanded
		if (member.isCalculated()) {
			return false;
		}

		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		return (quax == null) ? false : quax.canCollapse(pathMembers);
	}

	/**
	 * Expand a member in all positions this is done by applying
	 * ToggleDrillState to the Query
	 * 
	 * @param member
	 *            member to be expanded
	 */
	public void expand(Member member) {
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		if (logger.isInfoEnabled()) {
			logger.info("Expand member{}", getPositionString(null, member));
		}

		if ((quax == null) || !quax.canExpand(member)) {
			String msg = "Expand member failed for " + member.getUniqueName();
			throw new PivotException(msg);
		}

		quax.expand(member);
	}

	/**
	 * Expand a member in a specific position
	 * 
	 * @param pathMembers
	 *            members to be expanded
	 */
	public void expand(List<Member> pathMembers) {
		Member member = pathMembers.get(pathMembers.size() - 1);
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);

		if (logger.isInfoEnabled()) {
			logger.info("Expand path{}", getPositionString(pathMembers, null));
		}

		if ((quax == null) || !quax.canExpand(pathMembers)) {
			String msg = "Expand failed for"
					+ getPositionString(pathMembers, null);
			throw new PivotException(msg);
		}

		quax.expand(pathMembers);
	}

	/**
	 * Collapse a member in all positions
	 * 
	 * @param member
	 *            Member to be collapsed
	 */
	public void collapse(Member member) {
		Dimension dim = member.getLevel().getHierarchy().getDimension();

		if (logger.isInfoEnabled()) {
			logger.info("Collapse " + member.getUniqueName());
		}

		Quax quax = findQuax(dim);
		if (quax == null) {
			String msg = "Collapse quax was null " + member.getUniqueName();
			throw new PivotException(msg);
		}

		quax.collapse(member);
	}

	/**
	 * Collapse a member in a specific position
	 * 
	 * @param pathMembers
	 *            Positions to be collapsed
	 */
	public void collapse(List<Member> pathMembers) {
		if (logger.isDebugEnabled()) {
			logger.debug("Collapse{}", getPositionString(pathMembers, null));
		}

		Member member = pathMembers.get(pathMembers.size() - 1);
		Dimension dim = member.getLevel().getHierarchy().getDimension();

		Quax quax = findQuax(dim);
		if (quax == null) {
			String msg = "Target quax was null"
					+ getPositionString(pathMembers, null);
			throw new PivotException(msg);
		}

		quax.collapse(pathMembers);
	}

	/**
	 * Drill down is possible if <code>member</code> has children
	 * 
	 * @param member
	 *            Member to drill down
	 */
	public boolean canDrillDown(Member member) {
		try {
			if (member.getChildMemberCount() <= 0) {
				return false;
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		Dimension dim = member.getLevel().getHierarchy().getDimension();

		Quax quax = findQuax(dim);
		return (quax == null) ? false : quax.canDrillDown(member);
	}

	/**
	 * Drill up is possible if at least ONE member in the tree is not at the top
	 * level of this hierarchy.
	 */
	public boolean canDrillUp(Hierarchy hierarchy) {
		Quax quax = findQuax(hierarchy.getDimension());
		return (quax == null) ? false : quax.canDrillUp(hierarchy);
	}

	/**
	 * After switch to Qubon mode: replaces the members. Let <code>H</code> be
	 * the hierarchy that member belongs to. Then drillDown will replace all
	 * members from <code>H</code> that are currently visible with the children
	 * of <code>member</code>.
	 */
	public void drillDown(Member member) {
		// switch to Qubon mode, if not yet in
		Quax quax = findQuax(member.getLevel().getHierarchy().getDimension());

		if (quax == null) {
			String msg = "Target quax was null"
					+ getPositionString(null, member);
			throw new PivotException(msg);
		}

		// replace dimension iDim by monMember.children
		quax.drillDown(member);

		if (logger.isInfoEnabled()) {
			logger.info("Drill down{}", getPositionString(null, member));
		}
	}

	/**
	 * After switch to Qubon mode: replaces all visible members of hier with the
	 * members of the next higher level.
	 */
	public void drillUp(Hierarchy hierarchy) {
		// switch to Qubon mode, if not yet in
		Quax quax = findQuax(hierarchy.getDimension());
		if (quax == null) {
			String msg = "Drill up hierarchy quax was null "
					+ hierarchy.getCaption();
			throw new PivotException(msg);
		}

		quax.drillUp(hierarchy);

		if (logger.isInfoEnabled()) {
			logger.info("Drill up hierarchy {}", hierarchy.getCaption());
		}
	}

	/**
	 * @param exp
	 */
	public void changeSlicer(Exp exp) {
		parsedQuery.setSlicer(exp);
		fireQueryChanged(false);
	}

	/**
	 * Display position member for debugging purposes
	 * 
	 * @param posMembers
	 * @param member
	 * @return
	 */
	protected String getPositionString(List<Member> posMembers, Member member) {
		StringBuilder sb = new StringBuilder();
		if (posMembers != null) {
			sb.append(" Position=");
			int i = 0;
			for (Member m : posMembers) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(m.getUniqueName());
				i++;
			}
		}

		if (member != null) {
			sb.append(" Member=");
			sb.append(member.getUniqueName());
		}

		return sb.toString();
	}

	/**
	 * @param quax
	 * @param changedByNavigator
	 */
	protected void onQuaxChanged(Quax quax, boolean changedByNavigator) {
		// if the axis to sort (normaly *not* the measures)
		// was changed by the Navi GUI, we want to switch sorting off
		if (changedByNavigator && model.isSorting() && quax == getQuaxToSort()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Quax changed by navi - switch sorting off");
			}

			model.setSorting(false);
		}

		fireQueryChanged();
	}

	/**
	 * @see org.pivot4j.state.Bookmarkable#saveState()
	 */
	public Serializable saveState() {
		Serializable[] state = new Serializable[4];

		state[0] = isAxesSwapped();
		state[1] = getUseQuax();

		if (quaxToSort == null) {
			state[2] = -1;
		} else {
			state[2] = quaxToSort.getOrdinal();
		}

		if (getUseQuax()) {
			Serializable[] quaxStates = new Serializable[quaxes.size()];

			int i = 0;
			for (Axis axis : quaxes.keySet()) {
				Quax quax = quaxes.get(axis);

				if (quax == null) {
					quaxStates[i++] = new Serializable[] { axis.axisOrdinal(),
							null };
				} else {
					quaxStates[i++] = new Serializable[] { axis.axisOrdinal(),
							quax.saveState() };
				}
			}

			state[3] = quaxStates;
		} else {
			state[3] = null;
		}

		return state;
	}

	/**
	 * @see org.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		this.axesSwapped = (Boolean) states[0];
		this.useQuax = (Boolean) states[1];

		int quaxOrdinal = (Integer) states[2];

		this.quaxToSort = null;

		if (quaxOrdinal > -1) {
			for (Quax quax : quaxes.values()) {
				if (quaxOrdinal == quax.getOrdinal()) {
					this.quaxToSort = quax;
					break;
				}
			}
		}

		if (useQuax) {
			Serializable[] quaxStates = (Serializable[]) states[3];

			// reset the quaxes to current state
			if (quaxes.size() != quaxStates.length) {
				throw new IllegalArgumentException(
						"Stored quax state is not compatible with the current MDX.");
			}

			for (int i = 0; i < quaxStates.length; i++) {
				Serializable[] quaxState = (Serializable[]) quaxStates[i];

				int ordinal = (Integer) quaxState[0];

				Axis axis = Axis.Factory.forOrdinal(ordinal);

				if (quaxState[1] == null) {
					quaxes.remove(axis);
				} else {
					quaxes.get(axis).restoreState(quaxState[1]);
				}
			}
		}
	}
}
