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
import java.util.List;

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

	protected static ConditionFactory conditionFactory = new DefaultConditionFactory() {

		@Override
		public List<String> getAvailableConditions() {
			List<String> conditions = super.getAvailableConditions();
			conditions.add("TRUE");
			conditions.add("FALSE");

			return conditions;
		}

		@Override
		public Condition createCondition(String name) {
			if (name.equals("TRUE")) {
				return TestCondition.TRUE;
			} else if (name.equals("FALSE")) {
				return TestCondition.FALSE;
			}

			return super.createCondition(name);
		}
	};

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

		private TestCondition(boolean result) {
			super(conditionFactory);

			this.result = result;
		}

		@Override
		public String getName() {
			return result ? "TRUE" : "FALSE";
		}

		@Override
		public boolean matches(RenderContext context) {
			return result;
		}

		@Override
		public Serializable saveState() {
			return result;
		}

		@Override
		public void restoreState(Serializable state) {
			this.result = (Boolean) state;
		}

		@Override
		public void saveSettings(HierarchicalConfiguration configuration) {
			super.saveSettings(configuration);

			configuration.setProperty("[@result]", result);
		}

		@Override
		public void restoreSettings(HierarchicalConfiguration configuration) {
			this.result = configuration.getBoolean("[@result]");
		}
	}
}
