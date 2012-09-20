/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olap4j.OlapException;
import org.olap4j.mdx.DimensionNode;
import org.olap4j.mdx.LevelNode;
import org.olap4j.mdx.MemberNode;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.type.LevelType;
import org.olap4j.type.MemberType;
import org.olap4j.type.Type;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.FunCall;
import com.eyeq.pivot4j.mdx.ParseTreeNodeExp;
import com.eyeq.pivot4j.mdx.SetExp;
import com.eyeq.pivot4j.mdx.Syntax;

public class QuaxUtil {

	private QuaxUtil() {
	}

	/**
	 * @param oExp
	 * @return true if oExp is a member expression
	 */
	public static boolean isMember(Exp oExp) {
		if (oExp instanceof ParseTreeNodeExp) {
			ParseTreeNodeExp adapter = (ParseTreeNodeExp) oExp;
			return adapter.getType() instanceof MemberType;
		}

		return false;
	}

	/**
	 * @param oExp
	 * @return true if oExp is a FunCall expression
	 */
	public static boolean isFunCall(Exp oExp) {
		return (oExp instanceof FunCall);
	}

	/**
	 * @param oExp
	 * @param member
	 * @return true if oExp is equal to member
	 */
	public static boolean equalMember(Exp oExp, Member member) {
		return member.equals(memberForExp(oExp));
	}

	/**
	 * @param oExp
	 * @param function
	 * @return true if oExp is a specific function call
	 */
	public static boolean isFunCallTo(Exp oExp, String function) {
		return isFunCall(oExp) && ((FunCall) oExp).isCallTo(function);
	}

	/**
	 * Check, whether member is parent of other member
	 * 
	 * @param pMember
	 *            (parent)
	 * @param cMember
	 *            (child)
	 * @return true if cMember (2.arg) is child of pMember (1.arg)
	 */
	public static boolean checkParent(Member pMember, Exp cMembObj) {
		Member child = memberForExp(cMembObj);
		return child != null && pMember.equals(child.getParentMember());
	}

