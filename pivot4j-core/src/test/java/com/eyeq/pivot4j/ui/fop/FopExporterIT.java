package com.eyeq.pivot4j.ui.fop;

/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.print.attribute.standard.OrientationRequested;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.table.TableRenderer;

public class FopExporterIT extends AbstractIntegrationTestCase {

	private String testQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize(Union(Union(CrossJoin({[Time].[1997]}, {[Promotion Media].[All Media]}), "
			+ "CrossJoin({[Time].[1997]}, [Promotion Media].[All Media].Children)), "
			+ "{CrossJoin({[Time].[1998]}, [Promotion Media].[All Media].Children)})) ON ROWS FROM [Sales]";

	private boolean deleteTestFile = true;

	/**
	 * @see com.eyeq.pivot4j.AbstractIntegrationTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PivotModel model = getPivotModel();
		model.setMdx(testQuery);
		model.initialize();
	}

	@Test
	public void testExportPdf() throws IOException {
		OutputStream out = null;

		File file = File.createTempFile("pivot4j-", ".pdf");

		if (deleteTestFile) {
			file.deleteOnExit();
		}

		try {
			out = new FileOutputStream(file);

			TableRenderer renderer = new TableRenderer();
			renderer.setShowParentMembers(true);
			renderer.setShowDimensionTitle(true);
			renderer.setHideSpans(false);

			FopExporter exporter = new FopExporter(out);
			exporter.setOrientation(OrientationRequested.LANDSCAPE);

			renderer.render(getPivotModel(), exporter);
		} finally {
			out.flush();
			IOUtils.closeQuietly(out);
		}
	}
}
