<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="groupId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationEntity/Groups/Group" />
</xsl:template>



<xsl:template match="Group">
    <xsl:if test="@id=$groupId">
      <h2><xsl:value-of select="@id" /> (group)</h2>
      <hr />
      
      created from specification 
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <xsl:if test="@owner">
	, owner is 
	<xsl:call-template name="AgentRef">
   	  <xsl:with-param name="id"><xsl:value-of select="@owner"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>

      <br/>
      
      <xsl:apply-templates select="WellFormed" />
      <xsl:apply-templates select="Players" />

    </xsl:if>
    <xsl:apply-templates select="SubGroups" />
</xsl:template>

<xsl:template match="SubGroups">
    <xsl:apply-templates select="Group" />
</xsl:template>

</xsl:stylesheet>

