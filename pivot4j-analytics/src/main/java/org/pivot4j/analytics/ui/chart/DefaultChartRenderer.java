package org.pivot4j.analytics.ui.chart;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.ui.chart.ChartRenderer;

public class DefaultChartRenderer extends ChartRenderer {

	private String chartName;

	private int width = 0;

	private int height = 300;

	private Position legendPosition = Position.w;

	private int xAxisAngle = 30;

	private int yAxisAngle = 0;

	/**
	 * @return the chartName
	 */
	public String getChartName() {
		return chartName;
	}

	/**
	 * @param chartName
	 *            the chartName to set
	 */
	public void setChartName(String chartName) {
		this.chartName = chartName;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the legendPosition
	 */
	public Position getLegendPosition() {
		return legendPosition;
	}

	/**
	 * @param legendPosition
	 *            the legendPosition to set
	 */
	public void setLegendPosition(Position legendPosition) {
		this.legendPosition = legendPosition;
	}

	/**
	 * @return the xAxisAngle
	 */
	public int getXAxisAngle() {
		return xAxisAngle;
	}

	/**
	 * @param xAxisAngle
	 *            the xAxisAngle to set
	 */
	public void setXAxisAngle(int xAxisAngle) {
		this.xAxisAngle = xAxisAngle;
	}

	/**
	 * @return the yAxisAngle
	 */
	public int getYAxisAngle() {
		return yAxisAngle;
	}

	/**
	 * @param yAxisAngle
	 *            the yAxisAngle to set
	 */
	public void setYAxisAngle(int yAxisAngle) {
		this.yAxisAngle = yAxisAngle;
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderer#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[7];

		int index = 0;

		states[index++] = super.saveState();
		states[index++] = chartName;
		states[index++] = width;
		states[index++] = height;
		states[index++] = xAxisAngle;
		states[index++] = yAxisAngle;

		String position = null;

		if (legendPosition != null) {
			position = legendPosition.name();
		}

		states[index++] = position;

		return states;
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		int index = 0;

		super.restoreState(states[index++]);

		this.chartName = (String) states[index++];
		this.width = (Integer) states[index++];
		this.height = (Integer) states[index++];
		this.xAxisAngle = (Integer) states[index++];
		this.yAxisAngle = (Integer) states[index++];

		String position = (String) states[index++];

		if (position == null) {
			this.legendPosition = null;
		} else {
			this.legendPosition = Position.valueOf(position);
		}
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderer#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		configuration.addProperty("[@type]", chartName);
		configuration.addProperty("dimension[@width]", width);
		configuration.addProperty("dimension[@height]", height);

		configuration.addProperty("axes.axis(0)[@name]", "x");
		configuration.addProperty("axes.axis(0).label[@angle]", xAxisAngle);

		configuration.addProperty("axes.axis(1)[@name]", "y");
		configuration.addProperty("axes.axis(1).label[@angle]", yAxisAngle);

		if (legendPosition != null) {
			configuration.addProperty("legend.position", legendPosition.name());
		}
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderer#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.chartName = configuration.getString("[@type]");
		this.width = configuration.getInt("dimension[@width]", 0);
		this.height = configuration.getInt("dimension[@height]", 0);

		List<HierarchicalConfiguration> axisConfigs = configuration
				.configurationsAt("axes.axis");

		for (HierarchicalConfiguration axisConfig : axisConfigs) {
			if (axisConfig.getString("[@name]", "x").equals("x")) {
				this.xAxisAngle = axisConfig.getInt("label[@angle]", 30);
			} else {
				this.yAxisAngle = axisConfig.getInt("label[@angle]", 0);
			}
		}

		String position = configuration.getString("legend.position",
				Position.w.name());

		this.legendPosition = Position.valueOf(position);
	}

	public enum Position {

		n, w, s, e
	}
}
