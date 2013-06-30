package com.eyeq.pivot4j.analytics.graphene;

import org.apache.commons.lang.NullArgumentException;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Tab<T extends PageObject> implements PageFragment {

	private WebElement header;

	private WebElement panel;

	private Class<T> pageType;

	/**
	 * @param header
	 * @param panel
	 * @param pageType
	 */
	public Tab(WebElement header, WebElement panel, Class<T> pageType) {
		if (header == null) {
			throw new NullArgumentException("header");
		}

		if (panel == null) {
			throw new NullArgumentException("panel");
		}

		if (pageType == null) {
			throw new NullArgumentException("pageType");
		}

		this.header = header;
		this.panel = panel;
		this.pageType = pageType;
	}

	public String getTitle() {
		return header.getText();
	}

	public T switchToPage() {
		WebDriver driver = GrapheneContext.getProxy();

		if (driver == null) {
			throw new IllegalStateException(
					"No WebDriver proxy instance is available.");
		}

		driver.switchTo().frame(panel);

		T page = Graphene.createPageFragment(pageType, driver.switchTo()
				.activeElement());

		return page;
	}
}
