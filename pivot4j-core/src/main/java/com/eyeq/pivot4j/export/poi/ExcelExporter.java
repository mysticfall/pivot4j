/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.export.poi;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.olap4j.Axis;
import org.olap4j.OlapException;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.export.AbstractPivotExporter;
import com.eyeq.pivot4j.ui.RenderContext;

public class ExcelExporter extends AbstractPivotExporter {

	private Format format = Format.HSSF;

	private Workbook workbook;

	private Sheet sheet;

	private Row row;

	private Cell cell;

	private CellStyle headerCellStyle;

	private CellStyle valueCellStyle;

	private List<CellRangeAddress> mergedRegions;

	private String fontFamily = "Arial";

	private int fontSize = 10;

	private int sheetIndex = 0;

	public ExcelExporter() {
		reset();
	}

	/**
	 * @param out
	 */
	public ExcelExporter(OutputStream out) {
		super(out);
		reset();
	}

	/**
	 * @see com.eyeq.pivot4j.export.PivotExporter#getContentType()
	 */
	@Override
	public String getContentType() {
		return "application/vnd.ms-excel";
	}

	/**
	 * @return the format
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * @return the fontFamily
	 */
	public String getFontFamily() {
		return fontFamily;
	}

	/**
	 * @param fontFamily
	 *            the fontFamily to set
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	/**
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	protected void reset() {
		this.workbook = null;
		this.sheet = null;
		this.row = null;
		this.cell = null;
		this.mergedRegions = new ArrayList<CellRangeAddress>();
		this.sheetIndex = 0;

		this.headerCellStyle = null;
		this.valueCellStyle = null;
	}

	/**
	 * @see com.eyeq.pivot4j.export.AbstractPivotExporter#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		this.workbook = createWorkbook();

		super.render(model);

		try {
			workbook.write(getOutputStream());
		} catch (IOException e) {
			throw new PivotException(e);
		}

		reset();
	}

	protected Workbook createWorkbook() {
		Workbook newWorkbook = null;

		switch (format) {
		case XSSF:
			newWorkbook = new XSSFWorkbook();
			break;
		case HSSF:
			newWorkbook = new HSSFWorkbook();
			break;
		case SXSSF:
			newWorkbook = new SXSSFWorkbook(500);
			break;
		default:
			assert false;
		}

		return newWorkbook;
	}

	/**
	 * @return workbook
	 */
	protected Workbook getWorkbook() {
		return workbook;
	}

	/**
	 * @param workbook
	 * @return
	 */
	protected Sheet createSheet(RenderContext context, Workbook workbook) {
		Sheet newSheet = workbook.createSheet(WorkbookUtil
				.createSafeSheetName(getSheetName(context, sheetIndex)));

		sheetIndex++;
		return newSheet;
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getSheetName(RenderContext context, int sheetIndex) {
		return context.getModel().getCube().getCaption();
	}

	/**
	 * @return sheet
	 */
	protected Sheet getSheet() {
		return sheet;
	}

	protected int getRowOffset() {
		return 0;
	}

	protected int getColOffset() {
		return 0;
	}

	/**
	 * @param sheet
	 * @param rowIndex
	 * @return
	 */
	protected Row createRow(Sheet sheet, int rowIndex) {
		return sheet.createRow(rowIndex);
	}

	/**
	 * @return row
	 */
	protected Row getRow() {
		return row;
	}

	/**
	 * @param row
	 * @return
	 */
	protected Cell createCell(Row row, int colIndex) {
		return row.createCell(colIndex);
	}

	/**
	 * @return cell
	 */
	protected Cell getCell() {
		return cell;
	}

	protected CellStyle createHeaderCellStyle() {
		CellStyle style = workbook.createCellStyle();

		Font font = workbook.createFont();

		font.setFontName(fontFamily);
		font.setFontHeightInPoints((short) fontSize);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);

		style.setFont(font);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		switch (format) {
		case XSSF:
		case SXSSF:
			((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(
					Color.lightGray));
			break;
		case HSSF:
			style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
			break;
		default:
			assert false;
		}

		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);

		return style;
	}

