package org.pivot4j.analytics.ui.chart;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.pivot4j.PivotException;

@ManagedBean(name = "chartBuilderFactory")
@ApplicationScoped
public class ChartBuilderFactory {

	private Map<String, Class<? extends ChartBuilder>> builders;

	public ChartBuilderFactory() {
		this.builders = new LinkedHashMap<String, Class<? extends ChartBuilder>>();

		registerDefaultChartBuilders(builders);
	}

	/**
	 * @param builders
	 */
	protected void registerDefaultChartBuilders(
			Map<String, Class<? extends ChartBuilder>> builders) {
		builders.put(PieChartBuilder.NAME, PieChartBuilder.class);
		builders.put(BarChartBuilder.NAME, BarChartBuilder.class);
		builders.put(LineChartBuilder.NAME, LineChartBuilder.class);
		builders.put(HorizontalBarChartBuilder.NAME,
				HorizontalBarChartBuilder.class);
		builders.put(StackedBarChartBuilder.NAME, StackedBarChartBuilder.class);
		builders.put(StackedAreaChartBuilder.NAME,
				StackedAreaChartBuilder.class);
	}

	public List<String> getBuilderNames() {
		return new LinkedList<String>(builders.keySet());
	}

	/**
	 * @param name
	 * @param context
	 * @return
	 */
	public <T extends ChartBuilder> T createChartBuilder(String name,
			FacesContext context) {
		T builder = null;

		@SuppressWarnings("unchecked")
		Class<T> implementationType = (Class<T>) builders.get(name);

		if (implementationType != null) {
			try {
				Constructor<T> constructor = implementationType
						.getConstructor(FacesContext.class);
				builder = constructor.newInstance(context);
			} catch (NoSuchMethodException e) {
				String msg = "The registered implementation class does not have a suitable constructor : "
						+ implementationType;
				throw new IllegalArgumentException(msg);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				String msg = "Failed to instantiate the chart builder class : "
						+ implementationType;
				throw new PivotException(msg, e);
			}
		}

		return builder;
	}
}
