package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.ui.table.TableRenderer;

public class ReportContent implements Serializable {

	private static final long serialVersionUID = 8261947657917338352L;

	private transient HierarchicalConfiguration configuration;

	/**
	 * @param state
	 */
	public ReportContent(ViewState state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		this.configuration = createConfiguration();

		ConnectionInfo connectionInfo = state.getConnectionInfo();

		if (connectionInfo != null) {
			configuration.addProperty("connection", "");
			connectionInfo.saveSettings(configuration.configurationAt(
					"connection", true));
		}

		PivotModel model = state.getModel();
		if (model != null) {
			configuration.addProperty("model", "");
			model.saveSettings(configuration.configurationAt("model", true));
		}

		if (state.getRendererState() != null) {
			TableRenderer renderer = new TableRenderer();

			renderer.restoreState(state.getRendererState());

			configuration.addProperty("render", "");
			renderer.saveSettings(configuration.configurationAt("render"));
		}
	}

	/**
	 * @param in
	 * @throws ConfigurationException
	 */
	public ReportContent(InputStream in) throws ConfigurationException {
		if (in == null) {
			throw new NullArgumentException("in");
		}

		FileConfiguration config = (FileConfiguration) createConfiguration();
		config.load(in);

		this.configuration = (HierarchicalConfiguration) config;
	}

	/**
	 * Constructor used in serialization.
	 */
	ReportContent() {
	}

	/**
	 * @param out
	 * @throws ConfigurationException
	 */
	public void write(OutputStream out) throws ConfigurationException {
		FileConfiguration config = (FileConfiguration) this.configuration;
		config.save(out);
	}

	/**
	 * @return the configuration
	 */
	protected HierarchicalConfiguration createConfiguration() {
		XMLConfiguration config = new XMLConfiguration();

		config.setRootElementName("report");
		config.setDelimiterParsingDisabled(true);

		return config;
	}

	/**
	 * @param state
	 * @param manager
	 * @return
	 */
	public ViewState read(ViewState state, DataSourceManager manager)
			throws ConfigurationException, DataSourceNotFoundException {
		ConnectionInfo connectionInfo = new ConnectionInfo();

		try {
			connectionInfo.restoreSettings(configuration
					.configurationAt("connection"));
		} catch (IllegalArgumentException e) {
		}

		state.setConnectionInfo(connectionInfo);

		OlapDataSource dataSource = manager.getDataSource(connectionInfo);

		if (dataSource == null) {
			throw new DataSourceNotFoundException(connectionInfo);
		}

		PivotModel model = new PivotModelImpl(dataSource);

		try {
			model.restoreSettings(configuration.configurationAt("model"));
		} catch (IllegalArgumentException e) {
		}

		Map<String, Object> parameters = state.getParameters();

		if (parameters == null) {
			parameters = Collections.emptyMap();
		}

		model.getExpressionContext().put("parameters", parameters);

		state.setModel(model);

		try {
			TableRenderer renderer = new TableRenderer();
			renderer.restoreSettings(configuration.configurationAt("render"));

			state.setRendererState(renderer.saveState());
		} catch (IllegalArgumentException e) {
		}

		return state;
	}

	/**
	 * @param in
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream in) throws IOException {
		this.configuration = createConfiguration();

		FileConfiguration fileConfig = (FileConfiguration) configuration;

		try {
			fileConfig.load(in);
		} catch (ConfigurationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		FileConfiguration fileConfig = (FileConfiguration) configuration;

		try {
			fileConfig.save(out);
		} catch (ConfigurationException e) {
			throw new IOException(e);
		}
	}
}
