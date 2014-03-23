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

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.ui.CartesianRenderContext;

public class ChartRenderContext extends CartesianRenderContext {

	private int pageCount;

	private int chartCount;

	private int seriesCount;

	private int plotCount;

	private int pageIndex = 0;

	private int chartIndex = 0;

	private int seriesIndex = 0;

	private int plotIndex = 0;

	private List<Member> pagePath = Collections.emptyList();

	private List<Member> chartPath = Collections.emptyList();

	private List<Member> seriesPath = Collections.emptyList();

	private List<Member> plotPath = Collections.emptyList();

	/**
	 * @param model
	 * @param renderer
	 */
	public ChartRenderContext(PivotModel model, ChartRenderer renderer) {
		super(model, renderer);
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
	 * @return the pageCount
	 */
	public int getPageCount() {
		return pageCount;
	}

	/**
	 * @param pageCount
	 *            the pageCount to set
	 */
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	/**
	 * @return the chartCount
	 */
	public int getChartCount() {
		return chartCount;
	}

	/**
	 * @param chartCount
	 *            the chartCount to set
	 */
	public void setChartCount(int chartCount) {
		this.chartCount = chartCount;
	}

	/**
	 * @return the seriesCount
	 */
	public int getSeriesCount() {
		return seriesCount;
	}

	/**
	 * @param seriesCount
	 *            the seriesCount to set
	 */
	public void setSeriesCount(int seriesCount) {
		this.seriesCount = seriesCount;
	}

	/**
	 * @return the plotCount
	 */
	public int getPlotCount() {
		return plotCount;
	}

	/**
	 * @param plotCount
	 *            the plotCount to set
	 */
	public void setPlotCount(int plotCount) {
		this.plotCount = plotCount;
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
			throw new NullArgumentException("pagePath");
		}

		this.pagePath = pagePath;
	}

	/**
	 * @return the chartPath
	 */
	public List<Member> getChartPath() {
		return chartPath;
	}

	/**
	 * @param chartPath
	 *            the chartPath to set
	 */
	public void setChartPath(List<Member> chartPath) {
		if (chartPath == null) {
			throw new NullArgumentException("chartPath");
		}

		this.chartPath = chartPath;
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
			throw new NullArgumentException("seriesPath");
		}

		this.seriesPath = seriesPath;
	}

	/**
	 * @return the plotPath
	 */
	public List<Member> getPlotPath() {
		return plotPath;
	}

	/**
	 * @param plotPath
	 *            the plotPath to set
	 */
	public void setPlotPath(List<Member> plotPath) {
		if (plotPath == null) {
			throw new NullArgumentException("plotPath");
		}

		this.plotPath = plotPath;
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

		context.put("pageCount", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return pageCount;
			}
		});

		context.put("chartCount",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return chartCount;
					}
				});

		context.put("seriesCount",
				new ExpressionContext.ValueBinding<Integer>() {

					@Override
					public Integer getValue() {
						return seriesCount;
					}
				});

		context.put("plotCount", new ExpressionContext.ValueBinding<Integer>() {

			@Override
			public Integer getValue() {
				return plotCount;
			}
		});

		context.put("pagePath",
				new ExpressionContext.ValueBinding<List<Member>>() {

					@Override
					public List<Member> getValue() {
						return pagePath;
					}
				});

		context.put("chartPath",
				new ExpressionContext.ValueBinding<List<Member>>() {

					@Override
					public List<Member> getValue() {
						return chartPath;
					}
				});

		context.put("seriesPath",
				new ExpressionContext.ValueBinding<List<Member>>() {

					@Override
					public List<Member> getValue() {
						return seriesPath;
					}
				});

		return context;
	}
}
