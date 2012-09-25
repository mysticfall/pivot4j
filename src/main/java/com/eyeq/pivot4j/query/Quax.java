/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.StateHolder;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.SetExp;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.util.TreeNode;
import com.eyeq.pivot4j.util.TreeNodeCallback;

public class Quax implements StateHolder {

	protected static Logger logger = LoggerFactory.getLogger(Quax.class);

	private int nDimension;

	private List<Hierarchy> hiers;

	// currently, we can handle the following Funcalls
	// member.children, member.descendants, level.members
	// other funcalls are "unknown functions"
	private boolean[] containsUF;

	private List[] ufMemberLists; // if there are unknonwn functions

	// private UnknownFunction[] unknownFunctions;
	private TreeNode<Exp> posTreeRoot = null; // Position tree used in normal
												// mode

	private int ordinal; // ordinal of query axis, never changed by swap

	private boolean qubonMode = false;

	private boolean hierarchizeNeeded = false;

	// if there are multiple hierarchies on this quax,
	// "nHierExclude" hierarchies (from right to left)
	// will *not* be included to the Hierarchize Function.
	// So MDX like
	// Crossjoin(Hierarchize(Dim1.A + Dim1.A.Children), {Measures.A.
	// Measures.B})
	// will be generated, so that the Measures are excluded from Hierarchize.
	private int nHierExclude = 0;

	private CalcSetMode generateMode = CalcSetMode.Simple;

	private int generateIndex = -1; // we handle generate for only 1 dimension

	private Object expGenerate = null;

	private Collection<QuaxChangeListener> changeListeners = new ArrayList<QuaxChangeListener>();

	private Map<Member, Boolean> canExpandMemberMap = new HashMap<Member, Boolean>();

	private Map<List<Member>, Boolean> canExpandPosMap = new HashMap<List<Member>, Boolean>();

	private Map<Member, Boolean> canCollapseMemberMap = new HashMap<Member, Boolean>();

	private Map<List<Member>, Boolean> canCollapsePosMap = new HashMap<List<Member>, Boolean>();

