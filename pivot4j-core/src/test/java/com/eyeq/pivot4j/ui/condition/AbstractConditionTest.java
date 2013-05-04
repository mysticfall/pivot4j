/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;
import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.el.freemarker.FreeMarkerExpressionEvaluatorFactory;
import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.html.HtmlRenderer;

public abstract class AbstractConditionTest {

	protected static ConditionFactory conditionFactory = new DefaultConditionFactory();

	protected RenderContext createDummyRenderContext() {
		PivotModel model = new PivotModelImpl(new SimpleOlapDataSource());
		PivotRenderer renderer = new HtmlRenderer(new StringWriter());

		ExpressionEvaluator evaluator = new FreeMarkerExpressionEvaluatorFactory()
				.createEvaluator();

		RenderContext context = new RenderContext(model, renderer, 0, 0, 0, 0,
				evaluator, null);

		return context;
	}

	static class TestCondition extends AbstractCondition {

		private boolean result;

		static Condition TRUE = new TestCondition(true);

		static Condition FALSE = new TestCondition(false);

		/**
		 * @param result
		 */
		private TestCondition(boolean result) {
			super(conditionFactory);

			this.result = result;
		}

		@Override
		public String getName() {
			return "TEST";
		}

		@Override
		public boolean matches(RenderContext context) {
			return result;
		}

		@Override
		public Serializable saveState() {
			return null;
		}

		@Override
		public void restoreState(Serializable state) {
		}

		@Override
		public void restoreSettings(HierarchicalConfiguration configuration) {
		}
	}
}
