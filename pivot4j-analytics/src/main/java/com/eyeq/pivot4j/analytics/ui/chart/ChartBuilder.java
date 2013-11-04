package com.eyeq.pivot4j.analytics.ui.chart;

import javax.faces.component.UIComponent;

import com.eyeq.pivot4j.ui.chart.ChartRenderCallback;

public interface ChartBuilder extends ChartRenderCallback {

	String getName();

	UIComponent getComponent();

	void setComponent(UIComponent component);
}
