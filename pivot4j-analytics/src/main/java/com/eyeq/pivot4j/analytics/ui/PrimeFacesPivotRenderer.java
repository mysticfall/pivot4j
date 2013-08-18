package com.eyeq.pivot4j.analytics.ui;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.row.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.el.EvaluationFailedException;
import com.eyeq.pivot4j.ui.AbstractPivotUIRenderer;
import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.CellParameters;
import com.eyeq.pivot4j.ui.property.PropertySupport;
import com.eyeq.pivot4j.util.CssWriter;

public class PrimeFacesPivotRenderer extends AbstractPivotUIRenderer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, String> iconMap;

	private PanelGrid component;

	private PanelGrid filterComponent;

	private FacesContext facesContext;

	private ExpressionFactory expressionFactory;

	private HtmlPanelGroup header;

	private Row row;

	private Column column;

	private int commandIndex = 0;

	/**
	 * @param facesContext
	 */
	public PrimeFacesPivotRenderer(FacesContext facesContext) {
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

		this.commandIndex = 0;
	}

	/**
	 * @return the parent JSF component
	 */
	public PanelGrid getComponent() {
		return component;
	}

	/**
	 * @param component
	 */
	public void setComponent(PanelGrid component) {
		this.component = component;
	}

	/**
	 * @return filterComponent
	 */
	public PanelGrid getFilterComponent() {
		return filterComponent;
	}

	/**
	 * @param filterComponent
	 */
	public void setFilterComponent(PanelGrid filterComponent) {
		this.filterComponent = filterComponent;
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#createDefaultResourceBundle(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	protected ResourceBundle createDefaultResourceBundle(PivotModel model) {
		return facesContext.getApplication().getResourceBundle(facesContext,
				"msg");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startTable(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startTable(RenderContext context) {
		if (context.getAxis() == Axis.FILTER) {
			filterComponent.getChildren().clear();
		} else {
			component.getChildren().clear();
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startHeader(RenderContext context) {
		this.header = new HtmlPanelGroup();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endHeader(RenderContext context) {
		if (context.getAxis() != Axis.FILTER) {
			component.getFacets().put("header", header);
		}

		this.header = null;
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
		this.row = new Row();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotUIRenderer#startCell(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.util.List)
	 */
	@Override
	public void startCell(RenderContext context, List<CellCommand<?>> commands) {
		this.column = new Column();

		String id = "col-" + column.hashCode();

		column.setId(id);
		column.setColspan(context.getColumnSpan());
		column.setRowspan(context.getRowSpan());

		String styleClass;

		StringWriter writer = new StringWriter();
		CssWriter cssWriter = new CssWriter(writer);

		switch (context.getCellType()) {
		case Header:
		case Title:
		case None:
			if (context.getAxis() == Axis.COLUMNS) {
				styleClass = "col-hdr-cell";
			} else {
				if (context.getCellType() == CellType.Header) {
					styleClass = "row-hdr-cell ui-widget-header";
				} else {
					styleClass = "ui-widget-header";
				}
			}

			if (!getShowParentMembers() && context.getMember() != null) {
				int padding = context.getMember().getDepth() * 10;
				cssWriter.writeStyle("padding-left", padding + "px");
			}

			break;
		case Aggregation:
			if (context.getAxis() == Axis.ROWS) {
				styleClass = "ui-widget-header ";
			} else {
				styleClass = "";
			}

			styleClass += "agg-title";

			if (!getShowParentMembers() && context.getMember() != null) {
				int padding = context.getMember().getDepth() * 10;
				cssWriter.writeStyle("padding-left", padding + "px");
			}

			break;
		case Value:
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
			break;
		default:
			styleClass = null;
		}

		if (expressionFactory != null) {
			for (CellCommand<?> command : commands) {
				CellParameters parameters = command.createParameters(context);

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
								"#{pivotGridHandler.executeCommand}",
								Void.class, new Class<?>[0]);
				button.setActionExpression(expression);
				button.setUpdate(":grid-form,:editor-form:mdx-editor,:editor-form:editor-toolbar,:source-tree-form,:target-tree-form");
				button.setOncomplete("onViewChanged()");

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

		PropertySupport properties = getProperties(context);

		if (properties != null) {
			cssWriter.writeStyle("color",
					getPropertyValue("fgColor", properties, context));

			String bgColor = getPropertyValue("bgColor", properties, context);
			if (bgColor != null) {
				cssWriter.writeStyle("background-color", bgColor);
				cssWriter.writeStyle("background-image", "none");
			}

			cssWriter.writeStyle("font-family",
					getPropertyValue("fontFamily", properties, context));
			cssWriter.writeStyle("font-size",
					getPropertyValue("fontSize", properties, context));

			String fontStyle = getPropertyValue("fontStyle", properties,
					context);
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

			String styleClassProperty = getPropertyValue("styleClass",
					properties, context);
			if (styleClassProperty != null) {
				if (styleClass == null) {
					styleClass = styleClassProperty;
				} else {
					styleClass += " " + styleClassProperty;
				}
			}
		}

		column.setStyleClass(styleClass);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#onPropertyEvaluationFailure(java.lang.String,
	 *      com.eyeq.pivot4j.ui.RenderContext,
	 *      com.eyeq.pivot4j.el.EvaluationFailedException)
	 */
	@Override
	protected void onPropertyEvaluationFailure(String key,
			RenderContext context, EvaluationFailedException e) {
		super.onPropertyEvaluationFailure(key, context, e);

		// In order not to bombard users with similar error messages.
		String attributeName = "property.hasError." + key;

		if (context.getAttribute(attributeName) == null) {
			MessageFormat mf = new MessageFormat(getResourceBundle().getString(
					"error.property.expression.title"));

			String title = mf.format(new String[] { getResourceBundle()
					.getString("properties." + key) });

			facesContext.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, e.getMessage()));

			if (logger.isWarnEnabled()) {
				logger.warn(title, e);
			}

			context.setAttribute(attributeName, true);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void cellContent(RenderContext context, String label) {
		PropertySupport properties = getProperties(context);

		HtmlOutputText text = new HtmlOutputText();
		String id = "txt-" + text.hashCode();

		text.setId(id);
		text.setValue(label);

		String link = null;

		if (properties != null) {
			link = getPropertyValue("link", properties, context);
		}

		if (link == null) {
			column.getChildren().add(text);
		} else {
			HtmlOutputLink anchor = new HtmlOutputLink();
			anchor.setValue(link);
			anchor.getChildren().add(text);

			column.getChildren().add(anchor);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endCell(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endCell(RenderContext context) {
		row.getChildren().add(column);
		this.column = null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endRow(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRow(RenderContext context) {
		if (header == null) {
			UIComponent parent = context.getAxis() == Axis.FILTER ? filterComponent
					: component;
			parent.getChildren().add(row);
		} else {
			header.getChildren().add(row);
		}

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
		this.commandIndex = 0;
	}
}
