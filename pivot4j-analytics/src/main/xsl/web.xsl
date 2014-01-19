<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE stylesheet [
  <!ENTITY tab "<xsl:text>&#9;</xsl:text>">
  <!ENTITY cr "<xsl:text>
</xsl:text>">
]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:web-app="http://java.sun.com/xml/ns/javaee">
	<xsl:output method="xml" indent="yes" />
	<xsl:param name="jsf.project.stage" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template
		match="*/web-app:context-param[contains(./web-app:param-name/text(), 'javax.faces.PROJECT_STAGE')]">
		<context-param>&cr;&tab;&tab;
			<param-name>javax.faces.PROJECT_STAGE</param-name>&cr;&tab;&tab;
			<param-value>
				<xsl:value-of select="$jsf.project.stage"></xsl:value-of>
			</param-value>&cr;&tab;
		</context-param>
	</xsl:template>

	<xsl:template match="*/web-app:listener[last()]">
		<xsl:copy-of select="." />
		<listener>&cr;&tab;&tab;
			<listener-class>org.apache.myfaces.webapp.StartupServletContextListener</listener-class>&cr;&tab;
		</listener>
	</xsl:template>
</xsl:stylesheet>