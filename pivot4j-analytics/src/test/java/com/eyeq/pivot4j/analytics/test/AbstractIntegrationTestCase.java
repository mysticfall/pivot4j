package com.eyeq.pivot4j.analytics.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derived from the PrimeFaces showcase.
 */
public abstract class AbstractIntegrationTestCase {

	private static final String JQUERY_ACTIVE_CONNECTIONS_QUERY = "return $.active == 0;";

	private static final int DEFAULT_SLEEP_TIME_IN_SECONDS = 2;

	private static final int DEFAULT_ANIMATED_INTERVAL_IN_SECONDS = 400;

	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 10;

	private static WebDriver driver;

	protected static Logger log = LoggerFactory
			.getLogger(AbstractIntegrationTestCase.class);

	@BeforeClass
	public static void setUpClass() throws Exception {
		driver = getDriver();

		Assume.assumeThat(driver, is(notNullValue()));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
	}

	protected static WebDriver getDriver() {
		if (driver == null) {
			driver = createDriver();
		}

		return driver;
	}

	protected static WebDriver createDriver() {
		String type = StringUtils.trimToNull(System
				.getProperty("web.test.driver"));

		return createDriver(type);
	}

	/**
	 * @param type
	 * @return
	 */
	protected static WebDriver createDriver(String type) {
		if (type == null) {
			if (log.isInfoEnabled()) {
				log.info("No web driver type specified. Skipping test.");
			}

			return null;
		}

		WebDriver driver = null;

		if (log.isInfoEnabled()) {
			log.info("Initializing web driver : " + type);
		}

		if (type.equals("win32ie")) {
			DesiredCapabilities capabilities = DesiredCapabilities
					.internetExplorer();

			capabilities
					.setCapability(
							InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
							true);
			driver = new InternetExplorerDriver(capabilities);
		} else if (type.equals("firefox")) {
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("intl.accept_languages", "en");

			FirefoxDriver firefox = new FirefoxDriver(profile);

			firefox.manage().window().setPosition(new Point(0, 0));
			firefox.manage().window().setSize(new Dimension(1200, 800));

			driver = firefox;
		} else {
			if (log.isWarnEnabled()) {
				log.warn("Unknown web driver type specified. Skipping test.");
			}
		}

		return driver;
	}

	@Before
	public void before() {
		if (driver != null) {
			String url = StringUtils.trimToNull(System
					.getProperty("web.test.url"));
			if (url == null) {
				url = "http://localhost:8080/pivot4j";
			}

			url += getPageUrl();

			if (log.isInfoEnabled()) {
				log.info("Opening URL : " + url);
			}

			driver.get(url);
		}
	}

	protected String getPageUrl() {
		return "/view.xhtml";
	}

	/**
	 * Use when element is on the page or will be on the page. Can be used
	 * element is not on the page before the ajax call and will be on the page
	 * after the ajax call
	 * 
	 * @param elementId
	 * @param value
	 */
	protected void waitUntilElementGetsValue(final String elementId,
			final String value) {
		new FluentWait<WebDriver>(driver)
				.withTimeout(DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
				.pollingEvery(DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS)
				.ignoring(NoSuchElementException.class)
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver wd) {
						WebElement element = wd.findElement(By.id(elementId));
						return element.getText().equals(value);
					}
				});
	}

	/**
	 * Use when element is already precisely on the page. Throws
	 * NoSuchElementException when element is not found
	 * 
	 * @param elementId
	 * @param value
	 */
	protected void waitUntilElementExistsAndGetsValue(final String elementId,
			final String value) {
		new FluentWait<WebDriver>(driver)
				.withTimeout(DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
				.pollingEvery(DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS)
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver wd) {
						WebElement element = wd.findElement(By.id(elementId));
						return element.getText().equals(value);
					}
				});
	}

	protected void waitUntilAjaxRequestCompletes() {
		new FluentWait<WebDriver>(driver)
				.withTimeout(DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
				.pollingEvery(DEFAULT_SLEEP_TIME_IN_SECONDS, TimeUnit.SECONDS)
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						JavascriptExecutor jsExec = (JavascriptExecutor) d;
						return (Boolean) jsExec
								.executeScript(JQUERY_ACTIVE_CONNECTIONS_QUERY);
					}
				});
	}

	/**
	 * Waits until body elements animated with JS.
	 */
	protected void waitUntilAllAnimationsComplete() {
		waitUntilAnimationCompletes("body *");
	}

	/**
	 * Waits until given selector elements animated with JS.
	 * 
	 * @param selector
	 *            : jQuery element selector
	 */
	protected void waitUntilAnimationCompletes(final String selector) {
		new FluentWait<WebDriver>(driver)
				.withTimeout(DEFAULT_TIMEOUT_IN_SECONDS * 2, TimeUnit.SECONDS)
				.pollingEvery(DEFAULT_ANIMATED_INTERVAL_IN_SECONDS,
						TimeUnit.MILLISECONDS)
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return (Boolean) ((JavascriptExecutor) d)
								.executeScript("return ! $('" + selector
										+ "').is(':animated');");
					}
				});
	}
}
