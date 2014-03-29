package org.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.CubeNode;
import org.pivot4j.analytics.ui.navigator.HierarchyNode;
import org.pivot4j.analytics.ui.navigator.LevelNode;
import org.pivot4j.analytics.ui.navigator.MeasureNode;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.PlaceHierarchiesOnAxes;
import org.pivot4j.transform.PlaceLevelsOnAxes;
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "navigatorHandler")
@RequestScoped
public class NavigatorHandler implements ModelChangeListener, NodeFilter {

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	private CubeNode cubeNode;

	private TreeNode targetNode;

	private List<Dimension> dimensions;

	private Map<Axis, List<Hierarchy>> hierarchies;

	private Map<Hierarchy, List<Level>> levels;

	private Map<Hierarchy, List<Member>> members;

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
	 * @param axis
	 * @return
	 */
	protected List<Dimension> getDimensions(Axis axis) {
		if (dimensions == null) {
			this.dimensions = new ArrayList<Dimension>();

			for (Hierarchy hierarchy : getHierarchies(axis)) {
				dimensions.add(hierarchy.getDimension());
			}
		}

		return dimensions;
	}

	/**
	 * @param axis
	 * @return
	 */
	protected List<Hierarchy> getHierarchies(Axis axis) {
		if (hierarchies == null) {
			this.hierarchies = new HashMap<Axis, List<Hierarchy>>(2);
		}

		List<Hierarchy> result = hierarchies.get(axis);
		if (result == null) {
			if (axis.equals(Axis.FILTER)) {
				ChangeSlicer transform = model.getTransform(ChangeSlicer.class);
				result = transform.getHierarchies();
			} else {
				PlaceHierarchiesOnAxes transform = model
						.getTransform(PlaceHierarchiesOnAxes.class);
				result = transform.findVisibleHierarchies(axis);
			}

			hierarchies.put(axis, result);
		}

		return result;
	}

	/**
	 * @param hierarchy
	 * @return
	 */
	protected List<Level> getLevels(Hierarchy hierarchy) {
		if (levels == null) {
			this.levels = new HashMap<Hierarchy, List<Level>>();
		}

		List<Level> result = levels.get(hierarchy);
		if (result == null) {
			PlaceLevelsOnAxes transform = model
					.getTransform(PlaceLevelsOnAxes.class);

			result = new ArrayList<Level>(
					transform.findVisibleLevels(hierarchy));

			Collections.sort(result, new Comparator<Level>() {

				@Override
				public int compare(Level l1, Level l2) {
					Integer d1 = l1.getDepth();
					Integer d2 = l2.getDepth();

					return d1.compareTo(d2);
				}
			});

			levels.put(hierarchy, result);
		}

		return result;
	}

	/**
	 * @param hierarchy
	 * @return
	 */
	protected List<Member> getMembers(Hierarchy hierarchy) {
		if (members == null) {
			this.members = new HashMap<Hierarchy, List<Member>>();
		}

		List<Member> result = members.get(hierarchy);
		if (result == null) {
			PlaceMembersOnAxes transform = model
					.getTransform(PlaceMembersOnAxes.class);

			result = transform.findVisibleMembers(hierarchy);
			members.put(hierarchy, result);
		}

		return result;
	}

	/**
	 * @return the cubeNode
	 */
	public CubeNode getCubeNode() {
		if (model != null && model.isInitialized()) {
			if (cubeNode == null) {
				this.cubeNode = new CubeNode(model.getCube());
				cubeNode.setNodeFilter(this);
			}
		} else {
			this.cubeNode = null;
		}

		return cubeNode;
	}

	/**
	 * @param cubeNode
	 *            the cubeNode to set
	 */
	public void setCubeNode(CubeNode cubeNode) {
		this.cubeNode = cubeNode;

		this.dimensions = null;
		this.hierarchies = null;
		this.levels = null;
		this.members = null;
	}

