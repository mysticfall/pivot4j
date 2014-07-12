package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.MergeCombiner;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.ui.DefaultTableRenderer;
import org.pivot4j.analytics.ui.LayoutRegion;
import org.pivot4j.analytics.ui.chart.DefaultChartRenderer;
import org.pivot4j.impl.PivotModelImpl;

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
			DefaultTableRenderer renderer = new DefaultTableRenderer();

			renderer.restoreState(state.getRendererState());

			configuration.addProperty("render", "");
			renderer.saveSettings(configuration.configurationAt("render"));
		}

		if (state.getChartState() != null) {
			DefaultChartRenderer renderer = new DefaultChartRenderer();

			renderer.restoreState(state.getChartState());

			configuration.addProperty("chart", "");
			renderer.saveSettings(configuration.configurationAt("chart"));
		}

		Map<LayoutRegion, Boolean> regions = state.getLayoutRegions();

		configuration.addProperty("views", "");

		HierarchicalConfiguration views = configuration.configurationAt(
				"views", true);

		int index = 0;

		MessageFormat mf = new MessageFormat("view({0})[@{1}]");

		for (LayoutRegion region : regions.keySet()) {
			Boolean visibility = regions.get(region);

			if (visibility == null) {
				visibility = false;
			}

			views.addProperty(
					mf.format(new String[] { Integer.toString(index), "name" }),
					region.name());
			views.addProperty(mf.format(new String[] { Integer.toString(index),
					"visible" }), visibility.toString());

			index++;
		}
	}

	/**
	 * @param in
	 * @throws ConfigurationException
	 */
	public ReportContent(InputStream in) throws IOException,
			ConfigurationException {
		if (in == null) {
			throw new NullArgumentException("in");
		}

		FileConfiguration config = (FileConfiguration) createConfiguration();
		config.load(new InputStreamReader(in, "UTF-8"));

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
	public void write(OutputStream out) throws IOException,
			ConfigurationException {
		if (out == null) {
			throw new NullArgumentException("out");
		}

		FileConfiguration config = (FileConfiguration) this.configuration;
		config.save(new OutputStreamWriter(out, "UTF-8"));
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
	 * @param defaultSettings
	 * @return
	 * @throws ConfigurationException
	 * @throws DataSourceNotFoundException
	 */
	public ViewState read(ViewState state, DataSourceManager manager,
			HierarchicalConfiguration defaultSettings)
			throws ConfigurationException, DataSourceNotFoundException {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		if (manager == null) {
			throw new NullArgumentException("manager");
		}

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

		CombinedConfiguration mergedSettings = new CombinedConfiguration();
		mergedSettings.setNodeCombiner(new MergeCombiner());

		if (defaultSettings != null) {
			mergedSettings.addConfiguration(defaultSettings);
		}

		mergedSettings.addConfiguration(configuration);

		PivotModel model = new PivotModelImpl(dataSource);

		try {
			model.restoreSettings(mergedSettings.configurationAt("model"));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		Map<String, Object> parameters = state.getParameters();

		if (parameters == null) {
			parameters = Collections.emptyMap();
		}

		model.getExpressionContext().put("parameters", parameters);

		state.setModel(model);

		try {
			DefaultTableRenderer renderer = new DefaultTableRenderer();

			renderer.restoreSettings(mergedSettings.configurationAt("render"));

			state.setRendererState(renderer.saveState());
		} catch (IllegalArgumentException e) {
		}

		try {
			DefaultChartRenderer renderer = new DefaultChartRenderer();
			renderer.restoreSettings(configuration.configurationAt("chart"));

			state.setChartState(renderer.saveState());
		} catch (IllegalArgumentException e) {
		}

		state.getLayoutRegions().clear();

		List<HierarchicalConfiguration> views = configuration
				.configurationsAt("views.view");

		for (HierarchicalConfiguration view : views) {
			LayoutRegion region = LayoutRegion.valueOf(view
					.getString("[@name]"));

			boolean visibility = view.getBoolean("[@visible]", true);

			state.setRegionVisible(region, visibility);
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
