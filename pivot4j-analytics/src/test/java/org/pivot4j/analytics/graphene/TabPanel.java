package org.pivot4j.analytics.graphene;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class TabPanel<T extends PageObject> implements PageFragment {

	@Root
	private WebElement panel;

	@FindBy(css = ".ui-tabs-nav a")
	private List<WebElement> headers;

	@FindBy(tagName = "iframe")
	private List<WebElement> panels;

	@Drone
	private WebDriver driver;

	public Dimension getSize() {
		return panel.getSize();
	}

	public List<Tab<T>> getTabs() {
		if (headers == null || panels == null) {
			return Collections.emptyList();
		}

		Iterator<WebElement> headerIt = headers.iterator();
		Iterator<WebElement> panelIt = panels.iterator();

		List<Tab<T>> tabs = new LinkedList<Tab<T>>();

		while (headerIt.hasNext() && panelIt.hasNext()) {
			tabs.add(new Tab<T>(headerIt.next(), panelIt.next(), getPageType(),
					driver));
		}

		return tabs;
	}

	protected abstract Class<T> getPageType();
}
