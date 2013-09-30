/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.pentaho.content;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.Converter;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.pentaho.repository.PentahoReportFile;

/**
 * XXX Temporary workaround for file export problem. Nobody in right mind should
 * ever have to resort to such a horrible hack to make things work. We really
 * need more sane method for plugins to register converters.
 */
public class ConverterRegistrationHelper {

	@PostConstruct
	protected void initialize() {
		Logger log = LoggerFactory.getLogger(getClass());

		if (log.isInfoEnabled()) {
			log.info("Trying to register a content converter.");
		}

		try {
			DefaultExportHandler handler = PentahoSystem
					.get(DefaultExportHandler.class);

			Field field = handler.getClass().getDeclaredField("converters");
			field.setAccessible(true);

			try {
				@SuppressWarnings("unchecked")
				Map<String, Converter> converters = (Map<String, Converter>) field
						.get(handler);

				Converter streamConverter = null;

				for (Converter converter : converters.values()) {
					if (converter instanceof StreamConverter) {
						streamConverter = converter;
					}
				}

				if (streamConverter != null) {
					if (log.isInfoEnabled()) {
						log.info(String
								.format("Registering converter for extension '%s' : %s",
										PentahoReportFile.DEFAULT_EXTENSION,
										streamConverter));
					}

					converters.put(PentahoReportFile.DEFAULT_EXTENSION,
							streamConverter);
				}

			} finally {
				field.setAccessible(false);
			}
		} catch (Exception e) {
			if (log.isWarnEnabled()) {
				log.warn(String.format(
						"Failed to register a converter for extension '%s' : "
								+ e, PentahoReportFile.DEFAULT_EXTENSION), e);
			}
		}
	}
}
