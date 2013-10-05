/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.aggregator;

import java.util.List;

import org.olap4j.Axis;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.ui.RenderContext;

public interface Aggregator {

	String getName();

	Axis getAxis();

	List<Member> getMembers();

	Level getLevel();

	Measure getMeasure();

	/**
	 * @param context
	 */
	void aggregate(RenderContext context);

	/**
	 * @param context
	 * @return
	 */
	String getLabel(RenderContext context);

	/**
	 * @param context
	 * @return
	 */
	Double getValue(RenderContext context);

	/**
	 * @param context
	 * @return
	 */
	String getFormattedValue(RenderContext context);

	void reset();
}
