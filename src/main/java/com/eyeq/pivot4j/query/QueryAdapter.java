/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java_cup.runtime.Symbol;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.StateHolder;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.Lexer;
import com.eyeq.pivot4j.mdx.Literal;
import com.eyeq.pivot4j.mdx.ParsedQuery;
import com.eyeq.pivot4j.mdx.Parser;
import com.eyeq.pivot4j.mdx.QueryAxis;
import com.eyeq.pivot4j.mdx.Syntax;

/**
 * Adapt the MDX query to the model
 */
public class QueryAdapter implements StateHolder {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private PivotModel model;

	private MdxParser parser;

	private List<Quax> quaxes; // Array of query axis state object

	private boolean useQuax = false;

	private boolean axesSwapped = false;

	private Quax quaxToSort; // this is the Quax to be sorted

	private ParsedQuery parsedQuery;

	private ParsedQuery cloneQuery;

	private Collection<QueryChangeListener> listeners = new ArrayList<QueryChangeListener>();

	private QuaxChangeListener quaxListener = new QuaxChangeListener() {

		public void quaxChanged(QuaxChangeEvent e) {
			onQuaxChanged(e.getQuax(), e.isChangedByNavigator());
		}
	};

	/**
	 * @param model
	 * @param parser
	 */
	public QueryAdapter(PivotModel model, MdxParser parser) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		if (parser == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'parser'.");
		}

		this.model = model;
		this.parser = parser;

