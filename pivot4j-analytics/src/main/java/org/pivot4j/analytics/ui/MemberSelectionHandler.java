package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.MemberNode;
import org.pivot4j.analytics.ui.navigator.SelectionNode;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.MemberSelection;
import org.pivot4j.util.OlapUtils;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "memberSelectionHandler")
@ViewScoped
public class MemberSelectionHandler implements NodeFilter, Serializable {

	private static final long serialVersionUID = -2124965576827229229L;

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	private TreeNode sourceNode;

	private TreeNode targetNode;

	private TreeNode[] sourceSelection;

	private TreeNode[] targetSelection;

	private Hierarchy hierarchy;

	private String hierarchyName;

	private CommandButton buttonAdd;

	private CommandButton buttonRemove;

	private CommandButton buttonUp;

	private CommandButton buttonDown;

	private CommandButton buttonApply;

	private CommandButton buttonOk;

	private MemberSelection selection;

	@PostConstruct
	protected void initialize() {
	}

	@PreDestroy
	protected void destroy() {
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
	 * @return the sourceNode
	 */
	public TreeNode getSourceNode() {
		if (sourceNode == null) {
			this.sourceNode = new DefaultTreeNode();

			Hierarchy hier = getHierarchy();
			if (hier != null) {
				try {
					boolean isMeasure = hierarchy.getDimension()
							.getDimensionType() == Type.MEASURE;

					List<? extends Member> members = hier.getRootMembers();

					for (Member member : members) {
						if (isMeasure && !member.isVisible()) {
							continue;
						}

						MemberNode node = new MemberNode(member);
						node.setNodeFilter(this);

						if (isVisible(member)) {
							node.setExpanded(isExpanded(member));
							node.setSelectable(isSelectable(member));
							node.setSelected(isSelected(member));
							node.getData().setSelected(isActive(member));

							sourceNode.getChildren().add(node);
						}
					}
				} catch (OlapException e) {
					throw new FacesException(e);
				}
			}
		}

		return sourceNode;
	}

	/**
	 * @param sourceNode
	 *            the sourceNode to set
	 */
	public void setSourceNode(TreeNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	/**
	 * @return the targetNode
	 */
	public TreeNode getTargetNode() {
		if (targetNode == null) {
			MemberSelection sel = getSelection();

			if (sel != null) {
				this.targetNode = new SelectionNode(sel);

				targetNode.setExpanded(true);
			}
		}

		return targetNode;
	}

	/**
	 * @param targetNode
	 *            the targetNode to set
	 */
	public void setTargetNode(TreeNode targetNode) {
		this.targetNode = targetNode;
	}

	public void show() {
		reset();

		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		this.hierarchyName = parameters.get("hierarchy");
	}

	public void reset() {
		buttonAdd.setDisabled(true);
		buttonRemove.setDisabled(true);
		buttonUp.setDisabled(true);
		buttonDown.setDisabled(true);
		buttonApply.setDisabled(true);
		buttonOk.setDisabled(true);

		this.hierarchyName = null;
		this.hierarchy = null;
		this.sourceNode = null;
		this.targetNode = null;
		this.selection = null;
	}

	public void apply() {
		PlaceMembersOnAxes transform = model
				.getTransform(PlaceMembersOnAxes.class);
		transform.placeMembers(getHierarchy(), getSelection().getMembers());

		buttonApply.setDisabled(true);
		buttonOk.setDisabled(true);
	}

	public void add() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		String modeName = parameters.get("mode");

		if (modeName == null) {
			modeName = SelectionMode.Single.name();
		}

		add(modeName);
	}

	/**
	 * @param modeName
	 */
	public void add(String modeName) {
		SelectionMode mode = null;

		if (modeName != null) {
			mode = SelectionMode.valueOf(modeName);
		}

		MemberSelection sel = getSelection();

		if (mode == null) {
			sel.clear();
		} else {
			boolean empty = true;

			List<Member> members = sel.getMembers();

			for (TreeNode node : sourceSelection) {
				MemberNode memberNode = (MemberNode) node;

				Member member = memberNode.getObject();

				List<Member> targetMembers = mode.getTargetMembers(member);

				for (Member target : targetMembers) {
					if (!members.contains(target)) {
						members.add(target);
						empty = false;
					}
				}
			}

			if (empty) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("warn.noMembers.title");
				String message = bundle
						.getString("warn.noMembers.select.message");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, title,
								message));
				return;
			}

			this.selection = new MemberSelection(members, model.getCube());

