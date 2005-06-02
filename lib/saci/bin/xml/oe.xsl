<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="functions.xsl" />

<xsl:template match="OrganizationEntity">
   <html>
      <body>
         <h2><xsl:value-of select="@os" /> (Organization Entity)</h2>
         <xsl:apply-templates select="Agents" />
         <xsl:apply-templates select="Groups" />
         <xsl:apply-templates select="Schemes" />
      </body>
   </html>
</xsl:template>


<xsl:template match="Agents">
  <hr/><h3>Agents</h3>
	 <ul>
	 <xsl:apply-templates select="Agent" />
	 </ul>
</xsl:template>


<xsl:template match="Agent">
    <li>
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      <!-- <xsl:apply-templates select="Roles" /> -->
    </li>
</xsl:template>

<xsl:template match="Roles">
	 <ul>
	 <xsl:apply-templates select="RolePlayer" />
	 </ul>
</xsl:template>


<xsl:template match="RolePlayer">
    <li>
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>
      plays 
      <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
      </xsl:call-template>
    </li>
</xsl:template>





<xsl:template match="Groups">
  <hr/><h3>Groups</h3>
	 <ul>
	 <xsl:apply-templates select="Group" />
	 </ul>
</xsl:template>

<xsl:template match="Group">
    <li>
      <xsl:call-template name="GroupRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      created from specification 
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <br/>
      
      <xsl:apply-templates select="WellFormed" />
      <xsl:apply-templates select="Players" />
      <xsl:apply-templates select="SubGroups" />
    </li>
</xsl:template>


<xsl:template match="WellFormed">
    <br/><b>Well formation:</b> 
    <blockquote>
        <xsl:apply-templates />
    </blockquote>
</xsl:template>

<xsl:template match="Players">
    <br/><b>players</b>
	 <ul>
	 <xsl:apply-templates select="RolePlayer" />
	 <xsl:apply-templates select="MissionPlayer" />
	 </ul>
</xsl:template>

<xsl:template match="SubGroups">
    <br/><b>Sub-Groups</b>
	 <ul>
	 <xsl:apply-templates select="Group" />
	 </ul>
</xsl:template>


<xsl:template match="Schemes">
  <hr/><h3>Schemes</h3>
	 <ul>
	 <xsl:apply-templates select="Scheme" />
	 </ul>
</xsl:template>

<xsl:template match="Scheme">
    <li>
      <xsl:call-template name="SchemeRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>
      <br/>
      <xsl:apply-templates select="WellFormed" />
      <xsl:apply-templates select="ResponsibleGroups" />
      <br/>
      <xsl:apply-templates select="Players" />
      <xsl:apply-templates select="Goals" />
    </li>
</xsl:template>

<xsl:template match="ResponsibleGroups">
    <b>Responsible groups</b>:
            <xsl:for-each select="Group">
                <xsl:call-template name="GroupRef">
                    <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                </xsl:call-template>, 
            </xsl:for-each>
</xsl:template>


<xsl:template match="MissionPlayer">
    <li>
      <xsl:call-template name="AgentRef">
            <xsl:with-param name="id"><xsl:value-of select="@agent"/></xsl:with-param>
      </xsl:call-template>
      committed to 
      <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
      </xsl:call-template>
    </li>
</xsl:template>

<xsl:template match="Goals">
         <table cellpadding="5">
         <tr> 
         <td><b>goal</b></td>
         <td><b>satisfied</b></td> 
         <td><b>possible</b></td> 
         <td><b>permitted</b></td>
         <td><b>committed</b></td>
         <td><b>arguments</b></td>
         <td><b>plan</b></td> 
         </tr>
	 <xsl:apply-templates select="Goal" />
         </table>
</xsl:template>

<xsl:template match="Goal">
      <tr>
      <td>
      <xsl:call-template name="GoalRef">
            <xsl:with-param name="id"><xsl:value-of select="@specification"/></xsl:with-param>
      </xsl:call-template>
      <xsl:if test="../../@rootGoal=@specification">
              <br/><b>(root)</b>
      </xsl:if>
      </td>
      <td><xsl:value-of select="@satisfied"/></td>
      <td><xsl:value-of select="@possible"/></td>
      <td><xsl:value-of select="@permitted"/></td>
      <td><xsl:value-of select="@committedAgs"/></td>
      <td>
      <xsl:if test="count(argument)>0">
        <xsl:for-each select="argument">
           <xsl:value-of select="@id"/> = <xsl:value-of select="@value"/>
           <xsl:if test="not(position()=last())">
              <br/>
           </xsl:if>
        </xsl:for-each>
      </xsl:if>
      </td>

      <td>
      <xsl:if test="count(Plan)>0">
	 <xsl:apply-templates select="Plan" />
      </xsl:if>
      </td> 
      </tr>
</xsl:template>

<xsl:template match="Plan">
                    <xsl:value-of select="@headGoal"/>
                    <xsl:variable name="pOp" select='@operator'/> 
                    =<sub>(<xsl:value-of select="@successRate"/>)&#160;</sub>
                    <xsl:for-each select="Goal">
                        <xsl:call-template name="GoalRef">
                            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                        </xsl:call-template>
                        <xsl:if test="not(position()=last())">
                            <xsl:if test="$pOp='sequence'">,</xsl:if>
                            <xsl:if test="$pOp='choice'"> | </xsl:if>
                            <xsl:if test="$pOp='parallel'"> || </xsl:if>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:apply-templates select="Property" />
</xsl:template>


</xsl:stylesheet>