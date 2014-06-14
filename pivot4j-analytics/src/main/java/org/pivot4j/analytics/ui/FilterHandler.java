package org.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;

import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.component.tree.NodeCollector;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.HierarchyNode;
import org.pivot4j.analytics.ui.navigator.LevelNode;
import org.pivot4j.analytics.ui.navigator.MemberNode;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.MemberSelection;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.commandlink.CommandLink;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "filterHandler")
@RequestScoped
public class FilterHandler implements ModelChangeListener, NodeFilter {

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	@ManagedProperty(value = "#{navigatorHandler}")
	private NavigatorHandler navigator;

	private TreeNode filterNode;

	private TreeNode[] selection;

	private MemberSelection filterMembers;

	private UIComponent filterPanel;

	private CommandButton buttonApply;

	@PostConstruct
	protected void initialize() {
		if (model != null) {
			model.addModelChangeListener(this);
		}
	}

	@PreDestroy
	protected void destroy() {
		if (model != null) {
			model.removeModelChangeListener(this);
		}
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
	 * @return the navigator
	 */
	public NavigatorHandler getNavigator() {
		return navigator;
	}

	/**
	 * @param navigator
	 *            the navigator to set
	 */
	public void setNavigator(NavigatorHandler navigator) {
		this.navigator = navigator;
	}

	protected MemberSelection getFilteredMembers() {
		if (filterMembers == null) {
			Hierarchy hierarchy = getHierarchy();

			if (hierarchy != null) {
				ChangeSlicer transform = model.getTransform(ChangeSlicer.class);

				this.filterMembers = new MemberSelection(
						transform.getSlicer(hierarchy), model.getCube());

				if (model instanceof PivotModelImpl) {
					MemberHierarchyCache cache = ((PivotModelImpl) model)
							.getMemberHierarchyCache();
					filterMembers.setMemberHierarchyCache(cache);
				}
			}

		}

		return filterMembers;
	}

	/**
	 * @return the filterNode
	 */
	public TreeNode getFilterNode() {
		if (model != null && model.isInitialized()) {
			Hierarchy hierarchy = getHierarchy();

			if (filterNode == null && hierarchy != null) {
				this.filterNode = new DefaultTreeNode();

				filterNode.setExpanded(true);

				List<Member> members;
				boolean isMeasure;

				try {
					members = hierarchy.getRootMembers();
					isMeasure = hierarchy.getDimension().getDimensionType() == Type.MEASURE;
				} catch (OlapException e) {
					throw new FacesException(e);
				}

				for (Member member : members) {
					if (isMeasure && !member.isVisible()) {
						continue;
					}

					MemberNode node = new MemberNode(member);

					node.setNodeFilter(this);
					node.setExpanded(true);
					node.setSelectable(true);
					node.setSelected(isSelected(member));

					filterNode.getChildren().add(node);
				}

				List<TreeNode> initialSelection = ((DefaultTreeNode) filterNode)
						.collectNodes(new NodeCollector() {

							@Override
							public boolean collectNode(TreeNode node) {
								return node.isSelected();
							}

							@Override
							public boolean searchNode(TreeNode node) {
								return node.isExpanded();
							}
						});

				this.selection = initialSelection
						.toArray(new TreeNode[initialSelection.size()]);
			}
		} else {
			this.filterNode = null;
		}

		return filterNode;
	}

	/**
	 * @param filterNode
	 *            the filterNode to set
	 */
	public void setFilterNode(TreeNode filterNode) {
		this.filterNode = filterNode;
	}

	/**
	 * @return the selection
	 */
	public TreeNode[] getSelection() {
		return selection;
	}

	/**
	 * @param newSelection
	 *            the selection to set
	 */
	public void setSelection(TreeNode[] newSelection) {
		if (newSelection == null) {
			this.selection = null;
		} else {
			this.selection = Arrays.copyOf(newSelection, newSelection.length);
		}
	}

	/**
	 * @return the filterPanel
	 */
	public UIComponent getFilterPanel() {
		return filterPanel;
	}

	/**
	 * @param filterPanel
	 *            the filterPanel to set
	 */
	public void setFilterPanel(UIComponent filterPanel) {
		this.filterPanel = filterPanel;
	}

	/**
	 * @return the filteredHierarchy
	 */
	protected String getHierarchyName() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot view = context.getViewRoot();

		return (String) view.getAttributes().get("hierarchy");
	}