		initialize();
	}

	public void initialize() {
		this.useQuax = false;
		this.axesSwapped = false;
		this.quaxToSort = null;

		this.parsedQuery = parseQuery(model.getMdx());
		this.cloneQuery = null;

		QueryAxis[] queryAxes = parsedQuery.getAxes();

		this.quaxes = new ArrayList<Quax>(queryAxes.length);

		int ordinal = 0;
		for (@SuppressWarnings("unused")
		QueryAxis queryAxis : queryAxes) {
			Quax quax = new Quax(ordinal++, parser);
			quax.addChangeListener(quaxListener);

			quaxes.add(quax);
		}
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		return model;
	}

	/**
	 * @return the parser
	 */
	public MdxParser getMdxParser() {
		return parser;
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
	protected ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	public String getCurrentMdx() {
		return parsedQuery.toMdx();
	}

	/**
	 * @return
	 */
	public List<Quax> getQuaxes() {
		return Collections.unmodifiableList(quaxes);
	}

	/**
	 * @return true if quas is to be used
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
		if (parsedQuery.getAxes().length >= 2
				&& axesSwapped != this.axesSwapped) {
			this.axesSwapped = axesSwapped;

			if (logger.isInfoEnabled()) {
				logger.info("swapAxes : " + axesSwapped);
			}

			QueryAxis[] queryAxes = parsedQuery.getAxes();

			Exp exp = queryAxes[0].getExp();
			queryAxes[0].setExp(queryAxes[1].getExp());
			queryAxes[1].setExp(exp);

			Quax quax = quaxes.get(0);
			quaxes.set(0, quaxes.get(1));
			quaxes.set(1, quax);

			fireQueryChanged();
		}
	}

	public boolean isNonEmpty() {
		boolean nonEmpty = true;

		QueryAxis[] queryAxes = parsedQuery.getAxes();
		for (QueryAxis axis : queryAxes) {
			nonEmpty &= axis.isNonEmpty();
		}

		return queryAxes.length > 0 && nonEmpty;
	}

	/**
	 * @param nonEmpty
	 */
	public void setNonEmpty(boolean nonEmpty) {
		boolean changed = nonEmpty != isNonEmpty();

		if (changed) {
			QueryAxis[] queryAxes = parsedQuery.getAxes();
			for (QueryAxis axis : queryAxes) {
				axis.setNonEmpty(nonEmpty);
			}
		}

		fireQueryChanged(false);
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
		for (Quax quax : quaxes) {
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
	public ParsedQuery updateQuery() {
		// if quax is to be used, generate axes from quax
		if (useQuax) {
			int iQuaxToSort = activeQuaxToSort();

			QueryAxis[] qAxes = parsedQuery.getAxes();

			int i = 0;
			for (Quax quax : quaxes) {
				if (quax.getPosTreeRoot() == null) {
					continue;
				}

				boolean doHierarchize = false;
				if (quax.isHierarchizeNeeded() && i != iQuaxToSort) {
					doHierarchize = true;

					if (logger.isDebugEnabled()) {
						logger.debug("MDX Generation added Hierarchize()");
					}
				}

				Exp eSet = quax.genExp(doHierarchize);
				qAxes[i].setExp(eSet);

				i++;
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
					this.cloneQuery = parsedQuery.clone();
				}
			} else {
				// reset to original state
				if (isSortOnQuery()) {
					this.parsedQuery = cloneQuery.clone();
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
			switch (model.getSortMode()) {
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
				return; // do nothing
			}
		}
	}

	/**
	 * Add Order Funcall to QueryAxis
	 * 
	 * @param monAx
	 * @param monSortMode
	 */
	protected void orderAxis(ParsedQuery pq) {
		// Order(TopCount) is allowed, Order(Order) is not permitted
		QueryAxis[] queryAxes = pq.getAxes();
		QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];

		Exp setForAx = qa.getExp();

		// setForAx is the top level Exp of the axis
		// put an Order FunCall around
		Exp[] args = new Exp[3];
		args[0] = setForAx; // the set to be sorted is the set representing the
							// query axis
		// if we got more than 1 position member, generate a tuple for the 2.arg
		Exp sortExp;

		List<Member> sortPosMembers = model.getSortPosMembers();
		if (sortPosMembers == null) {
			return;
		}

		if (sortPosMembers.size() > 1) {
			Exp[] memberExp = new Exp[sortPosMembers.size()];
			for (int i = 0; i < memberExp.length; i++) {
				memberExp[i] = QuaxUtil.expForMember(sortPosMembers.get(i));
			}

			sortExp = new FunCall("()", memberExp, Syntax.Parentheses);
		} else {
			sortExp = QuaxUtil.expForMember(sortPosMembers.get(0));
		}

		args[1] = sortExp;
		args[2] = Literal.createString(model.getSortMode().name());

		FunCall order = new FunCall("Order", args, Syntax.Function);
		qa.setExp(order);
	}

	/**
	 * Add Top/BottomCount Funcall to QueryAxis
	 * 
	 * @param monAx
	 * @param nShow
	 */
	protected void topBottomAxis(ParsedQuery pq, String function) {
		// TopCount(TopCount) and TopCount(Order) is not permitted
		QueryAxis[] queryAxes = pq.getAxes();
		QueryAxis qa = queryAxes[quaxToSort.getOrdinal()];
		Exp setForAx = qa.getExp();
		Exp sortExp;

		List<Member> sortPosMembers = model.getSortPosMembers();
		if (sortPosMembers == null) {
			return;
		}

		// if we got more than 1 position member, generate a tuple
		if (sortPosMembers.size() > 1) {
			Exp[] memberExp = new Exp[sortPosMembers.size()];
			for (int i = 0; i < memberExp.length; i++) {
				memberExp[i] = QuaxUtil.expForMember(sortPosMembers.get(i));
			}
			sortExp = new FunCall("()", memberExp, Syntax.Parentheses);
		} else {
			sortExp = QuaxUtil.expForMember(sortPosMembers.get(0));
		}

		Exp[] args = new Exp[3];
		args[0] = setForAx; // the set representing the query axis
		args[1] = Literal.create(model.getTopBottomCount());
		args[2] = sortExp;

		FunCall topbottom = new FunCall(function, args, Syntax.Function);
		qa.setExp(topbottom);
	}

	/**
	 * @param mdxQuery
	 */
	protected ParsedQuery parseQuery(String mdxQuery) {
		Reader reader = new StringReader(mdxQuery);
		Parser parser = new Parser(new Lexer(reader));

		ParsedQuery parsedQuery;

		try {
			Symbol parseTree = parser.parse();
			parsedQuery = (ParsedQuery) parseTree.value;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new PivotException(e);
		}

		parsedQuery.afterParse();

		return parsedQuery;
	}

	/**
	 * After the startup query was run: get the current positions as array of
	 * array of member. Called from Model.getResult after the query was
	 * executed.
	 * 
	 * @param result
	 *            the result which redefines the query axes
	 */
	public void afterExecute(CellSet cellSet) {
		List<CellSetAxis> axes = cellSet.getAxes();

		// initialization: get the result positions and set it to quax
		// if the quaxes are not yet used to generate the query
		if (!useQuax) {
			int i = 0;
			for (CellSetAxis axis : axes) {
				List<Position> positions = axis.getPositions();

				int index = axesSwapped ? (i + 1) % 2 : i;
				quaxes.get(index).initialize(positions);

				i++;
			}
		} else {
			// hierarchize result if neccessary
			int i = 0;
			for (Quax quax : quaxes) {
				int index = axesSwapped ? (i + 1) % 2 : i;
				List<Position> positions = axes.get(index).getPositions();

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
							List<Member> members = memListForHier(j, positions);
							quax.setHierMemberList(j, members);
						}
					}
				}
				i++;
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
		Exp[] exps = new Exp[members.size()];

		int i = 0;
		for (Member member : members) {
			exps[i++] = QuaxUtil.expForMember(member);
		}

		return new FunCall("{}", exps, Syntax.Braces);
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
	 * @param position
	 *            position to be expanded
	 * @return true if the position can be collapsed
	 */
	public boolean canCollapse(List<Member> pathMembers) {
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
			logger.info("Expand member" + getPositionString(null, member));
		}

		if ((quax == null) || !quax.canExpand(member)) {
			String msg = "Expand member failed for" + member.getUniqueName();
			throw new PivotException(msg);
		}

		quax.expand(member);

		fireQueryChanged();
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

		if (logger.isDebugEnabled()) {
			logger.info("Expand path" + getPositionString(pathMembers, null));
		}

		if ((quax == null) || !quax.canExpand(pathMembers)) {
			String msg = "Expand failed for"
					+ getPositionString(pathMembers, null);
			throw new PivotException(msg);
		}

		quax.expand(pathMembers);

		fireQueryChanged();
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

		fireQueryChanged();
	}

	/**
	 * Collapse a member in a specific position
	 * 
	 * @param position
	 *            Position to be collapsed
	 */
	public void collapse(List<Member> pathMembers) {
		if (logger.isDebugEnabled()) {
			logger.debug("Collapse" + getPositionString(pathMembers, null));
		}

		Member member = pathMembers.get(pathMembers.size() - 1);
		Dimension dim = member.getLevel().getHierarchy().getDimension();
		Quax quax = findQuax(dim);
		if (quax == null) {
			String msg = "Collapse quax was null"
					+ getPositionString(pathMembers, null);
			throw new PivotException(msg);
		}

		quax.collapse(pathMembers);

		fireQueryChanged();
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
	 * Drill up is possible if at least one member in the tree is not at the top
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
			logger.info("drillDown Quax was null"
					+ getPositionString(null, member));
			return;
		}

		// replace dimension iDim by monMember.children
		quax.drillDown(member);

		fireQueryChanged();

		if (logger.isInfoEnabled()) {
			logger.info("Drill down " + getPositionString(null, member));
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

		fireQueryChanged();

		if (logger.isInfoEnabled())
			logger.info("Drill up hierarchy " + hierarchy.getCaption());
	}

	/**
	 * @param slicerExp
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
	 * @see com.eyeq.pivot4j.StateHolder#bookmarkState()
	 */
	public Serializable bookmarkState() {
		Serializable[] state = new Serializable[4];

		state[0] = isAxesSwapped();
		state[1] = getUseQuax();

		Quax quaxToSort = getQuaxToSort();

		if (quaxToSort == null) {
			state[2] = -1;
		} else {
			state[2] = quaxToSort.getOrdinal();
		}

		if (getUseQuax()) {
			List<Quax> quaxes = getQuaxes();

			Serializable[] quaxStates = new Serializable[quaxes.size()];
			for (int i = 0; i < quaxStates.length; i++) {
				quaxStates[i] = quaxes.get(i).bookmarkState();
			}

			state[3] = quaxStates;
		} else {
			state[3] = null;
		}

		return state;
	}

	/**
	 * @see com.eyeq.pivot4j.StateHolder#restoreState(java.io.Serializable)
	 */
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		this.axesSwapped = (Boolean) states[0];
		this.useQuax = (Boolean) states[1];

		int quaxOrdinal = (Integer) states[2];

		Quax quaxToSort = null;

		if (quaxOrdinal > -1) {
			List<Quax> quaxes = getQuaxes();
			for (Quax quax : quaxes) {
				if (quaxOrdinal == quax.getOrdinal()) {
					quaxToSort = quax;
					break;
				}
			}
		}

		this.quaxToSort = quaxToSort;

		if (useQuax) {
			Serializable[] quaxStates = (Serializable[]) states[3];

			// reset the quaxes to current state
			List<Quax> quaxes = getQuaxes();
			if (quaxes.size() != quaxStates.length) {
				throw new IllegalArgumentException(
						"Stored quax state is not compatible with the current MDX.");
			}

			for (int i = 0; i < quaxStates.length; i++) {
				quaxes.get(i).restoreState(quaxStates[i]);
			}
		}
	}
}