			if (model instanceof PivotModelImpl) {
				MemberHierarchyCache cache = ((PivotModelImpl) model)
						.getMemberHierarchyCache();
				selection.setMemberHierarchyCache(cache);
			}
		}

		this.sourceNode = null;
		this.targetNode = null;

		this.sourceSelection = null;
		this.targetSelection = null;

		updateButtonStatus();

		buttonApply.setDisabled(false);
		buttonOk.setDisabled(false);
	}

	public void remove() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		String modeName = parameters.get("mode");

		if (modeName == null) {
			modeName = SelectionMode.Single.name();
		}

		remove(modeName);
	}

	/**
	 * @param modeName
	 */
	public void remove(String modeName) {
		SelectionMode mode = null;

		if (modeName != null) {
			mode = SelectionMode.valueOf(modeName);
		}

		MemberSelection sel = getSelection();

		if (mode == null) {
			sel.clear();
		} else {
			boolean empty = true;

			List<Member> members = sel.getMembers();

			OlapUtils utils = new OlapUtils(model.getCube());

			if (model instanceof PivotModelImpl) {
				utils.setMemberHierarchyCache(((PivotModelImpl) model)
						.getMemberHierarchyCache());
			}

			for (TreeNode node : targetSelection) {
				SelectionNode memberNode = (SelectionNode) node;

				Member member = memberNode.getObject();

				List<Member> targetMembers = mode.getTargetMembers(member);

				for (Member target : targetMembers) {
					Member wrappedMember = utils.wrapRaggedIfNecessary(target);

					if (members.contains(wrappedMember)) {
						members.remove(wrappedMember);
						empty = false;
					}
				}
			}

			if (empty) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("warn.noMembers.title");
				String message = bundle
						.getString("warn.noMembers.remove.message");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, title,
								message));
				return;
			}

			this.selection = new MemberSelection(members, model.getCube());

			if (model instanceof PivotModelImpl) {
				MemberHierarchyCache cache = ((PivotModelImpl) model)
						.getMemberHierarchyCache();
				selection.setMemberHierarchyCache(cache);
			}
		}

		this.sourceNode = null;
		this.targetNode = null;

		this.sourceSelection = null;
		this.targetSelection = null;

		updateButtonStatus();

		buttonApply.setDisabled(false);
		buttonOk.setDisabled(false);
	}

	public void moveUp() {
		SelectionNode node = (SelectionNode) targetSelection[0];
		Member member = node.getObject();

		MemberSelection sel = getSelection();
		sel.moveUp(member);

		SelectionNode parent = (SelectionNode) node.getParent();
		parent.moveUp(node);

		updateButtonStatus();

		buttonApply.setDisabled(false);
		buttonOk.setDisabled(false);
	}

	public void moveDown() {
		SelectionNode node = (SelectionNode) targetSelection[0];
		Member member = node.getObject();

		MemberSelection sel = getSelection();
		sel.moveDown(member);

		SelectionNode parent = (SelectionNode) node.getParent();
		parent.moveDown(node);

		updateButtonStatus();

		buttonApply.setDisabled(false);
		buttonOk.setDisabled(false);
	}

	public Hierarchy getHierarchy() {
		if (hierarchy == null && hierarchyName != null && model.isInitialized()) {
			this.hierarchy = model.getCube().getHierarchies()
					.get(hierarchyName);
		}

		return hierarchy;
	}

	protected MemberSelection getSelection() {
		if (selection == null) {
			Hierarchy hier = getHierarchy();

			if (hier != null) {
				PlaceMembersOnAxes transform = model
						.getTransform(PlaceMembersOnAxes.class);

				List<Member> members = transform.findVisibleMembers(hier);
				this.selection = new MemberSelection(members, model.getCube());

				if (model instanceof PivotModelImpl) {
					MemberHierarchyCache cache = ((PivotModelImpl) model)
							.getMemberHierarchyCache();
					selection.setMemberHierarchyCache(cache);
				}
			}
		}

		return selection;
	}

	public boolean isAddButtonEnabled() {
		boolean canAdd;

		if (sourceSelection == null || sourceSelection.length == 0) {
			canAdd = false;
		} else {
			canAdd = true;

			for (TreeNode node : sourceSelection) {
				if (((MemberNode) node).getData().isSelected()) {
					canAdd = false;
					break;
				}
			}
		}

		return canAdd;
	}

	public boolean isRemoveButtonEnabled() {
		boolean canRemove;

		if (targetSelection == null || targetSelection.length == 0) {
			canRemove = false;
		} else {
			canRemove = true;

			for (TreeNode node : targetSelection) {
				if (!((SelectionNode) node).getData().isSelected()) {
					canRemove = false;
					break;
				}
			}
		}

		return canRemove;
	}

	public boolean isUpButtonEnabled() {
		boolean canMoveUp;

		if (targetSelection == null || targetSelection.length != 1) {
			canMoveUp = false;
		} else {
			SelectionNode node = (SelectionNode) targetSelection[0];

			Member member = node.getObject();

			MemberSelection sel = getSelection();

			canMoveUp = sel.canMoveUp(member);
		}

		return canMoveUp;
	}

	public boolean isDownButtonEnabled() {
		boolean canMoveDown;

		if (targetSelection == null || targetSelection.length != 1) {
			canMoveDown = false;
		} else {
			SelectionNode node = (SelectionNode) targetSelection[0];

			Member member = node.getObject();

			MemberSelection sel = getSelection();
			canMoveDown = sel.canMoveDown(member);
		}

		return canMoveDown;
	}

	/**
	 * @param e
	 */
	public void onSourceNodeSelected(NodeSelectEvent e) {
		updateButtonStatus();
	}

	/**
	 * @param e
	 */
	public void onTargetNodeSelected(NodeSelectEvent e) {
		updateButtonStatus();
	}

	protected void updateButtonStatus() {
		buttonAdd.setDisabled(!isAddButtonEnabled());
		buttonRemove.setDisabled(!isRemoveButtonEnabled());
		buttonUp.setDisabled(!isUpButtonEnabled());
		buttonDown.setDisabled(!isDownButtonEnabled());
	}

	/**
	 * @return the hierarchyName
	 */
	public String getHierarchyName() {
		return hierarchyName;
	}

	/**
	 * @param hierarchyName
	 *            the hierarchyName to set
	 */
	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}

	/**
	 * @return the sourceSelection
	 */
	public TreeNode[] getSourceSelection() {
		return sourceSelection;
	}

	/**
	 * @param newSelection
	 *            the sourceSelection to set
	 */
	public void setSourceSelection(TreeNode[] newSelection) {
		if (newSelection == null) {
			this.sourceSelection = null;
		} else {
			this.sourceSelection = Arrays.copyOf(newSelection,
					newSelection.length);
		}
	}

	/**
	 * @return the targetSelection
	 */
	public TreeNode[] getTargetSelection() {
		return targetSelection;
	}

	/**
	 * @param newSelection
	 *            the targetSelection to set
	 */
	public void setTargetSelection(TreeNode[] newSelection) {
		if (newSelection == null) {
			this.targetSelection = null;
		} else {
			this.targetSelection = Arrays.copyOf(newSelection,
					newSelection.length);
		}
	}

	/**
	 * @return the buttonAdd
	 */
	public CommandButton getButtonAdd() {
		return buttonAdd;
	}

	/**
	 * @param buttonAdd
	 *            the buttonAdd to set
	 */
	public void setButtonAdd(CommandButton buttonAdd) {
		this.buttonAdd = buttonAdd;
	}

	/**
	 * @return the buttonRemove
	 */
	public CommandButton getButtonRemove() {
		return buttonRemove;
	}

	/**
	 * @param buttonRemove
	 *            the buttonRemove to set
	 */
	public void setButtonRemove(CommandButton buttonRemove) {
		this.buttonRemove = buttonRemove;
	}

	/**
	 * @return the buttonUp
	 */
	public CommandButton getButtonUp() {
		return buttonUp;
	}

	/**
	 * @param buttonUp
	 *            the buttonUp to set
	 */
	public void setButtonUp(CommandButton buttonUp) {
		this.buttonUp = buttonUp;
	}

	/**
	 * @return the buttonDown
	 */
	public CommandButton getButtonDown() {
		return buttonDown;
	}

	/**
	 * @param buttonDown
	 *            the buttonDown to set
	 */
	public void setButtonDown(CommandButton buttonDown) {
		this.buttonDown = buttonDown;
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
	 * @return the buttonOk
	 */
	public CommandButton getButtonOk() {
		return buttonOk;
	}

	/**
	 * @param buttonOk
	 *            the buttonOk to set
	 */
	public void setButtonOk(CommandButton buttonOk) {
		this.buttonOk = buttonOk;
	}

	/**
	 * @param element
	 * @return
	 */
	@Override
	public <T extends MetadataElement> boolean isSelected(T element) {
		return false;
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
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isVisible(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isVisible(T element) {
		Member member = (Member) element;

		try {
			return !isActive(element) || member.getChildMemberCount() > 0;
		} catch (OlapException e) {
			throw new FacesException(e);
		}
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isActive(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isActive(T element) {
		return getSelection().isSelected((Member) element);
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isExpanded(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isExpanded(T element) {
		return getSelection().findChild((Member) element) != null;
	}
}
