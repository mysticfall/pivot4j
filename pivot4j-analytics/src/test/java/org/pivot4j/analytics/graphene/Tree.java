package org.pivot4j.analytics.graphene;

import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Tree implements PageFragment {

	@Root
	private WebElement navigator;

	@FindBy(className = "ui-widget-header")
	private WebElement header;

	public Dimension getSize() {
		return navigator.getSize();
	}

	public String getTitle() {
		if (header == null) {
			return null;
		}

		return header.getText();
	}
}
