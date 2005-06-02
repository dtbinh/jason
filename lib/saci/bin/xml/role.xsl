<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="roleId"/>

<xsl:template match="/">
    <xsl:apply-templates select="RoleDesc" />
</xsl:template>

<xsl:template match="RoleDesc">
    <xsl:if test="@id=$roleId">
      <h2><xsl:value-of select="@id" /> (role)</h2>
      <hr />
      
      <xsl:if test="count(extends)>0">
        <b>Extends</b> 
        <ul><xsl:apply-templates select="extends" /></ul>
      </xsl:if>
      
      <xsl:if test="count(Property)>0">
        <b>Properties</b>:
        <ul><xsl:apply-templates select="Property" /> </ul>
      </xsl:if>
    
      <xsl:if test="count(Specialization)>0">
        <b>Specializations</b> 
        <ul><xsl:apply-templates select="Specialization" /></ul>
      </xsl:if>
      
      <xsl:if test="count(Group)=0">
        it is an <b>abstract</b> role (it can not be played).
      </xsl:if>
      
      <xsl:if test="count(Group)>0">
        Can be played in the following <b>groups</b>
        <ul><xsl:apply-templates select="Group" /></ul>
      </xsl:if>
      
      <xsl:if test="count(DeonticRelation)>0">
        This role has the following <b>deontic relations</b> to missions
        <ul><xsl:apply-templates select="DeonticRelation" /></ul>
      </xsl:if>
      
    </xsl:if>
</xsl:template>


<xsl:template match="extends">
            <li>
            <xsl:call-template name="RoleRef">
                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
            </xsl:call-template>
            <ul><xsl:apply-templates select="extends" /></ul>
            </li>
</xsl:template>



<xsl:template match="Specialization">
    <li>
            <xsl:call-template name="RoleRef">
                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
            </xsl:call-template>
    </li>
</xsl:template>


<xsl:template match="Group">
    <li>
        <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>

        <xsl:if test="count(Link)>0">
            using the following links
            <ul><xsl:apply-templates select="Link" /></ul>
        </xsl:if>
        
        <xsl:if test="count(Compatibility)>0">
            and the following  compatibilities
            <ul><xsl:apply-templates select="Compatibility" /></ul>
        </xsl:if>
    </li>
</xsl:template>



<xsl:template match="Link">
            <li>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@source" /></xsl:with-param>
            </xsl:call-template> 
            <i><xsl:value-of select="@type" /></i> link to
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@destination" /></xsl:with-param>
            </xsl:call-template>
            (<xsl:value-of select="@scope" />)
            , defined in the group
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="@grSpecId"/></xsl:with-param>
                </xsl:call-template>
            </li>
</xsl:template>

<xsl:template match="Compatibility">
            <li>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@source" /></xsl:with-param>
            </xsl:call-template> 
            compatible with
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@destination" /></xsl:with-param>
            </xsl:call-template>
            (<xsl:value-of select="@scope" />)
            , defined in the group
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="@grSpecId"/></xsl:with-param>
                </xsl:call-template>
            </li>
</xsl:template>


<xsl:template match="DeonticRelation">
	 <li>
         has <i><xsl:value-of select="@type"/> </i>
         for the mission
         <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
         </xsl:call-template>
         with the "<xsl:value-of select="@timeConstraint"/>"  time constraint
	 </li>
</xsl:template>



</xsl:stylesheet>