	/**
	 * @param ordinal
	 */
	public Quax(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * register change listener
	 * 
	 * @param listener
	 */
	public void addChangeListener(QuaxChangeListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * unregister change listener
	 * 
	 * @param listener
	 */
	public void removeChangeListener(QuaxChangeListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * Handle change
	 * 
	 * @param changedByNavigator
	 *            true if the memberset was changed by the navigator
	 */
	protected void fireQuaxChanged(boolean changedByNavigator) {
		QuaxChangeEvent e = new QuaxChangeEvent(this, changedByNavigator);
		for (QuaxChangeListener listener : changeListeners) {
			listener.quaxChanged(e);
		}

		canExpandMemberMap.clear();
		canExpandPosMap.clear();
		canCollapseMemberMap.clear();
		canCollapsePosMap.clear();
	}

	/**
	 * Initialize quax from result positions
	 * 
	 * @param positions
	 */
	public void initialize(List<Position> positions) {
		List<List<Member>> posMembers;

		int nDimension = 0;

		this.hierarchizeNeeded = false;
		this.nHierExclude = 0;
		this.qubonMode = true;

		if (positions.isEmpty()) {
			// the axis does not have any positions
			posMembers = new ArrayList<List<Member>>(0);
			setHierarchies(new ArrayList<Hierarchy>(0));
			return;
		} else {
			nDimension = positions.get(0).getMembers().size();
			posMembers = new ArrayList<List<Member>>(positions.size());

			for (Position position : positions) {
				posMembers.add(new ArrayList<Member>(position.getMembers()));
			}
		}

		List<Hierarchy> hiers = new ArrayList<Hierarchy>(nDimension);

		List<Member> firstMembers = posMembers.get(0);
		for (Member member : firstMembers) {
			hiers.add(member.getLevel().getHierarchy());
		}

		setHierarchies(hiers);
		initPositions(posMembers);

		// initialize the dimension flags
		// if there is only one set node per dimension,
		// we are in qubon mode
		posTreeRoot.walkTree(new TreeNodeCallback<Exp>() {

			/**
			 * callback check qubon mode
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int iDim = node.getLevel();

				if (iDim == Quax.this.nDimension) {
					return TreeNodeCallback.BREAK; // bottom reached
				}

				if (node.getChildren().size() == 1) {
					return TreeNodeCallback.CONTINUE; // continue next level
				} else {
					// more than one child - break out
					Quax.this.qubonMode = false;
					return TreeNodeCallback.BREAK;
				}
			}
		});

		if (qubonMode) {
			nHierExclude = nDimension - 1; // nothing hierarchized
		}
	}

	/**
	 * Initialize position member list after first result gotten
	 * 
	 * @param posMemStart
	 */
	private void initPositions(List<List<Member>> posMemStart) {
		// no positions - no tree
		if (posMemStart.isEmpty()) {
			this.posTreeRoot = null;
			return;
		}

		// before the position tree is created,
		// we want to hierarchize
		/*
		 * if (nDimension > 1) hierarchizePositions(aPosMemStart);
		 */

		// init position tree
		this.posTreeRoot = new TreeNode<Exp>(null); // root
		int end = addToPosTree(posMemStart, 0, posMemStart.size(), 0,
				posTreeRoot);
		while (end < posMemStart.size()) {
			end = addToPosTree(posMemStart, end, posMemStart.size(), 0,
					posTreeRoot);
		}

		// try to factor out the members of the last dimension
		posTreeRoot.walkTree(new TreeNodeCallback<Exp>() {

			/**
			 * callback create member set for last dimension
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int dimIndex = node.getLevel();

				if (dimIndex == Quax.this.nDimension - 1) {
					if (node.getChildren().size() <= 1) {
						return TreeNodeCallback.CONTINUE_SIBLING;
					}
					// continue
					// next
					// sibling
					// more than one child in last dimension
					// create a single set function node
					Exp[] memArray = new Exp[node.getChildren().size()];
					int i = 0;
					for (TreeNode<Exp> child : node.getChildren()) {
						memArray[i++] = child.getReference();
					}

					node.getChildren().clear();

					Exp oFun = QuaxUtil.createFunCall("{}", memArray,
							Syntax.Braces);

					TreeNode<Exp> newChild = new TreeNode<Exp>(oFun);
					node.addChild(newChild);

					return TreeNodeCallback.CONTINUE_SIBLING; // continue next
					// sibling
				}
				return TreeNodeCallback.CONTINUE;
			}
		});

		this.containsUF = new boolean[nDimension]; // init false
		this.ufMemberLists = new List[nDimension];

		if (logger.isDebugEnabled()) {
			logger.debug("after initPositions " + this.toString());
		}
	}

	/**
	 * add members of dimension to tree recursively
	 * 
	 * @param posMembers
	 *            positon member array
	 * @param startIndex
	 *            start position for this dimension
	 * @param endIndex
	 *            end position for this dimension
	 * @param dimIndex
	 *            index of this dimension
	 * @param parentNode
	 *            parent node (previous dimension)
	 * @return index of position where the member of this dimension changes
	 */
	protected int addToPosTree(List<List<Member>> posMembers, int startIndex,
			int endIndex, int dimIndex, TreeNode<Exp> parentNode) {
		Member currentOfDim = posMembers.get(startIndex).get(dimIndex);

		Exp exp = QuaxUtil.expForMember(currentOfDim);
		TreeNode<Exp> newNode = new TreeNode<Exp>(exp);
		parentNode.addChild(newNode);

		// check range where member of this dimension is constant
		int endRange = startIndex + 1;
		for (; endRange < endIndex; endRange++) {
			if (!posMembers.get(endRange).get(dimIndex).equals(currentOfDim)) {
				break;
			}
		}

		int nextDim = dimIndex + 1;
		if (nextDim < nDimension) {
			int endChild = addToPosTree(posMembers, startIndex, endRange,
					nextDim, newNode);
			while (endChild < endRange) {
				endChild = addToPosTree(posMembers, endChild, endRange,
						nextDim, newNode);
			}
		}

		return endRange;
	}

	/**
	 * @return
	 */
	public int getNDimension() {
		return nDimension;
	}

	/**
	 * @return posTreeRoot
	 */
	public TreeNode<Exp> getPosTreeRoot() {
		return posTreeRoot;
	}

	/**
	 * @param posTreeRoot
	 * @param hiersChanged
	 */
	public void setPosTreeRoot(TreeNode<Exp> posTreeRoot, boolean hiersChanged) {
		this.posTreeRoot = posTreeRoot;

		if (hiersChanged) {
			// count dimensions, set hierarchies
			TreeNode<Exp> firstNode = posTreeRoot;

			List<Hierarchy> hiersList = new ArrayList<Hierarchy>();
			List<TreeNode<Exp>> children = firstNode.getChildren();

			while (children.size() > 0) {
				firstNode = children.get(0);
				Exp oExp = firstNode.getReference();

				Hierarchy hier;
				try {
					hier = QuaxUtil.hierForExp(oExp);
				} catch (UnknownExpressionException e) {
					throw new PivotException(
							"Could not determine Hierarchy for set : "
									+ e.getExpression());
				}

				hiersList.add(hier);

				++nDimension;
				children = firstNode.getChildren();
			}

			hiers = hiersList;

			nDimension = hiers.size();

			containsUF = new boolean[nDimension]; // init false
			ufMemberLists = new List[nDimension];

			// go through nodes and check for Unknown functions
			// only one unknown function is possible in one hierarchy
			posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

				/**
				 * callback find unknown functions
				 */
				public int handleTreeNode(TreeNode<Exp> node) {
					int nodeIndex = node.getLevel() - 1;

					Exp oExp = node.getReference();
					if (!QuaxUtil.canHandle(oExp)) {
						// indicate that dimension i contains an unknown
						// function,
						// which cannot be handled in some cases.
						// this will cause the member list of this dimension to
						// be stored
						containsUF[nodeIndex] = true;
					}

					return TreeNodeCallback.CONTINUE;
				}
			});
		}
	}

	public int getGenerateIndex() {
		return generateIndex;
	}

	public void setGenerateIndex(int i) {
		this.generateIndex = i;
	}

	public CalcSetMode getGenerateMode() {
		return generateMode;
	}

	public void setGenerateMode(CalcSetMode mode) {
		this.generateMode = mode;
	}

	/**
	 * reset generate "topcount"
	 */
	public void resetGenerate() {
		this.generateMode = CalcSetMode.Simple;
		this.generateIndex = -1;
		this.expGenerate = null;
	}

	/**
	 * @return Returns the nHierExclude.
	 */
	public int getNHierExclude() {
		return nHierExclude;
	}

	/**
	 * @param hierExclude
	 *            The nHierExclude to set.
	 */
	public void setNHierExclude(int hierExclude) {
		this.nHierExclude = hierExclude;
	}

	/**
	 * only allow expand/collapse left of a "sticky topcount"
	 */
	private boolean allowNavigate(Member member, boolean qubon) {
		int iDim = dimIdx(member.getDimension());
		return allowNavigate(iDim, qubon);
	}

	/**
	 * Only allow expand/collapse left of a "sticky topcount"
	 */
	private boolean allowNavigate(int dimIndex, boolean qubon) {
		if (qubon && generateIndex >= 0 && generateMode == CalcSetMode.Sticky
				&& dimIndex == generateIndex) {
			return false;
		} else if (!qubon && generateIndex >= 0
				&& generateMode == CalcSetMode.Sticky
				&& dimIndex >= generateIndex) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @return
	 */
	public boolean isHierarchizeNeeded() {
		return hierarchizeNeeded;
	}

	/**
	 * @param b
	 */
	public void setHierarchizeNeeded(boolean b) {
		hierarchizeNeeded = b;
	}

	/**
	 * get Ordinal for axis, this is the immutable id of the quax
	 * 
	 * @return ordinal
	 */
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * @return hierarchies
	 */
	public List<Hierarchy> getHierarchies() {
		return hiers;
	}

	/**
	 * @param hierarchies
	 */
	public void setHierarchies(List<Hierarchy> hierarchies) {
		this.hiers = hierarchies;
		this.nDimension = hierarchies.size();
	}

	/**
	 * @return
	 */
	public boolean isQubonMode() {
		return qubonMode;
	}

	/**
	 * @param qubonMode
	 */
	public void setQubonMode(boolean qubonMode) {
		this.qubonMode = qubonMode;
	}

	/**
	 * Find out, whether axis contains dimension
	 * 
	 * @param dim
	 * @return index of dimension, -1 if not there
	 */
	public int dimIdx(Dimension dim) {
		if (hiers == null || hiers.isEmpty()) {
			return -1; // quax was not initialized yet
		}

		int i = 0;
		for (Hierarchy hierarchy : hiers) {
			if (hierarchy.getDimension().equals(dim)) {
				return i;
			}

			i++;
		}

		return -1;
	}

	/**
	 * Regenerate the position tree as crossjoin between sets
	 * 
	 * @param hiersChanged
	 *            indicates that the hierarchies were changed
	 */
	public void regeneratePosTree(List<Exp> sets, boolean hiersChanged) {
		if (hiersChanged) {
			this.nDimension = sets.size();
			this.hiers = new ArrayList<Hierarchy>(nDimension);

			for (Exp set : sets) {
				try {
					hiers.add(QuaxUtil.hierForExp(set));
				} catch (UnknownExpressionException e) {
					throw new PivotException("Unknown expression : "
							+ e.getExpression());
				}
			}

			this.containsUF = new boolean[nDimension]; // init false
			this.ufMemberLists = new List[nDimension];
			this.generateIndex = 0;
			this.generateMode = CalcSetMode.Simple;
		}

		if (posTreeRoot == null) {
			return;
		}

		posTreeRoot.getChildren().clear();

		TreeNode<Exp> current = posTreeRoot;

		// it would be fine, if we could get rid of an existing Hierarchize
		// - but this is not easy to decide.
		// we will not do it, if there is a "children" function call
		// not on the highest Level. This indicates that we have drilled
		// down any member.
		this.nHierExclude = 0;

		int nChildrenFound = 0;
		boolean childrenFound = false;
		for (int i = 0; i < nDimension; i++) {
			TreeNode<Exp> newNode;

			Exp set = sets.get(i);
			if (set instanceof SetExp) {
				SetExp setx = (SetExp) set;
				newNode = new TreeNode<Exp>(setx.getExpression());

				CalcSetMode mode = setx.getMode();
				if (mode != CalcSetMode.Simple) {
					this.generateMode = mode;
					this.generateIndex = i;
					this.expGenerate = setx.getExpression();
				}
			} else {
				// can we remove an existing "hierarchize needed"?
				boolean bChildrenFound = findChildrenCall(set, 0);
				if (bChildrenFound) {
					childrenFound = true;
					nChildrenFound = i + 1;
				}

				newNode = new TreeNode<Exp>(set);
				if (generateIndex == i && generateMode == CalcSetMode.Sticky) {
					// there was a sticky generate on this hier
					// reset, if set expression is different now
					if (!set.equals(expGenerate)) {
						resetGenerate();
					}
				}
			}
			current.addChild(newNode);
			current = newNode;

			if (!QuaxUtil.canHandle(newNode.getReference())) {
				// indicate that dimension i contains an unknown function,
				// which cannot be handled in some cases.
				// this will cause the member list of this dimension to be
				// stored
				containsUF[i] = true;
			}
		}

		this.qubonMode = true;
		this.nHierExclude = nDimension - nChildrenFound;

		if (!childrenFound) {
			this.hierarchizeNeeded = false;
		}
	}

	/**
	 * Recursively find "children" Funcall
	 */
	private boolean findChildrenCall(Exp oExp, int level) {
		if (!QuaxUtil.isFunCall(oExp))
			return false; // member or level or ...
		if (level > 0 && QuaxUtil.isFunCallTo(oExp, "children")) {
			return true;
		}

		int argCount = QuaxUtil.funCallArgCount(oExp);
		for (int i = 0; i < argCount; i++) {
			if (findChildrenCall(QuaxUtil.funCallArg(oExp, i), level + 1)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check, whether a member in a specific position path can be expanded
	 * 
	 * @param memberPath
	 *            position path to be expanded
	 */
	public boolean canExpand(List<Member> memberPath) {
		int dimIndex = memberPath.size() - 1;

		// we only allow expand / collapse for a dimension
		// left of a "sticky topcount"
		if (!allowNavigate(dimIndex, false)) {
			return false;
		}

		// first check the cache
		if (canExpandPosMap.containsKey(memberPath)) {
			Boolean bCanExpand = (Boolean) canExpandPosMap.get(memberPath);
			return bCanExpand.booleanValue();
		}

		// loop over Position Tree
		// reject expansion, if the axis already contains child-positions
		boolean childFound = checkChildPosition(memberPath);

		// cache the result
		Boolean bool = new Boolean(!childFound);
		canExpandPosMap.put(memberPath, bool);

		return !childFound;
	}

	/**
	 * Expand position path
	 * 
	 * @param memberPath
	 */
	public void expand(List<Member> memberPath) {
		if (qubonMode) {
			resolveUnions();

			if (logger.isDebugEnabled()) {
				logger.debug("Expand after resolveUnions " + this.toString());
			}
		}

		int dimIndex = memberPath.size() - 1;

		// update the position member tree
		// assume mPath = (Product.Drink,Time.2003,Customers.USA)
		// 1. find the node N1 for (Product.Drink,Time.2003)
		// 2. add the child node Customers.USA.Children to the node N1
		//
		// if the node N1 for (Product.Drink,Time.2003) was not found:
		// we look for a matching node and find for instance
		// node N2 = (Product.AllProducts.Children,Time.2003)
		// here, we cannot append Customers.USA.Children as a child node.
		// we add a new branch
		// (Product.Drink,Time.2003,Customers.USA.Children) to the tree.

		TreeNode<Exp> bestNode = findBestNode(memberPath);
		int bestNodeIndex = bestNode.getLevel() - 1;

		// add branch at startNode
		// example
		// dimensions: Product,MaritalStatus,Gender,Customer
		// mPath to Drill Down = (Product.AllProducts, MaritalStatus.M,
		// Gender.AllGender)
		// MaritalStatus.AllMaritalStatus was drilled down so best match is
		// (Product.AllProducts)
		// add the branch from MaritalStatus to this node giving
		// (Product.AllProducts,MaritalStatus.M,Gender.AllGender.children)
		// for the Customer Dimension, add all nodes matching
		// (Product.AllProducts, MaritalStatus.M, Gender.AllGender, * )

		List<TreeNode<Exp>> tailNodeList;
		if (memberPath.size() < nDimension) {
			tailNodeList = collectTailNodes(posTreeRoot, memberPath);
		} else {
			tailNodeList = Collections.emptyList();
		}

		TreeNode<Exp> newNode;

		Exp oMember = QuaxUtil.expForMember(memberPath.get(dimIndex));
		Exp fChildren = QuaxUtil.createFunCall("Children",
				new Exp[] { oMember }, Syntax.Property);

		TreeNode<Exp> parent = bestNode;

		// if bestNode is matching mPath[iDim]
		// we will add the children Funcall to its parent
		// otherwise create path from bestNode to mPath[iDim-1] and
		// add the children FunCall there
		if (bestNodeIndex == dimIndex) {
			parent = bestNode.getParent();
		} else {
			for (int i = bestNodeIndex + 1; i < memberPath.size() - 1; i++) {
				oMember = QuaxUtil.expForMember(memberPath.get(i));
				newNode = new TreeNode<Exp>(oMember);

				parent.addChild(newNode);
				parent = newNode;
			}
		}

		// any dimension left and including iDim will *not* be excluded from
		// hierarchize
		int n = nDimension - dimIndex - 1;
		if (n < nHierExclude) {
			this.nHierExclude = n;
		}

		newNode = new TreeNode<Exp>(fChildren);
		parent.addChild(newNode);

		if (memberPath.size() < nDimension) {
			for (TreeNode<Exp> tailNode : tailNodeList) {
				newNode.addChild(tailNode.deepCopy());
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("After expand " + this.toString());
		}

		this.qubonMode = false;
		this.hierarchizeNeeded = true;

		fireQuaxChanged(false);
	}

	/**
	 * Check, whether a member can be expanded
	 * 
	 * @param member
	 *            member to be expanded
	 */
	public boolean canExpand(Member member) {
		// we only allow expand / collapse for a dimension
		// left of a "sticky topcount"
		if (!allowNavigate(member, false)) {
			return false;
		}

		// first check the cache
		if (canExpandMemberMap.containsKey(member)) {
			boolean canExpand = canExpandMemberMap.get(member);
			return canExpand;
		}

		// loop over Position Tree
		// reject expansion, if the axis already contains children of member
		boolean found = !findMemberChild(member);

		// cache the result
		canExpandMemberMap.put(member, found);

		return found;
	}

	/**
	 * Expand member all over position tree
	 * 
	 * @param member
	 */
	public void expand(final Member member) {
		if (qubonMode) {
			resolveUnions();

			if (logger.isDebugEnabled()) {
				logger.debug("Expand after resolveUnions " + this.toString());
			}
		}

		// old stuff, always hierarchize everything
		this.nHierExclude = 0;

		final int dimIndex = this.dimIdx(member.getDimension());
		final List<TreeNode<Exp>> nodesForMember = new ArrayList<TreeNode<Exp>>();

		// update the position member tree
		// wherever we find monMember, expand it
		// collect all nodes for monMember in workList
		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find node matching member Path exactly
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE; // we are below iDim,
					// don't care
				}

				// iDimNode == iDim
				// node Exp must contain children of member[iDim]
				Exp oExp = node.getReference();
				if (QuaxUtil.isMember(oExp)) {
					if (QuaxUtil.equalMember(oExp, member)) {
						nodesForMember.add(node);
					}
				} else {
					// must be FunCall
					if (isMemberInFunCall(oExp, member, dimIndex)) {
						nodesForMember.add(node);
					}
				}

				return TreeNodeCallback.CONTINUE_SIBLING; // continue next
				// sibling
			}
		});

		// add children of member to each node in list
		Exp oMember = QuaxUtil.expForMember(member);
		Exp fChildren = QuaxUtil.createFunCall("Children",
				new Exp[] { oMember }, Syntax.Property);

		for (TreeNode<Exp> node : nodesForMember) {
			TreeNode<Exp> newNode = new TreeNode<Exp>(fChildren);

			for (TreeNode<Exp> child : node.getChildren()) {
				newNode.addChild(child.deepCopy());
			}

			TreeNode<Exp> parent = node.getParent();
			parent.addChild(newNode);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("After expand member " + this.toString());
		}

		this.hierarchizeNeeded = true;

		fireQuaxChanged(false);
	}

	/**
	 * Check, whether a member path can be collapsed this is true if there is a
	 * child position path
	 * 
	 * @param memberPath
	 *            position path to be collapsed
	 */
	public boolean canCollapse(List<Member> memberPath) {
		int dimIndex = memberPath.size() - 1;

		// we only allow expand / collapse for a dimension
		// left of a "sticky topcount"
		if (!allowNavigate(dimIndex, false)) {
			return false;
		}

		// first check the cache
		if (canCollapsePosMap.containsKey(memberPath)) {
			boolean canCollapse = canCollapsePosMap.get(memberPath);
			return canCollapse;
		}

		// loop over Position Tree
		// collapse is possible, if the axis already contains child-positions
		boolean childFound = checkChildPosition(memberPath);

		// cache the result
		canCollapsePosMap.put(memberPath, childFound);

		return childFound;
	}

	/**
	 * Remove child positions of mPath from position tree
	 * 
	 * @param memberPath
	 *            member path to be collapsed
	 */
	public void collapse(final List<Member> memberPath) {
		if (qubonMode) {
			resolveUnions();

			if (logger.isDebugEnabled()) {
				logger.debug("Collapse after resolveUnions " + this.toString());
			}
		}

		final int dimIndex = memberPath.size() - 1;

		int pathSize = memberPath.size();

		// determine FunCall nodes to be split
		final List<List<TreeNode<Exp>>> splitLists = new ArrayList<List<TreeNode<Exp>>>(
				pathSize);
		for (int i = 0; i < pathSize; i++) {
			splitLists.add(new ArrayList<TreeNode<Exp>>());
		}

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback Find child paths of member path. Collect FunCall nodes
			 * above in List. We have a list for any dimension, so that we can
			 * avoid dependency conflicts when we split the FunCalls.
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				// check, whether this node matches mPath
				Exp oExp = node.getReference();

				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					if (QuaxUtil.isMember(oExp)) {
						if (QuaxUtil.equalMember(oExp,
								memberPath.get(nodeIndex))) {
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.CONTINUE_SIBLING;
						}
					} else {
						// Funcall
						if (isMemberInFunCall(oExp, memberPath.get(nodeIndex),
								nodeIndex)) {
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.CONTINUE_SIBLING;
						}
					}
				}
				// idi == iDim
				// oExp *must* be descendant of mPath[iDim] to get deleted
				boolean found = false;
				if (QuaxUtil.isMember(oExp)) {
					// Member
					if (QuaxUtil.isDescendant(memberPath.get(dimIndex), oExp)) {
						found = true;
					}
				} else {
					// FunCall
					if (isChildOfMemberInFunCall(oExp,
							memberPath.get(dimIndex), dimIndex)) {
						found = true;
					}
				}

				if (found) {
					// add this node and all parent nodes, if they are funcalls,
					// to split list
					int level = node.getLevel();
					TreeNode<Exp> currentNode = node;
					while (level > 0) {
						Exp o = currentNode.getReference();
						if (!QuaxUtil.isMember(o)) {
							List<TreeNode<Exp>> list = splitLists
									.get(level - 1);
							// Funcall
							if (!list.contains(currentNode)) {
								list.add(currentNode);
							}
						}
						currentNode = currentNode.getParent();
						level = currentNode.getLevel();
					}
				}
				return TreeNodeCallback.CONTINUE_SIBLING;
			} // handleTreeNode
		});

		// split all FunCall nodes collected in worklist
		// start with higher levels to avoid dependency conflicts
		for (int i = pathSize - 1; i >= 0; i--) {
			List<TreeNode<Exp>> list = splitLists.get(i);
			Member member = memberPath.get(i);
			for (TreeNode<Exp> node : list) {
				splitFunCall(node, member, i);
			}
		}

		// remove child Paths of mPath from position tree
		// collect nodes to be deleted
		final List<TreeNode<Exp>> removeList = new ArrayList<TreeNode<Exp>>();

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {
			/**
			 * callback remove child nodes of member path, first collect nodes
			 * in workList
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				// check, whether this node matches mPath
				Exp oExp = node.getReference();
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					if (QuaxUtil.isMember(oExp)) {
						if (QuaxUtil.equalMember(oExp,
								memberPath.get(nodeIndex))) {
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.CONTINUE_SIBLING;
						}
					} else {
						// FunCall
						// cannot match as we just did the split of FunCalls
						return TreeNodeCallback.CONTINUE_SIBLING;
					}
				} else if (nodeIndex == dimIndex) {
					// *must* be descendant of mPath[iDim] to get deleted
					if (!QuaxUtil.isMember(oExp)) {
						// FunCall
						if (QuaxUtil.isFunCallTo(oExp, "Children")) {
							Exp oMember = QuaxUtil.funCallArg(oExp, 0);

							if (QuaxUtil.expForMember(memberPath.get(dimIndex))
									.equals(oMember)
									|| QuaxUtil.isDescendant(
											memberPath.get(dimIndex), oMember)) {
								removeList.add(node); // add to delete list
							}
						} else if (QuaxUtil.isFunCallTo(oExp, "{}")) {
							// set of members may be there as result of split,
							// we will remove any descendant member from the
							// set.
							// if the set is empty thereafter, we will add the
							// node
							// to the remove list.
							int argCount = QuaxUtil.funCallArgCount(oExp);
							List<Exp> removeMembers = new ArrayList<Exp>();
							for (int i = 0; i < argCount; i++) {
								Exp oSetMember = QuaxUtil.funCallArg(oExp, i);
								if (QuaxUtil.isDescendant(
										memberPath.get(dimIndex), oSetMember)) {
									removeMembers.add(oSetMember);
								}
							}
							int nRemove = removeMembers.size();
							if (nRemove == argCount) {
								// all memers in set are descendants, remove the
								// node
								removeList.add(node); // add to delete list
							} else if (nRemove > 0) {
								// remove descendant nodes from set
								Exp[] remaining = new Exp[argCount - nRemove];
								int j = 0;
								for (int i = 0; i < argCount; i++) {
									Exp oSetMember = QuaxUtil.funCallArg(oExp,
											i);
									if (!removeMembers.contains(oSetMember)) {
										remaining[j++] = oSetMember;
									}
								}

								if (remaining.length == 1) {
									node.setReference(remaining[0]); // single
									// member
								} else {
									Exp newSet = QuaxUtil.createFunCall("{}",
											remaining, Syntax.Braces);
									node.setReference(newSet);
								}
							}
						} else if (QuaxUtil.isFunCallTo(oExp, "Union")) {
							// HHTASK Cleanup, always use
							// removeDescendantsFromFunCall
							Exp oRemain = removeDescendantsFromFunCall(oExp,
									memberPath.get(dimIndex), dimIndex);
							if (oRemain == null) {
								removeList.add(node);
							} else {
								node.setReference(oRemain);
							}
						}
						return TreeNodeCallback.CONTINUE_SIBLING;
					} else if (QuaxUtil.isMember(oExp)) {
						if (QuaxUtil.isDescendant(memberPath.get(dimIndex),
								oExp)) {
							removeList.add(node);
						}
					}
					return TreeNodeCallback.CONTINUE_SIBLING;
					// always break on level iDim, next sibling
				} else {
					// should never get here
					throw new PivotException("Unexpected tree node level "
							+ nodeIndex + " "
							+ QuaxUtil.memberString(memberPath));
				}
			}
		});

		// remove nodes collected in work list
		for (TreeNode<Exp> nodeToRemove : removeList) {
			removePathToNode(nodeToRemove);
		}

		// any dimension left and including iDim will *not* be excluded from
		// hierarchize
		int n = nDimension - dimIndex - 1;
		if (n < nHierExclude) {
			this.nHierExclude = n;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("After collapse " + this.toString());
		}

		fireQuaxChanged(false);
	}

	/**
	 * Check, whether a member path can be collapsed this is true if there is a
	 * child position path
	 * 
	 * @param member
	 *            position path to be collapsed
	 */
	public boolean canCollapse(Member member) {
		// we only allow expand / collapse for a dimension
		// left of a "sticky topcount"
		if (!allowNavigate(member, false)) {
			return false;
		}

		// first check the cache
		if (canCollapseMemberMap.containsKey(member)) {
			boolean canCollapse = canCollapseMemberMap.get(member);
			return canCollapse;
		}

		// loop over Position Tree
		// can collapse, if we find a descendant of member
		boolean found = findMemberChild(member);

		// cache the result
		canCollapseMemberMap.put(member, found);

		return found;
	}

	/**
	 * Remove child nodes of monMember
	 * 
	 * @param member
	 *            member to be collapsed
	 */
	public void collapse(final Member member) {
		if (qubonMode) {
			resolveUnions();

			if (logger.isDebugEnabled()) {
				logger.debug("collapse member after resolveUnions "
						+ this.toString());
			}
		}

		final int dimIndex = this.dimIdx(member.getDimension());

		final List<TreeNode<Exp>> nodesForMember = new ArrayList<TreeNode<Exp>>();

		// update the position member tree
		// wherever we find a descendant node of monMember, split and remove it
		// collect all descendant nodes for monMember in workList
		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find node matching member Path exactly
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE; // we are below iDim,
					// don't care
				}

				// iDimNode == iDim
				// node Exp must contain children of member[iDim]
				Exp oExp = node.getReference();
				if (QuaxUtil.isMember(oExp)) {
					if (QuaxUtil.isDescendant(member, oExp)) {
						nodesForMember.add(node);
					}
				} else {
					// must be FunCall
					if (isDescendantOfMemberInFunCall(oExp, member, nodeIndex)) {
						nodesForMember.add(node);
					}
				}
				return TreeNodeCallback.CONTINUE_SIBLING; // continue next
				// sibling
			}
		});

		for (TreeNode<Exp> node : nodesForMember) {
			Exp oExp = node.getReference();
			if (QuaxUtil.isMember(oExp)) {
				removePathToNode(node);
			} else {
				// FunCall
				Exp oComplement = removeDescendantsFromFunCall(oExp, member,
						dimIndex);
				if (oComplement == null) {
					removePathToNode(node);
				} else {
					node.setReference(oComplement); // replace node object by
													// complement
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("After collapse " + this.toString());
		}

		fireQuaxChanged(false);
	}

	/**
	 * drill down is possible if there is no sticky generate
	 */
	public boolean canDrillDown(Member member) {
		return allowNavigate(member, true);
	}

	/**
	 * Drill down
	 * 
	 * @param member
	 *            drill down member
	 */
	public void drillDown(Member member) {
		final int dimIndex = this.dimIdx(member.getDimension());

		// collect the Exp's of all dimensions except iDim
		List<Exp> sets = new ArrayList<Exp>(nDimension);

		Exp oMember = QuaxUtil.expForMember(member);
		Exp fChildren = QuaxUtil.createFunCall("Children",
				new Exp[] { oMember }, Syntax.Property);

		for (int i = 0; i < nDimension; i++) {
			if (i == dimIndex) {
				// replace drilldown dimension by member.children
				sets.add(fChildren);
			} else {
				// generate exp for all nodes of this dimension
				sets.add(genExpForDim(i));
			}
		}

		// regenerate the position tree as crossjoin of sets
		regeneratePosTree(sets, false);

		fireQuaxChanged(false);
	}

	/**
	 * Drill up is possible if at least one member in the tree is not at the top
	 * level of this hierarchy.
	 */
	public boolean canDrillUp(Hierarchy hierarchy) {
		final int dimIndex = this.dimIdx(hierarchy.getDimension());

		if (!allowNavigate(dimIndex, true)) {
			return false;
		}

		int result = posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * Callback check for member of hierarchy not on top level
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE;
				}

				// iDimNode == workInt
				Exp oExp = node.getReference();
				if (!QuaxUtil.isMember(oExp)) {
					// FunCall
					if (isFunCallNotTopLevel(oExp, nodeIndex)) {
						return TreeNodeCallback.BREAK; // got it
					} else {
						return TreeNodeCallback.CONTINUE_SIBLING;
					}
				} else {
					// member
					if (QuaxUtil.levelDepthForMember(oExp) > 0) {
						return TreeNodeCallback.BREAK; // got it
					} else {
						return TreeNodeCallback.CONTINUE_SIBLING;
					}
				}
			}
		});

		return (result == TreeNodeCallback.BREAK);
	}

