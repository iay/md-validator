<?xml version="1.0" encoding="UTF-8"?>
<!--

    Inject an "info" status.

-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="urn:oasis:names:tc:SAML:2.0:metadata">

    <!--
        Common support functions.
    -->
    <xsl:import href="../_rules/check_framework.xsl"/>

    <xsl:template match="md:EntityDescriptor">
        <xsl:call-template name="info">
            <xsl:with-param name="m">
                <xsl:text>informational message for entity '</xsl:text>
                <xsl:value-of select="@entityID"/>
                <xsl:text>'</xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