	/**
	 * @return headerCellStyle
	 */
	protected CellStyle getHeaderCellStyle() {
		if (headerCellStyle == null) {
			this.headerCellStyle = createHeaderCellStyle();
		}

		return headerCellStyle;
	}

	protected CellStyle createValueCellStyle() {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();

		font.setFontName(fontFamily);
		font.setFontHeightInPoints((short) fontSize);
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

		style.setFont(font);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setDataFormat((short) 4);

		return style;
	}

	/**
	 * @return valueCellStyle
	 */
	protected CellStyle getValueCellStyle() {
		if (valueCellStyle == null) {
			this.valueCellStyle = createValueCellStyle();
		}

		return valueCellStyle;
	}

	/**
	 * @param context
	 * @return
	 */
	protected CellStyle getCellStyle(RenderContext context) {
		if (context.getCell() == null) {
			return getHeaderCellStyle();
		} else {
			return getValueCellStyle();
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startTable(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startTable(RenderContext context) {
		this.sheet = createSheet(context, workbook);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startHeader(RenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endHeader(RenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startBody(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startBody(RenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startRow(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRow(RenderContext context) {
		this.row = createRow(sheet, context.getRowIndex() + getRowOffset());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#startCell(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startCell(RenderContext context) {
		this.cell = createCell(row, context.getColIndex() + getColOffset());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void cellContent(RenderContext context) {
		cell.setCellStyle(getCellStyle(context));

		if (context.getCell() == null) {
			super.cellContent(context);
		} else {
			try {
				Double value = context.getCell().isEmpty() ? null : context
						.getCell().getDoubleValue();

				cellContent(context, value);
			} catch (OlapException e) {
				throw new PivotException(e);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void cellContent(RenderContext context, String label) {
		cell.setCellValue(label);
		cell.setCellType(Cell.CELL_TYPE_STRING);
	}

	/**
	 * @param context
	 * @param value
	 */
	public void cellContent(RenderContext context, Double value) {
		if (value == null) {
			cell.setCellValue("");
		} else {
			cell.setCellValue(value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endCell(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endCell(RenderContext context) {
		this.cell = null;

		CellRangeAddress range = createMergedRegion(context);
		if (range != null) {
			mergedRegions.add(range);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endRow(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRow(RenderContext context) {
		this.row = null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endBody(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endBody(RenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endTable(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endTable(RenderContext context) {
		mergeCells(context, sheet, mergedRegions);
		adjustColumnSizes(context, sheet);

		this.sheet = null;
	}

	/**
	 * @param context
	 * @return
	 */
	protected CellRangeAddress createMergedRegion(RenderContext context) {
		if (context.getColSpan() > 1 || context.getRowSpan() > 1) {
			int firstRow = context.getRowIndex() + getRowOffset();
			int lastRow = firstRow + context.getRowSpan() - 1;
			int firstCol = context.getColIndex() + getColOffset();
			int lastCol = firstCol + context.getColSpan() - 1;

			return new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
		} else {
			return null;
		}
	}

	/**
	 * @param context
	 * @param sheet
	 * @param regions
	 */
	protected void mergeCells(RenderContext context, Sheet sheet,
			List<CellRangeAddress> regions) {
		for (CellRangeAddress region : regions) {
			sheet.addMergedRegion(region);
		}
	}

	/**
	 * @param context
	 * @param sheet
	 */
	protected void adjustColumnSizes(RenderContext context, Sheet sheet) {
		for (int i = 0; i < context.getColumnCount(); i++) {
			try {
				sheet.autoSizeColumn(getColOffset() + i,
						!mergedRegions.isEmpty());
			} catch (Exception e) {
				// Ignore any problem while calculating size of the columns.
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getCellLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getCellLabel(RenderContext context) {
		String label = super.getCellLabel(context);

		if (!getShowParentMembers() && label != null
				&& context.getAxis() == Axis.ROWS
				&& context.getMember() != null && context.getCell() == null) {
			label = StringUtils.leftPad(label, context.getMember().getDepth()
					+ label.length());
		}

		return label;
	}
}