	/**
	 * Drill up
	 * 
	 * @param hierarchy
	 *            drill down member
	 */
	public void drillUp(Hierarchy hierarchy) {
		int dimIndex = dimIdx(hierarchy.getDimension());

		// collect the Exp's of all dimensions
		List<Exp> sets = new ArrayList<Exp>(nDimension);

		for (int i = 0; i < nDimension; i++) {
			if (i == dimIndex) {
				// replace drillup dimension by drillup set
				sets.add(drillupExp(dimIndex, hierarchy));
			} else {
				sets.add(genExpForDim(i));
			}
		}

		// regenerate the position tree as crossjoin of sets
		regeneratePosTree(sets, false);

		fireQuaxChanged(false);
	}

	/**
	 * MDX Generation generate Exp from tree
	 * 
	 * @return Exp for axis set
	 */
	public Exp genExp(boolean genHierarchize) {
		if (generateMode != CalcSetMode.Simple && generateIndex > 0) {
			return genGenerateExp(genHierarchize);
		} else {
			return genNormalExp(genHierarchize);
		}
	}

	/**
	 * Normal MDX Generation - no Generate
	 * 
	 * @return Exp for axis set
	 */
	private Exp genNormalExp(boolean genHierarchize) {
		ExpGenerator expGenerator = new ExpGenerator();

		if (!genHierarchize) {
			// no Hierarchize
			expGenerator.init(posTreeRoot, hiers.size());
			return expGenerator.generate();
		}

		// do we need a special hierarchize ?
		// this will be true, if nHierExclude > 0
		if (nHierExclude == 0) {
			// no special hierarchize needed
			expGenerator.init(posTreeRoot, hiers.size());

			Exp exp = expGenerator.generate();
			// Hierarchize around "everything"
			return QuaxUtil.createFunCall("Hierarchize", new Exp[] { exp },
					Syntax.Function);
		}

		// special hierarchize to be generated
		// the Qubon Mode Hierarchies are factored out,
		// as they consist only of a single set of members.
		// the left expression will be generated and then hierarchized,
		// *before* beeing crossjoined to the right Expression.
		return genLeftRight(expGenerator, nDimension - nHierExclude,
				nHierExclude);
	}

