package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.fop.FopExporter;
import org.pivot4j.ui.poi.ExcelExporter;
import org.pivot4j.ui.poi.Format;
import org.pivot4j.ui.table.TableRenderer;

@ManagedBean(name = "pivotExportHandler")
@RequestScoped
public class PivotExportHandler {

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	@ManagedProperty(value = "#{viewHandler}")
	private ViewHandler viewHandler;

	private boolean showHeader = true;

	private String headerText;

	private boolean showFooter = true;

	private String footerText;

	private int paperSize = MediaSizeName.ISO_A4.getValue();

	private List<SelectItem> paperSizes;

	private Orientation orientation = Orientation.Portrait;

	private List<SelectItem> orientations;

	private int fontSize = 8;

	private int headerFontSize = 10;

	private int footerFontSize = 10;

	public enum Orientation {
		Portrait {
			@Override
			OrientationRequested getValue() {
				return OrientationRequested.PORTRAIT;
			}
		},
		Landscape {
			@Override
			OrientationRequested getValue() {
				return OrientationRequested.LANDSCAPE;
			}
		};

		abstract OrientationRequested getValue();
	};

	/**
	 * @return the viewHandler
	 */
	public ViewHandler getViewHandler() {
		return viewHandler;
	}

	/**
	 * @param viewHandler
	 *            the viewHandler to set
	 */
	public void setViewHandler(ViewHandler viewHandler) {
		this.viewHandler = viewHandler;
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(PivotModel model) {
		this.model = model;
	}

	/**
	 * @return the showHeader
	 */
	public boolean getShowHeader() {
		return showHeader;
	}

	/**
	 * @param showHeader
	 *            the showHeader to set
	 */
	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	/**
	 * @return the headerText
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * @param headerText
	 *            the headerText to set
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * @return the showFooter
	 */
	public boolean getShowFooter() {
		return showFooter;
	}

	/**
	 * @param showFooter
	 *            the showFooter to set
	 */
	public void setShowFooter(boolean showFooter) {
		this.showFooter = showFooter;
	}

	/**
	 * @return the footerText
	 */
	public String getFooterText() {
		return footerText;
	}

	/**
	 * @param footerText
	 *            the footerText to set
	 */
	public void setFooterText(String footerText) {
		this.footerText = footerText;
	}

	/**
	 * @return the paperSize
	 */
	public int getPaperSize() {
		return paperSize;
	}

	/**
	 * @param paperSize
	 *            the paperSize to set
	 */
	public void setPaperSize(int paperSize) {
		this.paperSize = paperSize;
	}

	/**
	 * @return the paperSizes
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public List<SelectItem> getPaperSizes() throws IllegalAccessException {
		if (paperSizes == null) {
			this.paperSizes = new ArrayList<SelectItem>();

			Field[] fields = MediaSizeName.class.getFields();
			for (Field field : fields) {
				String name = field.getName();
				MediaSizeName media = (MediaSizeName) field.get(null);
				paperSizes.add(new SelectItem(
						Integer.toString(media.getValue()), name));
			}
		}

		return paperSizes;
	}

	/**
	 * @return the orientation
	 */
	public Orientation getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation
	 *            the orientation to set
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	/**
	 * @return the orientations
	 */
	public List<SelectItem> getOrientations() {
		if (orientations == null) {
			FacesContext context = FacesContext.getCurrentInstance();

			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			this.orientations = new ArrayList<SelectItem>();

			for (Orientation orient : Orientation.values()) {
				String label;

				try {
					label = bundle
							.getString("label.pdfExport.page.orientation."
									+ orient.name().toLowerCase());
				} catch (MissingResourceException e) {
					label = orient.name();
				}

				orientations.add(new SelectItem(orient, label));
			}
		}

		return orientations;
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
	 * @return the headerFontSize
	 */
	public int getHeaderFontSize() {
		return headerFontSize;
	}

	/**
	 * @param headerFontSize
	 *            the headerFontSize to set
	 */
	public void setHeaderFontSize(int headerFontSize) {
		this.headerFontSize = headerFontSize;
	}

	/**
	 * @return the footerFontSize
	 */
	public int getFooterFontSize() {
		return footerFontSize;
	}

	/**
	 * @param footerFontSize
	 *            the footerFontSize to set
	 */
	public void setFooterFontSize(int footerFontSize) {
		this.footerFontSize = footerFontSize;
	}

	public void exportExcel() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();

		ExternalContext externalContext = context.getExternalContext();

		Map<String, String> parameters = externalContext
				.getRequestParameterMap();

		Format format;

		if (parameters.containsKey("format")) {
			format = Format.valueOf(parameters.get("format"));
		} else {
			format = Format.HSSF;
		}

		exportExcel(format);

		context.responseComplete();
	}

	/**
	 * @param format
	 * @throws IOException
	 */
	protected void exportExcel(Format format) throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();

		String disposition = String.format("attachment; filename=\"%s.%s\"",
				model.getCube().getName(), format.getExtension());

		ExternalContext externalContext = context.getExternalContext();
		externalContext.setResponseHeader("Content-Disposition", disposition);

		TableRenderer renderer = viewHandler.getRenderer();

		boolean renderSlicer = renderer.getRenderSlicer();
		boolean inline = renderer.getShowSlicerMembersInline();

		OutputStream out = externalContext.getResponseOutputStream();

		ExcelExporter exporter = new ExcelExporter(out);
		exporter.setFormat(format);

		externalContext.setResponseContentType(exporter.getContentType());

		try {
			renderer.setRenderSlicer(viewHandler.getRenderSlicer());
			renderer.setShowSlicerMembersInline(false);

			renderer.render(model, exporter);
		} finally {
			renderer.setRenderSlicer(renderSlicer);
			renderer.setShowSlicerMembersInline(inline);

			out.flush();
			IOUtils.closeQuietly(out);
		}
	}

