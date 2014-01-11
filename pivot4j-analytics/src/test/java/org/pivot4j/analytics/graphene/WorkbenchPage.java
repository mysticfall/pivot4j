package org.pivot4j.analytics.graphene;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class WorkbenchPage implements PageObject {

	@FindBy(id = "toolbar-pane")
	private Toolbar toolbar;

	@FindBy(id = "navigator-pane")
	private Tree navigator;

	@FindBy(id = "main-content-pane")
	private WebElement contentPane;

	@FindBy(id = "tab-panel")
	private ReportTabPanel tabPanel;

	/**
	 * @return the toolbar
	 */
	public Toolbar getToolbar() {
		return toolbar;
	}

	/**
	 * @return the navigator
	 */
	public Tree getNavigator() {
		return navigator;
	}

	/**
	 * @return the contentPane
	 */
	public WebElement getContentPane() {
		return contentPane;
	}

	/**
	 * @return the tabPanel
	 */
	public ReportTabPanel getTabPanel() {
		return tabPanel;
	}
}
