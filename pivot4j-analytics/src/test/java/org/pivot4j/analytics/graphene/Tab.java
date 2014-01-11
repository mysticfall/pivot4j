package org.pivot4j.analytics.graphene;

import org.apache.commons.lang.NullArgumentException;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Tab<T extends PageObject> implements PageFragment {

	private WebElement header;

	private WebElement panel;

	private Class<T> pageType;

	private WebDriver driver;

	/**
	 * @param header
	 * @param panel
	 * @param pageType
	 * @param driver
	 */
	public Tab(WebElement header, WebElement panel, Class<T> pageType,
			WebDriver driver) {
		if (header == null) {
			throw new NullArgumentException("header");
		}

		if (panel == null) {
			throw new NullArgumentException("panel");
		}

		if (pageType == null) {
			throw new NullArgumentException("pageType");
		}

		if (driver == null) {
			throw new NullArgumentException("driver");
		}

		this.header = header;
		this.panel = panel;
		this.pageType = pageType;
		this.driver = driver;
	}

	public String getTitle() {
		return header.getText();
	}

	public T switchToPage() {
		driver.switchTo().frame(panel);

		T page = Graphene.createPageFragment(pageType, driver.switchTo()
				.activeElement());

		return page;
	}
}