	/**
	 * @param hierarchyName
	 */
	protected void setHierarchyName(String hierarchyName) {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot view = context.getViewRoot();

		if (hierarchyName == null) {
			view.getAttributes().remove("hierarchy");
		} else {
			view.getAttributes().put("hierarchy", hierarchyName);
		}

		this.filterMembers = null;
		this.filterNode = null;
		this.selection = null;
	}

	protected Hierarchy getHierarchy() {
		String hierarchyName = getHierarchyName();

		if (hierarchyName != null) {
			return model.getCube().getHierarchies().get(hierarchyName);
		}

		return null;
	}

	/**
	 * @return the buttonApply
	 */
	public CommandButton getButtonApply() {
		return buttonApply;
	}

	/**
	 * @param buttonApply
	 *            the buttonApply to set
	 */
	public void setButtonApply(CommandButton buttonApply) {
		this.buttonApply = buttonApply;
	}

	/**
	 * @param id
	 * @return
	 */
	protected List<Integer> getNodePath(String id) {
		// there should be a cleaner way to get data from the dropped component.
		// it's a limitation on PFs' side :
		// http://code.google.com/p/primefaces/issues/detail?id=2781
		String[] segments = id.split(":");
		String[] indexSegments = segments[segments.length - 2].split("_");

		List<Integer> path = new ArrayList<Integer>(indexSegments.length);
		for (String index : indexSegments) {
			path.add(Integer.parseInt(index));
		}

		return path;
	}

	/**
	 * @param id
	 * @return
	 */
	protected boolean isSourceNode(String id) {
		return id.startsWith("source-tree-form:cube-navigator");
	}

	/**
	 * @param e
	 */
	public void onNodeSelected(NodeSelectEvent e) {
		buttonApply.setDisabled(false);
	}

	/**
	 * @param e
	 */
	public void onNodeUnselected(NodeUnselectEvent e) {
		buttonApply.setDisabled(false);
	}

	public void onClose() {
		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);

		Hierarchy hierarchy = getHierarchy();

		if (!transform.getHierarchies().contains(hierarchy)) {
			removeHierarchy(getHierarchyName());
		}

