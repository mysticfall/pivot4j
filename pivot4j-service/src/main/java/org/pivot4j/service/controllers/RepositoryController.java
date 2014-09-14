/*
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.service.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.pivot4j.service.repository.ReportFile;
import org.pivot4j.service.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repository")
public class RepositoryController implements ServiceController {

	@Autowired
	private ReportRepository reportRepository;

	@RequestMapping(value = "/info", method = RequestMethod.GET, headers = HEADER_JSON)
	public ReportFile getFile() throws IOException {
		return reportRepository.getRoot();
	}

	@RequestMapping(value = "/{id}/info", method = RequestMethod.GET, headers = HEADER_JSON)
	public ReportFile getFile(@PathVariable String id,
			HttpServletResponse response) throws IOException {
		ReportFile file = reportRepository.getFileById(id);

		if (file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Unable to find a file with the specified id: " + id);
		}

		return file;
	}

	@RequestMapping(value = "/children", method = RequestMethod.GET, headers = HEADER_JSON)
	public List<ReportFile> getFiles(HttpServletResponse response)
			throws IOException {
		return reportRepository.getFiles(reportRepository.getRoot());
	}

	@RequestMapping(value = "/{id}/children", method = RequestMethod.GET, headers = HEADER_JSON)
	public List<ReportFile> getFiles(@PathVariable String id,
			HttpServletResponse response) throws IOException {
		ReportFile file = reportRepository.getFileById(id);

		if (file == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND,
					"Unable to find a file with the specified id: " + id);
		} else if (!file.isDirectory()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"The specified file is not a directory: " + id);
		}

		return reportRepository.getFiles(file);
	}

	/**
	 * @return the reportRepository
	 */
	public ReportRepository getReportRepository() {
		return reportRepository;
	}

	/**
	 * @param reportRepository
	 *            the reportRepository to set
	 */
	public void setReportRepository(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}
}