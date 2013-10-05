/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.fop;

import static com.eyeq.pivot4j.ui.CellTypes.VALUE;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.FOElementMapping;
import org.olap4j.Axis;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.AbstractContentRenderCallback;
import com.eyeq.pivot4j.ui.command.UICommand;
import com.eyeq.pivot4j.ui.table.TableRenderCallback;
import com.eyeq.pivot4j.ui.table.TableRenderContext;

public class FopExporter extends
		AbstractContentRenderCallback<TableRenderContext> implements
		TableRenderCallback {

	private FopFactory fopFactory;

	private FOUserAgent userAgent;

	private DefaultHandler documentHandler;

	private MediaSize mediaSize = MediaSize
			.getMediaSizeForName(MediaSizeName.ISO_A4);

	private boolean showHeader = true;

	private boolean showFooter = true;

	private String titleText;

	private String footerText;

	private String fontSize = "8pt";

	private String fontFamily = "Verdana, Geneva, Arial, Helvetica, sans-serif";

	private String titleFontSize = "10pt";

	private String titleFontFamily = "Verdana, Geneva, Arial, Helvetica, sans-serif";

	private String footerFontSize = "10pt";

	private String footerFontFamily = "Verdana, Geneva, Arial, Helvetica, sans-serif";

	private OrientationRequested orientation = OrientationRequested.PORTRAIT;

	/**
	 * @param out
	 */
	public FopExporter(OutputStream out) {
		super(out);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractRenderCallback#getContentType()
	 */
	@Override
	public String getContentType() {
		return MimeConstants.MIME_PDF;
	}

	/**
	 * @return the mediaSize
	 */
	public MediaSize getMediaSize() {
		return mediaSize;
	}

	/**
	 * @param mediaSize
	 *            the mediaSize to set
	 */
	public void setMediaSize(MediaSize mediaSize) {
		this.mediaSize = mediaSize;
	}

	/**
	 * @return the orientation
	 */
	public OrientationRequested getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation
	 *            the orientation to set
	 */
	public void setOrientation(OrientationRequested orientation) {
		this.orientation = orientation;
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
	 * @return the titleText
	 */
	public String getTitleText() {
		return titleText;
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
	 * @param titleText
	 *            the titleText to set
	 */
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	/**
	 * @return the fontSize
	 */
	public String getFontSize() {
		return fontSize;
	}

	/**
	 * @param fontSize
	 *            the fontSize to set
	 */
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
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
	 * @return the titleFontSize
	 */
	public String getTitleFontSize() {
		return titleFontSize;
	}

	/**
	 * @param titleFontSize
	 *            the titleFontSize to set
	 */
	public void setTitleFontSize(String titleFontSize) {
		this.titleFontSize = titleFontSize;
	}

	/**
	 * @return the titleFontFamily
	 */
	public String getTitleFontFamily() {
		return titleFontFamily;
	}

	/**
	 * @param titleFontFamily
	 *            the titleFontFamily to set
	 */
	public void setTitleFontFamily(String titleFontFamily) {
		this.titleFontFamily = titleFontFamily;
	}

	/**
	 * @return the footerFontSize
	 */
	public String getFooterFontSize() {
		return footerFontSize;
	}

	/**
	 * @param footerFontSize
	 *            the footerFontSize to set
	 */
	public void setFooterFontSize(String footerFontSize) {
		this.footerFontSize = footerFontSize;
	}

	/**
	 * @return the footerFontFamily
	 */
	public String getFooterFontFamily() {
		return footerFontFamily;
	}

	/**
	 * @param footerFontFamily
	 *            the footerFontFamily to set
	 */
	public void setFooterFontFamily(String footerFontFamily) {
		this.footerFontFamily = footerFontFamily;
	}

	protected FopFactory createFopFactory() {
		return FopFactory.newInstance();
	}

	/**
	 * @return the fopFactory
	 */
	protected FopFactory getFopFactory() {
		return fopFactory;
	}

	/**
	 * @param factory
	 * @return
	 */
	protected FOUserAgent createUserAgent(FopFactory factory) {
		return factory.newFOUserAgent();
	}

	/**
	 * @return the userAgent
	 */
	protected FOUserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * @param factory
	 * @param agent
	 * @param out
	 * @return
	 * @throws FOPException
	 */
	protected Fop createFop(FopFactory factory, FOUserAgent agent,
			OutputStream out) throws FOPException {
		return factory.newFop(getContentType(), agent, out);
	}

	/**
	 * @return the documentHandler
	 */
	protected DefaultHandler getDocumentHandler() {
		return documentHandler;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#startRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRender(TableRenderContext context) {
		this.fopFactory = createFopFactory();
		this.userAgent = createUserAgent(fopFactory);

		try {
			Fop fop = createFop(getFopFactory(), getUserAgent(),
					getOutputStream());

			this.documentHandler = fop.getDefaultHandler();

			this.documentHandler.startDocument();
			this.documentHandler.startElement(FOElementMapping.URI, "root",
					"root", new AttributesImpl());

			startLayoutDefinition(context);
			endLayoutDefinition(context);

			startPageSequence(context);
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startTable(TableRenderContext context) {
		try {
			this.documentHandler.startElement(FOElementMapping.URI, "table",
					"table", createTableAttributes(context));
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startHeader(TableRenderContext context) {
		try {
			this.documentHandler.startElement(FOElementMapping.URI,
					"table-header", "table-header",
					createTableHeaderAttributes(context));
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endHeader(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endHeader(TableRenderContext context) {
		try {
			if (context.getAxis() == Axis.FILTER) {
				this.documentHandler.startElement(FOElementMapping.URI,
						"table-row", "table-row", new AttributesImpl());

				this.documentHandler.startElement(FOElementMapping.URI,
						"table-cell", "table-cell", new AttributesImpl());

				this.documentHandler.startElement(FOElementMapping.URI,
						"block", "block", createTitleTextAttributes(context));

				String title = context.getResourceBundle().getString(
						"label.filter");

				this.documentHandler.characters(title.toCharArray(), 0,
						title.length());

				this.documentHandler.endElement(FOElementMapping.URI, "block",
						"block");

				this.documentHandler.endElement(FOElementMapping.URI,
						"table-cell", "table-cell");
				this.documentHandler.endElement(FOElementMapping.URI,
						"table-row", "table-row");
			}

			this.documentHandler.endElement(FOElementMapping.URI,
					"table-header", "table-header");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startBody(TableRenderContext context) {
		try {
			this.documentHandler.startElement(FOElementMapping.URI,
					"table-body", "table-body",
					createTableBodyAttributes(context));
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startRow(TableRenderContext context) {
		try {
			this.documentHandler.startElement(FOElementMapping.URI,
					"table-row", "table-row", createRowAttributes(context));
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#startCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startCell(TableRenderContext context) {
		try {
			this.documentHandler.startElement(FOElementMapping.URI,
					"table-cell", "table-cell", createCellAttributes(context));
		} catch (SAXException e) {
			throw new PivotException(e);
		}
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
		try {
			this.documentHandler.startElement(FOElementMapping.URI, "block",
					"block", createCellContentAttributes(context));
			if (label != null) {
				this.documentHandler.characters(label.toCharArray(), 0,
						label.length());
			}
			this.documentHandler.endElement(FOElementMapping.URI, "block",
					"block");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endCell(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endCell(TableRenderContext context) {
		try {
			this.documentHandler.endElement(FOElementMapping.URI, "table-cell",
					"table-cell");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endRow(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endRow(TableRenderContext context) {
		try {
			this.documentHandler.endElement(FOElementMapping.URI, "table-row",
					"table-row");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endBody(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endBody(TableRenderContext context) {
		try {
			this.documentHandler.endElement(FOElementMapping.URI, "table-body",
					"table-body");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.table.TableRenderCallback#endTable(com.eyeq.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endTable(TableRenderContext context) {
		try {
			this.documentHandler.endElement(FOElementMapping.URI, "table",
					"table");
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#endRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRender(TableRenderContext context) {
		try {
			endPageSequence(context);

			this.documentHandler.endElement(FOElementMapping.URI, "root",
					"root");
			this.documentHandler.endDocument();
		} catch (SAXException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void startLayoutDefinition(TableRenderContext context)
			throws SAXException {
		this.documentHandler.startElement(FOElementMapping.URI,
				"layout-master-set", "layout-master-set",
				createLayoutMasterSetAttributes(context));
		this.documentHandler.startElement(FOElementMapping.URI,
				"simple-page-master", "simple-page-master",
				createPageMasterAttributes(context));

		this.documentHandler.startElement(FOElementMapping.URI, "region-body",
				"region-body", createRegionBodyAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createLayoutMasterSetAttributes(
			TableRenderContext context) {
		return new AttributesImpl();
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createPageMasterAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "master-name", "master-name", "CDATA",
				"content");

		String width = mediaSize.getX(Size2DSyntax.MM) + "mm";
		String height = mediaSize.getY(Size2DSyntax.MM) + "mm";

		if (orientation.equals(OrientationRequested.LANDSCAPE)
				|| orientation.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
			attributes.addAttribute("", "page-width", "page-width", "CDATA",
					height);
			attributes.addAttribute("", "page-height", "page-height", "CDATA",
					width);
		} else if (orientation.equals(OrientationRequested.PORTRAIT)
				|| orientation.equals(OrientationRequested.REVERSE_PORTRAIT)) {
			attributes.addAttribute("", "page-width", "page-width", "CDATA",
					width);
			attributes.addAttribute("", "page-height", "page-height", "CDATA",
					height);
		} else {
			assert false;
		}

		attributes.addAttribute("", "margin", "margin", "CDATA", "1cm");

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createRegionBeforeAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "extent", "extent", "CDATA", "1cm");

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createRegionAfterAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "extent", "extent", "CDATA", "0.5cm");

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createRegionBodyAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		if (getShowHeader()) {
			attributes.addAttribute("", "margin-top", "margin-top", "CDATA",
					"1.5cm");
		}

		if (getShowFooter()) {
			attributes.addAttribute("", "margin-bottom", "margin-bottom",
					"CDATA", "1.5cm");
		}

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createFlowAttributes(TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "flow-name", "flow-name", "CDATA",
				"xsl-region-body");

		return attributes;
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void endLayoutDefinition(TableRenderContext context)
			throws SAXException {
		this.documentHandler.endElement(FOElementMapping.URI, "region-body",
				"region-body");

		if (getShowHeader()) {
			this.documentHandler.startElement(FOElementMapping.URI,
					"region-before", "region-before",
					createRegionBeforeAttributes(context));
			this.documentHandler.endElement(FOElementMapping.URI,
					"region-before", "region-before");
		}

		if (getShowFooter()) {
			this.documentHandler.startElement(FOElementMapping.URI,
					"region-after", "region-after",
					createRegionAfterAttributes(context));
			this.documentHandler.endElement(FOElementMapping.URI,
					"region-after", "region-after");
		}

		this.documentHandler.endElement(FOElementMapping.URI,
				"simple-page-master", "simple-page-master");

		this.documentHandler.endElement(FOElementMapping.URI,
				"layout-master-set", "layout-master-set");
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void startPageSequence(TableRenderContext context)
			throws SAXException {
		this.documentHandler.startElement(FOElementMapping.URI,
				"page-sequence", "page-sequence",
				createPageSequenceAttributes(context));

		if (getShowHeader()) {
			AttributesImpl attributes = new AttributesImpl();
			attributes.addAttribute("", "flow-name", "flow-name", "CDATA",
					"xsl-region-before");

			this.documentHandler.startElement(FOElementMapping.URI,
					"static-content", "static-content", attributes);

			startHeaderBlock(context);
			endHeaderBlock(context);

			this.documentHandler.endElement(FOElementMapping.URI,
					"static-content", "static-content");
		}

		if (getShowFooter()) {
			AttributesImpl attributes = new AttributesImpl();
			attributes.addAttribute("", "flow-name", "flow-name", "CDATA",
					"xsl-region-after");

			this.documentHandler.startElement(FOElementMapping.URI,
					"static-content", "static-content", attributes);

			startFooterBlock(context);
			createFooterContent(context);
			endFooterBlock(context);

			this.documentHandler.endElement(FOElementMapping.URI,
					"static-content", "static-content");
		}

		this.documentHandler.startElement(FOElementMapping.URI, "flow", "flow",
				createFlowAttributes(context));

		if (getShowHeader()) {
			createHeaderContent(context);
		}
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void createHeaderContent(TableRenderContext context)
			throws SAXException {
		PivotModel model = context.getModel();

		AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute("", "marker-class-name", "marker-class-name",
				"CDATA", "page-head");

		this.documentHandler.startElement(FOElementMapping.URI, "marker",
				"marker", attributes);

		AttributesImpl tableAttrs = new AttributesImpl();
		tableAttrs.addAttribute("", "width", "width", "CDATA", "100%");
		tableAttrs.addAttribute("", "table-layout", "table-layout", "CDATA",
				"fixed");

		this.documentHandler.startElement(FOElementMapping.URI, "table",
				"table", tableAttrs);

		AttributesImpl colAttrs1 = new AttributesImpl();
		colAttrs1.addAttribute("", "width", "width", "CDATA",
				"proportional-column-width(4)");
		AttributesImpl colAttrs2 = new AttributesImpl();
		colAttrs1.addAttribute("", "width", "width", "CDATA",
				"proportional-column-width(1)");

		this.documentHandler.startElement(FOElementMapping.URI, "table-column",
				"table-column", colAttrs1);
		this.documentHandler.endElement(FOElementMapping.URI, "table-column",
				"table-column");
		this.documentHandler.startElement(FOElementMapping.URI, "table-column",
				"table-column", colAttrs2);
		this.documentHandler.endElement(FOElementMapping.URI, "table-column",
				"table-column");

		this.documentHandler.startElement(FOElementMapping.URI, "table-body",
				"table-body", new AttributesImpl());

		this.documentHandler.startElement(FOElementMapping.URI, "table-row",
				"table-row", new AttributesImpl());

		this.documentHandler.startElement(FOElementMapping.URI, "table-cell",
				"table-cell", new AttributesImpl());

		this.documentHandler.startElement(FOElementMapping.URI, "block",
				"block", createTitleTextAttributes(context));

		String title = getTitleText();
		if (title == null) {
			title = model.getCube().getCaption();
		}

		this.documentHandler.characters(title.toCharArray(), 0, title.length());

		this.documentHandler.endElement(FOElementMapping.URI, "block", "block");

		this.documentHandler.endElement(FOElementMapping.URI, "table-cell",
				"table-cell");

		this.documentHandler.startElement(FOElementMapping.URI, "table-cell",
				"table-cell", new AttributesImpl());

		AttributesImpl cellAttrs2 = new AttributesImpl();
		cellAttrs2.addAttribute("", "text-align", "text-align", "CDATA",
				"right");

		this.documentHandler.startElement(FOElementMapping.URI, "block",
				"block", cellAttrs2);

		this.documentHandler.characters("Page ".toCharArray(), 0, 5);

		this.documentHandler.startElement(FOElementMapping.URI, "page-number",
				"page-number", new AttributesImpl());
		this.documentHandler.endElement(FOElementMapping.URI, "page-number",
				"page-number");

		this.documentHandler.endElement(FOElementMapping.URI, "block", "block");

		this.documentHandler.endElement(FOElementMapping.URI, "table-cell",
				"table-cell");

		this.documentHandler.endElement(FOElementMapping.URI, "table-row",
				"table-row");

		this.documentHandler.endElement(FOElementMapping.URI, "table-body",
				"table-body");

		this.documentHandler.endElement(FOElementMapping.URI, "table", "table");

		this.documentHandler.endElement(FOElementMapping.URI, "marker",
				"marker");
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createTitleTextAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes
				.addAttribute("", "text-align", "text-align", "CDATA", "left");

		if (titleFontSize != null) {
			attributes.addAttribute("", "font-size", "font-size", "CDATA",
					titleFontSize);
		}

		if (titleFontFamily != null) {
			attributes.addAttribute("", "font-family", "font-family", "CDATA",
					titleFontFamily);
		}

		return attributes;
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void createFooterContent(TableRenderContext context)
			throws SAXException {
		this.documentHandler.startElement(FOElementMapping.URI, "block",
				"block", createFooterTextAttributes(context));

		String footer = getFooterText();
		if (footer == null) {
			footer = DateFormat.getDateTimeInstance().format(new Date());
		}

		this.documentHandler.characters(footer.toCharArray(), 0,
				footer.length());

		this.documentHandler.endElement(FOElementMapping.URI, "block", "block");
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createFooterTextAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "text-align", "text-align", "CDATA",
				"right");

		if (footerFontSize != null) {
			attributes.addAttribute("", "font-size", "font-size", "CDATA",
					footerFontSize);
		}

		if (footerFontFamily != null) {
			attributes.addAttribute("", "font-family", "font-family", "CDATA",
					footerFontFamily);
		}

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createPageSequenceAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "master-reference", "master-reference",
				"CDATA", "content");

		return attributes;
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void startHeaderBlock(TableRenderContext context)
			throws SAXException {
		this.documentHandler.startElement(FOElementMapping.URI, "block",
				"block", createHeaderBlockAttributes(context));

		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "retrieve-class-name",
				"retrieve-class-name", "CDATA", "page-head");

		this.documentHandler.startElement(FOElementMapping.URI,
				"retrieve-marker", "retrieve-marker", attributes);
		this.documentHandler.endElement(FOElementMapping.URI,
				"retrieve-marker", "retrieve-marker");
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createHeaderBlockAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "border-bottom", "border-bottom", "CDATA",
				"1pt solid black");

		if (fontSize != null) {
			attributes.addAttribute("", "font-size", "font-size", "CDATA",
					fontSize);
		}

		attributes.addAttribute("", "font-weight", "font-weight", "CDATA",
				"bold");
		attributes.addAttribute("", "padding-top", "padding-top", "CDATA",
				"2mm");
		attributes.addAttribute("", "padding-bottom", "padding-bottom",
				"CDATA", "2mm");

		return attributes;
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void endHeaderBlock(TableRenderContext context)
			throws SAXException {
		this.documentHandler.endElement(FOElementMapping.URI, "block", "block");
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void startFooterBlock(TableRenderContext context)
			throws SAXException {
		this.documentHandler.startElement(FOElementMapping.URI, "block",
				"block", createFooterBlockAttributes(context));
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createFooterBlockAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "border-top", "border-top", "CDATA",
				"1pt solid black");
		attributes.addAttribute("", "padding-top", "padding-top", "CDATA",
				"2mm");
		attributes.addAttribute("", "padding-bottom", "padding-bottom",
				"CDATA", "2mm");

		return attributes;
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void endFooterBlock(TableRenderContext context)
			throws SAXException {
		this.documentHandler.endElement(FOElementMapping.URI, "block", "block");
	}

	/**
	 * @param context
	 * @throws SAXException
	 */
	protected void endPageSequence(TableRenderContext context)
			throws SAXException {
		this.documentHandler.endElement(FOElementMapping.URI, "flow", "flow");

		this.documentHandler.endElement(FOElementMapping.URI, "page-sequence",
				"page-sequence");
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createTableAttributes(TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		attributes.addAttribute("", "width", "width", "CDATA", "100%");
		attributes.addAttribute("", "table-layout", "table-layout", "CDATA",
				"fixed");

		if (context.getAxis() == Axis.FILTER) {
			attributes.addAttribute("", "margin-top", "margin-top", "CDATA",
					"1.0em");
		}

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createTableHeaderAttributes(
			TableRenderContext context) {
		return new AttributesImpl();
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createTableBodyAttributes(
			TableRenderContext context) {
		return new AttributesImpl();
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createRowAttributes(TableRenderContext context) {
		return new AttributesImpl();
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createCellAttributes(TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		if (context.getColumnSpan() > 1) {
			attributes.addAttribute("", "number-columns-spanned",
					"number-columns-spanned", "CDATA",
					Integer.toString(context.getColumnSpan()));
		}

		if (context.getRowSpan() > 1) {
			attributes.addAttribute("", "number-rows-spanned",
					"number-rows-spanned", "CDATA",
					Integer.toString(context.getRowSpan()));
		}

		if (context.getCell() == null && !VALUE.equals(context.getCellType())) {
			attributes.addAttribute("", "background-color", "background-color",
					"CDATA", "#DEDEDE");
			attributes.addAttribute("", "font-weight", "font-weight", "CDATA",
					"bold");
		} else {
			if (context.getRowIndex() % 2 == 1) {
				attributes.addAttribute("", "background-color",
						"background-color", "CDATA", "#EFF2F5");
			}
		}

		attributes.addAttribute("", "border", "border", "CDATA",
				"1px solid black");
		attributes.addAttribute("", "padding-top", "padding-top", "CDATA",
				"1mm");
		attributes.addAttribute("", "padding-left", "padding-left", "CDATA",
				"1mm");
		attributes.addAttribute("", "padding-bottom", "padding-bottom",
				"CDATA", "1mm");
		attributes.addAttribute("", "padding-right", "padding-right", "CDATA",
				"1mm");

		if (fontSize != null) {
			attributes.addAttribute("", "font-size", "font-size", "CDATA",
					fontSize);
		}

		if (fontFamily != null) {
			attributes.addAttribute("", "font-family", "font-family", "CDATA",
					fontFamily);
		}

		return attributes;
	}

	/**
	 * @param context
	 * @return
	 */
	protected AttributesImpl createCellContentAttributes(
			TableRenderContext context) {
		AttributesImpl attributes = new AttributesImpl();

		if (VALUE.equals(context.getCellType())) {
			attributes.addAttribute("", "text-align", "text-align", "CDATA",
					"right");
		}

		return attributes;
	}
}