		setHierarchyName(null);
	}

	/**
	 * @param e
	 */
	public void onDrop(DragDropEvent e) {
		List<Integer> sourcePath = getNodePath(e.getDragId());

		Hierarchy hierarchy = null;

		if (isSourceNode(e.getDragId())) {
			TreeNode sourceNode = findNodeFromPath(navigator.getCubeNode(),
					sourcePath);

			if (sourceNode instanceof HierarchyNode) {
				HierarchyNode node = (HierarchyNode) sourceNode;
				hierarchy = node.getObject();
			} else if (sourceNode instanceof LevelNode) {
				LevelNode node = (LevelNode) sourceNode;
				Level level = node.getObject();

				hierarchy = level.getHierarchy();
			}

			if (hierarchy == null) {
				return;
			}

			if (navigator.isSelected(hierarchy)) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("error.filter.title");
				String message = bundle.getString("error.filter.message");

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_WARN, title, message));
				return;
			}

			UIComponent panel = createFilterItem(hierarchy);
			filterPanel.getChildren().add(panel);

			show(hierarchy.getName());

			RequestContext.getCurrentInstance().execute("PF('filterDialog').show();");
		}
	}

	protected void configureFilter() {
		if (model != null && filterPanel != null) {
			filterPanel.getChildren().clear();

			if (model.isInitialized()) {
				ChangeSlicer transform = model.getTransform(ChangeSlicer.class);

				List<Hierarchy> hierarchies = transform.getHierarchies();

				for (Hierarchy hierarchy : hierarchies) {
					UIComponent panel = createFilterItem(hierarchy);
					filterPanel.getChildren().add(panel);
				}
			}
		}
	}

	/**
	 * @param hierarchy
	 * @return
	 */
	protected UIComponent createFilterItem(Hierarchy hierarchy) {
		String id = "filter-item-" + hierarchy.getUniqueName().hashCode();

		HtmlPanelGroup panel = new HtmlPanelGroup();
		panel.setId(id);
		panel.setLayout("block");
		panel.setStyleClass("ui-widget-header filter-item");

		CommandLink link = new CommandLink();
		link.setId(id + "-link");
		link.setValue(hierarchy.getCaption());
		link.setTitle(hierarchy.getUniqueName());

		FacesContext context = FacesContext.getCurrentInstance();
		ExpressionFactory factory = context.getApplication()
				.getExpressionFactory();

		link.setActionExpression(factory.createMethodExpression(
				context.getELContext(), "#{filterHandler.show}", Void.class,
				new Class<?>[0]));
		link.setUpdate(":filter-form");
		link.setOncomplete("PF('filterDialog').show();");

		UIParameter parameter = new UIParameter();
		parameter.setName("hierarchy");
		parameter.setValue(hierarchy.getName());

		link.getChildren().add(parameter);

		panel.getChildren().add(link);

		CommandButton closeButton = new CommandButton();
		closeButton.setId(id + "-button");
		closeButton.setIcon("ui-icon-close");
		closeButton.setActionExpression(factory.createMethodExpression(
				context.getELContext(), "#{filterHandler.removeHierarchy}",
				Void.class, new Class<?>[0]));
		closeButton
				.setUpdate(":filter-items-form,:source-tree-form,:grid-form,:editor-form:mdx-editor,:editor-form:editor-toolbar");
		closeButton.setOncomplete("onViewChanged()");

		UIParameter parameter2 = new UIParameter();
		parameter2.setName("hierarchy");
		parameter2.setValue(hierarchy.getName());

		closeButton.getChildren().add(parameter2);

		panel.getChildren().add(closeButton);

		return panel;
	}

	public String getFilterItemId() {
		String hierarchyName = getHierarchyName();

		if (hierarchyName == null) {
			return null;
		}

		return ":filter-item-"
				+ hierarchyName.replaceAll("[\\[\\]]", "")
						.replaceAll("[\\s\\.]", "_").toLowerCase();
	}

	public void onPreRenderView() {
		FacesContext context = FacesContext.getCurrentInstance();

		if (!context.isPostback()) {
			configureFilter();
		}
	}

	public void show() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		String hierarchyName = parameters.get("hierarchy");
		show(hierarchyName);
	}

	/**
	 * @param hierarchyName
	 */
	public void show(String hierarchyName) {
		setHierarchyName(hierarchyName);

		buttonApply.setDisabled(true);
	}

	public void apply() {
		List<Member> members = null;

		if (selection != null) {
			members = new ArrayList<Member>(selection.length);

			for (TreeNode node : selection) {
				MemberNode memberNode = (MemberNode) node;
				members.add(memberNode.getObject());
			}
		}

		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);
		transform.setSlicer(getHierarchy(), members);
	}

	public void removeHierarchy() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		String hierarchyName = parameters.get("hierarchy");
		removeHierarchy(hierarchyName);
	}

	/**
	 * @param hierarchyName
	 */
	public void removeHierarchy(String hierarchyName) {
		Hierarchy hierarchy = model.getCube().getHierarchies()
				.get(hierarchyName);

		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);
		transform.setSlicer(hierarchy, null);

		configureFilter();
	}

	/**
	 * @param parent
	 * @param indexes
	 * @return
	 */
	protected TreeNode findNodeFromPath(TreeNode parent, List<Integer> indexes) {
		if (indexes.size() > 1) {
			return findNodeFromPath(parent.getChildren().get(indexes.get(0)),
					indexes.subList(1, indexes.size()));
		} else {
			return parent.getChildren().get(indexes.get(0));
		}
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelInitialized(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelInitialized(ModelChangeEvent e) {
		configureFilter();
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelDestroyed(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelDestroyed(ModelChangeEvent e) {
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelChanged(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelChanged(ModelChangeEvent e) {
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#structureChanged(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void structureChanged(ModelChangeEvent e) {
		configureFilter();
	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public <T extends MetadataElement> boolean isSelected(T element) {
		return getFilteredMembers().isSelected((Member) element);
	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public <T extends MetadataElement> boolean isSelectable(T element) {
		return true;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isActive(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isActive(T element) {
		return false;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isVisible(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isVisible(T element) {
		return true;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isExpanded(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isExpanded(T element) {
		return getFilteredMembers().findChild((Member) element) != null;
	}
}
