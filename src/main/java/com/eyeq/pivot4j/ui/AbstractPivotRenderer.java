/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.olap4j.OlapException;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.impl.RenderStrategyImpl;

public abstract class AbstractPivotRenderer implements PivotRenderer,
		PivotLayoutCallback {

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

	private PropertyCollector propertyCollector;

	private RenderStrategy renderStrategy;

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#initialize()
	 */
	public void initialize() {
		this.renderStrategy = createRenderStrategy();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		if (renderStrategy == null) {
			throw new IllegalStateException("Renderer was not initialized yet.");
		}

		renderStrategy.render(model, this, this);
	}

	/**
	 * @return the hideSpans
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getHideSpans()
	 */
	public boolean getHideSpans() {
		return hideSpans;
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setHideSpans(boolean)
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * @return the showParentMembers
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowParentMembers()
	 */
	public boolean getShowParentMembers() {
		return showParentMembers;
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowParentMembers(boolean)
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	/**
	 * @return the showDimensionTitle
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowDimensionTitle()
	 */
	public boolean getShowDimensionTitle() {
		return showDimensionTitle;
	}

	/**
	 * @param showDimensionTitle
	 *            the showDimensionTitle to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowDimensionTitle(boolean)
	 */
	public void setShowDimensionTitle(boolean showDimensionTitle) {
		this.showDimensionTitle = showDimensionTitle;
	}

	/**
	 * @return the propertyCollector
	 */
	public PropertyCollector getPropertyCollector() {
		return propertyCollector;
	}

	/**
	 * @param propertyCollector
	 *            the propertyCollector to set
	 */
	public void setPropertyCollector(PropertyCollector propertyCollector) {
		this.propertyCollector = propertyCollector;
	}

	/**
	 * @return renderStrategy
	 */
	protected RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}

	/**
	 * @return
	 */
	protected RenderStrategy createRenderStrategy() {
		return new RenderStrategyImpl();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public void cellContent(RenderContext context) {
		cellContent(context, getCellLabel(context));
	}

	public abstract void cellContent(RenderContext context, String label);

	/**
	 * @param context
	 * @return
	 */
	protected String getCellLabel(RenderContext context) {
		String label;

		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
			if (context.getProperty() == null) {
				label = context.getMember().getCaption();
			} else {
				try {
					label = context.getMember().getPropertyFormattedValue(
							context.getProperty());
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}
			break;
		case ColumnTitle:
		case RowTitle:
			if (context.getProperty() != null) {
				label = context.getProperty().getCaption();
			} else if (context.getLevel() != null) {
				label = context.getLevel().getCaption();
			} else if (context.getHierarchy() != null) {
				label = context.getHierarchy().getCaption();
			} else {
				label = null;
			}
			break;
		case Value:
			label = context.getCell().getFormattedValue();
			break;
		case None:
		default:
			label = null;
			break;
		}

		return label;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { showDimensionTitle, showParentMembers,
				hideSpans };
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new IllegalArgumentException(
					"Required argument 'state' cannot be null.");
		}

		Serializable[] states = (Serializable[]) state;

		this.showDimensionTitle = (Boolean) states[0];
		this.showParentMembers = (Boolean) states[1];
		this.hideSpans = (Boolean) states[2];

		initialize();
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(
					"Configuration object cannot be null.");
		}

		configuration.setDelimiterParsingDisabled(true);

		if (configuration.getLogger() == null) {
			configuration.setLogger(LogFactory.getLog(getClass()));
		}

		configuration.addProperty("render.showDimensionTitle",
				showDimensionTitle);
		configuration
				.addProperty("render.showParentMembers", showParentMembers);
		configuration.addProperty("render.hideSpans", hideSpans);
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(
					"Configuration object cannot be null.");
		}

		this.showDimensionTitle = configuration.getBoolean(
				"render.showDimensionTitle", true);
		this.showParentMembers = configuration.getBoolean(
				"render.showParentMembers", false);
		this.hideSpans = configuration.getBoolean("render.hideSpans", false);

		initialize();
	}
}
