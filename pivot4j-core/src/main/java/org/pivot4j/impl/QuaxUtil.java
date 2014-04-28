/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotException;
import org.pivot4j.mdx.Exp;
import org.pivot4j.mdx.FunCall;
import org.pivot4j.mdx.Syntax;
import org.pivot4j.mdx.metadata.DimensionExp;
import org.pivot4j.mdx.metadata.LevelExp;
import org.pivot4j.mdx.metadata.MemberExp;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.OlapUtils;

public class QuaxUtil {

	private Cube cube;

	private MemberHierarchyCache cache;

	private OlapUtils olapUtils;

	/**
	 * @param cube
	 */
	public QuaxUtil(Cube cube) {
		this(cube, null);
	}

	/**
	 * @param cube
	 * @param cache
	 */
	public QuaxUtil(Cube cube, MemberHierarchyCache cache) {
		if (cube == null) {
			throw new NullArgumentException("cube");
		}

		this.cube = cube;

		if (cache == null) {
			this.cache = new MemberHierarchyCache(cube);
		} else {
			this.cache = cache;
		}

		this.olapUtils = new OlapUtils(cube);
		olapUtils.setMemberHierarchyCache(cache);
	}

	/**
	 * @return cube
	 */
	protected Cube getCube() {
		return cube;
	}

	/**
	 * @return the olapUtils
	 */
	protected OlapUtils getOlapUtils() {
		return olapUtils;
	}

	/**
	 * @param oExp
	 * @return true if oExp is a member expression
	 */
	public boolean isMember(Exp oExp) {
		return oExp instanceof MemberExp;
	}

	/**
	 * @param oExp
	 * @return true if oExp is a FunCall expression
	 */
	public boolean isFunCall(Exp oExp) {
		return (oExp instanceof FunCall);
	}

	/**
	 * @param oExp
	 * @param member
	 * @return true if oExp is equal to member
	 */
	public boolean equalMember(Exp oExp, Member member) {
		return OlapUtils.equals(member, memberForExp(oExp));
	}

	/**
	 * @param oExp
	 * @param function
	 * @return true if oExp is a specific function call
	 */
	public boolean isFunCallTo(Exp oExp, String function) {
		return isFunCall(oExp) && ((FunCall) oExp).isCallTo(function);
	}

	/**
	 * Check, whether member is parent of other member
	 * 
	 * @param pMember
	 *            (parent)
	 * @param cMembObj
	 *            (child)
	 * @return true if cMember (2.arg) is child of pMember (1.arg)
	 */
	public boolean checkParent(Member pMember, Exp cMembObj) {
		Member child = memberForExp(cMembObj);

		return child != null
				&& OlapUtils.equals(pMember, cache.getParentMember(child));
	}

	/**
	 * Check, whether member is child of other member
	 * 
	 * @param cMember
	 *            (child)
	 * @param pMembObj
	 *            (parent)
	 * @return true if cMember (1.arg) is child of pMember (2.arg)
	 */
	public boolean checkChild(Member cMember, Exp pMembObj) {
		Member parent = memberForExp(pMembObj);
		return parent != null
				&& OlapUtils.equals(parent, cache.getParentMember(cMember));
	}

	/**
	 * Check, whether member is descendant of other member
	 * 
	 * @param aMember
	 *            (ancestor)
	 * @param dMember
	 *            (descendant)
	 * @return true if dMember (2.arg) is descendant of aMember (1.arg)
	 */
	public boolean checkDescendantM(Member aMember, Member dMember) {
		return dMember.getAncestorMembers().contains(dMember);
	}

