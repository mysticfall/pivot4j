package com.eyeq.pivot4j.analytics.graphene;

import java.util.List;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.jboss.arquillian.graphene.spi.annotations.Root;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public class Toolbar implements PageFragment {

	@Root
	private WebElement toolbar;

	@FindBy(jquery = ".ui-button:visible")
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