	public void exportPdf() throws IOException, IllegalAccessException {
		TableRenderer renderer = viewHandler.getRenderer();

		FacesContext context = FacesContext.getCurrentInstance();

		String disposition = String.format("attachment; filename=\"%s.%s\"",
				model.getCube().getName(), "pdf");

		ExternalContext externalContext = context.getExternalContext();

		OutputStream out = externalContext.getResponseOutputStream();

		FopExporter exporter = new FopExporter(out);
		exporter.setShowHeader(showHeader);

		if (StringUtils.isNotBlank(headerText)) {
			exporter.setTitleText(headerText);
		}

		exporter.setShowFooter(showFooter);

		if (StringUtils.isNotBlank(footerText)) {
			exporter.setFooterText(footerText);
		}

		exporter.setFontSize(fontSize + "pt");
		exporter.setTitleFontSize(headerFontSize + "pt");
		exporter.setFooterFontSize(footerFontSize + "pt");
		exporter.setOrientation(orientation.getValue());

		MediaSize mediaSize = null;

		Field[] fields = MediaSizeName.class.getFields();
		for (Field field : fields) {
			MediaSizeName name = (MediaSizeName) field.get(null);
			if (name.getValue() == paperSize) {
				mediaSize = MediaSize.getMediaSizeForName(name);
				break;
			}
		}

		exporter.setMediaSize(mediaSize);

		externalContext.setResponseContentType(exporter.getContentType());
		externalContext.setResponseHeader("Content-Disposition", disposition);

		boolean renderSlicer = renderer.getRenderSlicer();

		try {
			renderer.setRenderSlicer(viewHandler.getRenderSlicer());

			renderer.render(model, exporter);
		} finally {
			renderer.setRenderSlicer(renderSlicer);

			out.flush();
			IOUtils.closeQuietly(out);
		}

		context.responseComplete();
	}
}