	/**
	 * Generate an expression with hierarchize for the hierarchies <
	 * nHierExclude without hierarchize for the hierarchies >= nHierExclude
	 */
	private Exp genLeftRight(ExpGenerator expGenerator, int leftDepth,
			int rightDepth) {
		// generate left expression to be hierarchized
		Exp leftExp = null;
		if (leftDepth > 0) {
			TreeNode<Exp> leftRoot = posTreeRoot.deepCopyPrune(leftDepth);
			leftRoot.setReference(null);

			List<Hierarchy> leftHiers = new ArrayList<Hierarchy>(leftDepth);
			for (int i = 0; i < leftDepth; i++) {
				leftHiers.add(hiers.get(i));
			}

			expGenerator.init(leftRoot, leftHiers.size());

			leftExp = QuaxUtil.createFunCall("Hierarchize",
					new Exp[] { expGenerator.generate() }, Syntax.Function);
		}

		// generate the right expression, not to be hierarchized
		Exp rightExp = null;

		List<Hierarchy> rightHiers = new ArrayList<Hierarchy>(rightDepth);
		for (int i = 0; i < rightDepth; i++) {
			rightHiers.add(hiers.get(leftDepth + i));
		}

		// go down to the first hier to be excluded from hierarchize
		// note: the subtree tree under any node of the hierarchy above
		// is always the same, so we can replicate any subtree under
		// a node of hierarchy nLeft-1
		TreeNode<Exp> rightRoot = new TreeNode<Exp>(null);
		TreeNode<Exp> current = posTreeRoot;
		for (int i = 0; i < leftDepth; i++) {
			List<TreeNode<Exp>> list = current.getChildren();
			current = list.get(0);
		}

		List<TreeNode<Exp>> list = current.getChildren();
		for (TreeNode<Exp> node : list) {
			rightRoot.addChild(node.deepCopy());
		}

		expGenerator.init(rightRoot, rightHiers.size());

		rightExp = expGenerator.generate();
		if (leftExp == null) {
			return rightExp;
		}

		Exp exp = QuaxUtil.createFunCall("CrossJoin", new Exp[] { leftExp,
				rightExp }, Syntax.Function);

		return exp;
	}