	/**
	 * @return the cubeNode
	 */
	public TreeNode getTargetNode() {
		if (model != null && model.isInitialized()) {
			if (targetNode == null) {
				this.targetNode = new DefaultTreeNode();

				TreeNode columns = new DefaultTreeNode("columns", Axis.COLUMNS,
						targetNode);
				columns.setExpanded(true);

				configureAxis(columns, Axis.COLUMNS);

				TreeNode rows = new DefaultTreeNode("rows", Axis.ROWS,
						targetNode);
				rows.setExpanded(true);

				configureAxis(rows, Axis.ROWS);
			}
		} else {
			this.targetNode = null;
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

	/**
	 * @param axisRoot
	 * @param axis
	 */
	protected void configureAxis(TreeNode axisRoot, Axis axis) {
		List<Hierarchy> hierarchyList = getHierarchies(axis);
		for (Hierarchy hierarchy : hierarchyList) {
			TreeNode hierarchyNode = new DefaultTreeNode("hierarchy",
					hierarchy, axisRoot);
			hierarchyNode.setExpanded(true);

			Type type;

			try {
				type = hierarchy.getDimension().getDimensionType();
			} catch (OlapException e) {
				throw new FacesException(e);
			}

			if (type == Type.MEASURE) {
				List<Member> memberList = getMembers(hierarchy);
				for (Member member : memberList) {
					new DefaultTreeNode("measure", member, hierarchyNode);
				}
			} else {
				List<Level> levelList = getLevels(hierarchy);
				for (Level level : levelList) {
					new DefaultTreeNode("level", level, hierarchyNode);
				}
			}
		}
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
	public void onDrop(DragDropEvent e) {
		String dragId = e.getDragId();

		if (StringUtils.isEmpty(dragId)) {
			return;
		}

		List<Integer> path = getNodePath(dragId);

		boolean fromNavigator = isSourceNode(e.getDragId());
		if (fromNavigator) {
			return;
		}

		TreeNode node = findNodeFromPath(getTargetNode(), path);

		if (node.getData() instanceof Hierarchy) {
			Axis axis = (Axis) node.getParent().getData();
			Hierarchy hierarchy = (Hierarchy) node.getData();

			removeHierarhy(axis, hierarchy);
		} else if (node.getData() instanceof Level) {
			Axis axis = (Axis) node.getParent().getParent().getData();
			Level level = (Level) node.getData();

			removeLevel(axis, level);
		} else if (node.getData() instanceof Member) {
			Member member = (Member) node.getData();

			removeMember(member);
		}
	}

	/**
	 * @param e
	 */
	public void onDropOnAxis(DragDropEvent e) {
		List<Integer> sourcePath = getNodePath(e.getDragId());
		List<Integer> targetPath = getNodePath(e.getDropId());

		boolean fromNavigator = isSourceNode(e.getDragId());

		TreeNode root = fromNavigator ? getCubeNode() : getTargetNode();

		TreeNode source = findNodeFromPath(root, sourcePath);
		TreeNode target = findNodeFromPath(getTargetNode(), targetPath);

		if (fromNavigator) {
			onDropOnAxis(source, target);
		} else if (source.getData() instanceof Hierarchy) {
			Axis targetAxis = (Axis) target.getData();
			Hierarchy hierarchy = (Hierarchy) source.getData();

			if (source.getParent().equals(target)) {
				moveHierarhy(targetAxis, hierarchy, 0);
			} else {
				Axis sourceAxis = (Axis) source.getParent().getData();

				removeHierarhy(sourceAxis, hierarchy);
				addHierarhy(targetAxis, hierarchy);
			}
		}
	}

	/**
	 * @param sourceNode
	 * @param targetNode
	 */
	protected void onDropOnAxis(TreeNode sourceNode, TreeNode targetNode) {
		Axis axis = (Axis) targetNode.getData();

		if (sourceNode instanceof HierarchyNode) {
			HierarchyNode node = (HierarchyNode) sourceNode;
			Hierarchy hierarchy = node.getObject();

			addHierarhy(axis, hierarchy);
		} else if (sourceNode instanceof LevelNode) {
			LevelNode node = (LevelNode) sourceNode;
			Level level = node.getObject();

			addLevel(axis, level);
		} else if (sourceNode instanceof MeasureNode) {
			MeasureNode node = (MeasureNode) sourceNode;
			Member member = node.getObject();

			addMember(axis, member);
		}
	}

	/**
	 * @param e
	 */
	public void onDropOnHierarchy(DragDropEvent e) {
		List<Integer> sourcePath = getNodePath(e.getDragId());
		List<Integer> targetPath = getNodePath(e.getDropId());

		int position = targetPath.get(targetPath.size() - 1) + 1;

		boolean fromNavigator = isSourceNode(e.getDragId());

		TreeNode root = fromNavigator ? getCubeNode() : getTargetNode();

		TreeNode source = findNodeFromPath(root, sourcePath);
		TreeNode target = findNodeFromPath(getTargetNode(), targetPath);

		if (fromNavigator) {
			onDropOnHierarchy(source, target, position);
		} else if (source.getData() instanceof Hierarchy) {
			Axis targetAxis = (Axis) target.getParent().getData();
			Hierarchy hierarchy = (Hierarchy) source.getData();

			if (source.getParent().equals(target.getParent())) {
				moveHierarhy(targetAxis, hierarchy, position);
			} else {
				Axis sourceAxis = (Axis) source.getParent().getData();

				removeHierarhy(sourceAxis, hierarchy);
				addHierarhy(targetAxis, hierarchy, position);
			}
		} else if (source.getData() instanceof Member) {
			if (source.getParent().equals(target)) {
				moveMember((Member) source.getData(), 0);
			}
		}
	}

	/**
	 * @param sourceNode
	 * @param targetNode
	 * @param position
	 */
	protected void onDropOnHierarchy(TreeNode sourceNode, TreeNode targetNode,
			int position) {
		Axis axis = (Axis) targetNode.getParent().getData();

		if (sourceNode instanceof HierarchyNode) {
			HierarchyNode node = (HierarchyNode) sourceNode;
			Hierarchy hierarchy = node.getObject();

			addHierarhy(axis, hierarchy, position);
		} else if (sourceNode instanceof LevelNode) {
			LevelNode node = (LevelNode) sourceNode;
			Level level = node.getObject();

			addLevel(axis, level, position);
		} else if (sourceNode instanceof MeasureNode) {
			MeasureNode node = (MeasureNode) sourceNode;
			Member member = node.getObject();

			if (member.getHierarchy().equals(targetNode.getData())) {
				addMember(axis, member);
			} else {
				addMember(axis, member, position);
			}
		}
	}

	/**
	 * @param e
	 */
	public void onDropOnMember(DragDropEvent e) {
		List<Integer> sourcePath = getNodePath(e.getDragId());
		List<Integer> targetPath = getNodePath(e.getDropId());

		int position = targetPath.get(targetPath.size() - 1) + 1;

		boolean fromNavigator = isSourceNode(e.getDragId());

		TreeNode root = fromNavigator ? getCubeNode() : getTargetNode();

		TreeNode source = findNodeFromPath(root, sourcePath);
		TreeNode target = findNodeFromPath(getTargetNode(), targetPath);

		Member member;

		if (fromNavigator) {
			if (!(source instanceof MeasureNode)) {
				return;
			}

			member = ((MeasureNode) source).getObject();

			Axis axis = (Axis) target.getParent().getParent().getData();
			addMember(axis, member, position);
		} else {
			if (!(source.getData() instanceof Member)) {
				return;
			}

			member = (Member) source.getData();
			moveMember(member, position);
		}
	}

	/**
	 * @param axis
	 * @param hierarchy
	 */
	protected void addHierarhy(Axis axis, Hierarchy hierarchy) {
		addHierarhy(axis, hierarchy, 0);
	}

	/**
	 * @param axis
	 * @param hierarchy
	 * @param position
	 */
	protected void addHierarhy(Axis axis, Hierarchy hierarchy, int position) {
		for (Axis ax : new Axis[] { Axis.COLUMNS, Axis.ROWS, Axis.FILTER }) {
			List<Hierarchy> hiersInAxis = getHierarchies(ax);

			if (hiersInAxis.contains(hierarchy)) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("warn.hierarchy.exists.title");
				String message = String.format(
						bundle.getString("warn.hierarchy.exists.message"),
						ax.name());

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_WARN, title, message));
				return;
			}
		}

		PlaceHierarchiesOnAxes transform = getModel().getTransform(
				PlaceHierarchiesOnAxes.class);

		transform.addHierarchy(axis, hierarchy, false, position);
	}

