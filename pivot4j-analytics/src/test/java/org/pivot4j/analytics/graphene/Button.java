package org.pivot4j.analytics.graphene;

import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Button implements PageFragment {

	@Root
	private WebElement button;

	@FindBy(className = "ui-button-text")
	private WebElement label;

	public String getLabel() {
		return label.getText();
	}

	public boolean isEnabled() {
		return !button.getAttribute("class").contains("ui-state-disabled");
	}

	public void click() {
		button.click();
	}
}
