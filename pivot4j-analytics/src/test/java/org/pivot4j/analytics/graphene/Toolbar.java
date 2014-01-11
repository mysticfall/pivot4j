package org.pivot4j.analytics.graphene;

import java.util.List;

import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Toolbar implements PageFragment {

	@Root
	private WebElement toolbar;

	@FindBy(className = "ui-button")
	private List<Button> buttons;

	public Dimension getSize() {
		return toolbar.getSize();
	}

	/**
	 * @return the buttons
	 */
	public List<Button> getButtons() {
		return buttons;
	}
}