	/**
	 * @param axis
	 * @param hierarchy
	 * @param position
	 */
	protected void moveHierarhy(Axis axis, Hierarchy hierarchy, int position) {
		PlaceHierarchiesOnAxes transform = getModel().getTransform(
				PlaceHierarchiesOnAxes.class);
		transform.moveHierarchy(axis, hierarchy, position);
	}

	/**
	 * @param axis
	 * @param hierarchy
	 */
	protected void removeHierarhy(Axis axis, Hierarchy hierarchy) {
		PlaceHierarchiesOnAxes transform = getModel().getTransform(
				PlaceHierarchiesOnAxes.class);
		transform.removeHierarchy(axis, hierarchy);
	}

	/**
	 * @param axis
	 * @param level
	 */
	protected void addLevel(Axis axis, Level level) {
		addLevel(axis, level, 0);
	}

	/**
	 * @param axis
	 * @param level
	 * @param position
	 */
	protected void addLevel(Axis axis, Level level, int position) {
		Hierarchy hierarchy = level.getHierarchy();

		for (Axis ax : new Axis[] { Axis.COLUMNS, Axis.ROWS, Axis.FILTER }) {
			if (ax.equals(axis)) {
				continue;
			}

			List<Hierarchy> hiersInAxis = getHierarchies(ax);

			if (hiersInAxis.contains(hierarchy)) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("warn.level.exists.title");
				String message = String.format(
						bundle.getString("warn.level.exists.message"),
						ax.name());

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_WARN, title, message));
				return;
			}
		}

		PlaceLevelsOnAxes transform = getModel().getTransform(
				PlaceLevelsOnAxes.class);
		transform.addLevel(axis, level, position);
	}

	/**
	 * @param axis
	 * @param level
	 */
	protected void removeLevel(Axis axis, Level level) {
		PlaceLevelsOnAxes transform = getModel().getTransform(
				PlaceLevelsOnAxes.class);
		transform.removeLevel(axis, level);
	}

	/**
	 * @param member
	 * @param position
	 */
	protected void addMember(Member member, int position) {
		PlaceMembersOnAxes transform = model
				.getTransform(PlaceMembersOnAxes.class);

		transform.addMember(member, position);
	}

	/**
	 * @param axis
	 * @param member
	 */
	protected void addMember(Axis axis, Member member) {
		addMember(axis, member, 0);
	}

	/**
	 * @param axis
	 * @param member
	 * @param position
	 */
	protected void addMember(Axis axis, Member member, int position) {
		Hierarchy hierarchy = member.getHierarchy();

		for (Axis ax : new Axis[] { Axis.COLUMNS, Axis.ROWS, Axis.FILTER }) {
			if (ax.equals(axis)) {
				continue;
			}

			List<Hierarchy> hiersInAxis = getHierarchies(ax);

			if (hiersInAxis.contains(hierarchy)) {
				FacesContext context = FacesContext.getCurrentInstance();

				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "msg");

				String title = bundle.getString("warn.member.exists.title");
				String message = String.format(
						bundle.getString("warn.member.exists.message"),
						ax.name());

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_WARN, title, message));
				return;
			}
		}

		PlaceMembersOnAxes transform = getModel().getTransform(
				PlaceMembersOnAxes.class);
		transform.addMember(axis, member, position);
	}

	/**
	 * @param member
	 * @param position
	 */
	protected void moveMember(Member member, int position) {
		PlaceMembersOnAxes transform = model
				.getTransform(PlaceMembersOnAxes.class);

		transform.moveMember(member, position);
	}

	/**
	 * @param member
	 */
	protected void removeMember(Member member) {
		PlaceMembersOnAxes transform = getModel().getTransform(
				PlaceMembersOnAxes.class);
		transform.removeMember(member);
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
		this.cubeNode = null;
		this.targetNode = null;

		this.dimensions = null;
		this.hierarchies = null;
		this.levels = null;
		this.members = null;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isSelected(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isSelected(T element) {
		if (element instanceof Dimension) {
			Dimension dimension = (Dimension) element;
			return getDimensions(Axis.COLUMNS).contains(dimension)
					|| getDimensions(Axis.ROWS).contains(dimension);
		}

		if (element instanceof Hierarchy) {
			Hierarchy hierarchy = (Hierarchy) element;
			return getHierarchies(Axis.COLUMNS).contains(hierarchy)
					|| getHierarchies(Axis.ROWS).contains(hierarchy);
		}

		if (element instanceof Level) {
			Level level = (Level) element;
			return getLevels(level.getHierarchy()).contains(level);
		}

		if (element instanceof Member) {
			Member member = (Member) element;
			return getMembers(member.getHierarchy()).contains(member);
		}

		return false;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isSelectable(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isSelectable(T element) {
		return false;
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
		if (element instanceof Member) {
			return !isSelected(element);
		}

		if (element instanceof Hierarchy) {
			return !getHierarchies(Axis.FILTER).contains(element);
		}

		return true;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.NodeFilter#isExpanded(org.olap4j.metadata.MetadataElement)
	 */
	@Override
	public <T extends MetadataElement> boolean isExpanded(T element) {
		if (element instanceof Dimension) {
			return true;
		} else if (element instanceof Hierarchy) {
			Hierarchy hierarchy = (Hierarchy) element;

			boolean isMeasure;

			try {
				isMeasure = hierarchy.getDimension().getDimensionType() == Type.MEASURE;
			} catch (OlapException e) {
				throw new FacesException(e);
			}

			return isMeasure || isSelected(element);
		}

		return false;
	}
}
