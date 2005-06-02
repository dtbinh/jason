<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="oe.xsl" />

<xsl:param name="agentId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationEntity/Agents/Agent" />
</xsl:template>



<xsl:template match="Agent">
    <xsl:if test="@id=$agentId">
      <h2><xsl:value-of select="@id" /> (agent)</h2>
      <hr />

      <xsl:if test="text()">
          <b>Obligations</b> status: 
          <blockquote>
            <xsl:value-of select="text()" />
          </blockquote>
      </xsl:if>
      
      <b>Roles</b>
      <xsl:apply-templates select="../../Groups" />
      
      <b>Missions</b>
      <xsl:apply-templates select="../../Schemes" />

    </xsl:if>
</xsl:template>

<xsl:template match="Groups">
      <ul>
	 <xsl:apply-templates select="Group" />
      </ul>
</xsl:template>

<xsl:template match="Schemes">
      <ul>
	 <xsl:apply-templates select="Scheme" />
      </ul>
</xsl:template>



<xsl:template match="Group">
      <xsl:apply-templates select="Players" />
      <xsl:apply-templates select="SubGroups" />
</xsl:template>

<xsl:template match="SubGroups">
	 <xsl:apply-templates select="Group" />
</xsl:template>

<xsl:template match="Scheme">
      <xsl:apply-templates select="Players" />
</xsl:template>

<xsl:template match="Players">
	 <xsl:apply-templates select="RolePlayer" />
	 <xsl:apply-templates select="MissionPlayer" />
</xsl:template>



<xsl:template match="RolePlayer">
    <xsl:if test="@agent=$agentId">
    <li>
      plays 
      <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
      </xsl:call-template>
      in 
      <xsl:call-template name="GroupRef">
            <xsl:with-param name="id"><xsl:value-of select="../../@id"/></xsl:with-param>
      </xsl:call-template>
    </li>
    </xsl:if>
</xsl:template>

<xsl:template match="MissionPlayer">
    <xsl:if test="@agent=$agentId">
    <li>
      committed to 
      <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
      </xsl:call-template>
      in 
      <xsl:call-template name="SchemeRef">
            <xsl:with-param name="id"><xsl:value-of select="../../@id"/></xsl:with-param>
      </xsl:call-template>
    </li>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>

