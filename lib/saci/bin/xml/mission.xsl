<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="missionId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationalSpecification/FunctionalSpecification/Scheme/Mission" />
</xsl:template>



<xsl:template match="Mission">
    <xsl:if test="@id=$missionId">
      <h2><xsl:value-of select="../@id" />.<xsl:value-of select="@id" /> (mission)</h2>
      <hr />

        <p/>goals = {
            <xsl:for-each select="Goal">
                    <xsl:call-template name="GoalRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                    </xsl:call-template>
                <xsl:if test="not(position()=last())">,</xsl:if>
            </xsl:for-each>
        }
    
        <xsl:if test="count(Property)>0">
            <p/>properties:
            <ul><xsl:apply-templates select="Property" /> </ul>
        </xsl:if>    
    
        <xsl:if test="@min">
            <br/>cardinality = (<xsl:value-of select="@min"/>, <xsl:value-of select="@max"/>)
        </xsl:if>

        <xsl:variable name="misId" select='@id'/> 
        <p/>preferable missions = 
            <xsl:for-each select="../../Preference">
                <xsl:if test="@mission=$misId">
                    <xsl:call-template name="MissionRef">
                        <xsl:with-param name="id"><xsl:value-of select="@preferable"/></xsl:with-param>
                    </xsl:call-template>,
                </xsl:if>
            </xsl:for-each>
        
      
    </xsl:if>
</xsl:template>



</xsl:stylesheet>

