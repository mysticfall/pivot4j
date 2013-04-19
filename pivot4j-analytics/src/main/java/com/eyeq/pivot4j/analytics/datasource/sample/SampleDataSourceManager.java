package com.eyeq.pivot4j.analytics.datasource.sample;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.olap4j.OlapDataSource;

import com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.datasource.SimpleOlapDataSource;

@ManagedBean(name = "dataSourceManager")
@ApplicationScoped
public class SampleDataSourceManager extends AbstractDataSourceManager {

	private OlapDataSource dataSource;

	@PostConstruct
	protected void initialize() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) facesContext
				.getExternalContext().getContext();

		String database = servletContext.getRealPath("/WEB-INF/foodmart");
		String schema = servletContext.getRealPath("/WEB-INF/FoodMart.xml");

		if (log.isInfoEnabled()) {
			log.info("Initializing a sample OLAP datasource.");
			log.info("	- database path : " + database);
			log.info("	- schema path : " + schema);
		}

		String driverName = "mondrian.olap4j.MondrianOlap4jDriver";

		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			String msg = "Failed to load JDBC driver : " + driverName;
			throw new RuntimeException(msg, e);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:mondrian:");
		builder.append("Jdbc=jdbc:derby:");
		builder.append(database);
		builder.append(";");
		builder.append("JdbcDrivers=org.apache.derby.jdbc.EmbeddedDriver;");
		builder.append("JdbcUser=sa;");
		builder.append("Catalog=file:");
		builder.append(schema);
		builder.append(";");

		String url = builder.toString();

		SimpleOlapDataSource dataSource = new SimpleOlapDataSource();
		dataSource.setConnectionString(url);

		this.dataSource = dataSource;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.datasource.AbstractDataSourceManager#doCreateDataSource(com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata)
	 */
	@Override
	protected OlapDataSource doCreateDataSource(
			ConnectionMetadata connectionInfo) {
		return dataSource;
	}
}
