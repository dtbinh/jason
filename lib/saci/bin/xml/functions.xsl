<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>


<!--
          * General propose functions *
-->

<xsl:template name="RoleRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">role.xsl?roleId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>


<xsl:template name="MissionRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">mission.xsl?missionId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>


<xsl:template name="GoalRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">goal.xsl?goalId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>


<xsl:template name="GrSpecRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">grSpec.xsl?grSpecId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>


<xsl:template name="AgentRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">agent.xsl?agentId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>


<xsl:template name="GroupRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">group.xsl?groupId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>

<xsl:template name="SchemeRef">
   <xsl:param name="id" select="UNDEFINED"/>
   <a>
      <xsl:attribute name="href">scheme.xsl?schId=<xsl:value-of select="$id" /></xsl:attribute>
      <xsl:value-of select="$id" />
   </a>
</xsl:template>

</xsl:stylesheet>

