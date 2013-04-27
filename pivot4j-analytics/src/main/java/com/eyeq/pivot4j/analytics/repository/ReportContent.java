package com.eyeq.pivot4j.analytics.repository;

import java.io.InputStream;
import java.io.OutputStream;

import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.analytics.datasource.DataSourceManager;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.ui.PrimeFacesPivotRenderer;
import com.eyeq.pivot4j.impl.PivotModelImpl;

public class ReportContent {

	private HierarchicalConfiguration configuration;

	/**
	 * @param state
	 */
	public ReportContent(ViewState state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		this.configuration = createConfiguration();

		ConnectionMetadata connectionInfo = state.getConnectionInfo();

		if (connectionInfo != null) {
			connectionInfo.saveSettings(configuration);
		}

		PivotModel model = state.getModel();
		if (model != null) {
			model.saveSettings(configuration);
		}

		if (state.getRendererState() != null) {
			PrimeFacesPivotRenderer renderer = new PrimeFacesPivotRenderer(
					FacesContext.getCurrentInstance());

			renderer.restoreState(state.getRendererState());
			renderer.saveSettings(configuration);
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

		FileConfiguration configuration = (FileConfiguration) createConfiguration();
		configuration.load(in);

		this.configuration = (HierarchicalConfiguration) configuration;
	}

	/**
	 * @param out
	 * @throws ConfigurationException
	 */
	public void write(OutputStream out) throws ConfigurationException {
		FileConfiguration configuration = (FileConfiguration) this.configuration;
		configuration.save(out);
	}

	/**
	 * @return the configuration
	 */
	protected HierarchicalConfiguration createConfiguration() {
		XMLConfiguration configuration = new XMLConfiguration();

		configuration.setRootElementName("report");
		configuration.setDelimiterParsingDisabled(true);

		return configuration;
	}

	/**
	 * @param state
	 * @param manager
	 * @return
	 */
	public ViewState read(ViewState state, DataSourceManager manager) {
		ConnectionMetadata connectionInfo = new ConnectionMetadata();
		connectionInfo.restoreSettings(configuration);

		state.setConnectionInfo(connectionInfo);

		OlapDataSource dataSource = manager.getDataSource(connectionInfo);

		PivotModel model = new PivotModelImpl(dataSource);
		model.restoreSettings(configuration);

		state.setModel(model);

		PrimeFacesPivotRenderer renderer = new PrimeFacesPivotRenderer(
				FacesContext.getCurrentInstance());
		renderer.restoreSettings(configuration);

		state.setRendererState(renderer.saveState());

		return state;
	}
}
