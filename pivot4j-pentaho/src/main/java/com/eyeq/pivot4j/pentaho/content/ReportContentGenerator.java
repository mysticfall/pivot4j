package com.eyeq.pivot4j.pentaho.content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.services.solution.BaseContentGenerator;

public class ReportContentGenerator extends BaseContentGenerator {

	private static final long serialVersionUID = 7257498161100674425L;

	private transient Log log;

	/**
	 * @see org.pentaho.platform.engine.services.solution.BaseContentGenerator#getLogger()
	 */
	@Override
	public Log getLogger() {
		if (log == null) {
			this.log = LogFactory.getLog(getClass());
		}

		return log;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return false;
	}

	/**
	 * @see org.pentaho.platform.engine.services.solution.BaseContentGenerator#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		HttpServletRequest request = (HttpServletRequest) this.parameterProviders
				.get("path").getParameter("httprequest");

		HttpServletResponse response = (HttpServletResponse) this.parameterProviders
				.get("path").getParameter("httpresponse");

		RepositoryFile file = (RepositoryFile) parameterProviders.get("path")
				.getParameter("file");

		request.setAttribute("file", file);
		request.getRequestDispatcher(
				"/plugin/pivot4j/faces/index.xhtml?editable=" + isEditable())
				.forward(request, response);
	}
}
