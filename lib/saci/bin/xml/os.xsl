<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="functions.xsl" />

<xsl:template match="OrganizationalSpecification">
   <html>
      <body>
         <h2><xsl:value-of select="@id" /> (Organizational Specification)</h2>
         
         <xsl:if test="count(Property)>0">
            <b>Properties</b>
            <ul><xsl:apply-templates select="Property" /> </ul>
         </xsl:if>
         
         <xsl:apply-templates select="StructuralSpecification" />
         <xsl:apply-templates select="FunctionalSpecification" />
         <xsl:apply-templates select="DeonticSpecification" />
      </body>
   </html>
</xsl:template>


<xsl:template match="StructuralSpecification">
  <hr/><h3>Structural Specification</h3>
  
        <xsl:if test="count(Property)>0">
            <b>Properties</b>
            <ul><xsl:apply-templates select="Property" /> </ul>
        </xsl:if>
    
        <b>Roles</b>
	 <ul>
	 <xsl:apply-templates select="RolesDefinition/Role" />
	 </ul>
        <b>Groups</b>
	 <ul>
	 <xsl:apply-templates select="GroupSpecification" />
	 </ul>
</xsl:template>


<xsl:template match="Role">
    <li>
        <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>
        extends
        <xsl:for-each select="extends">
            <xsl:call-template name="RoleRef">
                <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="not(position()=last())">,</xsl:if>
        </xsl:for-each>.
  </li>
</xsl:template>


<xsl:template match="GroupSpecification">
  <li>
      <xsl:call-template name="GrSpecRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
      </xsl:call-template>

      <xsl:if test="count(SubGroups/GroupSpecification)>0">
        <ul>
          <xsl:apply-templates select="SubGroups/GroupSpecification" />
        </ul>
      </xsl:if>
  </li>
</xsl:template>




<xsl:template match="FunctionalSpecification">
    <hr/><h3>Functional Specification</h3>
  
    <xsl:if test="count(Property)>0">
        <b>Properties</b>:
        <ul><xsl:apply-templates select="Property" /> </ul>
    </xsl:if>    
    <xsl:apply-templates select="Scheme" />

</xsl:template>

<xsl:template match="Scheme">
    scheme <b><xsl:value-of select="@id"/></b>
    <ul>

    <xsl:if test="count(Property)>0">
        <li>
        Properties:
        <ul><xsl:apply-templates select="Property" /> </ul>
        </li>
    </xsl:if>    

    <li>
    Goals
    <table cellpadding="5">
    <tr>
    <td><b>goal</b></td>
    <td><b>description</b></td>
    </tr>
    <xsl:apply-templates select="Goal" />
    </table>
    </li>

    <li>
    Plans
    <ul><xsl:apply-templates select="Plan" /> </ul>
    </li>

    <li>
    Missions
    <ul><xsl:apply-templates select="Mission" /> </ul>
    </li>
    
    </ul>
</xsl:template>

<xsl:template match="Goal">
    <tr>
        <td>
        <xsl:call-template name="GoalRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>
        <xsl:if test="../@rootGoal=@id">
              <br/><b>(root)</b>
        </xsl:if>
        </td>
        
        <td>
        <xsl:value-of select="text()"/> 
        </td>
    </tr>
</xsl:template>


<xsl:template match="Plan">
    <li>
                    <xsl:call-template name="GoalRef">
                        <xsl:with-param name="id"><xsl:value-of select="@headGoal"/></xsl:with-param>
                    </xsl:call-template>
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
                    <xsl:if test="count(Property)>0">
                        <ul><xsl:apply-templates select="Property" /> </ul>
                    </xsl:if>
    </li>
</xsl:template>



<xsl:template match="Mission">
    <li>
        <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
        </xsl:call-template>,
        goals = {
            <xsl:for-each select="Goal">
                    <xsl:call-template name="GoalRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
                    </xsl:call-template>
                <xsl:if test="not(position()=last())">,</xsl:if>
            </xsl:for-each>
        }
        <xsl:if test="@min">
            , cardinality = (<xsl:value-of select="@min"/>, <xsl:value-of select="@max"/>)
        </xsl:if>

    </li>
</xsl:template>




<xsl:template match="DeonticSpecification">
    <hr/><h3>Deontic Specification</h3>
    <xsl:if test="count(Property)>0">
        <b>Properties</b>:
        <ul><xsl:apply-templates select="Property" /> </ul>
    </xsl:if>
    
    <table cellpadding="5">
    <tr>
    <td><b>role</b></td> 
    <td><b>relation</b></td>
    <td><b>mission</b></td>
    <td><b>time constraint</b></td>
    <td><b>properties</b></td>
    </tr>
	 <xsl:apply-templates select="DeonticRelation" />
    </table>
</xsl:template>

<xsl:template match="DeonticRelation">
    <tr>
        <td> 
        <xsl:call-template name="RoleRef">
            <xsl:with-param name="id"><xsl:value-of select="@role"/></xsl:with-param>
        </xsl:call-template>
        </td>
        
        <td>
        <i><xsl:value-of select="@type"/> </i>
        </td>
        
        <td>
        <xsl:call-template name="MissionRef">
            <xsl:with-param name="id"><xsl:value-of select="@mission"/></xsl:with-param>
        </xsl:call-template>
        </td>
        
        <td>
        <xsl:value-of select="@timeConstraint"/>
        </td>
        
        <td>
        <xsl:apply-templates select="Property" />
        </td>
    </tr>
</xsl:template>

<xsl:template match="Property">
    <xsl:value-of select="@id"/>=<xsl:value-of select="@value"/><br/>
</xsl:template>

</xsl:stylesheet>

