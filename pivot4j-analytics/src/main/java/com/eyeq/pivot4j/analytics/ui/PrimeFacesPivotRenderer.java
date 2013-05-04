package com.eyeq.pivot4j.analytics.ui;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;

import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.metadata.Measure;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.row.Row;
import org.primefaces.context.RequestContext;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.AbstractPivotUIRenderer;
import com.eyeq.pivot4j.ui.CellType;
import com.eyeq.pivot4j.ui.PivotUIRenderer;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.aggregator.Aggregator;
import com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.CellParameters;

public class PrimeFacesPivotRenderer extends AbstractPivotUIRenderer {

	private Map<String, String> iconMap;

	private PanelGrid component;

	private FacesContext facesContext;

	private ExpressionFactory expressionFactory;

	private HtmlPanelGroup header;

	private Row row;

	private Column column;

	private int commandIndex = 0;

	private ResourceBundle bundle;

	/**
	 * @param facesContext
	 */
	public PrimeFacesPivotRenderer(FacesContext facesContext) {
		this.facesContext = facesContext;

		if (facesContext != null) {
			Application application = facesContext.getApplication();

			this.expressionFactory = application.getExpressionFactory();
			this.bundle = facesContext.getApplication().getResourceBundle(
					facesContext, "msg");
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
	 * @return bundle
	 */
	protected ResourceBundle getBundle() {
		return bundle;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotUIRenderer#registerCommands()
	 */
	@Override
	protected void registerCommands() {
		super.registerCommands();

		addCommand(new DrillThroughCommandImpl(this));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#getCellLabel(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	protected String getCellLabel(RenderContext context) {
		String label = super.getCellLabel(context);

		if (context.getCellType() == CellType.Aggregation) {
			Aggregator aggregator = context.getAggregator();

			if (aggregator != null
					&& (context.getMember() == null || !(context.getMember() instanceof Measure))) {
				String key = "label.aggregation.type." + aggregator.getName();
				String value = bundle.getString(key);

				if (value != null) {
					label = value;
				}
			}
		}

		return label;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startTable(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startTable(RenderContext context) {
		component.getChildren().clear();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#startHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startHeader(RenderContext context) {
		this.header = new HtmlPanelGroup();
		header.setId("pivot-header");
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotLayoutCallback#endHeader(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endHeader(RenderContext context) {
		component.getFacets().put("header", header);
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
		column.setColspan(context.getColSpan());
		column.setRowspan(context.getRowSpan());

		String styleClass;

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
				column.setStyle("padding-left: " + padding + "px");
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
				column.setStyle("padding-left: " + padding + "px");
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

		column.setStyleClass(styleClass);

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
				hierarchyParam.setName("cell");
				hierarchyParam.setValue(parameters.getCellOrdinal());
				button.getChildren().add(cellParam);

				column.getChildren().add(button);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext,
	 *      java.lang.String)
	 */
	@Override
	public void cellContent(RenderContext context, String label) {
		HtmlOutputText text = new HtmlOutputText();
		String id = "txt-" + text.hashCode();

		text.setId(id);
		text.setValue(label);

		column.getChildren().add(text);
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
			component.getChildren().add(row);
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

	/**
	 * Workaround to implement lazy rendering due to limitation in Olap4J's API
	 * :
	 * 
	 * @see http://sourceforge.net/p/olap4j/bugs/15/
	 */
	class DrillThroughCommandImpl extends BasicDrillThroughCommand {

		/**
		 * @param renderer
		 */
		public DrillThroughCommandImpl(PivotUIRenderer renderer) {
			super(renderer);
		}

		/**
		 * @see com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand#execute(com.eyeq.pivot4j.PivotModel,
		 *      com.eyeq.pivot4j.ui.command.CellParameters)
		 */
		@Override
		public ResultSet execute(PivotModel model, CellParameters parameters) {
			Cell cell = model.getCellSet().getCell(parameters.getCellOrdinal());

			DrillThroughDataModel data = facesContext.getApplication()
					.evaluateExpressionGet(facesContext, "#{drillThroughData}",
							DrillThroughDataModel.class);
			data.initialize(cell);
			data.setPageSize(15);

			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("drillThrough();");

			return null;
		}
	}
}
