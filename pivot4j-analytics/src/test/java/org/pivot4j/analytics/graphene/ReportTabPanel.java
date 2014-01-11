package org.pivot4j.analytics.graphene;

public class ReportTabPanel extends TabPanel<ReportPage> {

	/**
	 * @see org.pivot4j.analytics.graphene.TabPanel#getPageType()
	 */
	@Override
	protected Class<ReportPage> getPageType() {
		return ReportPage.class;
	}
}
