package com.eyeq.pivot4j.export.poi;

/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;

public class ExcelExporterIT extends AbstractIntegrationTestCase {

	private String testQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize(Union(Union(CrossJoin({[Time].[1997]}, {[Promotion Media].[All Media]}), "
			+ "CrossJoin({[Time].[1997]}, [Promotion Media].[All Media].Children)), "
			+ "{([Time].[1998], [Promotion Media].[All Media])})) ON ROWS FROM [Sales]";

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
	public void testExportHSSF() throws IOException, InvalidFormatException {
		testExport(Format.HSSF, true, true, false, 17, 6);
		testExport(Format.HSSF, false, true, false, 17, 3);
		testExport(Format.HSSF, true, false, false, 16, 5);
		testExport(Format.HSSF, true, false, true, 16, 3);
	}

	@Test
	public void testExportXSSF() throws IOException, InvalidFormatException {
		testExport(Format.XSSF, true, true, false, 17, 6);
		testExport(Format.XSSF, false, true, false, 17, 3);
		testExport(Format.XSSF, true, false, false, 16, 5);
		testExport(Format.XSSF, true, false, true, 16, 3);
	}

	@Test
	public void testExportSXSSF() throws IOException, InvalidFormatException {
		testExport(Format.SXSSF, true, true, false, 17, 6);
		testExport(Format.SXSSF, false, true, false, 17, 3);
		testExport(Format.SXSSF, true, false, false, 16, 5);
		testExport(Format.SXSSF, true, false, true, 16, 3);
	}

	/**
	 * @param format
	 * @param showParentMember
	 * @param showDimensionTitle
	 * @param hideSpans
	 * @param rows
	 * @param mergedRegions
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	protected void testExport(Format format, boolean showParentMember,
			boolean showDimensionTitle, boolean hideSpans, int rows,
			int mergedRegions) throws IOException, InvalidFormatException {
		OutputStream out = null;

		File file = File
				.createTempFile("pivot4j-", "." + format.getExtension());

		if (deleteTestFile) {
			file.deleteOnExit();
		}

		try {
			out = new FileOutputStream(file);
			ExcelExporter exporter = new ExcelExporter(out);

			exporter.setFormat(format);
			exporter.setShowParentMembers(showParentMember);
			exporter.setShowDimensionTitle(showDimensionTitle);
			exporter.setHideSpans(hideSpans);

			exporter.initialize();
			exporter.render(getPivotModel());
		} finally {
			out.flush();
			IOUtils.closeQuietly(out);
		}

		Workbook workbook = WorkbookFactory.create(file);

		assertNotNull("Workbook cannot be null.", workbook);

		Sheet sheet = workbook.getSheetAt(0);
		assertNotNull("Worksheet cannot be null.", sheet);

		assertEquals("Invalid worksheet name.", "Sales", sheet.getSheetName());

		assertEquals("Wrong number of rows.", rows, sheet.getLastRowNum());
		assertEquals("Wrong number of merged regions.", mergedRegions,
				sheet.getNumMergedRegions());
	}
}
