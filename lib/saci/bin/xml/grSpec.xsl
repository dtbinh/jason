<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="os.xsl" />

<xsl:param name="grSpecId"/>

<xsl:template match="/">
    <xsl:apply-templates select="OrganizationalSpecification/StructuralSpecification/GroupSpecification" />
</xsl:template>


<!--
          * GrSpec *
-->

<xsl:template match="GroupSpecification">
    <xsl:if test="@id=$grSpecId">
      <h2><xsl:value-of select="@id" /> (group specification)</h2>
      <hr />

      Defined in
           <xsl:call-template name="GrSpecRef">
               <xsl:with-param name="id"><xsl:value-of select="../../@id" /></xsl:with-param>
           </xsl:call-template>

      <xsl:if test="count(Property)>0">
        <p/><b>Properties:</b>:
        <ul><xsl:apply-templates select="Property" /> </ul>
      </xsl:if>
      
      <xsl:if test="count(Roles/Role)>0">
         <p/><b>Possible roles: </b>
         <xsl:for-each select="Roles/Role">
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates select="../../ConstrainFormation/Cardinality">
               <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:apply-templates>
            <xsl:if test="not(position()=last())">, </xsl:if>
         </xsl:for-each>.
      </xsl:if>


      <xsl:if test="count(SubGroups/GroupSpecification)>0">
        <p/><b>Sub-groups</b>:
        <xsl:for-each select="SubGroups/GroupSpecification">
            <xsl:call-template name="GrSpecRef">
               <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
            </xsl:call-template>
            <xsl:apply-templates select="../../ConstrainFormation/Cardinality">
               <xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
            </xsl:apply-templates>
            <xsl:if test="not(position()=last())">, </xsl:if>
        </xsl:for-each>.
      </xsl:if>

      <xsl:apply-templates select="Links">
         <xsl:with-param name="soExtends">false</xsl:with-param>
      </xsl:apply-templates>

      <xsl:apply-templates select="ConstrainFormation">
         <xsl:with-param name="soExtends">false</xsl:with-param>
      </xsl:apply-templates>

    </xsl:if>
    <xsl:apply-templates select="SubGroups/GroupSpecification" />

</xsl:template>



<!--
          * Links *
-->
<xsl:template match="Links">
    <xsl:param name="soExtends" select="false" />

    <xsl:if test="$soExtends='false'">
        <p/><b>Links</b>
    </xsl:if>

    <ul>

    <xsl:if test="$soExtends='false'">
        <li>Local links</li>
    </xsl:if>
    <xsl:if test="$soExtends='true'">
        <li>Links from
            <xsl:call-template name="GrSpecRef">
               <xsl:with-param name="id"><xsl:value-of select="../@id"/></xsl:with-param>
            </xsl:call-template>
       </li>
    </xsl:if>

    <ul>
    <xsl:for-each select="Link">
        <xsl:if test="($soExtends='true' and @extendsToSubGroups='true') or $soExtends='false'">
            <li>
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@source" /></xsl:with-param>
            </xsl:call-template> 
            has a <i><xsl:value-of select="@type" /></i> link to
            <xsl:call-template name="RoleRef">
               <xsl:with-param name="id"><xsl:value-of select="@destination" /></xsl:with-param>
            </xsl:call-template>
            <xsl:if test="@symmetric='true'">
                and vice versa
            </xsl:if>
            (<xsl:value-of select="@scope" />
            
            <xsl:if test="$soExtends='false'">
            , <xsl:if test="@extendsToSubGroups='true'">extends to sub-groups</xsl:if>
              <xsl:if test="@extendsToSubGroups='false'">does not extend to sub-groups</xsl:if>
            </xsl:if>
            )
            <xsl:if test="count(Property)>0">
                <ul><xsl:apply-templates select="Property" /> </ul>
            </xsl:if>
            </li>
        </xsl:if>
    </xsl:for-each>
    </ul>

    </ul>
    
    
    <xsl:apply-templates select="../../../Links">
         <xsl:with-param name="soExtends">true</xsl:with-param>
    </xsl:apply-templates>

</xsl:template>




<!--
          * Constrain Formation *
-->
<xsl:template match="ConstrainFormation">
    <xsl:param name="soExtends" select="false" />

    <xsl:if test="$soExtends='false'">
        <p/><b>Constraint Formation</b>
    </xsl:if>
        
    <ul>
    <xsl:if test="count(Cardinality)>0 and $soExtends='false'">
        <li>Cardinalities</li>
        <ul>
        <xsl:for-each select="Cardinality">
            <li>cardinality of
                <xsl:if test="(@object='role')">
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="(@object='grSpec')">
                    <xsl:call-template name="GrSpecRef">
                        <xsl:with-param name="id"><xsl:value-of select="@id" /></xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
                is (<xsl:value-of select="@min"/>,<xsl:value-of select="@max"/>)
            </li>
        </xsl:for-each>
        </ul>
    </xsl:if>
    
    
    <xsl:if test="count(Compatibility)>0">
        <xsl:if test="$soExtends='false'">
            <li>Local compatibilities</li>
        </xsl:if>
        <xsl:if test="$soExtends='true'">
            <li>Compatibilities from 
                <xsl:call-template name="GrSpecRef">
                    <xsl:with-param name="id"><xsl:value-of select="../@id"/></xsl:with-param>
                </xsl:call-template>
            </li>
        </xsl:if>

        <ul>
        <xsl:for-each select="Compatibility">
            <xsl:if test="($soExtends='true' and @extendsToSubGroups='true') or $soExtends='false'">
                <li>role
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@source" /></xsl:with-param>
                    </xsl:call-template>
                    is <xsl:value-of select="@scope"/> compatible with
                    <xsl:call-template name="RoleRef">
                        <xsl:with-param name="id"><xsl:value-of select="@destination"/></xsl:with-param>
                    </xsl:call-template>
                    <xsl:if test="@symmetric='true'">
                        and vice versa
                    </xsl:if>
                    <xsl:if test="$soExtends='false'">
                        (this compatibility 
                        <xsl:if test="@extendsToSubGroups='true'">is extended to sub-groups</xsl:if>
                        <xsl:if test="@extendsToSubGroups='false'">is not extended to sub-groups</xsl:if>)
                    </xsl:if>
                </li>
            </xsl:if>
        </xsl:for-each>
        </ul>
    </xsl:if>
    </ul>

    <xsl:apply-templates select="../../../ConstrainFormation" >
        <xsl:with-param name="soExtends">true</xsl:with-param>
    </xsl:apply-templates>

</xsl:template>



<!--
          * Cardinality *
-->
<xsl:template match="Cardinality">
    <xsl:param name="id" />

    <xsl:if test="(@id=$id)">
        (<xsl:value-of select="@min"/>,<xsl:value-of select="@max"/>)
    </xsl:if>
</xsl:template>


</xsl:stylesheet>
