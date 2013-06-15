package com.eyeq.pivot4j.analytics.state;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.analytics.datasource.ConnectionInfo;
import com.eyeq.pivot4j.analytics.repository.RepositoryFile;

public class ViewState {

	private String id;

	private String name;

	private boolean dirty = false;

	private RepositoryFile file;

	private boolean readOnly = false;

	private Date lastActive = new Date();

	private ConnectionInfo connectionInfo;

	private PivotModel model;

	private Serializable rendererState;

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
	}

	/**
	 * @param id
	 * @param name
	 * @param connectionInfo
	 * @param model
	 * @param file
	 */
	public ViewState(String id, String name, ConnectionInfo connectionInfo,
			PivotModel model, RepositoryFile file) {
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
		this.name = name;
	}

	/**
	 * @return the file
	 */
	public RepositoryFile getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(RepositoryFile file) {
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