	/**
	 * Check, whether funcall set contains member
	 * 
	 * @param oExp
	 * @param member
	 * @return true if FunCall contains member
	 */
	public boolean isMemberInFunCall(Exp oExp, Member member)
			throws UnknownExpressionException {
		if (!isFunCall(oExp)) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			return isMemberInChildren(f, member);
		} else if (f.isCallTo("Descendants")) {
			return isMemberInDescendants(f, member);
		} else if (f.isCallTo("Members")) {
			return isMemberInLevel(f, member);
		} else if (f.isCallTo("Union")) {
			return isMemberInUnion(f, member);
		} else if (f.isCallTo("{}")) {
			return isMemberInSet(f, member);
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * Check, whether a funcall set contains any child of a specific member
	 * 
	 * @param oExp
	 * @param member
	 * @return true, if FunCall contains member's child
	 * @throws UnknownExpressionException
	 */
	public boolean isChildOfMemberInFunCall(Exp oExp, Member member)
			throws UnknownExpressionException {
		// calculated members do not have children
		if (!isFunCall(oExp) || member.isCalculated()) {
			return false;
		}

		Member mem = olapUtils.wrapRaggedIfNecessary(member);

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			return OlapUtils.equals(mem, memberForExp(f.getArgs().get(0)));
		} else if (f.isCallTo("Descendants")) {
			// true, if f = descendants(m2, level) contains any child of m
			// so level must be parent-level of m
			Member ancestor = memberForExp(f.getArgs().get(0));

			Level level = levelForExp(f.getArgs().get(1));
			Level parentLevel = getParentLevel(level);

			if (parentLevel != null
					&& OlapUtils.equals(mem.getLevel(), parentLevel)) {
				int ancestorLevelNumber = ancestor.getLevel().getDepth();
				while (ancestorLevelNumber < mem.getLevel().getDepth()) {
					mem = cache.getParentMember(mem);
				}

				return OlapUtils.equals(mem, ancestor);
			} else {
				return false;
			}
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs().get(0));
			Level parentLevel = null;

			if (level.getDepth() > 0) {
				List<Level> levels = level.getHierarchy().getLevels();
				for (Level l : levels) {
					if (l.getDepth() == level.getDepth() - 1) {
						parentLevel = l;
						break;
					}
				}
			}

			return (parentLevel != null && OlapUtils.equals(mem.getLevel(),
					parentLevel));
		} else if (f.isCallTo("Union")) {
			return isChildOfMemberInFunCall(f.getArgs().get(0), mem)
					|| isChildOfMemberInFunCall(f.getArgs().get(1), mem);
		} else if (f.isCallTo("{}")) {
			for (Exp exp : f.getArgs()) {
				Member mm = memberForExp(exp);

				Member mmp = olapUtils.getTopLevelRaggedMember(mm);
				if (mmp != null
						&& OlapUtils.equals(
								olapUtils.wrapRaggedIfNecessary(mmp), mem)) {
					return true;
				}
			}
			return false;
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * Check, whether funcall set contains descendant of a specific member
	 * 
	 * @param oExp
	 * @param member
	 * @return true if FunCall contains descendant of member
	 */
	public boolean isDescendantOfMemberInFunCall(Exp oExp, Member member)
			throws UnknownExpressionException {
		// calculated members do not have children
		if (!isFunCall(oExp) || member.isCalculated()) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			// true, if m2.children contains descendants of m
			// <==> m is equal or ancestor of m2
			Member mExp = memberForExp(f.getArgs().get(0));
			return OlapUtils.equals(member, mExp) || isDescendant(member, mExp);
		} else if (f.isCallTo("Descendants")) {
			// true, if descendants(m2) contain descendants of m
			// <==> m is equal or ancestor of m2
			Member mExp = memberForExp(f.getArgs().get(0));
			return OlapUtils.equals(member, mExp) || isDescendant(member, mExp);
		} else if (f.isCallTo("Members")) {
			Level levExp = levelForExp(f.getArgs().get(0));
			return levExp.getDepth() > member.getLevel().getDepth();
		} else if (f.isCallTo("Union")) {
			if (isDescendantOfMemberInFunCall(f.getArgs().get(0), member)) {
				return true;
			} else {
				return isDescendantOfMemberInFunCall(f.getArgs().get(1), member);
			}
		} else if (f.isCallTo("{}")) {
			for (Exp arg : f.getArgs()) {
				Member mExp = memberForExp(arg);
				if (!OlapUtils.equals(member, mExp)
						&& isDescendant(member, mExp)) {
					return true;
				}
			}

			return false;
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * @param ancestor
	 * @param descendant
	 */
	public boolean isDescendant(Member ancestor, Exp descendant) {
		Member member = memberForExp(descendant);
		if (member == null) {
			return false;
		}

		return isDescendant(ancestor, member);
	}

	/**
	 * @param ancestor
	 * @param descendant
	 */
	public boolean isDescendant(Member ancestor, Member descendant) {
		// a calculated member, even if defined under "ancestor" is *not*
		// descendant,
		// WITM MEMBER a.b as '..'
		// a.children does *not* include b
		if (descendant.isCalculated()) {
			return false;
		}
		if (OlapUtils.equals(ancestor, descendant)) {
			return false;
		}

		int ancestorLevelNumber = ancestor.getDepth();
		Member mm = descendant;
		while (mm != null && ancestorLevelNumber < mm.getDepth()) {
			mm = cache.getParentMember(mm);
		}

		return OlapUtils.equals(mm, ancestor);
	}

	/**
	 * Check whether a Funcall does NOT resolve to top level of hierarchy
	 * 
	 * @param oExp
	 *            - FunCall Exp
	 * @return true, if any member of the set defined by funcall is NOT top
	 *         level
	 */
	public boolean isFunCallNotTopLevel(Exp oExp)
			throws UnknownExpressionException {
		if (!isFunCall(oExp)) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			// children *not* top level
			return true;
		} else if (f.isCallTo("Descendants")) {
			// descendants*not* top level
			return true;
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs().get(0));
			return (level.getDepth() > 0);
		} else if (f.isCallTo("Union")) {
			if (isFunCallNotTopLevel(f.getArgs().get(0))) {
				return true;
			}
			return isFunCallNotTopLevel(f.getArgs().get(1));
		} else if (f.isCallTo("{}")) {
			for (Exp exp : f.getArgs()) {
				if (!isMemberOnToplevel(exp)) {
					return true;
				}
			}
			return false;
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * Check, whether a member is on top level (has no parent);
	 * 
	 * @param oMem
	 *            - member to be checked
	 * @return true - if member is on top level
	 */
	public boolean isMemberOnToplevel(Exp oMem) {
		Member member = memberForExp(oMem);
		return (member.getLevel().getDepth() <= 0);
	}

	/**
	 * Check a Funcall expression whether we can handle it. currently we can
	 * basically handle following FunCalls member.children, member.descendants,
	 * level.members
	 */
	public boolean canHandle(Exp oExp) {
		if (isMember(oExp)) {
			return true;
		} else if (isFunCall(oExp)) {
			FunCall f = (FunCall) oExp;

			if (f.isCallTo("children")) {
				return true;
			}
			if (f.isCallTo("descendants")) {
				return true;
			}
			if (f.isCallTo("members")) {
				return true;
			}
			if (f.isCallTo("{}")) {
				return true;
			}
			if (f.isCallTo("union")) {
				for (Exp exp : f.getArgs()) {
					if (!canHandle(exp)) {
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public Member getParentMember(Exp oExp) {
		return cache.getParentMember(memberForExp(oExp));
	}

	/**
	 * @param oParent
	 * @return
	 */
	public List<Exp> getChildMembers(Exp oParent) {
		Member parent = memberForExp(oParent);

		if (parent == null) {
			return Collections.emptyList();
		}

		List<? extends Member> members;

		try {
			members = parent.getChildMembers();
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		List<Exp> children = new ArrayList<Exp>(members.size());
		for (Member member : members) {
			children.add(expForMember(member));
		}

		return children;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public Member memberForExp(Exp oExp) {
		if (oExp instanceof MemberExp) {
			return ((MemberExp) oExp).getMetadata(cube);
		} else if (oExp instanceof FunCall && isFunCallTo(oExp, "{}")) {
			FunCall func = (FunCall) oExp;
			if (!func.getArgs().isEmpty()) {
				return memberForExp(func.getArgs().get(0));
			}
		}

		return null;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public Level levelForExp(Exp oExp) {
		if (oExp instanceof DimensionExp) {
			return ((LevelExp) oExp).getMetadata(cube);
		}

		return null;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public StringBuilder funString(Exp oExp) {
		FunCall f = (FunCall) oExp;

		StringBuilder sb = new StringBuilder();

		if (f.isCallTo("Children")) {
			Member m = memberForExp(f.getArgs().get(0));
			sb.append(m.getUniqueName());
			sb.append(".Children");
		} else if (f.isCallTo("Descendants")) {
			Member m = memberForExp(f.getArgs().get(0));
			Level lev = levelForExp(f.getArgs().get(1));
			sb.append("Descendants(");
			sb.append(m.getUniqueName());
			sb.append(",");
			sb.append(lev.getUniqueName());
			sb.append(")");
		} else if (f.isCallTo("members")) {
			Level lev = levelForExp(f.getArgs().get(0));
			sb.append(lev.getUniqueName());
			sb.append(".Members");
		} else if (f.isCallTo("Union")) {
			sb.append("Union(");
			FunCall f1 = (FunCall) f.getArgs().get(0);
			sb.append(funString(f1));
			sb.append(",");
			FunCall f2 = (FunCall) f.getArgs().get(1);
			sb.append(funString(f2));
			sb.append(")");
		} else if (f.isCallTo("{}")) {
			sb.append("{");

			boolean isFollow = false;
			for (Exp exp : f.getArgs()) {
				if (isFollow) {
					sb.append(",");
				} else {
					isFollow = true;
				}

				Member m = memberForExp(exp);
				sb.append(m.getUniqueName());
			}
			sb.append("}");
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")) {
			// just generate Topcount(set)
			sb.append(f.getFunction());
			sb.append("(");
			FunCall f1 = (FunCall) f.getArgs().get(0);
			sb.append(funString(f1));
			sb.append(")");
		}
		return sb;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public String getMemberUniqueName(Exp oExp) {
		Member member = memberForExp(oExp);
		return member.getUniqueName();
	}

	/**
	 * Expression Object for member
	 * 
	 * @param member
	 * @return Expression Object
	 */
	public Exp expForMember(Member member) {
		return new MemberExp(olapUtils.wrapRaggedIfNecessary(member));
	}

	/**
	 * Expression Object for Dimension
	 * 
	 * @param dimension
	 * @return Expression Object
	 */
	public Exp expForDim(Dimension dimension) {
		return new DimensionExp(dimension);
	}

	/**
	 * Expression Object for level
	 * 
	 * @param level
	 * @return Expression Object
	 */
	public Exp expForLevel(Level level) {
		return new LevelExp(level);
	}

	/**
	 * @param path
	 * @return
	 */
	public String memberString(List<Member> path) {
		if (path == null || path.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (Member member : path) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(member.getUniqueName());
			i++;
		}
		return sb.toString();
	}

	/**
	 * generate an object for a list of members
	 * 
	 * @param members
	 *            list of members
	 * @return null for empty list, single member or set function otherwise
	 */
	public Exp createMemberSet(List<Member> members) {
		if (members == null || members.isEmpty()) {
			return null;
		} else if (members.size() == 1) {
			return expForMember(members.get(0));
		} else {
			List<Exp> remExps = new ArrayList<Exp>(members.size());

			for (Member member : members) {
				remExps.add(expForMember(member));
			}

			return new FunCall("{}", Syntax.Braces, remExps);
		}
	}

	/**
	 * Level depth for member
	 * 
	 * @param oExp
	 *            - member
	 * @return depth
	 */
	public int levelDepthForMember(Exp oExp) {
		Member member = memberForExp(oExp);
		Level level = member.getLevel();

		return level.getDepth();
	}

	/**
	 * @param oExp
	 * @return hierarchy for Exp
	 * @throws UnknownExpressionException
	 */
	public Hierarchy hierForExp(Exp oExp) throws UnknownExpressionException {
		if (isMember(oExp)) {
			return memberForExp(oExp).getHierarchy();
		} else if (oExp instanceof SetExp) {
			// set expression generated by CalcSet extension
			SetExp set = (SetExp) oExp;
			return set.getHierarchy();
		}

		// must be FunCall
		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children") || f.isCallTo("Descendants")
				|| f.isCallTo("{}")) {
			Member member = memberForExp(f.getArgs().get(0));
			return member.getHierarchy();
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs().get(0));
			return level.getHierarchy();
		} else if (f.isCallTo("Union")) {
			// continue with first set
			return hierForExp(f.getArgs().get(0));
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")
				|| f.isCallTo("Filter")) {
			// continue with base set of top bottom function
			return hierForExp(f.getArgs().get(0));
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * @param hierarchy
	 *            the Hierarchy
	 * @param expandAllMember
	 *            if true, an "All" member will be expanded
	 * @return a set for the top level members of an hierarchy
	 */
	public Exp topLevelMembers(Hierarchy hierarchy, boolean expandAllMember) {
		List<Level> levels = hierarchy.getLevels();

		if (levels.isEmpty()) {
			return null;
		}

		Level topLevel = hierarchy.getLevels().get(0);

		if (!topLevel.isVisible()) {
			return null;
		}

		Member mAll;
		try {
			mAll = hierarchy.getDefaultMember();

			if (!mAll.isAll()) {
				mAll = null;
				for (Member m : hierarchy.getRootMembers()) {
					if (m.isAll()) {
						mAll = m;
						break;
					}
				}
			}

			if (mAll != null && !OlapUtils.isVisible(mAll)) {
				mAll = null;
			}
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		// if there is an All Member, we will have to expand it
		// according to expandAllMember flag
		if (mAll != null) {
			List<Exp> memar = new ArrayList<Exp>();
			memar.add(expForMember(mAll));

			Exp mAllSet = new FunCall("{}", Syntax.Braces, memar);
			if (!expandAllMember) {
				return memar.get(0);
			}

			// must expand
			// create Union({AllMember}, AllMember.children)
			Exp mAllChildren = new FunCall("children", Syntax.Property, memar);

			FunCall union = new FunCall("Union", Syntax.Function);
			union.getArgs().add(mAllSet);
			union.getArgs().add(mAllChildren);

			return union;
		}

		// HHTASK ok, for a parent-child hierarchy ?
		List<Member> topMembers;
		try {
			topMembers = topLevel.getMembers();
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		List<Exp> topExp = new ArrayList<Exp>(topMembers.size());

		for (Member member : topMembers) {
			if (OlapUtils.isVisible(member)) {
				topExp.add(expForMember(member));
			}
		}

		if (topExp.size() == 1) {
			// single member
			return topExp.get(0);
		}

		return new FunCall("{}", Syntax.Braces, topExp);
	}

	/**
	 * Get number of funCall arguments
	 * 
	 * @param oFun
	 *            funcall expression
	 * @return number of args
	 */
	public int funCallArgCount(Exp oFun) {
		FunCall f = (FunCall) oFun;
		return f.getArgs().size();
	}

	/**
	 * Get funcall name
	 * 
	 * @param oFun
	 *            funcall expression
	 * @return function name
	 */
	public String funCallName(Exp oFun) {
		return ((FunCall) oFun).getFunction();
	}

	/**
	 * Get funcall argument
	 * 
	 * @param oFun
	 *            funcall expression
	 * @param index
	 *            index of argument
	 * @return argument object
	 */
	public Exp funCallArg(Exp oFun, int index) {
		return ((FunCall) oFun).getArgs().get(index);
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public void addMemberUncles(List<Exp> list, Member member, int[] maxLevel) {
		int parentLevel = member.getLevel().getDepth() - 1;

		if (parentLevel < maxLevel[0]) {
			return;
		}

		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}

		if (parentLevel > 0) {
			Member parent = cache.getParentMember(member);
			Member grandPa = cache.getParentMember(parent);

			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& OlapUtils.equals(
									memberForExp(f.getArgs().get(0)), grandPa)) {
						return;
					}
				}
			}

			FunCall uncles = new FunCall("Children", Syntax.Property);
			uncles.getArgs().add(expForMember(grandPa));

			list.add(uncles);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public void addMemberSiblings(List<Exp> list, Member member, int[] maxLevel) {
		int level = member.getLevel().getDepth();
		if (level < maxLevel[0]) {
			return;
		}

		if (level > maxLevel[0]) {
			maxLevel[0] = level;
			list.clear();
		}

		if (level > 0) {
			Member parent = cache.getParentMember(member);

			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& OlapUtils.equals(
									memberForExp(f.getArgs().get(0)), parent)) {
						return;
					}
				}
			}

			FunCall siblings = new FunCall("Children", Syntax.Property);
			siblings.getArgs().add(expForMember(parent));

			list.add(siblings);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public void addMemberChildren(List<Exp> list, Member member, int[] maxLevel) {
		int childLevel = member.getLevel().getDepth() + 1;
		if (childLevel < maxLevel[0]) {
			return;
		}

		if (childLevel > maxLevel[0]) {
			maxLevel[0] = childLevel;
			list.clear();
		}

		if (childLevel > 0) {
			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& OlapUtils.equals(
									memberForExp(f.getArgs().get(0)), member)) {
						return;
					}
				}
			}

			FunCall children = new FunCall("Children", Syntax.Property);
			children.getArgs().add(expForMember(member));

			list.add(children);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param level
	 * @param maxLevel
	 */
	public void addMemberDescendants(List<Exp> list, Member member,
			Level level, int[] maxLevel) {
		int parentLevel = member.getLevel().getDepth() - 1;
		if (parentLevel < maxLevel[0]) {
			return;
		}

		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}

		if (parentLevel > 0) {
			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Descendants")
							&& OlapUtils.equals(
									memberForExp(f.getArgs().get(0)), member)) {
						return;
					}
				}
			}

			FunCall children = new FunCall("Descendants", Syntax.Function);

			children.getArgs().add(expForMember(member));
			children.getArgs().add(expForLevel(level));

			list.add(children);
		}
	}

	/**
	 * @param list
	 * @param level
	 * @param maxLevel
	 */
	public void addLevelMembers(List<Exp> list, Level level, int[] maxLevel) {
		int depth = level.getDepth();
		if (depth < maxLevel[0]) {
			return;
		}

		if (depth > maxLevel[0]) {
			maxLevel[0] = depth;
			list.clear();
		}

		if (depth > 0) {
			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Members")) {
						return;
					}
				}
			}

			FunCall members = new FunCall("Members", Syntax.Property);
			members.getArgs().add(expForLevel(level));

			list.add(members);
		}
	}

	/**
	 * @param level
	 * @return
	 */
	public Level getParentLevel(Level level) {
		Level parent = null;

		if (level.getDepth() > 0) {
			List<Level> levels = level.getHierarchy().getLevels();
			for (Level l : levels) {
				if (l.getDepth() == level.getDepth() - 1) {
					parent = l;
					break;
				}
			}
		}

		return parent;
	}

	/**
	 * @param f
	 *            Children FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set of children function
	 */
	public boolean isMemberInChildren(FunCall f, Member member) {
		// calculated members are not really child
		if (member.isCalculated()) {
			return false;
		}

		Member parent = memberForExp(f.getArgs().get(0));
		return OlapUtils.equals(parent,
				olapUtils.getTopLevelRaggedMember(member));
	}

	/**
	 * @param f
	 *            Descendants FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set of Descendants function
	 */
	public boolean isMemberInDescendants(FunCall f, Member member) {
		// calculated members are not really child
		if (member.isCalculated()) {
			return false;
		}

		Member ancestor = memberForExp(f.getArgs().get(0));

		Level level = levelForExp(f.getArgs().get(1));
		Level mLevel = member.getLevel();

		if (!OlapUtils.equals(mLevel, level)) {
			return false;
		}

		if (OlapUtils.equals(member, ancestor)) {
			return false;
		}

		int ancestorLevelNumber = ancestor.getLevel().getDepth();

		Member mm = member;
		while (ancestorLevelNumber < mm.getLevel().getDepth()) {
			mm = olapUtils.getTopLevelRaggedMember(mm);
		}

		return OlapUtils.equals(mm, ancestor);
	}

	/**
	 * @param f
	 *            Members FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set of Members function
	 */
	public boolean isMemberInLevel(FunCall f, Member member) {
		Level level = levelForExp(f.getArgs().get(0));
		return OlapUtils.equals(level, member.getLevel());
	}

	/**
	 * @param f
	 *            Set FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set function
	 */
	public boolean isMemberInSet(FunCall f, Member member) {
		// set of members expected
		for (Exp arg : f.getArgs()) {
			Member m = memberForExp(arg);
			if (OlapUtils.equals(m, member)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param f
	 *            Union FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set function
	 * @throws UnknownExpressionException
	 */
	public boolean isMemberInUnion(FunCall f, Member member)
			throws UnknownExpressionException {
		// Unions may be nested
		for (int i = 0; i < 2; i++) {
			FunCall fChild = (FunCall) f.getArgs().get(i);
			if (isMemberInFunCall(fChild, member)) {
				return true;
			}
		}
		return false;
	}
}