	/**
	 * MDX Generation for Generate
	 * 
	 * @return Exp for axis set
	 */
	private Exp genGenerateExp(boolean genHierarchize) {
		ExpGenerator expGenerator = new ExpGenerator();

		// Generate(GSet, FSet) to be generated
		// hierarchies >= generateIndex will not be "hierarchized"
		// we expect the hierarchies >= generateIndex to be excluded
		// from hierarchize.
		if (nDimension - generateIndex > nHierExclude && logger.isWarnEnabled()) {
			logger.warn("Unexpected values: nHierExclude=" + nHierExclude
					+ " generateIndex=" + generateIndex);
		}

		// assume following situation:
		// 3 hierarchies
		// time - customers - product
		// we want top 5 customers, generated for each time member
		// 1. step
		// generate expression until customers (only time here), result = set1
		// if neccessary, put hierarchize around
		// 2. step
		// Generate(set1, Topcount(Crossjoin ({Time.Currentmember}, Set for
		// Customers),
		// 5, condition))
		// result = set2
		// 3.step
		// append the tail nodes , here Product
		// Crossjoin(set2 , Product dimension nodes)
		//
		// 1. step left expression, potentially hierarchized

		Exp leftExp = null;
		// if nHierExclude > nDimension - generateIndex
		// and nHierExclude < nDimension
		// the the left expression (inside Generate) will be partly
		// hierarchized
		if (genHierarchize && nHierExclude > nDimension - generateIndex
				&& nHierExclude < nDimension) {
			int leftDepth = nDimension - nHierExclude;
			int rightDepth = generateIndex - leftDepth;

			leftExp = genLeftRight(expGenerator, leftDepth, rightDepth);
		} else {
			TreeNode<Exp> leftRoot = posTreeRoot.deepCopyPrune(generateIndex);
			leftRoot.setReference(null);

			List<Hierarchy> leftHiers = new ArrayList<Hierarchy>(generateIndex);
			for (int i = 0; i < generateIndex; i++) {
				leftHiers.add(hiers.get(i));
			}

			expGenerator.init(leftRoot, leftHiers.size());
			leftExp = expGenerator.generate();

			if (genHierarchize) {
				leftExp = QuaxUtil.createFunCall("Hierarchize",
						new Exp[] { leftExp }, Syntax.Function);
			}
		}

		// 2. step Generate(set1, Topcount())
		TreeNode<Exp> topCountNode = posTreeRoot;
		// top count node can be anything like topcount, bottomcount, filter
		for (int i = 0; i <= generateIndex; i++) {
			// the path to the topcount node at generateIndex does not matter
			List<TreeNode<Exp>> children = topCountNode.getChildren();
			topCountNode = children.get(0);
		}

		Exp topcount = topCountNode.getReference();
		// we have to replace the "set" of the topcount function
		Exp origTopcountSet = QuaxUtil.funCallArg(topcount, 0);

		// generate the Tuple of dimension.currentmember until generateIndex
		Exp currentMembersTuple = genCurrentTuple();
		Exp ocj = QuaxUtil.createFunCall("Crossjoin", new Exp[] {
				currentMembersTuple, origTopcountSet }, Syntax.Function);

		// replace the topcout original set
		String fun = QuaxUtil.funCallName(topcount);

		int n = QuaxUtil.funCallArgCount(topcount);
		Exp[] args = new Exp[n];
		for (int i = 1; i < n; i++) {
			args[i] = QuaxUtil.funCallArg(topcount, i);
		}

		args[0] = ocj;

		Exp newTopcount = QuaxUtil.createFunCall(fun, args, Syntax.Function);
		Exp oGenerate = QuaxUtil.createFunCall("Generate", new Exp[] { leftExp,
				newTopcount }, Syntax.Function);

		if (generateIndex + 1 == nDimension) {
			return oGenerate;
		}

		// 3. step append the tail nodes
		// generate CrossJoin
		int nRight = nDimension - generateIndex - 1;
		Hierarchy[] rightHiers = new Hierarchy[nRight];
		for (int i = 1; i <= nRight; i++) {
			rightHiers[nRight - i] = hiers.get(nDimension - i);
		}

		TreeNode<Exp> root = new TreeNode<Exp>(null);
		List<TreeNode<Exp>> list = topCountNode.getChildren();
		for (TreeNode<Exp> node : list) {
			root.addChild(node.deepCopy());
		}

		expGenerator.init(root, rightHiers.length);
		Exp rightExp = expGenerator.generate();

		Exp exp = QuaxUtil.createFunCall("CrossJoin", new Exp[] { oGenerate,
				rightExp }, Syntax.Function);

		return exp;
	}

	/**
	 * Generate {(dim1.Currentmember, dim2.Currentmember, ... )}
	 */
	private Exp genCurrentTuple() {
		Exp[] currentsOfDim = new Exp[generateIndex];
		for (int i = 0; i < currentsOfDim.length; i++) {
			Dimension dim = hiers.get(i).getDimension();

			currentsOfDim[i] = QuaxUtil.createFunCall("CurrentMember",
					new Exp[] { QuaxUtil.expForDim(dim) }, Syntax.Property);
		}

		Exp oTuple;
		if (generateIndex > 1) {
			oTuple = QuaxUtil.createFunCall("()", currentsOfDim,
					Syntax.Parentheses);
		} else {
			oTuple = currentsOfDim[0]; // just dimension.currentmember
		}

		// generate set braces around tuple
		Exp oSet = QuaxUtil.createFunCall("{}", new Exp[] { oTuple },
				Syntax.Braces);

		return oSet;
	}

