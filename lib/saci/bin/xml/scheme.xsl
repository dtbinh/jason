<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="schId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationEntity/Schemes/Scheme" />
</xsl:template>


<xsl:template match="Scheme">
    <xsl:if test="@id=$schId">
      <h2><xsl:value-of select="@id" /> (scheme)</h2>
      <hr />

      created from specification 
      <xsl:value-of select="@specification"/>
      <xsl:if test="@owner">
	, owner is 
	<xsl:call-template name="AgentRef">
   	  <xsl:with-param name="id"><xsl:value-of select="@owner"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
      <br/>
      
      <xsl:apply-templates select="WellFormed" />
      <xsl:apply-templates select="ResponsibleGroups" />
      <br/>
      <xsl:apply-templates select="Players" />
      <xsl:apply-templates select="Goals" />

    </xsl:if>
</xsl:template>

</xsl:stylesheet>

