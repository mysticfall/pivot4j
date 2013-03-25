/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

public class StringUtil {

	private StringUtil() {
	}

	/**
	 * Put brackets around string, if not yet there
	 * 
	 * @param orig
	 * @return String
	 */
	public static String bracketsAround(String orig) {
		if (orig.startsWith("[") && orig.endsWith("]")) {
			return orig;
		} else {
			return "[" + orig + "]";
		}
	}

	/**
	 * split a unique name
	 * 
	 * @param uniqueName
	 * @return the name parts without brackets
	 */
	public static String[] splitUniqueName(String uniqueName) {
		// uniqueName = [Product].[All Products].[Drink]
		String str = uniqueName.trim();
		int l2 = str.length() - 1;
		if (str.charAt(0) != '[' || str.charAt(l2) != ']')
			return new String[] { uniqueName }; // should not occur
		// remove first opening bracket and last closing bracket
		str = str.substring(1, l2);
		// str = Product].[All Products].[Drink
		String[] nameParts = str.split("\\]\\.\\[");
		return nameParts;
	}

	/**
	 * create unique name from String array
	 * 
	 * @param strs
	 *            - name parts
	 * @param n
	 *            - number of name parts, all if n &lt;= 0
	 * @return unique name
	 */
	public static String createUName(String[] strs, int n) {
		if (n <= 0)
			n = strs.length;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; i++) {
			if (i > 0)
				sb.append('.');
			sb.append('[');
			sb.append(strs[i]);
			sb.append(']');
		}
		return sb.toString();
	}

	/**
	 * extract dimension (first part) from unique name
	 * 
	 * @param uName
	 *            - unique name
	 * @return first name part, with brackets
	 */
	public static String dimFromUName(String uName) {
		String[] strs = splitUniqueName(uName);
		return "[" + strs[0] + "]";
	}

	/**
	 * extract parent (all exept last part) from unique name
	 * 
	 * @param uName
	 *            - unique name
	 * @return all name parts except last, with brackets
	 */
	public static String parentFromUName(String uName) {
		String[] strs = splitUniqueName(uName);
		int n = strs.length;
		if (n < 3) {
			return null; // at least 3 parts required, if a parent exists
		}
		return createUName(strs, n - 1);
	}
}
