package com.eyeq.pivot4j.analytics.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class NavigatorIT extends AbstractIntegrationTestCase {

	@Test
	public void testCubeSelection() {
		WebDriver driver = getDriver();

		WebElement sourceTree = driver.findElement(By
				.cssSelector("#source-tree-pane .ui-tree-container"));
		assertThat("Unable to find source tree pane.", sourceTree,
				is(notNullValue()));

		WebElement targetTree = driver.findElement(By
				.cssSelector("#target-tree-pane .ui-tree-container"));
		assertThat("Unable to find target tree pane.", targetTree,
				is(notNullValue()));

		List<WebElement> sourceNodes = sourceTree.findElements(By
				.className("ui-treenode"));
		assertThat(
				"Source tree should not be visible before cube is selected.",
				sourceNodes.isEmpty(), is(true));

		List<WebElement> targetNodes = targetTree.findElements(By
				.className("ui-treenode"));
		assertThat(
				"Target tree should not be visible before cube is selected.",
				targetNodes.isEmpty(), is(true));

		assertMDX(driver, null);

		selectSalesCube(driver);

		sourceTree = driver.findElement(By
				.cssSelector("#source-tree-pane .ui-tree-container"));
		assertThat("Missing source tree pane after cube selection.",
				sourceTree, is(notNullValue()));

		targetTree = driver.findElement(By
				.cssSelector("#target-tree-pane .ui-tree-container"));
		assertThat("Missing target tree pane after cube selection.",
				targetTree, is(notNullValue()));

		sourceNodes = sourceTree.findElements(By.className("ui-treenode"));
		assertThat("Source tree should be visible after cube is selected.",
				sourceNodes.size(), is(equalTo(24)));

		targetNodes = targetTree.findElements(By.className("ui-treenode"));
		assertThat("Target tree should be visible after cube is selected.",
				targetNodes.size(), is(equalTo(2)));

		assertMDX(driver, "SELECT {} ON COLUMNS, {} ON ROWS FROM [Sales]");
	}

	@Test
	public void testDnDBasicDimensions() {
		WebDriver driver = getDriver();

		selectSalesCube(driver);

		WebElement unitSales = driver
				.findElement(By
						.cssSelector("#source-tree-pane .ui-tree-container .node-member"));
		assertThat("Unable to find Unit Sales measure node.", unitSales,
				is(notNullValue()));
		assertThat("Unexpected label on Unit Sales measure node.",
				unitSales.getText(),
				is(equalToIgnoringWhiteSpace("Unit Sales")));

		WebElement columns = driver
				.findElement(By
						.cssSelector("#target-tree-pane .ui-tree-container .node-axis"));
		assertThat("Missing columns axis node.", columns, is(notNullValue()));
		assertThat("Unexpected label on columns axis node.", columns.getText(),
				is(equalToIgnoringWhiteSpace("Columns")));

		Actions action = new Actions(driver);
		action.dragAndDrop(unitSales, columns).perform();

		waitUntilAjaxRequestCompletes();

		WebElement measures = driver
				.findElement(By
						.cssSelector("#target-tree-pane .ui-tree-container .node-hierarchy"));
		assertThat("Missing Measures hierarchy node.", measures,
				is(notNullValue()));
		assertThat("Unexpected label on Measures node.", measures.getText(),
				is(equalToIgnoringWhiteSpace("Measures")));

		assertMDX(driver,
				"SELECT {[Measures].[Unit Sales]} ON COLUMNS, {} ON ROWS FROM [Sales]");

		WebElement product = driver
				.findElements(
						By.cssSelector("#source-tree-pane .ui-tree-container .node-hierarchy"))
				.get(6);

		assertThat("Unable to find Product hierarchy node.", product,
				is(notNullValue()));
		assertThat("Unexpected label on Product hierarchy node.",
				product.getText(), is(equalToIgnoringWhiteSpace("Product")));

		WebElement rows = driver
				.findElements(
						By.cssSelector("#target-tree-pane .ui-tree-container .node-axis"))
				.get(1);
		assertThat("Missing rows axis node.", rows, is(notNullValue()));
		assertThat("Unexpected label on rows axis node.", rows.getText(),
				is(equalToIgnoringWhiteSpace("Rows")));

		JavascriptExecutor jsExec = (JavascriptExecutor) driver;
		jsExec.executeScript("jQuery(\"#source-tree-pane .ui-layout-content\").get(0).scrollTop=200");

		action = new Actions(driver);
		action.dragAndDrop(product, rows).perform();

		waitUntilAjaxRequestCompletes();

		product = driver
				.findElements(
						By.cssSelector("#target-tree-pane .ui-tree-container .node-hierarchy"))
				.get(1);
		assertThat("Missing selected Product hierarchy node.", product,
				is(notNullValue()));
		assertThat("Unexpected label on the selected Product hierarchy node.",
				product.getText(), is(equalToIgnoringWhiteSpace("Product")));

		WebElement allProducts = driver
				.findElement(By
						.cssSelector("#target-tree-pane .ui-tree-container .node-level"));
		assertThat("Missing All Products level node.", allProducts,
				is(notNullValue()));
		assertThat("Unexpected label on All Products level node.",
				allProducts.getText(), is(equalToIgnoringWhiteSpace("(All)")));

		assertMDX(
				driver,
				"SELECT {[Measures].[Unit Sales]} ON COLUMNS, {[Product].[All Products]} ON ROWS FROM [Sales]");

		WebElement table = driver.findElement(By
				.cssSelector("table.pivot-grid"));
		assertThat("Unable to find the pivot grid.", table, is(notNullValue()));

		List<WebElement> headers = table.findElements(By.xpath(".//thead/tr"));

		assertThat("Wrong number of column header rows.", headers.size(),
				is(equalTo(2)));

		List<WebElement> headerCells = headers.get(0).findElements(
				By.tagName("td"));

		assertThat("Wrong number of header cells (0).", headerCells.size(),
				is(equalTo(2)));

		assertThat("Wrong header cell label value (0, 0).",
				StringUtils.trimToNull(headerCells.get(0).getText()),
				is(equalTo(null)));

		assertThat("Wrong header cell label value (0, 1).",
				StringUtils.trimToNull(headerCells.get(1).getText()),
				is(equalTo("Measures")));

		headerCells = headers.get(1).findElements(By.tagName("td"));

		assertThat("Wrong number of header cells (1).", headerCells.size(),
				is(equalTo(2)));

		assertThat("Wrong header cell label value (1, 0).",
				StringUtils.trimToNull(headerCells.get(0).getText()),
				is(equalTo("Product")));

		assertThat(
				"Wrong header cell label value (1, 1).",
				StringUtils.trimToNull(headerCells.get(1)
						.findElement(By.xpath("./span")).getText()),
				is(equalTo("Unit Sales")));

		List<WebElement> dataRows = table.findElements(By.xpath(".//tbody/tr"));
		assertThat("Wrong number of content rows.", dataRows.size(),
				is(equalTo(1)));

		List<WebElement> dataCells = dataRows.get(0).findElements(
				By.tagName("td"));

		assertThat("Wrong number of content cells (0).", dataCells.size(),
				is(equalTo(2)));

		assertThat(
				"Wrong cell label value (0, 0).",
				StringUtils.trimToNull(dataCells.get(0)
						.findElement(By.xpath("./span")).getText()),
				is(equalTo("All Products")));

		assertThat(
				"Wrong cell label value (0, 1).",
				StringUtils.trimToNull(dataCells.get(1)
						.findElement(By.xpath("./span")).getText()),
				is(equalTo("266,773")));
	}

	/**
	 * @param driver
	 */
	protected void selectSalesCube(WebDriver driver) {
		WebElement cubes = driver.findElement(By
				.cssSelector("#cube-list-pane .ui-selectonemenu"));

		assertThat("Cube list menu cannot be found.", cubes, is(notNullValue()));

		cubes.findElement(By.className("ui-selectonemenu-trigger")).click();

		List<WebElement> items = driver.findElements(By
				.className("ui-selectonemenu-item"));

		assertThat("Wrong number of cube list items.", items.size(),
				is(equalTo(8)));

		WebElement salesItem = null;

		for (int i = 0; i < 8; i++) {
			WebElement item = items.get(i);

			if (item.getText().trim().equals("Sales")) {
				salesItem = item;
				break;
			}
		}

		assertThat("Unable to find the 'Sales' item.", salesItem, is(notNullValue()));

		salesItem.click();

		waitUntilAllAnimationsComplete();
		waitUntilAjaxRequestCompletes();
	}

	/**
	 * @param driver
	 * @param mdx
	 */
	protected void assertMDX(WebDriver driver, String mdx) {
		WebElement editor = driver.findElement(By
				.cssSelector("#editor-form textarea"));

		assertThat("MDX editor cannot be found.", editor, is(notNullValue()));
		assertThat("Unexpected MDX query.",
				StringUtils.trimToNull(editor.getAttribute("value")),
				is(equalTo(mdx)));
	}
}
