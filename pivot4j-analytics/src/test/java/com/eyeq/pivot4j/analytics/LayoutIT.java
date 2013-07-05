package com.eyeq.pivot4j.analytics;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.eyeq.pivot4j.analytics.graphene.Button;
import com.eyeq.pivot4j.analytics.graphene.ReportPage;
import com.eyeq.pivot4j.analytics.graphene.Tab;
import com.eyeq.pivot4j.analytics.graphene.TabPanel;
import com.eyeq.pivot4j.analytics.graphene.Toolbar;
import com.eyeq.pivot4j.analytics.graphene.Tree;
import com.eyeq.pivot4j.analytics.graphene.WorkbenchPage;

public class LayoutIT extends AbstractIntegrationTestCase {

	@Page
	private WorkbenchPage page;

	@Test
	public void testPageTitle() {
		assertThat("Unexpected page title.", getDriver().getTitle(),
				is(equalTo("Pivot4J Sample Application")));
	}

	@Test
	public void testToolbar() {
		Toolbar toolbar = page.getToolbar();

		assertThat("Unable to find the application toolbar.", toolbar,
				is(notNullValue()));
		assertThat("Toolbar pane has wrong height.", 35d,
				is(closeTo(toolbar.getSize().height, 5d)));

		List<Button> buttons = toolbar.getButtons();

		assertThat("Main toobar does not have any buttons.", buttons,
				is(notNullValue()));

		assertThat("Wrong number of toobar buttons.", buttons.size(),
				is(equalTo(10)));

		Iterator<Button> it = buttons.iterator();

		testButton("New", true, it.next());
		testButton("Open", false, it.next());
		testButton("Save", false, it.next());
		testButton("Save As", true, it.next());
		testButton("Delete", false, it.next());
		testButton("Refresh", true, it.next());

		testButton("Export", true, it.next());
		testButton("Print", false, it.next());

		testButton("Links", true, it.next());
		testButton("About", true, it.next());
	}

	/**
	 * @param label
	 * @param enabled
	 * @param button
	 */
	protected void testButton(String label, boolean enabled, Button button) {
		assertThat(String.format("Unexpected label found on the '%s' button.",
				label), button.getLabel(), is(equalTo(label)));

		assertThat(String.format(
				"Enabled status of '%s' button should be %s initially.", label,
				enabled), button.isEnabled(), is(equalTo(enabled)));
	}

	@Test
	public void testNavigator() {
		Tree navigator = page.getNavigator();

		assertThat("Unable to find the report navigator.", navigator,
				is(notNullValue()));
		assertThat("Tree pane has wrong width.", 200d,
				is(closeTo(navigator.getSize().width, 5d)));

		assertThat("Repository panel has a wrong title text.",
				navigator.getTitle(),
				is(equalToIgnoringWhiteSpace("Report Repository")));
	}

	@Test
	public void testContentPane() {
		WebElement contentPane = page.getContentPane();
		TabPanel<ReportPage> tabPanel = page.getTabPanel();

		assertThat("Unable to find the content pane.", contentPane,
				is(notNullValue()));
		assertThat("Unable to find the tab panel.", tabPanel,
				is(notNullValue()));

		Dimension contentSize = contentPane.getSize();
		Dimension tabSize = tabPanel.getSize();

		assertThat("Tab panel width is smaller than the content area width.",
				(double) contentSize.width, is(closeTo(tabSize.width, 5d)));
		assertThat("Tab panel height is smaller than the content area height.",
				(double) contentSize.height, is(closeTo(tabSize.height, 5d)));
	}

	@Test
	public void testInitialTab() {
		TabPanel<ReportPage> tabPanel = page.getTabPanel();
		assertThat("Unable to find the tab panel.", tabPanel,
				is(notNullValue()));

		List<Tab<ReportPage>> tabs = tabPanel.getTabs();

		assertThat("Wrong number of opened tab panels.", tabs.size(),
				is(equalTo(1)));

		Tab<ReportPage> tab = tabs.get(0);

		assertThat("The initial tab has wrong title text.", tab.getTitle(),
				is(equalToIgnoringWhiteSpace("*Untitled(1)")));

		ReportPage page = tab.switchToPage();

		assertThat("Unable to find the initial report.", page,
				is(notNullValue()));
	}

	@Test
	public void testReportToolbar() {
		Tab<ReportPage> tab = page.getTabPanel().getTabs().get(0);

		ReportPage reportPage = tab.switchToPage();

		Toolbar toolbar = reportPage.getToolbar();

		assertThat("Unable to find the report toolbar.", toolbar,
				is(notNullValue()));
		assertThat("Toolbar pane has wrong height.", 35d,
				is(closeTo(toolbar.getSize().height, 5d)));

		List<Button> buttons = toolbar.getButtons();

		assertThat("Main toobar does not have any buttons.", buttons,
				is(notNullValue()));

		assertThat("Wrong number of toobar buttons.", buttons.size(),
				is(equalTo(12)));

		Iterator<Button> it = buttons.iterator();

		testButton("Show Parent", false, it.next());
		testButton("Hide Spans", false, it.next());
		testButton("Non Empty", false, it.next());
		testButton("Swap Axes", false, it.next());

		testButton("Position", false, it.next());
		testButton("Member", false, it.next());
		testButton("Replace", false, it.next());

		testButton("Through", false, it.next());

		testButton("Properties", false, it.next());
		testButton("Agg.", false, it.next());
	}
}
