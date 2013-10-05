/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.poi;

import static com.eyeq.pivot4j.ui.CellTypes.VALUE;

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
import com.eyeq.pivot4j.ui.AbstractContentRenderCallback;
import com.eyeq.pivot4j.ui.command.UICommand;
import com.eyeq.pivot4j.ui.table.TableRenderCallback;
import com.eyeq.pivot4j.ui.table.TableRenderContext;

public class ExcelExporter extends
		AbstractContentRenderCallback<TableRenderContext> implements
		TableRenderCallback {

	private Format format = Format.HSSF;

	private Workbook workbook;

	private Sheet sheet;

	private Row row;

	private Cell cell;

	private CellStyle headerCellStyle;

	private CellStyle valueCellStyle;

	private CellStyle aggregationCellStyle;

	private List<CellRangeAddress> mergedRegions;

	private String fontFamily = "Arial";

	private int fontSize = 10;

	private int sheetIndex = 0;

	/**
	 * @param out
	 */
	public ExcelExporter(OutputStream out) {
		super(out);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractRenderCallback#getContentType()
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

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#startRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRender(TableRenderContext context) {
		this.sheet = null;
		this.row = null;
		this.cell = null;
		this.mergedRegions = new ArrayList<CellRangeAddress>();
		this.sheetIndex = 0;

		this.headerCellStyle = null;
		this.valueCellStyle = null;

		this.workbook = createWorkbook();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startTable(TableRenderContext context) {
		this.sheet = createSheet(context, workbook);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startHeader(TableRenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endHeader(TableRenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startBody(TableRenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startRow(TableRenderContext context) {
		this.row = createRow(sheet, context.getRowIndex() + getRowOffset());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startCell(TableRenderContext context) {
		this.cell = createCell(row, context.getColumnIndex() + getColOffset());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#renderCommands(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.util.List)
	 */
	@Override
	public void renderCommands(TableRenderContext context,
			List<UICommand<?>> commands) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#renderContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void renderContent(TableRenderContext context, String label) {
		cell.setCellStyle(getCellStyle(context));

		if (VALUE.equals(context.getCellType())
				&& context.getAxis() != Axis.FILTER) {
			Double value = null;

			if (context.getAggregator() != null) {
				value = context.getAggregator().getValue(context);
			} else if (context.getCell() != null
					&& !context.getCell().isEmpty()) {
				try {
					value = context.getCell().getDoubleValue();
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}

			renderContent(context, value);
		} else {
			boolean showParentMembers = context.getRenderer()
					.getShowParentMembers();

			if (!showParentMembers && label != null
					&& context.getAxis() == Axis.ROWS
					&& context.getMember() != null && context.getCell() == null) {
				label = StringUtils.leftPad(label, context.getMember()
						.getDepth() + label.length());
			}

			cell.setCellValue(label);
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}
	}

	/**
	 * @param context
	 * @param value
	 */
	protected void renderContent(TableRenderContext context, Double value) {
		if (value == null) {
			cell.setCellValue("");
		} else {
			cell.setCellValue(value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endCell(TableRenderContext context) {
		this.cell = null;

		CellRangeAddress range = createMergedRegion(context);
		if (range != null) {
			mergedRegions.add(range);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endRow(TableRenderContext context) {
		this.row = null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endBody(TableRenderContext context) {
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endTable(TableRenderContext context) {
		mergeCells(context, sheet, mergedRegions);
		adjustColumnSizes(context, sheet);

		mergedRegions.clear();

		this.sheet = null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#endRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRender(TableRenderContext context) {
		try {
			workbook.write(getOutputStream());
		} catch (IOException e) {
			throw new PivotException(e);
		}
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
	 * @param context
	 * @param workbook
	 * @return
	 */
	protected Sheet createSheet(TableRenderContext context, Workbook workbook) {
		Sheet newSheet = workbook.createSheet(WorkbookUtil
				.createSafeSheetName(getSheetName(context, sheetIndex)));

		sheetIndex++;
		return newSheet;
	}

	/**
	 * @param context
	 * @param sheetIndex
	 * @return
	 */
	protected String getSheetName(TableRenderContext context, int sheetIndex) {
		String name;

		if (context.getAxis() == Axis.FILTER) {
			name = context.getResourceBundle().getString("label.filter");
		} else {
			name = context.getModel().getCube().getCaption();
		}

		return name;
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
	 * @param colIndex
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

	protected CellStyle createAggregationCellStyle() {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();

		font.setFontName(fontFamily);
		font.setFontHeightInPoints((short) fontSize);
		font.setBoldweight(Font.BOLDWEIGHT_NORMAL);

		style.setFont(font);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
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
		style.setDataFormat((short) 4);

		return style;
	}

	/**
	 * @return aggregationCellStyle
	 */
	protected CellStyle getAggregationCellStyle() {
		if (aggregationCellStyle == null) {
			this.aggregationCellStyle = createAggregationCellStyle();
		}

		return aggregationCellStyle;
	}

	/**
	 * @param context
	 * @return
	 */
	protected CellStyle getCellStyle(TableRenderContext context) {
		if (VALUE.equals(context.getCellType())) {
			if (context.getAggregator() == null) {
				return getValueCellStyle();
			} else {
				return getAggregationCellStyle();
			}
		} else {
			return getHeaderCellStyle();
		}
	}

	/**
	 * @param context
	 * @return
	 */
	protected CellRangeAddress createMergedRegion(TableRenderContext context) {
		if (context.getColumnSpan() > 1 || context.getRowSpan() > 1) {
			int firstRow = context.getRowIndex() + getRowOffset();
			int lastRow = firstRow + context.getRowSpan() - 1;
			int firstCol = context.getColumnIndex() + getColOffset();
			int lastCol = firstCol + context.getColumnSpan() - 1;

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
	protected void mergeCells(TableRenderContext context, Sheet sheet,
			List<CellRangeAddress> regions) {
		for (CellRangeAddress region : regions) {
			sheet.addMergedRegion(region);
		}
	}

	/**
	 * @param context
	 * @param sheet
	 */
	protected void adjustColumnSizes(TableRenderContext context, Sheet sheet) {
		for (int i = 0; i < context.getColumnCount(); i++) {
			try {
				sheet.autoSizeColumn(getColOffset() + i,
						!mergedRegions.isEmpty());
			} catch (Exception e) {
				// Ignore any problem while calculating size of the columns.
			}
		}
	}
}
