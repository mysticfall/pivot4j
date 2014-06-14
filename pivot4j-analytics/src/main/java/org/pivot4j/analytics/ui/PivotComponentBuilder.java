package org.pivot4j.analytics.ui;

import static org.pivot4j.ui.CellTypes.AGG_VALUE;
import static org.pivot4j.ui.CellTypes.LABEL;
import static org.pivot4j.ui.CellTypes.VALUE;
import static org.pivot4j.ui.table.TableCellTypes.FILL;
import static org.pivot4j.ui.table.TableCellTypes.TITLE;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.convert.DoubleConverter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.pivot4j.el.EvaluationFailedException;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.ui.AbstractRenderCallback;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.command.UICommandParameters;
import org.pivot4j.ui.table.TableRenderCallback;
import org.pivot4j.ui.table.TableRenderContext;
import org.pivot4j.util.CssWriter;
import org.pivot4j.util.RenderPropertyUtils;
import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inplace.Inplace;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.row.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PivotComponentBuilder extends
		AbstractRenderCallback<TableRenderContext> implements
		TableRenderCallback {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, String> iconMap;

	private UIComponent gridPanel;

	private UIComponent filterPanel;

	private FacesContext facesContext;

	private ExpressionFactory expressionFactory;

	private PanelGrid grid;

	private HtmlPanelGroup header;

	private Row row;

	private Column column;

	private int commandIndex = 0;

	private boolean scenarioEnabled = false;

	private String updateTarget;

	private static Map<String, StyleClassResolver> styleClassResolvers;

	static {
		styleClassResolvers = new HashMap<String, StyleClassResolver>();

		styleClassResolvers.put(VALUE, new ValueStyleClassResolver());
		styleClassResolvers.put(AGG_VALUE, new AggregationStyleClassResolver());

		StyleClassResolver titleStlyeResolver = new TitleStyleClassResolver();

		styleClassResolvers.put(LABEL, titleStlyeResolver);
		styleClassResolvers.put(TITLE, titleStlyeResolver);
		styleClassResolvers.put(FILL, titleStlyeResolver);
	}

	/**
	 * @param facesContext
	 */
	public PivotComponentBuilder(FacesContext facesContext) {
		this.facesContext = facesContext;

		if (facesContext != null) {
			Application application = facesContext.getApplication();

			this.expressionFactory = application.getExpressionFactory();
		}

		// Map command mode names to jQuery's predefined icon names. It can be
		// also done by CSS.
		this.iconMap = new HashMap<String, String>();

		iconMap.put("expandPosition-position", "ui-icon-plus");
		iconMap.put("collapsePosition-position", "ui-icon-minus");
		iconMap.put("expandMember-member", "ui-icon-plusthick");
		iconMap.put("collapseMember-member", "ui-icon-minusthick");
		iconMap.put("drillDown-replace", "ui-icon-arrowthick-1-e");
		iconMap.put("drillUp-replace", "ui-icon-arrowthick-1-n");
		iconMap.put("sort-basic-natural", "ui-icon-triangle-2-n-s");
		iconMap.put("sort-basic-other-up", "ui-icon-triangle-1-n");
		iconMap.put("sort-basic-other-down", "ui-icon-triangle-1-s");
		iconMap.put("sort-basic-current-up", "ui-icon-circle-triangle-n");
		iconMap.put("sort-basic-current-down", "ui-icon-circle-triangle-s");
		iconMap.put("drillThrough", "ui-icon-search");
	}

	/**
	 * @return the parent JSF component for the data grid.
	 */
	public UIComponent getGridPanel() {
		return gridPanel;
	}

	/**
	 * @param gridPanel
	 */
	public void setGridPanel(UIComponent gridPanel) {
		this.gridPanel = gridPanel;
	}

	/**
	 * @return the parent JSF component for the filter grid.
	 */
	public UIComponent getFilterPanel() {
		return filterPanel;
	}

	/**
	 * @param filterPanel
	 */
	public void setFilterPanel(UIComponent filterPanel) {
		this.filterPanel = filterPanel;
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#getContentType()
	 */
	@Override
	public String getContentType() {
		return null;
	}

	/**
	 * @return the updateTarget
	 */
	protected String getUpdateTarget() {
		return updateTarget;
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @see org.pivot4j.ui.AbstractRenderCallback#startRender(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRender(TableRenderContext context) {
		super.startRender(context);

		ResourceBundle resources = facesContext.getApplication()
				.getResourceBundle(facesContext, "msg");
		context.setResourceBundle(resources);

		getRenderPropertyUtils().setSuppressErrors(true);

		this.commandIndex = 0;
		this.scenarioEnabled = context.getModel().isScenarioSupported()
				&& context.getModel().getScenario() != null;

		gridPanel.getFacets().clear();
		gridPanel.getChildren().clear();

		filterPanel.getFacets().clear();
		filterPanel.getChildren().clear();

		List<String> targets = new LinkedList<String>();

		targets.add(":grid-form");

		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();

		if (viewRoot.findComponent("editor-form") != null) {
			targets.add(":editor-form:mdx-editor");
			targets.add(":editor-form:editor-toolbar");
		}

		if (viewRoot.findComponent("source-tree-form") != null) {
			targets.add(":source-tree-form");
		}

		if (viewRoot.findComponent("target-tree-form") != null) {
			targets.add(":target-tree-form");
		}

		this.updateTarget = StringUtils.join(targets, ",");
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#startTable(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startTable(TableRenderContext context) {
		this.grid = new PanelGrid();

		if (context.getAxis() == Axis.FILTER) {
			grid.setStyleClass("filter-grid");
		} else {
			grid.setStyleClass("pivot-grid");
		}
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#startHeader(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startHeader(TableRenderContext context) {
		this.header = new HtmlPanelGroup();
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#endHeader(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endHeader(TableRenderContext context) {
		if (header.getChildCount() > 0) {
			grid.getFacets().put("header", header);
		}

		this.header = null;
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#startBody(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startBody(TableRenderContext context) {
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#startRow(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startRow(TableRenderContext context) {
		this.row = new Row();
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#startCell(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void startCell(TableRenderContext context) {
		this.column = new Column();

		String id = "col-" + column.hashCode();

		column.setId(id);
		column.setColspan(context.getColumnSpan());
		column.setRowspan(context.getRowSpan());

		RenderPropertyUtils propertyUtils = getRenderPropertyUtils();

		String propertyCategory = context.getRenderPropertyCategory();

		StringWriter writer = new StringWriter();
		CssWriter cssWriter = new CssWriter(writer);

		String type = context.getCellType();

		if (type.equals(LABEL) && !context.getRenderer().getShowParentMembers()
				&& context.getMember() != null
				&& context.getAxis() != Axis.FILTER) {
			int padding = context.getMember().getDepth() * 10;
			cssWriter.writeStyle("padding-left", padding + "px");
		}

		String fgColor = propertyUtils.getString("fgColor", propertyCategory,
				null);

		if (fgColor != null) {
			cssWriter.writeStyle("color", fgColor);
		}

		String bgColor = propertyUtils.getString("bgColor", propertyCategory,
				null);

		if (bgColor != null) {
			cssWriter.writeStyle("background-color", bgColor);
			cssWriter.writeStyle("background-image", "none");
		}

		String fontFamily = propertyUtils.getString("fontFamily",
				propertyCategory, null);

		if (fontFamily != null) {
			cssWriter.writeStyle("font-family", fontFamily);
		}

		String fontSize = propertyUtils.getString("fontSize", propertyCategory,
				null);

		if (fontSize != null) {
			cssWriter.writeStyle("font-size", fontSize);
		}

		String fontStyle = propertyUtils.getString("fontStyle",
				propertyCategory, null);

		if (fontStyle != null) {
			if (fontStyle.contains("bold")) {
				cssWriter.writeStyle("font-weight", "bold");
			}

			if (fontStyle.contains("italic")) {
				cssWriter.writeStyle("font-style", "oblique");
			}
		}

		writer.flush();

		IOUtils.closeQuietly(writer);

		String style = writer.toString();

		if (StringUtils.isNotEmpty(style)) {
			column.setStyle(style);
		}

		String styleClass = getStyleClass(context);
		String styleClassProperty = propertyUtils.getString("styleClass",
				propertyCategory, null);

		if (styleClassProperty != null) {
			if (styleClass == null) {
				styleClass = styleClassProperty;
			} else {
				styleClass += " " + styleClassProperty;
			}
		}

		column.setStyleClass(styleClass);
	}

	/**
	 * @param context
	 * @return
	 */
	protected String getStyleClass(TableRenderContext context) {
		String styleClass = null;

		String type = context.getCellType();

		StyleClassResolver resolver;

		if (LABEL.equals(type) && context.getAxis() == Axis.FILTER) {
			resolver = styleClassResolvers.get(VALUE);
		} else {
			resolver = styleClassResolvers.get(type);
		}

		if (resolver != null) {
			styleClass = resolver.resolve(context);
		}

		return styleClass;
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#renderCommands(org.pivot4j.ui.RenderContext,
	 *      java.util.List)
	 */
	@Override
	public void renderCommands(TableRenderContext context,
			List<UICommand<?>> commands) {
		if (expressionFactory != null) {
			for (UICommand<?> command : commands) {
				UICommandParameters parameters = command
						.createParameters(context);

				CommandButton button = new CommandButton();

				// JSF requires an unique id for command components.
				button.setId("btn-" + commandIndex++);

				button.setTitle(command.getDescription());

				String icon = null;

				String mode = command.getMode(context);
				if (mode == null) {
					icon = iconMap.get(command.getName());
				} else {
					icon = iconMap.get(command.getName() + "-" + mode);
				}

				button.setIcon(icon);

				MethodExpression expression = expressionFactory
						.createMethodExpression(facesContext.getELContext(),
								"#{viewHandler.executeCommand}", Void.class,
								new Class<?>[0]);
				button.setActionExpression(expression);
				button.setUpdate(updateTarget);
				button.setOncomplete("onViewChanged()");
				button.setProcess("@this");

				UIParameter commandParam = new UIParameter();
				commandParam.setName("command");
				commandParam.setValue(command.getName());
				button.getChildren().add(commandParam);

				UIParameter axisParam = new UIParameter();
				axisParam.setName("axis");
				axisParam.setValue(parameters.getAxisOrdinal());
				button.getChildren().add(axisParam);

				UIParameter positionParam = new UIParameter();
				positionParam.setName("position");
				positionParam.setValue(parameters.getPositionOrdinal());
				button.getChildren().add(positionParam);

				UIParameter memberParam = new UIParameter();
				memberParam.setName("member");
				memberParam.setValue(parameters.getMemberOrdinal());
				button.getChildren().add(memberParam);

				UIParameter hierarchyParam = new UIParameter();
				hierarchyParam.setName("hierarchy");
				hierarchyParam.setValue(parameters.getHierarchyOrdinal());
				button.getChildren().add(hierarchyParam);

				UIParameter cellParam = new UIParameter();
				cellParam.setName("cell");
				cellParam.setValue(parameters.getCellOrdinal());
				button.getChildren().add(cellParam);

				column.getChildren().add(button);
			}
		}
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#renderContent(org.pivot4j.ui.RenderContext,
	 *      java.lang.String, java.lang.Double)
	 */
	@Override
	public void renderContent(TableRenderContext context, String label,
			Double value) {
		ExpressionContext elContext = context.getExpressionContext();

		elContext.put("label", label);
		elContext.put("value", value);

		String labelText;

		RenderPropertyUtils propertyUtils = getRenderPropertyUtils();

		try {
			labelText = StringUtils.defaultIfEmpty(
					propertyUtils.getString("label",
							context.getRenderPropertyCategory(), label), "");
		} finally {
			elContext.remove("label");
			elContext.remove("value");
		}

		Cell cell = context.getCell();

		if (scenarioEnabled && context.getCellType().equals(VALUE)
				&& cell != null) {
			Inplace inplace = new Inplace();
			inplace.setId("inplace-" + context.getCell().getOrdinal());
			inplace.setLabel(labelText);
			inplace.setEditor(true);

			InputText input = new InputText();
			input.setId("input-" + context.getCell().getOrdinal());
			input.setValue(value);
			input.setConverter(new DoubleConverter());

			MethodExpression expression = expressionFactory
					.createMethodExpression(facesContext.getELContext(),
							"#{viewHandler.updateCell}", Void.class,
							new Class<?>[0]);

			AjaxBehavior behavior = new AjaxBehavior();
			behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(
					expression, expression));
			behavior.setProcess("@this");
			behavior.setUpdate("@form");

			UIParameter commandParam = new UIParameter();
			commandParam.setName("cell");
			commandParam.setValue(Integer.toString(cell.getOrdinal()));

			inplace.addClientBehavior("save", behavior);
			inplace.getChildren().add(commandParam);
			inplace.getChildren().add(input);

			column.getChildren().add(inplace);
		} else {
			HtmlOutputText text = new HtmlOutputText();
			String id = "txt-" + text.hashCode();

			text.setId(id);
			text.setValue(labelText);

			if (context.getMember() != null) {
				text.setTitle(context.getMember().getUniqueName());
			}

			String link = propertyUtils.getString("link",
					context.getRenderPropertyCategory(), null);

			if (link == null) {
				column.getChildren().add(text);
			} else {
				HtmlOutputLink anchor = new HtmlOutputLink();
				anchor.setValue(link);
				anchor.getChildren().add(text);

				column.getChildren().add(anchor);
			}
		}
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#endCell(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endCell(TableRenderContext context) {
		row.getChildren().add(column);
		this.column = null;
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#endRow(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endRow(TableRenderContext context) {
		if (header == null) {
			grid.getChildren().add(row);
		} else {
			header.getChildren().add(row);
		}

		this.row = null;
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#endBody(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endBody(TableRenderContext context) {
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderCallback#endTable(org.pivot4j.ui.table.TableRenderContext)
	 */
	@Override
	public void endTable(TableRenderContext context) {
		if (context.getAxis() == Axis.FILTER) {
			filterPanel.getChildren().add(grid);
		} else {
			gridPanel.getChildren().add(grid);
		}

		this.grid = null;
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#endRender(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRender(TableRenderContext context) {
		ResourceBundle resources = context.getResourceBundle();

		MessageFormat mf = new MessageFormat(
				resources.getString("error.property.expression.title"));

		// In order not to bombard users with similar error messages.
		for (String category : context.getRenderProperties().keySet()) {
			Map<String, EvaluationFailedException> errors = getRenderPropertyUtils()
					.getLastErrors(category);

			for (String property : errors.keySet()) {
				String title = mf.format(new String[] { resources
						.getString("properties." + property) });

				EvaluationFailedException e = errors.get(property);

				facesContext.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR, title, e.getMessage()));

				if (logger.isWarnEnabled()) {
					logger.warn(title, e);
				}
			}
		}

		this.commandIndex = 0;
		this.scenarioEnabled = false;

		super.endRender(context);
	}

	interface StyleClassResolver {

		String resolve(TableRenderContext context);
	}

	static class TitleStyleClassResolver implements StyleClassResolver {

		@Override
		public String resolve(TableRenderContext context) {
			String styleClass;

			String type = context.getCellType();

			if (context.getAxis() == Axis.COLUMNS) {
				styleClass = "col-hdr-cell";
			} else if (type.equals(LABEL)
					|| (context.getAxis() == Axis.FILTER && context.getLevel() != null)) {
				styleClass = "row-hdr-cell ui-widget-header";
			} else {
				styleClass = "ui-widget-header";
			}

			return styleClass;
		}
	}

	static class ValueStyleClassResolver implements StyleClassResolver {

		@Override
		public String resolve(TableRenderContext context) {
			String styleClass;

			if (context.getAggregator() == null) {
				// PrimeFaces' Row class doesn't have the styleClass property.
				if (context.getRowIndex() % 2 == 0) {
					styleClass = "value-cell cell-even";
				} else {
					styleClass = "value-cell cell-odd";
				}
			} else {
				styleClass = "ui-widget-header agg-cell";

				if (context.getAxis() == Axis.COLUMNS) {
					styleClass += " col-agg-cell";
				} else if (context.getAxis() == Axis.ROWS) {
					styleClass += " row-agg-cell";
				}
			}

			return styleClass;
		}
	}

	static class AggregationStyleClassResolver implements StyleClassResolver {

		@Override
		public String resolve(TableRenderContext context) {
			String styleClass;

			if (context.getAxis() == Axis.ROWS) {
				styleClass = "ui-widget-header ";
			} else {
				styleClass = "";
			}

			styleClass += "agg-title";

			return styleClass;
		}
	}
}
