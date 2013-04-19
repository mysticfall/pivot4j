package com.eyeq.pivot4j.analytics.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LayoutIT extends AbstractIntegrationTestCase {

	@Test
	public void testBasicLayout() {
		WebDriver driver = getDriver();

		assertThat("Unexpected page title.", driver.getTitle(),
				is(equalTo("Pivot4J Sample Application")));

		WebElement body = driver.findElement(By.id("body"));

		assertThat("Body pane cannot be found.", body, is(notNullValue()));

		WebElement toolbar = driver.findElement(By.id("toolbar-pane"));

		assertThat("Toolbar cannot be found.", toolbar, is(notNullValue()));
	}

	@Test
	public void testNavigatorLayout() {
		WebDriver driver = getDriver();

		WebElement navigator = driver.findElement(By.id("navigator-pane"));

		assertThat("Navigator pane cannot be found.", navigator,
				is(notNullValue()));

		List<WebElement> headers = navigator.findElements(By
				.cssSelector("div.ui-widget-header"));

		assertThat("Wrong number of headers.", headers.size(), is(equalTo(3)));

		assertThat("Unable to find the OLAP navigator panel.", headers.get(0)
				.getText(), is(equalToIgnoringWhiteSpace("OLAP Navigator")));

		assertThat("Unable to find the cube structure panel.", headers.get(1)
				.getText(), is(equalToIgnoringWhiteSpace("Cube Structure")));

		assertThat("Unable to find the pivot structure panel.", headers.get(2)
				.getText(), is(equalToIgnoringWhiteSpace("Pivot Structure")));
	}

	@Test
	public void testContentLayout() {
		WebDriver driver = getDriver();

		WebElement content = driver.findElement(By.id("content-pane"));

		assertThat("Content pane cannot be found.", content, is(notNullValue()));

		List<WebElement> headers = content.findElements(By
				.cssSelector("div.ui-widget-header"));

		assertThat("Wrong number of headers.", headers.size(), is(equalTo(3)));

		assertThat("Unable to find the grid panel.", headers.get(0).getText(),
				is(equalToIgnoringWhiteSpace("Query Result")));

		assertThat("Unable to find the MDX editor panel.", headers.get(1)
				.getText(), is(equalToIgnoringWhiteSpace("MDX Query")));
	}
}
