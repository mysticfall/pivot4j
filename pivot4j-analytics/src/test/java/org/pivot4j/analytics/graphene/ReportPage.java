package org.pivot4j.analytics.graphene;

import org.openqa.selenium.support.FindBy;

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
