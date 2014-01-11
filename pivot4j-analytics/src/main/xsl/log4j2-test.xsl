<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE stylesheet [
  <!ENTITY tab "<xsl:text>&#9;</xsl:text>">
  <!ENTITY cr "<xsl:text>
</xsl:text>">
]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />
	<xsl:param name="logger.level" />
	<xsl:param name="root.level" />

	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*/logger[@name='org.pivot4j']">
		<logger name="org.pivot4j" additivity="false">
			<xsl:attribute name="level">
				<xsl:value-of select="$logger.level" />
			</xsl:attribute>
		</logger>
	</xsl:template>

	<xsl:template match="*/root">
		<root>
			<xsl:attribute name="level">
				<xsl:value-of select="$root.level" />
			</xsl:attribute>&cr;&tab;&tab;&tab;
			<appender-ref ref="CONSOLE" />&cr;&tab;&tab;
		</root>
	</xsl:template>
</xsl:stylesheet>