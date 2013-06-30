package com.eyeq.pivot4j.analytics;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.analytics.repository.file.LocalFile;
import com.eyeq.pivot4j.analytics.repository.file.LocalFileSystemRepository;
import com.eyeq.pivot4j.analytics.repository.test.TestFile;
import com.eyeq.pivot4j.analytics.repository.test.TestRepository;
import com.eyeq.pivot4j.analytics.repository.test.TestRepositoryImpl;

@RunWith(Arquillian.class)
public abstract class AbstractIntegrationTestCase {

	@ArquillianResource
	private URL url;

	@Drone
	private WebDriver driver;

	@Deployment(testable = false)
	public static WebArchive createArtifact() throws TransformerException {
		Logger logger = LoggerFactory
				.getLogger(AbstractIntegrationTestCase.class);

		TransformerFactory factory = TransformerFactory.newInstance();
		Source template = new StreamSource(new File("src/main/xsl/web.xsl"));

		Transformer transformer = factory.newTransformer(template);

		Source source = new StreamSource(new File(
				"src/main/webapp/WEB-INF/web.xml"));

		StringWriter writer = new StringWriter();
		Result out = new StreamResult(writer);

		transformer.setParameter("jsf.project.stage", "UnitTest");
		transformer.transform(source, out);

		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "pivot4j.war")
				.merge(ShrinkWrap
						.create(GenericArchive.class)
						.as(ExplodedImporter.class)
						.importDirectory(
								"src/main/webapp".replace('/',
										File.separatorChar))
						.as(GenericArchive.class),
						"/",
						Filters.exclude("/WEB-INF/(web|faces-config|pivot4j-config).xml"))
				.addPackages(
						true,
						Filters.exclude(LocalFile.class,
								LocalFileSystemRepository.class),
						"com.eyeq.pivot4j.analytics")
				.addClasses(TestFile.class, TestRepositoryImpl.class,
						TestRepository.class)
				.addAsResource(
						getFileForPath("src/main/resources/com/eyeq/pivot4j/analytics/i18n/messages.properties"),
						"/com/eyeq/pivot4j/analytics/i18n/messages.properties")
				.addAsWebInfResource(new StringAsset(writer.toString()),
						"web.xml")
				.addAsWebInfResource(
						getFileForPath("src/test/webapp/WEB-INF/faces-config.xml"))
				.addAsWebInfResource(
						getFileForPath("src/test/resources/pivot4j-config.xml"))
				.addAsResource(
						getFileForPath("src/main/resources/logback-test.xml"))
				.addAsResource(
						getFileForPath("src/main/resources/mondrian.properties"));

		if (logger.isDebugEnabled()) {
			logger.debug("Creating an web archive for testing : " + archive);
			logger.debug(archive.toString(true));
		}

		return archive;
	}

	/**
	 * @param path
	 * @return
	 */
	private static File getFileForPath(String path) {
		return new File(path.replace('/', File.separatorChar));
	}

	@Before
	public void setUp() {
		String address = url.toExternalForm();
		if (!driver.getCurrentUrl().equals(address)) {
			driver.get(address);
		}
	}

	@After
	public void tearDown() {
		driver.switchTo().defaultContent();
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @return the driver
	 */
	public WebDriver getDriver() {
		return driver;
	}
}
