package org.pivot4j.service.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.NullArgumentException;

public class ReportContent implements Serializable {

	private static final long serialVersionUID = 8261947657917338352L;

	private transient HierarchicalConfiguration configuration;

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