	/**
	 * Check, whether member is child of other member
	 * 
	 * @param pMember
	 *            (child)
	 * @param cMember
	 *            (parent)
	 * @return true if cMember (1.arg) is child of pMember (2.arg)
	 */
	public static boolean checkChild(Member cMember, Exp pMembObj) {
		Member parent = memberForExp(pMembObj);
		return parent != null && parent.equals(cMember.getParentMember());
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
	public static boolean checkDescendantM(Member aMember, Member dMember) {
		return dMember.getAncestorMembers().contains(dMember);
	}

	/**
	 * Check, whether funcall set contains member
	 * 
	 * @param f
	 * @param m
	 * @return true if FunCall contains member
	 */
	public static boolean isMemberInFunCall(Exp oExp, Member member)
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
	public static boolean isChildOfMemberInFunCall(Exp oExp, Member member)
			throws UnknownExpressionException {
		// calculated members do not have children
		if (!isFunCall(oExp) || member.isCalculated()) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			return member.equals(memberForExp(f.getArgs()[0]));
		} else if (f.isCallTo("Descendants")) {
			// true, if f = descendants(m2, level) contains any child of m
			// so level must be parent-level of m
			Member ancestor = memberForExp(f.getArgs()[0]);

			Level level = levelForExp(f.getArgs()[1]);
			Level parentLevel = getParentLevel(level);

			if (parentLevel != null && member.getLevel().equals(parentLevel)) {
				int ancestorLevelNumber = ancestor.getLevel().getDepth();
				while (ancestorLevelNumber < member.getLevel().getDepth()) {
					member = member.getParentMember();
				}

				if (member.equals(ancestor)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs()[0]);
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

			if (parentLevel != null && member.getLevel().equals(parentLevel)) {
				return true;
			} else {
				return false;
			}
		} else if (f.isCallTo("Union")) {
			if (isChildOfMemberInFunCall(f.getArgs()[0], member))
				return true;
			else
				return isChildOfMemberInFunCall(f.getArgs()[1], member);
		} else if (f.isCallTo("{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				Member mm = memberForExp(f.getArgs()[i]);
				Member mmp = mm.getParentMember();
				if (mmp != null && mmp.equals(member)) {
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
	 * @param f
	 * @param m
	 * @return true if FunCall contains descendant of member
	 */
	public static boolean isDescendantOfMemberInFunCall(Exp oExp, Member member)
			throws UnknownExpressionException {
		// calculated members do not have children
		if (!isFunCall(oExp) || member.isCalculated()) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			// true, if m2.children contains descendants of m
			// <==> m is equal or ancestor of m2
			Member mExp = memberForExp(f.getArgs()[0]);
			return (member.equals(mExp) || isDescendant(member, mExp));
		} else if (f.isCallTo("Descendants")) {
			// true, if descendants(m2) contain descendants of m
			// <==> m is equal or ancestor of m2
			Member mExp = memberForExp(f.getArgs()[0]);
			return (member.equals(mExp) || isDescendant(member, mExp));
		} else if (f.isCallTo("Members")) {
			Level levExp = levelForExp(f.getArgs()[0]);
			return levExp.getDepth() > member.getLevel().getDepth();
		} else if (f.isCallTo("Union")) {
			if (isDescendantOfMemberInFunCall(f.getArgs()[0], member)) {
				return true;
			} else {
				return isDescendantOfMemberInFunCall(f.getArgs()[1], member);
			}
		} else if (f.isCallTo("{}")) {
			for (Exp arg : f.getArgs()) {
				Member mExp = memberForExp(arg);
				return (!member.equals(mExp) && isDescendant(member, mExp));
			}

			return false;
		}

		throw new UnknownExpressionException(f.getFunction());
	}

	/**
	 * @param ancestor
	 * @param descendant
	 */
	public static boolean isDescendant(Member ancestor, Exp descendant) {
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
	public static boolean isDescendant(Member ancestor, Member descendant) {
		// a calculated member, even if defined under "ancestor" is *not*
		// descendant,
		// WITM MEMBER a.b as '..'
		// a.children does *not* include b
		if (descendant.isCalculated()) {
			return false;
		}
		if (ancestor.equals(descendant)) {
			return false;
		}

		int ancestorLevelNumber = ancestor.getDepth();
		Member mm = descendant;
		while (mm != null && ancestorLevelNumber < mm.getDepth()) {
			mm = mm.getParentMember();
		}

		if (mm.equals(ancestor)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check whether a Funcall does NOT resolve to top level of hierarchy
	 * 
	 * @param oExp
	 *            - FunCall Exp
	 * @return true, if any member of the set defined by funcall is NOT top
	 *         level
	 */
	public static boolean isFunCallNotTopLevel(Exp oExp)
			throws UnknownExpressionException {
		if (!isFunCall(oExp)) {
			return false;
		}

		FunCall f = (FunCall) oExp;

		if (f.isCallTo("Children")) {
			return true; // children *not* top level
		} else if (f.isCallTo("Descendants")) {
			return true; // descendants*not* top level
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs()[0]);
			return (level.getDepth() > 0);
		} else if (f.isCallTo("Union")) {
			if (isFunCallNotTopLevel(f.getArgs()[0])) {
				return true;
			}
			return isFunCallNotTopLevel(f.getArgs()[1]);
		} else if (f.isCallTo("{}")) {
			for (int i = 0; i < f.getArgs().length; i++) {
				if (!isMemberOnToplevel(f.getArgs()[i])) {
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
	 * @param m
	 *            - member to be checked
	 * @return true - if member is on top level
	 */
	public static boolean isMemberOnToplevel(Exp oMem) {
		Member member = memberForExp(oMem);
		if (member.getLevel().getDepth() > 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check a Funcall expression whether we can handle it. currently we can
	 * basically handle following FunCalls member.children, member.descendants,
	 * level.members
	 */
	public static boolean canHandle(Exp oExp) {
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
				for (int i = 0; i < f.getArgs().length; i++) {
					if (!canHandle(f.getArgs()[i])) {
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
	public static Member getParentMember(Exp oExp) {
		return memberForExp(oExp).getParentMember();
	}

	/**
	 * @param oParent
	 * @return
	 */
	public static List<Exp> getChildMembers(Exp oParent) {
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
	public static Member memberForExp(Exp oExp) {
		if (oExp instanceof ParseTreeNodeExp) {
			ParseTreeNodeExp adapter = (ParseTreeNodeExp) oExp;

			Type type = adapter.getType();
			if (type instanceof MemberType) {
				return ((MemberType) type).getMember();
			}
		}

		return null;
	}

	/**
	 * @param oLevel
	 * @return
	 */
	public static Level levelForExp(Exp oExp) {
		if (oExp instanceof ParseTreeNodeExp) {
			ParseTreeNodeExp adapter = (ParseTreeNodeExp) oExp;

			Type type = adapter.getType();
			if (type instanceof LevelType) {
				return ((LevelType) type).getLevel();
			}
		}

		return null;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public static StringBuilder funString(Exp oExp) {
		FunCall f = (FunCall) oExp;

		StringBuilder sb = new StringBuilder();

		if (f.isCallTo("Children")) {
			Member m = memberForExp(f.getArgs()[0]);
			sb.append(m.getUniqueName());
			sb.append(".children");
		} else if (f.isCallTo("Descendants")) {
			Member m = memberForExp(f.getArgs()[0]);
			Level lev = levelForExp(f.getArgs()[1]);
			sb.append("Descendants(");
			sb.append(m.getUniqueName());
			sb.append(",");
			sb.append(lev.getUniqueName());
			sb.append(")");
		} else if (f.isCallTo("members")) {
			Level lev = levelForExp(f.getArgs()[0]);
			sb.append(lev.getUniqueName());
			sb.append(".Members");
		} else if (f.isCallTo("Union")) {
			sb.append("Union(");
			FunCall f1 = (FunCall) f.getArgs()[0];
			sb.append(funString(f1));
			sb.append(",");
			FunCall f2 = (FunCall) f.getArgs()[1];
			sb.append(funString(f2));
			sb.append(")");
		} else if (f.isCallTo("{}")) {
			sb.append("{");
			for (int i = 0; i < f.getArgs().length; i++) {
				if (i > 0)
					sb.append(",");
				Member m = memberForExp(f.getArgs()[i]);
				sb.append(m.getUniqueName());
			}
			sb.append("}");
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")) {
			// just generate Topcount(set)
			sb.append(f.getFunction());
			sb.append("(");
			FunCall f1 = (FunCall) f.getArgs()[0];
			sb.append(funString(f1));
			sb.append(")");
		}
		return sb;
	}

	/**
	 * @param oExp
	 * @return
	 */
	public static String getMemberUniqueName(Exp oExp) {
		Member member = memberForExp(oExp);
		return member.getUniqueName();
	}

	/**
	 * Expression Object for member
	 * 
	 * @param member
	 * @return Expression Object
	 */
	public static Exp expForMember(Member member) {
		return new ParseTreeNodeExp(new MemberNode(null, member));
	}

	/**
	 * Expression Object for Dimension
	 * 
	 * @param dimension
	 * @return Expression Object
	 */
	public static Exp expForDim(Dimension dimension) {
		return new ParseTreeNodeExp(new DimensionNode(null, dimension));
	}

	/**
	 * Expression Object for level
	 * 
	 * @param level
	 * @return Expression Object
	 */
	public static Exp expForLevel(Level level) {
		return new ParseTreeNodeExp(new LevelNode(null, level));
	}

	/**
	 * @param path
	 * @return
	 */
	public static String memberString(List<Member> path) {
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
	public static Exp createMemberSet(List<Member> members) {
		if (members == null || members.isEmpty()) {
			return null;
		} else if (members.size() == 1) {
			return expForMember(members.get(0));
		} else {
			Exp[] remExps = new Exp[members.size()];
			for (int i = 0; i < remExps.length; i++) {
				remExps[i] = expForMember(members.get(i));
			}
			return new FunCall("{}", remExps, Syntax.Braces);
		}
	}

	/**
	 * Level depth for member
	 * 
	 * @param oExp
	 *            - member
	 * @return depth
	 */
	public static int levelDepthForMember(Exp oExp) {
		Member member = memberForExp(oExp);
		Level level = member.getLevel();

		return level.getDepth();
	}

	/**
	 * @param oExp
	 * @return hierarchy for Exp
	 * @throws UnknownExpressionException
	 */
	public static Hierarchy hierForExp(Exp oExp)
			throws UnknownExpressionException {
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
			Member member = memberForExp(f.getArgs()[0]);
			return member.getHierarchy();
		} else if (f.isCallTo("Members")) {
			Level level = levelForExp(f.getArgs()[0]);
			return level.getHierarchy();
		} else if (f.isCallTo("Union")) {
			// continue with first set
			return hierForExp(f.getArgs()[0]);
		} else if (f.isCallTo("TopCount") || f.isCallTo("BottomCount")
				|| f.isCallTo("TopPercent") || f.isCallTo("BottomPercent")
				|| f.isCallTo("Filter")) {
			// continue with base set of top bottom function
			return hierForExp(f.getArgs()[0]);
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
	public static Exp topLevelMembers(Hierarchy hierarchy,
			boolean expandAllMember) {
		Level topLevel = hierarchy.getLevels().get(0);

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
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		// if there is an All Member, we will have to expand it
		// according to expandAllMember flag
		if (mAll != null) {
			Exp[] memar = new Exp[] { expForMember(mAll) };
			Exp mAllSet = new FunCall("{}", memar, Syntax.Braces);
			if (!expandAllMember) {
				return memar[0];
			}

			// must expand
			// create Union({AllMember}, AllMember.children)
			Exp mAllChildren = new FunCall("children", memar, Syntax.Property);
			Exp union = new FunCall("union",
					new Exp[] { mAllSet, mAllChildren }, Syntax.Function);

			return union;
		}

		// HHTASK ok, for a parent-child hierarchy ?
		List<Member> topMembers;
		try {
			topMembers = topLevel.getMembers();
		} catch (OlapException e) {
			throw new PivotException(e);
		}

		Exp[] topExp = new Exp[topMembers.size()];

		for (int i = 0; i < topExp.length; i++) {
			topExp[i] = expForMember(topMembers.get(i));
		}

		if (topExp.length == 1) {
			return topExp[0]; // single member
		}

		return new FunCall("{}", topExp, Syntax.Braces);
	}

	/**
	 * generation of FunCalls
	 * 
	 * @param function
	 *            name
	 * @param args
	 *            arguments
	 * @param funType
	 *            FUNTYPE
	 * @return function object
	 */
	public static Exp createFunCall(String function, Exp[] args, Syntax funType) {
		Exp[] expArgs = new Exp[args.length];
		for (int i = 0; i < expArgs.length; i++) {
			expArgs[i] = (Exp) args[i];
		}

		return new FunCall(function, expArgs, funType);
	}

	/**
	 * Get number of funCall arguments
	 * 
	 * @param oFun
	 *            funcall expression
	 * @return number of args
	 */
	public static int funCallArgCount(Exp oFun) {
		FunCall f = (FunCall) oFun;
		return f.getArgs().length;
	}

	/**
	 * Get funcall name
	 * 
	 * @param oFun
	 *            funcall expression
	 * @return function name
	 */
	public static String funCallName(Exp oFun) {
		return ((FunCall) oFun).getFunction();
	}

	/**
	 * Get funcall argument
	 * 
	 * @param oFun
	 *            funcall expression
	 * @param i
	 *            index of argument
	 * @return argument object
	 */
	public static Exp funCallArg(Exp oFun, int index) {
		return ((FunCall) oFun).getArgs()[index];
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public static void addMemberUncles(List<Exp> list, Member member,
			int[] maxLevel) {
		int parentLevel = member.getLevel().getDepth() - 1;

		if (parentLevel < maxLevel[0])
			return;
		if (parentLevel > maxLevel[0]) {
			maxLevel[0] = parentLevel;
			list.clear();
		}

		if (parentLevel > 0) {
			Member parent = member.getParentMember();
			Member grandPa = parent.getParentMember();

			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& memberForExp(f.getArgs()[0]).equals(grandPa)) {
						return;
					}
				}
			}

			FunCall uncles = new FunCall("Children",
					new Exp[] { expForMember(grandPa) }, Syntax.Property);
			list.add(uncles);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public static void addMemberSiblings(List<Exp> list, Member member,
			int[] maxLevel) {
		int level = member.getLevel().getDepth();
		if (level < maxLevel[0]) {
			return;
		}

		if (level > maxLevel[0]) {
			maxLevel[0] = level;
			list.clear();
		}

		if (level > 0) {
			Member parent = member.getParentMember();

			// do nothing if already on List
			for (Exp exp : list) {
				if (exp instanceof FunCall) {
					FunCall f = (FunCall) exp;
					if (f.isCallTo("Children")
							&& memberForExp(f.getArgs()[0]).equals(parent)) {
						return;
					}
				}
			}

			FunCall siblings = new FunCall("Children",
					new Exp[] { expForMember(parent) }, Syntax.Property);
			list.add(siblings);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param maxLevel
	 */
	public static void addMemberChildren(List<Exp> list, Member member,
			int[] maxLevel) {
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
							&& memberForExp(f.getArgs()[0]).equals(member)) {
						return;
					}
				}
			}

			FunCall children = new FunCall("Children",
					new Exp[] { expForMember(member) }, Syntax.Property);
			list.add(children);
		}
	}

	/**
	 * @param list
	 * @param member
	 * @param level
	 * @param maxLevel
	 */
	public static void addMemberDescendants(List<Exp> list, Member member,
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
							&& memberForExp(f.getArgs()[0]).equals(member)) {
						return;
					}
				}
			}
			FunCall children = new FunCall("Descendants", new Exp[] {
					expForMember(member), expForLevel(level) }, Syntax.Function);
			list.add(children);
		}
	}

	/**
	 * @param list
	 * @param level
	 * @param maxLevel
	 */
	public static void addLevelMembers(List<Exp> list, Level level,
			int[] maxLevel) {
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

			FunCall members = new FunCall("Members",
					new Exp[] { expForLevel(level) }, Syntax.Property);
			list.add(members);
		}
	}

	/**
	 * @param level
	 * @return
	 */
	public static Level getParentLevel(Level level) {
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
	public static boolean isMemberInChildren(FunCall f, Member member) {
		// calculated members are not really child
		if (member.isCalculated()) {
			return false;
		}

		Member parent = memberForExp(f.getArgs()[0]);
		return member.equals(parent);
	}

	/**
	 * @param f
	 *            Descendants FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set of Descendants function
	 */
	public static boolean isMemberInDescendants(FunCall f, Member member) {
		// calculated members are not really child
		if (member.isCalculated()) {
			return false;
		}

		Member ancestor = memberForExp(f.getArgs()[0]);

		Level level = levelForExp(f.getArgs()[1]);
		Level mLevel = member.getLevel();

		if (!mLevel.equals(level)) {
			return false;
		}

		if (member.equals(ancestor)) {
			return false;
		}

		int ancestorLevelNumber = ancestor.getLevel().getDepth();

		Member mm = member;
		while (ancestorLevelNumber < mm.getLevel().getDepth()) {
			mm = mm.getParentMember();
		}

		return mm.equals(ancestor);
	}

	/**
	 * @param f
	 *            Members FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set of Members function
	 */
	public static boolean isMemberInLevel(FunCall f, Member member) {
		Level level = levelForExp(f.getArgs()[0]);
		return level.equals(member.getLevel());
	}

	/**
	 * @param f
	 *            Set FunCall
	 * @param member
	 *            member to search for
	 * @return true if member mSearch is in set function
	 */
	public static boolean isMemberInSet(FunCall f, Member member) {
		// set of members expected
		for (Exp arg : f.getArgs()) {
			Member m = memberForExp(arg);
			if (m.equals(member)) {
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
	public static boolean isMemberInUnion(FunCall f, Member member)
			throws UnknownExpressionException {
		// Unions may be nested
		for (int i = 0; i < 2; i++) {
			FunCall fChild = (FunCall) f.getArgs()[i];
			if (isMemberInFunCall(fChild, member)) {
				return true;
			}
		}
		return false;
	}
}
