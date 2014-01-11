/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.property;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.assertThat;
import static org.pivot4j.ui.table.TablePropertyCategories.CELL;
import static org.pivot4j.ui.table.TablePropertyCategories.HEADER;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pivot4j.ui.AbstractHtmlTableTestCase;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.pivot4j.ui.table.TableRenderer;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class SimplePropertyRenderingIT extends AbstractHtmlTableTestCase {

	/**
	 * @see org.pivot4j.ui.AbstractHtmlTableTestCase#getQueryName()
	 */
	@Override
	protected String getQueryName() {
		return "simple";
	}

	/**
	 * @param renderer
	 * @see org.pivot4j.ui.AbstractHtmlTableTestCase#configureRenderer(org.pivot4j.ui.PivotRenderer)
	 */
	@Override
	protected void configureRenderer(TableRenderer renderer) {
		super.configureRenderer(renderer);

		RenderPropertyList headerProperties = renderer.getRenderProperties()
				.get(HEADER);
		RenderPropertyList cellProperties = renderer.getRenderProperties().get(
				CELL);

		headerProperties.setRenderProperty(new SimpleRenderProperty("bgColor",
				"<#if member?? && member.level.depth == 2>#990000</#if>"));
		headerProperties.setRenderProperty(new SimpleRenderProperty(
				"fontFamily", "serif"));
		headerProperties.setRenderProperty(new SimpleRenderProperty(
				"fontStyle",
				"<#if member?? && member.caption == 'Drink'>italic</#if>"));
		headerProperties
				.setRenderProperty(new SimpleRenderProperty(
						"link",
						"<#if member?? && member.all>javascript: alert('Unique name : ${member.uniqueName}');</#if>"));

		cellProperties.setRenderProperty(new SimpleRenderProperty("fgColor",
				"<#if cell?? && cell.doubleValue < 15000>#ff0000</#if>"));
		cellProperties.setRenderProperty(new SimpleRenderProperty("bgColor",
				"<#if aggregator?? && cellType == 'VALUE'>#00aa00</#if>"));
		cellProperties.setRenderProperty(new SimpleRenderProperty("fontStyle",
				"<#if cell?? && cell.doubleValue < 15000>bold</#if>"));
		cellProperties.setRenderProperty(new SimpleRenderProperty("label",
				"<#if cell??>${'$'}${cell.formattedValue}</#if>"));
	}

	@Test
	public void testColumnHeader() throws IOException {
		HtmlTable table = getTable();

		HtmlTableHeader header = table.getHeader();

		List<HtmlTableRow> rows = header.getRows();

		assertCell(rows, 0, 0, 1, 4, "Product");

		Map<String, String> styles = getCellStyles(rows, 0, 0);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(1)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));

		assertCell(rows, 0, 1, 1, 3, "Measures");

		styles = getCellStyles(rows, 0, 1);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(1)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));
	}

	@Test
	public void testRowHeaderAllProducts() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 1, 0, 22, 1, "All Products");

		HtmlTableCell cell = rows.get(1).getCells().get(0);

		assertThat("Unable to find an <A> tag", cell.getChildElementCount(),
				is(equalTo(1)));

		DomElement elem = cell.getChildElements().iterator().next();

		assertThat("Unable to find an <A> tag", elem.getClass(),
				typeCompatibleWith(HtmlAnchor.class));

		HtmlAnchor link = (HtmlAnchor) elem;

		assertThat(
				"The cell has an invalid link target",
				link.getHrefAttribute(),
				is(equalTo("javascript: alert('Unique name : [Product].[All Products]');")));

		Map<String, String> styles = getCellStyles(rows, 1, 0);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(2)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));
	}

	@Test
	public void testRowHeaderDrink() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 1, 1, 1, 3, "Drink");

		Map<String, String> styles = getCellStyles(rows, 1, 1);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(3)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));
		assertThat("The cell has a wrong font style", styles.get("font-style"),
				is(equalTo("oblique")));
	}

	@Test
	public void testRowHeaderBeverages() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 3, 0, 1, 2, "Beverages");

		Map<String, String> styles = getCellStyles(rows, 3, 0);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(4)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));
		assertThat("The cell has a wrong background color",
				styles.get("background-color"), is(equalTo("#990000")));
		assertThat("The cell has a wrong background image",
				styles.get("background-image"), is(equalTo("none")));
	}

	@Test
	public void testRowHeaderBread() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 7, 1, 1, 1, "Bread");

		Map<String, String> styles = getCellStyles(rows, 7, 1);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(2)));
		assertThat("The cell has a wrong font family style",
				styles.get("font-family"), is(equalTo("serif")));
	}

	@Test
	public void testMeasureCell() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 3, 1, 1, 1, "$27,748.53");

		Map<String, String> styles = getCellStyles(rows, 3, 1);

		assertThat("Wrong number of style rules", styles.isEmpty(), is(true));
	}

	@Test
	public void testMeasureCellMinus() throws IOException {
		HtmlTable table = getTable();

		HtmlTableBody body = table.getBodies().get(0);

		List<HtmlTableRow> rows = body.getRows();

		assertCell(rows, 3, 2, 1, 1, "$11,069.53");

		Map<String, String> styles = getCellStyles(rows, 3, 2);

		assertThat("Wrong number of style rules", styles.size(), is(equalTo(2)));
		assertThat("The cell has a wrong foreground color",
				styles.get("color"), is(equalTo("#ff0000")));
		assertThat("The cell has a wrong font style",
				styles.get("font-weight"), is(equalTo("bold")));
	}
}
