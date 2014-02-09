<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE stylesheet [
  <!ENTITY tab "<xsl:text>&#9;</xsl:text>">
  <!ENTITY cr "<xsl:text>
</xsl:text>">
]>
<xsl:stylesheet version="1.0"
	xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:faces-config="http://java.sun.com/xml/ns/javaee">
	<xsl:output method="xml" indent="yes" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*/faces-config:resource-bundle[last()]">
		<xsl:copy-of select="." />&cr;&tab;&tab;
		<resource-bundle>&cr;&tab;&tab;&tab;
			<base-name>webapp.resources.pivot4j.i18n.messages</base-name>&cr;&tab;&tab;&tab;
			<var>plugin_msg</var>&cr;&tab;&tab;
		</resource-bundle>&cr;
	</xsl:template>

	<xsl:template match="*/faces-config:lifecycle[last()]">
		<xsl:copy-of select="." />&cr;&cr;&tab;
		<managed-bean>&cr;&tab;&tab;
			<managed-bean-name>dataSourceManager</managed-bean-name>&cr;&tab;&tab;
			<managed-bean-class>org.pivot4j.pentaho.datasource.PentahoDataSourceManager
			</managed-bean-class>&cr;&tab;&tab;
			<managed-bean-scope>session</managed-bean-scope>&cr;&tab;
		</managed-bean>&cr;&tab;
		<managed-bean>&cr;&tab;&tab;
			<managed-bean-name>reportRepository</managed-bean-name>&cr;&tab;&tab;
			<managed-bean-class>org.pivot4j.pentaho.repository.PentahoReportRepository
			</managed-bean-class>&cr;&tab;&tab;
			<managed-bean-scope>session</managed-bean-scope>&cr;&tab;
		</managed-bean>&cr;&tab;
		<managed-bean>&cr;&tab;&tab;
			<managed-bean-name>reportOpener</managed-bean-name>&cr;&tab;&tab;
			<managed-bean-class>org.pivot4j.pentaho.ui.PentahoReportOpener
			</managed-bean-class>&cr;&tab;&tab;
			<managed-bean-scope>session</managed-bean-scope>&cr;&tab;&tab;
			<managed-property>&cr;&tab;&tab;&tab;
				<property-name>settings</property-name>&cr;&tab;&tab;&tab;
				<value>#{settings}</value>&cr;&tab;&tab;
			</managed-property>&cr;&tab;&tab;
			<managed-property>&cr;&tab;&tab;&tab;
				<property-name>viewStateHolder</property-name>&cr;&tab;&tab;&tab;
				<value>#{viewStateHolder}</value>&cr;&tab;&tab;
			</managed-property>&cr;&tab;&tab;
			<managed-property>&cr;&tab;&tab;&tab;
				<property-name>dataSourceManager</property-name>&cr;&tab;&tab;&tab;
				<value>#{dataSourceManager}</value>&cr;&tab;&tab;
			</managed-property>&cr;&tab;&tab;
			<managed-property>&cr;&tab;&tab;&tab;
				<property-name>reportRepository</property-name>&cr;&tab;&tab;&tab;
				<value>#{reportRepository}</value>&cr;&tab;&tab;
			</managed-property>&cr;&tab;
		</managed-bean>
	</xsl:template>
</xsl:stylesheet>