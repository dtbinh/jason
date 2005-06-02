<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="goalId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationalSpecification/FunctionalSpecification/Scheme/Goal" />
</xsl:template>



<xsl:template match="Goal">
    <xsl:if test="@id=$goalId">
      <h2><xsl:value-of select="@id" /> (goal)</h2>
      <hr />
      
      <xsl:value-of select="text()" />
      
      <xsl:if test="count(Property)>0">
        <p/><b>Properties</b>:
        <ul><xsl:apply-templates select="Property" /> </ul>
      </xsl:if>
      
      <xsl:variable name="goalId" select='@id'/> 
      <p/><b>Plan</b> <br/>
            <xsl:for-each select="../Plan">
                <xsl:if test="@headGoal=$goalId">
                    <xsl:apply-templates select="."/>
                </xsl:if>
            </xsl:for-each>
    </xsl:if>
</xsl:template>


</xsl:stylesheet>

