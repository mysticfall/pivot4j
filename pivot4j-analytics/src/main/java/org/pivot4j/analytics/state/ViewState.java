package org.pivot4j.analytics.state;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.ui.LayoutRegion;

public class ViewState {

	private String id;

	private String name;

	private boolean dirty = false;

	private ReportFile file;

	private boolean readOnly = false;

	private boolean editable = true;

	private Date lastActive = new Date();

	private ConnectionInfo connectionInfo;

	private PivotModel model;

	private Serializable rendererState;

	private Serializable chartState;

	private Map<String, Object> parameters;

	private Map<LayoutRegion, Boolean> layoutRegions;

	/**
	 * @param id
	 * @param name
	 */
	public ViewState(String id, String name) {
		if (id == null) {
			throw new NullArgumentException("id");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		this.id = id;
		this.name = name;
		this.layoutRegions = new HashMap<LayoutRegion, Boolean>();
	}

	/**
	 * @param id
	 * @param name
	 * @param connectionInfo
	 * @param model
	 * @param file
	 */
	public ViewState(String id, String name, ConnectionInfo connectionInfo,
			PivotModel model, ReportFile file) {
		if (id == null) {
			throw new NullArgumentException("id");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		this.id = id;
		this.name = name;
		this.connectionInfo = connectionInfo;
		this.model = model;
		this.file = file;
		this.layoutRegions = new HashMap<LayoutRegion, Boolean>();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		this.name = name;
	}

	/**
	 * @return the file
	 */
	public ReportFile getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(ReportFile file) {
		this.file = file;
	}

	/**
	 * @return the lastActive
	 */
	public Date getLastActive() {
		return lastActive;
	}

	/**
	 * @return the connectionInfo
	 */
	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	/**
	 * @param connectionInfo
	 *            the connectionInfo to set
	 */
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly
	 *            the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
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
	 * @return the rendererState
	 */
	public Serializable getRendererState() {
		return rendererState;
	}

	/**
	 * @param rendererState
	 *            the rendererState to set
	 */
	public void setRendererState(Serializable rendererState) {
		this.rendererState = rendererState;
	}

	/**
	 * @return the chartState
	 */
	public Serializable getChartState() {
		return chartState;
	}

	/**
	 * @param chartState
	 *            the chartState to set
	 */
	public void setChartState(Serializable chartState) {
		this.chartState = chartState;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the layoutRegions
	 */
	public Map<LayoutRegion, Boolean> getLayoutRegions() {
		return layoutRegions;
	}

	/**
	 * @param region
	 * @return
	 */
	public boolean isRegionVisible(LayoutRegion region) {
		if (region == null) {
			throw new NullArgumentException("region");
		}

		return !Boolean.FALSE.equals(layoutRegions.get(region));
	}

	/**
	 * @param region
	 * @param visible
	 */
	public void setRegionVisible(LayoutRegion region, boolean visible) {
		if (region == null) {
			throw new NullArgumentException("region");
		}

		layoutRegions.put(region, visible);
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * @param dirty
	 *            the dirty to set
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void update() {
		this.lastActive = new Date();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name)
				.append("connectionInfo", connectionInfo).toString();
	}
}
