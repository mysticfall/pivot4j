package org.pivot4j.analytics.ui.chart;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.ui.chart.ChartRenderer;

public class DefaultChartRenderer extends ChartRenderer {

	private String chartName;

	private int width = 0;

	private int height = 300;

	private Position legendPosition = Position.w;

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
	 * @see org.pivot4j.ui.chart.ChartRenderer#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[5];

		int index = 0;

		states[index++] = super.saveState();
		states[index++] = chartName;
		states[index++] = width;
		states[index++] = height;

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

		String position = configuration.getString("legend.position",
				Position.w.name());

		this.legendPosition = Position.valueOf(position);
	}

	public enum Position {

		n, w, s, e
	}
}
