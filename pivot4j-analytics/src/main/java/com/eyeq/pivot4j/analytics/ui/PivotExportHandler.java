package com.eyeq.pivot4j.analytics.ui;

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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.export.fop.FopExporter;
import com.eyeq.pivot4j.export.poi.ExcelExporter;
import com.eyeq.pivot4j.export.poi.Format;
import com.eyeq.pivot4j.ui.PivotRenderer;

@ManagedBean(name = "pivotExportHandler")
@RequestScoped
public class PivotExportHandler {

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	@ManagedProperty(value = "#{pivotGridHandler}")
	private PivotGridHandler gridHandler;

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
	 * @return the gridHandler
	 */
	public PivotGridHandler getGridHandler() {
		return gridHandler;
	}

	/**
	 * @param gridHandler
	 *            the gridHandler to set
	 */
	public void setGridHandler(PivotGridHandler gridHandler) {
		this.gridHandler = gridHandler;
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
	public List<SelectItem> getPaperSizes() throws IllegalArgumentException,
			IllegalAccessException {
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

			for (Orientation orientation : Orientation.values()) {
				String label;

				try {
					label = bundle
							.getString("label.pdf_export.page.orientation."
									+ orientation.name().toLowerCase());
				} catch (MissingResourceException e) {
					label = orientation.name();
				}

				orientations.add(new SelectItem(orientation, label));
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
		HierarchicalConfiguration configuration = new HierarchicalConfiguration();

		PivotRenderer renderer = gridHandler.getRenderer();
		renderer.saveSettings(configuration);

		ExcelExporter exporter = new ExcelExporter();
		exporter.restoreSettings(configuration);

		FacesContext context = FacesContext.getCurrentInstance();

		String disposition = String.format("attachment; filename=\"%s.%s\"",
				model.getCube().getName(), format.getExtension());

		ExternalContext externalContext = context.getExternalContext();
		externalContext.setResponseHeader("Content-Disposition", disposition);
		externalContext.setResponseContentType(exporter.getContentType());

		OutputStream out = externalContext.getResponseOutputStream();

		try {
			exporter.setFormat(format);
			exporter.setOutputStream(out);
			exporter.initialize();

			exporter.render(model);
		} finally {
			out.flush();
			IOUtils.closeQuietly(out);
		}
	}

	public void exportPdf() throws IOException, IllegalArgumentException,
			IllegalAccessException {
		HierarchicalConfiguration configuration = new HierarchicalConfiguration();

		PivotRenderer renderer = gridHandler.getRenderer();
		renderer.saveSettings(configuration);

		FopExporter exporter = new FopExporter();
		exporter.restoreSettings(configuration);

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

		FacesContext context = FacesContext.getCurrentInstance();

		String disposition = String.format("attachment; filename=\"%s.%s\"",
				model.getCube().getName(), "pdf");

		ExternalContext externalContext = context.getExternalContext();
		externalContext.setResponseHeader("Content-Disposition", disposition);
		externalContext.setResponseContentType(exporter.getContentType());

		OutputStream out = externalContext.getResponseOutputStream();

		try {
			exporter.setOutputStream(out);
			exporter.initialize();

			exporter.render(model);
		} finally {
			out.flush();
			IOUtils.closeQuietly(out);
		}

		context.responseComplete();
	}
}