	/**
	 * @return true if child position can be found
	 */
	private boolean checkChildPosition(final List<Member> memberPath) {

		int result = posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find node matching member Path exactly
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int dimIndex = memberPath.size() - 1;
				int nodeIndex = node.getLevel() - 1;

				Exp oExp = node.getReference();
				if (nodeIndex < dimIndex) {
					// node Exp must match member[iDim]
					if (QuaxUtil.isMember(oExp)) {
						if (QuaxUtil.equalMember(oExp,
								memberPath.get(nodeIndex))) {
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.CONTINUE_SIBLING; // continue
							// next
							// sibling
						}
					} else {
						// must be FunCall
						if (isMemberInFunCall(oExp, memberPath.get(nodeIndex),
								nodeIndex)) {
							return TreeNodeCallback.CONTINUE;
						} else {
							return TreeNodeCallback.CONTINUE_SIBLING; // continue
							// next
							// sibling
						}
					}
				}

				// iDimNode == iDim
				// node Exp must contain children of member[iDim]
				if (QuaxUtil.isMember(oExp)) {
					if (QuaxUtil.checkParent(memberPath.get(nodeIndex), oExp)) {
						return TreeNodeCallback.BREAK; // found
					} else {
						return TreeNodeCallback.CONTINUE_SIBLING; // continue
						// next
						// sibling
					}
				} else {
					// must be FunCall
					if (isChildOfMemberInFunCall(oExp,
							memberPath.get(nodeIndex), nodeIndex)) {
						return TreeNodeCallback.BREAK; // found
					} else {
						return TreeNodeCallback.CONTINUE_SIBLING; // continue
						// next
						// sibling
					}
				}
			}
		});

		return (result == TreeNodeCallback.BREAK);
	}

	/**
	 * Resolve the qubon mode unions and crossjoins only used in "old" expand
	 * mode
	 */
	private void resolveUnions() {
		final List<List<Exp>> setLists = new ArrayList<List<Exp>>(nDimension);

		for (int i = 0; i < nDimension; i++) {
			setLists.add(new ArrayList<Exp>());
		}

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback resolve sets of any dimension
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				Exp oExp = node.getReference();
				if (!QuaxUtil.isMember(oExp)) {
					// FunCall
					funToList(oExp, setLists.get(nodeIndex));
				} else {
					// member
					setLists.get(nodeIndex).add(oExp);
				}
				return TreeNodeCallback.CONTINUE;
			}
		});

		// unions and sets are resolved, now resolve crossjoins
		this.posTreeRoot = new TreeNode<Exp>(null);
		crossJoinTree(setLists, posTreeRoot, 0);

		this.qubonMode = false;
	}

	/**
	 * Find the best tree node for member path (longest match)
	 */
	private TreeNode<Exp> findBestNode(final List<Member> memberPath) {
		@SuppressWarnings("unchecked")
		final TreeNode<Exp>[] bestNode = new TreeNode[1];
		bestNode[0] = posTreeRoot;

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find node matching member Path exactly
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int dimIndex = memberPath.size() - 1;
				int nodeIndex = node.getLevel() - 1;
				Exp oExp = node.getReference();

				if (!QuaxUtil.isMember(oExp)) {
					return TreeNodeCallback.CONTINUE_SIBLING; // continue next
					// sibling
				}

				if (QuaxUtil.equalMember(oExp, memberPath.get(nodeIndex))) {
					// match
					if (nodeIndex == dimIndex) {
						// found exactly matching node
						bestNode[0] = node;
						return TreeNodeCallback.BREAK;
					} else {
						// best match up to now
						bestNode[0] = node;
						return TreeNodeCallback.CONTINUE;
					}
				} else {
					// no match
					return TreeNodeCallback.CONTINUE_SIBLING; // continue next
					// sibling
				}
			}
		});

		return bestNode[0];
	}

	/**
	 * Collect tail nodes for all nodes matching member path
	 */
	private List<TreeNode<Exp>> collectTailNodes(TreeNode<Exp> startNode,
			final List<Member> memberPath) {

		final List<TreeNode<Exp>> tailNodes = new ArrayList<TreeNode<Exp>>();
		startNode.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find node matching mPath collect tail nodes
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int dimIndex = memberPath.size() - 1;
				int nodeIndex = node.getLevel() - 1;

				Exp oExp = node.getReference();
				boolean match = false;
				if (QuaxUtil.isMember(oExp)) {
					// exp is member
					if (QuaxUtil.equalMember(oExp, memberPath.get(nodeIndex))) {
						match = true;
					}
				} else {
					// must be FunCall
					if (isMemberInFunCall(oExp, memberPath.get(nodeIndex),
							nodeIndex)) {
						match = true;
					}
				}

				if (match) {
					if (nodeIndex == dimIndex) {
						// add the children to the tail list
						tailNodes.addAll(node.getChildren());
						return TreeNodeCallback.CONTINUE_SIBLING;
					} else {
						// iDimNode < iDim
						return TreeNodeCallback.CONTINUE;
					}
				} else
					return TreeNodeCallback.CONTINUE_SIBLING; // no match,
				// continue next
				// sibling
			}
		});

		return tailNodes;
	}

	private boolean findMemberChild(final Member member) {
		final int iDim = this.dimIdx(member.getDimension());

		int result = posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback find child node of member
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < iDim) {
					return TreeNodeCallback.CONTINUE; // we are below iDim,
					// don't care
				}

				// iDimNode == iDim
				// node Exp must contain children of member[iDim]
				Exp oExp = node.getReference();
				if (QuaxUtil.isMember(oExp)) {
					if (QuaxUtil.checkParent(member, oExp)) {
						return TreeNodeCallback.BREAK; // found
					}
				} else {
					// must be FunCall
					if (isChildOfMemberInFunCall(oExp, member, nodeIndex)) {
						return TreeNodeCallback.BREAK; // found
					}
				}
				return TreeNodeCallback.CONTINUE_SIBLING; // continue next
				// sibling
			}
		});

		return (result == TreeNodeCallback.BREAK);
	}

	/**
	 * Build tree resolving crossjoin
	 * 
	 * @param currentNode
	 * @param dimIndex
	 */
	private void crossJoinTree(List<List<Exp>> setLists,
			TreeNode<Exp> currentNode, int dimIndex) {
		for (Exp oExp : setLists.get(dimIndex)) {
			TreeNode<Exp> newNode = new TreeNode<Exp>(oExp);
			if (dimIndex < nDimension - 1) {
				crossJoinTree(setLists, newNode, dimIndex + 1);
			}

			currentNode.addChild(newNode);
		}
	}

	/**
	 * Split Funcall to node and complement
	 */
	private void splitFunCall(TreeNode<Exp> funCall, Member member,
			int hierIndex) {
		Exp oExp = funCall.getReference();

		// it is possible (if the split member is of dimension to be collapsed),
		// that this funcall does not contain member.
		// Then - there is nothing to split.
		if (!isMemberInFunCall(oExp, member, funCall.getLevel() - 1)) {
			return; // nothing to split
		}

		Exp oComplement = createComplement(oExp, member, hierIndex); // can be
																		// null
		if (oComplement == null) {
			// this means, that the set resolves to a single member,
			// mPath[iDimNode]
			funCall.setReference(QuaxUtil.expForMember(member));
			// nothing to split
			return;
		}

		// split the Funcall
		TreeNode<Exp> newNodeComplement = new TreeNode<Exp>(oComplement);
		TreeNode<Exp> newNodeMember = new TreeNode<Exp>(
				QuaxUtil.expForMember(member));

		// add the children
		for (TreeNode<Exp> child : funCall.getChildren()) {
			newNodeComplement.addChild(child.deepCopy());
			newNodeMember.addChild(child.deepCopy());
		}

		TreeNode<Exp> insert = funCall.getParent();
		funCall.remove();

		insert.addChild(newNodeComplement);
		insert.addChild(newNodeMember);
	}

	/**
	 * Remove Children node
	 * 
	 * @param nodeToRemove
	 */
	private void removePathToNode(TreeNode<Exp> nodeToRemove) {
		if (nodeToRemove.getParent().getChildren().size() > 1) {
			// this node has siblings, just remove it
			nodeToRemove.remove();
		} else {
			// no siblings, remove the first parent node having siblings
			TreeNode<Exp> parent = nodeToRemove.getParent();
			while (parent.getParent().getChildren().size() == 1) {
				parent = parent.getParent();
			}

			if (parent.getLevel() > 0) { // should always be true
				parent.remove();
			}
		}
	}

	/**
	 * generate Exp for all nodes of dimension iDimension
	 * 
	 * @param dimIndex
	 * @return Exp for all nodes
	 */
	public Exp genExpForDim(int dimIndex) {
		// if we got a generate function on this hier, preserve it
		if (generateIndex >= 0 && generateIndex == dimIndex
				&& generateMode != CalcSetMode.Simple) {
			TreeNode<Exp> topCountNode = posTreeRoot.getChildren().get(0);
			for (int i = 0; i < generateIndex; i++) {
				// the path to the topcount node at generateIndex does not
				// matter
				List<TreeNode<Exp>> children = topCountNode.getChildren();
				topCountNode = children.get(0);
			}

			Exp topcount = topCountNode.getReference();

			SetExp setexp = new SetExp(generateMode, topcount,
					hiers.get(dimIndex));

			return setexp;
		}

		List<Exp> funCallList = collectFunCalls(dimIndex);
		List<Exp> memberList = collectMembers(dimIndex);

		cleanupMemberList(funCallList, memberList, dimIndex);

		if (funCallList.isEmpty() && memberList.size() == 1) {
			return memberList.get(0); // single member only
		}

		Exp mSet = null;
		if (memberList.size() > 0) {
			Exp[] aExp = memberList.toArray(new Exp[0]);
			mSet = QuaxUtil.createFunCall("{}", aExp, Syntax.Braces);
		}

		if (funCallList.isEmpty()) {
			return mSet;
		}

		if (funCallList.size() == 1 && mSet == null) {
			return funCallList.get(0);
		}

		Exp set;

		int start;
		if (mSet != null) {
			set = mSet;
			start = 0;
		} else {
			set = funCallList.get(0);
			start = 1;
		}
		for (int j = start; j < funCallList.size(); j++) {
			set = QuaxUtil.createFunCall("Union",
					new Exp[] { set, funCallList.get(j) }, Syntax.Function);
		}

		return set;
	}

	/**
	 * Create drillup expression for dimension
	 * 
	 * @param dimIndex
	 *            dimension to be drilled up
	 * @return
	 */
	private Exp drillupExp(int dimIndex, Hierarchy hierarchy) {
		// the drillup logic is:
		// for all members of this dimension find the deepest level.
		// find the members of this deepest level
		// find the grandfathers of those deepest members
		// drill up goes to the children of those grandfathers.
		// special cases:
		// the deepest level has all members (level.members)
		// the drillup goes to parent_level.members

		final int[] maxLevel = new int[1];
		maxLevel[0] = 0;

		List<Exp> drillupList = collectDrillup(dimIndex, maxLevel);

		Exp expForHier = null;
		if (maxLevel[0] == 0) {
			// drillup goes to top level members
			// we generate an explicit member set rather than level.members
			// usually, this is a single member "All xy"
			expForHier = QuaxUtil.topLevelMembers(hierarchy, false);
		} else {
			if (drillupList.size() == 1) {
				expForHier = drillupList.get(0);
			} else {
				// more than 1 set expression , need union
				for (Exp oExp : drillupList) {
					if (expForHier == null) {
						expForHier = oExp;
					} else {
						expForHier = QuaxUtil.createFunCall("Union", new Exp[] {
								expForHier, oExp }, Syntax.Function);
					}
				}
			}
		}

		return expForHier;
	}

	/**
	 * Collect drillup Exps of dimension i
	 * 
	 * @param dimIndex
	 */
	private List<Exp> collectDrillup(final int dimIndex, final int[] maxLevel) {
		final List<Exp> drillupList = new ArrayList<Exp>();

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback collect GrandFathers of deepest for dimension workInt
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE;
				}

				// iDimNode == workInt
				Exp oExp = node.getReference();
				if (!QuaxUtil.isMember(oExp)) {
					// FunCall
					addFunCallToDrillup(drillupList, oExp, maxLevel);
				} else {
					// member
					Member m = QuaxUtil.memberForExp(oExp);
					QuaxUtil.addMemberUncles(drillupList, m, maxLevel);
				}

				return TreeNodeCallback.CONTINUE_SIBLING;
			}
		});

		return drillupList;
	}

	/**
	 * Collect FunCalls of dimension iDim
	 * 
	 * @param dimIndex
	 */
	private List<Exp> collectFunCalls(final int dimIndex) {
		if (posTreeRoot == null) {
			return Collections.emptyList();
		}

		final List<Exp> funCalls = new ArrayList<Exp>();
		final List<String> uniqueNames = new ArrayList<String>();

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback collect Funcalls of dimension workInt
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE;
				}

				// iDimNode == workInt
				Exp oExp = node.getReference();
				if (!QuaxUtil.isMember(oExp)) {
					// FunCall need unique representation in order to avoid
					// doubles
					String unique = QuaxUtil.funString(oExp).toString();
					if (!uniqueNames.contains(unique)) {
						funCalls.add(oExp);
						uniqueNames.add(unique);
					}
				}

				return TreeNodeCallback.CONTINUE_SIBLING;
			}
		});

		return funCalls;
	}

	/**
	 * Remove members from member list being in FunCall list
	 * 
	 * @param funCalls
	 * @param memberList
	 * @param dimIndex
	 */
	private void cleanupMemberList(List<Exp> funCalls, List<Exp> memberList,
			int dimIndex) {
		if (!funCalls.isEmpty() && !memberList.isEmpty()) {
			MemberLoop: for (Iterator<Exp> itMem = memberList.iterator(); itMem
					.hasNext();) {
				Exp oMember = itMem.next();

				Member m = QuaxUtil.memberForExp(oMember);
				for (Iterator<Exp> itFun = funCalls.iterator(); itFun.hasNext();) {
					Exp oFun = itFun.next();
					if (isMemberInFunCall(oFun, m, dimIndex)) {
						itMem.remove();
						continue MemberLoop;
					}
				}
			}
		}
	}

	/**
	 * Collect Members of dimension iDim
	 * 
	 * @param dimIndex
	 */
	private List<Exp> collectMembers(final int dimIndex) {
		if (posTreeRoot == null) {
			return Collections.emptyList();
		}

		final List<Exp> memberList = new ArrayList<Exp>();

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback collect Funcalls of dimension workInt
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				if (nodeIndex < dimIndex) {
					return TreeNodeCallback.CONTINUE;
				}

				// iDimNode == workInt
				Exp oExp = node.getReference();
				if (QuaxUtil.isMember(oExp) && !memberList.contains(oExp)) {
					memberList.add(oExp);
				}

				return TreeNodeCallback.CONTINUE_SIBLING;
			}
		});

		return memberList;
	}

	/**
	 * Add a Funcall to Drillup list
	 */
	private void addFunCallToDrillup(List<Exp> list, Exp oFun, int[] maxLevel) {
		if (QuaxUtil.isFunCallTo(oFun, "Union")) {
			for (int i = 0; i < 2; i++) {
				Exp fExp = QuaxUtil.funCallArg(oFun, i);
				addFunCallToDrillup(list, fExp, maxLevel);
			}
		} else if (QuaxUtil.isFunCallTo(oFun, "{}")) {
			// set of members
			for (int i = 0; i < QuaxUtil.funCallArgCount(oFun); i++) {
				Exp oMember = QuaxUtil.funCallArg(oFun, i);
				Member m = QuaxUtil.memberForExp(oMember);
				QuaxUtil.addMemberUncles(list, m, maxLevel);
			}
		} else if (QuaxUtil.isFunCallTo(oFun, "Children")) {
			Exp oMember = QuaxUtil.funCallArg(oFun, 0);
			Member m = QuaxUtil.memberForExp(oMember);
			QuaxUtil.addMemberSiblings(list, m, maxLevel);
		} else if (QuaxUtil.isFunCallTo(oFun, "Descendants")) {
			Exp oMember = QuaxUtil.funCallArg(oFun, 0);
			Member m = QuaxUtil.memberForExp(oMember);
			Exp oLevel = QuaxUtil.funCallArg(oFun, 1);
			Level lev = QuaxUtil.levelForExp(oLevel);

			int level = m.getLevel().getDepth();
			int levlev = lev.getDepth();
			if (levlev == level + 1) {
				QuaxUtil.addMemberSiblings(list, m, maxLevel); // same as
																// children
			} else if (levlev == level + 2) {
				QuaxUtil.addMemberChildren(list, m, maxLevel); // m *is*
																// grandfather
			} else {
				// add descendants of parent level
				Level parentLevel = QuaxUtil.getParentLevel(lev);
				QuaxUtil.addMemberDescendants(list, m, parentLevel, maxLevel);
			}
		} else if (QuaxUtil.isFunCallTo(oFun, "Members")) {
			// add parent level members
			Exp oLevel = QuaxUtil.funCallArg(oFun, 0);
			Level lev = QuaxUtil.levelForExp(oLevel);

			int levlev = lev.getDepth();
			if (levlev == 0) {
				return; // cannot drill up
			}

			Level parentLevel = QuaxUtil.getParentLevel(lev);
			QuaxUtil.addLevelMembers(list, parentLevel, maxLevel);
		} else {
			// must be Top/Bottom Function with arg[0] being base set
			Exp oFun2 = QuaxUtil.funCallArg(oFun, 0);
			addFunCallToDrillup(list, oFun2, maxLevel); // do not have a better
														// solution
		}
	}

	/**
	 * Add FunCall to list
	 * 
	 * @param oFun
	 * @param list
	 */
	private void funToList(Exp oFun, List<Exp> list) {
		if (QuaxUtil.isFunCallTo(oFun, "Union")) {
			Exp arg0 = QuaxUtil.funCallArg(oFun, 0);
			Exp arg1 = QuaxUtil.funCallArg(oFun, 1);

			funToList(arg0, list);
			funToList(arg1, list);
		} else if (QuaxUtil.isFunCallTo(oFun, "{}")) {
			for (int i = 0; i < QuaxUtil.funCallArgCount(oFun); i++) {
				// member sets are resolved to single members
				Exp oMember = QuaxUtil.funCallArg(oFun, i);
				list.add(oMember);
			}
		} else {
			list.add(oFun);
		}
	}

	/**
	 * Check, whether member is in set defined by funcall
	 * 
	 * @param oExp
	 *            set funcall
	 * @param member
	 * @return
	 */
	private boolean isMemberInFunCall(Exp oExp, Member member, int hierIndex) {
		boolean result = false;

		try {
			result = QuaxUtil.isMemberInFunCall(oExp, member);
		} catch (UnknownExpressionException e) {
			// it is an Unkown FunCall
			// assume "true" if the member is in the List for this dimension
			if (ufMemberLists[hierIndex] == null)
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());

			result = ufMemberLists[hierIndex].contains(member);
		}

		return result;
	}

	/**
	 * Check whether a Funcall does NOT resolve to top level of hierarchy
	 */
	private boolean isFunCallNotTopLevel(Exp oExp, int hierIndex) {
		boolean result = false;

		try {
			result = QuaxUtil.isFunCallNotTopLevel(oExp);
		} catch (UnknownExpressionException e) {
			// it is an Unkown FunCall
			// assume "true" if the member is in the List for this dimension
			if (ufMemberLists[hierIndex] == null) {
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());
			}

			List<Member> members = ufMemberLists[hierIndex];
			for (Member member : members) {
				if (member.getLevel().getDepth() > 0) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Check whether a Funcall contains child of member
	 */
	private boolean isChildOfMemberInFunCall(Exp oExp, Member member,
			int hierIndex) {
		boolean result = false;

		try {
			result = QuaxUtil.isChildOfMemberInFunCall(oExp, member);
		} catch (UnknownExpressionException e) {
			// it is an Unkown FunCall
			// assume "true" if the member List for this dimension contains
			// child of member
			if (ufMemberLists[hierIndex] == null) {
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());
			}

			List<Member> members = ufMemberLists[hierIndex];
			for (Member m : members) {
				if (QuaxUtil.checkParent(member, QuaxUtil.expForMember(m))) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Check whether a Funcall contains descendant of member
	 */
	private boolean isDescendantOfMemberInFunCall(Exp oExp, Member member,
			int hierIndex) {
		boolean result = false;

		try {
			result = QuaxUtil.isDescendantOfMemberInFunCall(oExp, member);
		} catch (UnknownExpressionException e) {
			// it is an Unkown FunCall
			// assume "true" if the member List for this dimension contains
			// descendant of member
			if (ufMemberLists[hierIndex] == null) {
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());
			}

			List<Member> members = ufMemberLists[hierIndex];
			for (Member m : members) {
				if (QuaxUtil.checkDescendantM(member, m)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Remove descendants of member from Funcall set
	 * 
	 * @return the remainder after descendants were removed
	 */
	private Exp removeDescendantsFromFunCall(Exp oFun, Member member,
			int hierIndex) {
		try {
			return removeDescendantsFromFunCall(oFun, member);
		} catch (UnknownExpressionException e) {
			// the FunCall was not handled,
			// assume that it is an "Unkown FunCall" which was resolved by the
			// latest result
			// the "Unknown Functions" are probably not properly resolved
			if (logger.isErrorEnabled()) {
				logger.error("Unkown FunCall " + QuaxUtil.funCallName(oFun));
			}

			if (ufMemberLists[hierIndex] == null) {
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());
			}

			List<Exp> newList = new ArrayList<Exp>();

			List<Member> members = ufMemberLists[hierIndex];
			for (Member m : members) {
				if (!QuaxUtil.checkDescendantM(member, m)) {
					newList.add(QuaxUtil.expForMember(m));
				}
			}

			return QuaxUtil.createFunCall("{}",
					newList.toArray(new Exp[newList.size()]), Syntax.Braces);
		}
	}

	/**
	 * Remove descendants of member from Funcall set
	 * 
	 * @return the remainder after descendants were removed
	 */
	private Exp removeDescendantsFromFunCall(Exp oFun, Member member)
			throws UnknownExpressionException {
		if (QuaxUtil.isFunCallTo(oFun, "Children")) {
			// as we know, that there is a descendent of m in x.children,
			// we know that *all* x.children are descendants of m
			return null;
		} else if (QuaxUtil.isFunCallTo(oFun, "Descendants")) {
			// as we know, that there is a descendent of m in x.descendants
			// we know that *all* x.descendants are descendants of m
			return null;
		} else if (QuaxUtil.isFunCallTo(oFun, "Members")) {
			Level level = member.getLevel();

			List<Member> members;
			try {
				members = level.getMembers();
			} catch (OlapException e) {
				throw new PivotException(e);
			}

			List<Member> remainder = new ArrayList<Member>(members.size());
			for (Member m : members) {
				if (!QuaxUtil.isDescendant(member, m))
					remainder.add(m);
			}

			return QuaxUtil.createMemberSet(remainder);
		} else if (QuaxUtil.isFunCallTo(oFun, "{}")) {
			List<Member> remainder = new ArrayList<Member>();

			for (int i = 0; i < QuaxUtil.funCallArgCount(oFun); i++) {
				Exp arg = QuaxUtil.funCallArg(oFun, i);

				if (!QuaxUtil.isDescendant(member, arg)) {
					remainder.add(QuaxUtil.memberForExp(arg));
				}
			}

			return QuaxUtil.createMemberSet(remainder);
		} else if (QuaxUtil.isFunCallTo(oFun, "Union")) {
			Exp[] uargs = new Exp[2];
			uargs[0] = removeDescendantsFromFunCall(
					QuaxUtil.funCallArg(oFun, 0), member);
			uargs[1] = removeDescendantsFromFunCall(
					QuaxUtil.funCallArg(oFun, 0), member);

			if (uargs[0] == null && uargs[1] == null) {
				return null;
			}

			if (uargs[1] == null) {
				return uargs[0];
			}

			if (uargs[0] == null) {
				return uargs[1];
			}

			if (QuaxUtil.isMember(uargs[0])) {
				uargs[0] = QuaxUtil.createFunCall("{}", new Exp[] { uargs[0] },
						Syntax.Braces);
			}

			if (QuaxUtil.isMember(uargs[1])) {
				uargs[1] = QuaxUtil.createFunCall("{}", new Exp[] { uargs[1] },
						Syntax.Braces);
			}

			if (QuaxUtil.isFunCallTo(uargs[0], "{}")
					&& QuaxUtil.isFunCallTo(uargs[1], "{}")) {
				return unionOfSets(uargs[0], uargs[1]);
			}

			return QuaxUtil.createFunCall("Union", uargs, Syntax.Function);
		}

		throw new UnknownExpressionException(QuaxUtil.funCallName(oFun));
	}

	/**
	 * Determine complement set (set minus member)
	 */
	private Exp createComplement(Exp oFun, Member member, int hierIndex) {
		try {
			return createComplement(oFun, member);
		} catch (UnknownExpressionException e) {
			// the FunCall was not handled,
			// assume that it is an "Unkown FunCall" which was resolved by the
			// latest result
			// the "Unknown Functions" are probably not properly resolved
			if (logger.isErrorEnabled()) {
				logger.error("Unkown FunCall " + QuaxUtil.funCallName(oFun));
			}

			if (ufMemberLists[hierIndex] == null) {
				throw new PivotException(
						"Unknow Function - no member list, dimension="
								+ hierIndex + " function=" + e.getExpression());
			}

			List<Exp> newList = new ArrayList<Exp>();

			List<Member> members = ufMemberLists[hierIndex];
			for (Member m : members) {
				if (!member.equals(m)) {
					newList.add(QuaxUtil.expForMember(m));
				}
			}

			return QuaxUtil.createFunCall("{}",
					newList.toArray(new Exp[newList.size()]), Syntax.Braces);
		}
	}

	/**
	 * Determine complement set (set minus member)
	 * 
	 * @throws UnknownExpressionException
	 */
	private Exp createComplement(Exp oFun, Member member)
			throws UnknownExpressionException {
		if (QuaxUtil.isFunCallTo(oFun, "Children")) {
			Exp oParent = QuaxUtil.funCallArg(oFun, 0);

			// if member is NOT a child of Funcall arg, then the complement is
			// the original set
			Exp oMember = QuaxUtil.expForMember(member);
			if (!QuaxUtil.checkChild(member, oParent)) {
				return oFun;
			}

			List<Exp> oChildren = QuaxUtil.getChildMembers(oParent);
			if (oChildren.size() < 2) {
				return null;
			}

			Exp[] mComplement = new Exp[oChildren.size() - 1];
			int ii = 0;
			for (Exp child : oChildren) {
				if (!child.equals(oMember)) {
					mComplement[ii++] = child;
				}
			}

			if (mComplement.length == 1) {
				return mComplement[0]; // single member
			}

			Exp oComplement = QuaxUtil.createFunCall("{}", mComplement,
					Syntax.Braces);

			return oComplement;
		} else if (QuaxUtil.isFunCallTo(oFun, "{}")) {
			int nComp = 0;
			int nArg = QuaxUtil.funCallArgCount(oFun);

			Exp oMember = QuaxUtil.expForMember(member);
			for (int i = 0; i < nArg; i++) {
				Exp o = QuaxUtil.funCallArg(oFun, i);
				if (!(o.equals(oMember))) {
					++nComp;
				}
			}

			if (nComp == 0) {
				return null;
			}

			if (nComp == nArg) {
				// complement = same
				return oFun;
			}

			Exp[] mComplement = new Exp[nComp];
			int ii = 0;
			for (int i = 0; i < nArg; i++) {
				Exp o = QuaxUtil.funCallArg(oFun, i);
				if (!(o.equals(oMember)))
					mComplement[ii++] = o;
			}

			if (mComplement.length == 1) {
				return mComplement[0]; // single member
			}

			Exp oComplement = QuaxUtil.createFunCall("{}", mComplement,
					Syntax.Braces);

			return oComplement;
		} else if (QuaxUtil.isFunCallTo(oFun, "Union")) {
			// Union of FunCalls, recursive
			// Complement(Union(a,b)) = Union(Complement(a), Complement(b))
			Exp[] complements = new Exp[2];
			for (int i = 0; i < 2; i++) {
				Exp o = QuaxUtil.funCallArg(oFun, i);
				complements[i] = createComplement(o, member);
			}

			if (complements[0] == null && complements[1] == null) {
				return null;
			} else if (complements[0] != null && complements[1] == null) {
				return complements[0]; // No Union needed
			} else if (complements[0] == null && complements[1] != null) {
				return complements[1]; // No Union needed
			} else {
				// complement can be single member
				if (!QuaxUtil.isFunCall(complements[0])) {
					complements[0] = QuaxUtil.createFunCall("{}",
							new Exp[] { complements[0] }, Syntax.Braces);
				}

				if (!QuaxUtil.isFunCall(complements[1])) {
					complements[1] = QuaxUtil.createFunCall("{}",
							new Exp[] { complements[1] }, Syntax.Braces);
				}

				if (QuaxUtil.isFunCallTo(complements[0], "{}")
						&& QuaxUtil.isFunCallTo(complements[1], "{}")) {
					// create single set as union ow two sets
					return unionOfSets(complements[0], complements[1]);
				}

				Exp newUnion = QuaxUtil.createFunCall("Union", complements,
						Syntax.Function);

				return newUnion;
			}
		}

		// the fun call is not supported
		throw new UnknownExpressionException(QuaxUtil.funCallName(oFun));
	}

	/**
	 * Create new set as union of 2 sets
	 */
	private Exp unionOfSets(Exp set1, Exp set2) {
		// create single set as union ow two sets
		int n1 = QuaxUtil.funCallArgCount(set1);
		int n2 = QuaxUtil.funCallArgCount(set2);

		Exp[] newSet = new Exp[n1 + n2];
		int i = 0;
		for (int j = 0; j < n1; j++) {
			newSet[i++] = QuaxUtil.funCallArg(set1, j);
		}

		for (int j = 0; j < n2; j++) {
			newSet[i++] = QuaxUtil.funCallArg(set2, j);
		}
		return QuaxUtil.createFunCall("{}", newSet, Syntax.Braces);
	}

	/**
	 * @param iHier
	 *            index of Hierarchy
	 * @param list
	 *            Member List
	 */
	public void setHierMemberList(int iHier, List list) {
		ufMemberLists[iHier] = list;
	}

	/**
	 * 
	 * @param iHier
	 *            index of Hierarchy
	 * @return true, if the Hierarchy has an unknown function
	 */
	public boolean isUnknownFunction(int iHier) {
		return containsUF[iHier];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eyeq.pivot4j.StateHolder#bookmarkState()
	 */
	public Serializable bookmarkState() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eyeq.pivot4j.StateHolder#restoreState(java.io.Serializable)
	 */
	public void restoreState(Serializable bookmark) {
		// TODO Auto-generated method stub

	}

	/**
	 * String representation (debugging)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Number of hierarchies excluded from HIEARARCHIZE="
				+ nHierExclude);
		builder.append('\n');

		if (posTreeRoot == null) {
			builder.append("Root=null");
			return builder.toString();
		}

		posTreeRoot.walkChildren(new TreeNodeCallback<Exp>() {

			/**
			 * callback quax to String
			 */
			public int handleTreeNode(TreeNode<Exp> node) {
				int nodeIndex = node.getLevel() - 1;
				builder.append("\n");

				for (int i = 0; i < nodeIndex - 1; i++) {
					builder.append("   ");
				}

				if (nodeIndex > 0) {
					builder.append("+--");
				}

				Exp oExp = node.getReference();
				if (!QuaxUtil.isMember(oExp)) {
					// FunCall
					builder.append(QuaxUtil.funString(oExp));
				} else {
					// member
					builder.append(QuaxUtil.getMemberUniqueName(oExp));
				}

				return TreeNodeCallback.CONTINUE;
			}
		});

		return builder.toString();
	}
}
