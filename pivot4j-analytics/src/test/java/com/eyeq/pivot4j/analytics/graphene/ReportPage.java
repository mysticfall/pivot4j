package com.eyeq.pivot4j.analytics.graphene;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;

public class ReportPage implements PageObject {

	@FindBy(id = "toolbar-pane")
	private Toolbar toolbar;

	/**
	 * @return the toolbar
	 */
	public Toolbar getToolbar() {
		return toolbar;
	}
}
