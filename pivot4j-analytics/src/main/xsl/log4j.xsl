<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE stylesheet [
  <!ENTITY tab "<xsl:text>&#9;</xsl:text>">
  <!ENTITY cr "<xsl:text>
</xsl:text>">
]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />
	<xsl:param name="log4j.level" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*/category[@name='com.eyeq.pivot4j']">
		<category name="com.eyeq.pivot4j" additivity="false">&cr;&tab;&tab;
			<priority>
				<xsl:attribute name="value">
					<xsl:value-of select="$log4j.level" />
				</xsl:attribute>
			</priority>&cr;&tab;&tab;
			<appender-ref ref="CONSOLE" />&cr;&tab;
		</category>
	</xsl:template>
</xsl:stylesheet>