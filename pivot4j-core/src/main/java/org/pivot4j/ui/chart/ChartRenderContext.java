/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.chart;

import java.util.Collections;
import java.util.List;

import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.ui.CartesianRenderContext;

public class ChartRenderContext extends CartesianRenderContext {

	private int chartsPerPage;

	private int pageIndex = 0;

	private int chartIndex = 0;

	private int seriesIndex = 0;

	private int plotIndex = 0;

	private List<Member> pagePath = Collections.emptyList();

	private List<Member> seriesPath = Collections.emptyList();

	/**
	 * @param model
	 * @param renderer
	 * @param chartsPerPage
	 */
	public ChartRenderContext(PivotModel model, ChartRenderer renderer,
			int chartsPerPage) {
		super(model, renderer);

		this.chartsPerPage = chartsPerPage;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * @param pageIndex
	 *            the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the seriesIndex
	 */
	public int getSeriesIndex() {
		return seriesIndex;
	}

	/**
	 * @param seriesIndex
	 *            the seriesIndex to set
	 */
	public void setSeriesIndex(int seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	/**
	 * @return the chartIndex
	 */
	public int getChartIndex() {
		return chartIndex;
	}

	/**
	 * @param chartIndex
	 *            the chartIndex to set
	 */
	public void setChartIndex(int chartIndex) {
		this.chartIndex = chartIndex;
	}

	/**
	 * @return the plotIndex
	 */
	public int getPlotIndex() {
		return plotIndex;
	}

	/**
	 * @param plotIndex
	 *            the plotIndex to set
	 */
	public void setPlotIndex(int plotIndex) {
		this.plotIndex = plotIndex;
	}

	/**
	 * @return the pagePath
	 */
	public List<Member> getPagePath() {
		return pagePath;
	}

	/**
	 * @param pagePath
	 *            the pagePath to set
	 */
	public void setPagePath(List<Member> pagePath) {
		if (pagePath == null) {
			this.pagePath = Collections.emptyList();
		} else {
			this.pagePath = pagePath;
		}
	}

	/**
	 * @return the seriesPath
	 */
	public List<Member> getSeriesPath() {
		return seriesPath;
	}

	/**
	 * @param seriesPath
	 *            the seriesPath to set
	 */
	public void setSeriesPath(List<Member> seriesPath) {
		if (seriesPath == null) {
			this.seriesPath = Collections.emptyList();
		} else {
			this.seriesPath = seriesPath;
		}
	}

	/**
	 * @return the chartstPerPage
	 */
	public int getChartsPerPage() {
		return chartsPerPage;
	}

	/**
	 * @param chartsPerPage
	 *            the chartsPerPage to set
	 */
	public void setChartsPerPage(int chartsPerPage) {
		this.chartsPerPage = chartsPerPage;
	}

	/**
	 * @see org.pivot4j.ui.CartesianRenderContext#createExpressionContext(org.pivot4j.PivotModel)
	 */
	@Override
	protected ExpressionContext createExpressionContext(PivotModel model) {
		ExpressionContext context = super.createExpressionContext(model);

		context.put("pageIndex", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return pageIndex;
			}
		});

		context.put("chartIndex",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return chartIndex;
					}
				});

		context.put("seriesIndex",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return seriesIndex;
					}
				});

		context.put("plotIndex", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return plotIndex;
			}
		});

		context.put("pagePath",
				new ExpressionContext.ValueBinding<List<Member>>() {

					@Override
					public List<Member> getValue() {
						return pagePath;
					}
				});

		context.put("seriesPath",
				new ExpressionContext.ValueBinding<List<Member>>() {

					@Override
					public List<Member> getValue() {
						return seriesPath;
					}
				});

		context.put("chartsPerPage",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return chartsPerPage;
					}
				});

		return context;
	}
}
