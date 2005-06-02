<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>
<xsl:output encoding="ISO-8859-1" method="html" />
<xsl:strip-space elements="*"/>

<xsl:template match="saci">
   <html>
      <body>
         <h2>Saci Applications Resources</h2>
	 <hr size="1"/>

         <h3>Index</h3>
         <ul>
            <xsl:for-each select="application">
	       <li>
	         <a>
                 <xsl:attribute name="href">#<xsl:value-of select="@id"/></xsl:attribute>
                 <xsl:value-of select="@id"/>
                 </a>
               </li>
            </xsl:for-each>
         </ul>

         <xsl:apply-templates select="application" />
      </body>
   </html>
</xsl:template>



<xsl:template match="application">
    <a><xsl:attribute name="name">
         <xsl:value-of select="@id"/>
       </xsl:attribute>
    </a>
    <hr size="1"/>
    <h2>Application: <xsl:value-of select="@id"/></h2>
    <h3>Agent types</h3>
      <xsl:apply-templates select="agentType" />

    <xsl:if test="count(script)>0">
        <h3>Scripts</h3>
        <ul>
        <xsl:apply-templates select="script" />
        </ul>
    </xsl:if>
</xsl:template>



<xsl:template match="agentType">
         <table cellpadding="2" width="100%">
         <tr bgcolor="red" > 
         <td width="15%">
            <font color="white">
               <b>id</b>
            </font>
         </td>
         <td>
            <font color="white" size="+1">
              <b><xsl:value-of select="@id"/></b>
            </font>
         </td>
         </tr>

	 <xsl:if test="@description">
         <tr bgcolor="cyan"> 
         <td>description</td> 
         <td><xsl:value-of select="@description"/></td>
         </tr>
	 </xsl:if>

	 <xsl:if test="@class">
         <tr bgcolor="cyan"> 
         <td>class</td> 
         <td><xsl:value-of select="@class"/></td>
         </tr>
	 </xsl:if>

	 <xsl:if test="@defaultName">
         <tr bgcolor="cyan"> 
         <td>default name</td>
         <td><xsl:value-of select="@defaultName"/></td>
         </tr>
	 </xsl:if>

	 <xsl:if test="@defaultSociety">
         <tr bgcolor="cyan"> 
         <td>default society</td>
         <td><xsl:value-of select="@defaultSociety"/></td>
         </tr>
	 </xsl:if>

	 <xsl:if test="@defaultHost">
         <tr bgcolor="cyan"> 
         <td>default host</td>
         <td><xsl:value-of select="@defaultHost"/></td>
         </tr>
	 </xsl:if>

	 <xsl:if test="@creationMethod">
         <tr bgcolor="cyan"> 
         <td>creation method</td> 
         <td><xsl:value-of select="@creationMethod"/></td>
         </tr>
	 </xsl:if>

         <tr bgcolor="cyan"> 
         <td colspan="2"><xsl:apply-templates select="help" /></td>
         </tr>

	 </table>
</xsl:template>





<xsl:template match="script">
  <li>
  <xsl:value-of select="@description"/>
  (<xsl:value-of select="@id"/>):
  <ol>
  <xsl:apply-templates select="*" />	
  </ol>
  </li>
</xsl:template>


<xsl:template match="startSociety">
  <li>
  start <xsl:value-of select="@type"/> 
  society named <xsl:value-of select="@society.name"/>

  <xsl:if test="@type='moise'">
    (purpose=<xsl:value-of select="@purpose"/>,
     os file=<xsl:value-of select="@osURI"/>)
  </xsl:if>

  </li>
</xsl:template>

<xsl:template match="killFacilitator">
  <li>
  kill facilitator of society <xsl:value-of select="@society.name"/>
  </li>
</xsl:template>

<xsl:template match="killSocietyAgents">
  <li>
  kill all agents from society <xsl:value-of select="@society.name"/>
  </li>
</xsl:template>


<xsl:template match="startAgent">
  <li>
  start an agent, 
  <br/>type id = <xsl:value-of select="@agId"/> 

  <xsl:if test="@name">
    <br/>name = <xsl:value-of select="@name"/> 
  </xsl:if>

  <xsl:if test="@society">
    <br/>society = <xsl:value-of select="@society.name"/> 
  </xsl:if>

  <xsl:if test="@class">
    <br/>class = <xsl:value-of select="@class"/> 
  </xsl:if>

  <xsl:if test="@creationMethod">
    <br/>creation method = <xsl:value-of select="@creationMethod"/> 
  </xsl:if>

  <xsl:if test="@args">
    <br/>arguments = <xsl:value-of select="@args"/> 
  </xsl:if>

  <xsl:if test="@host">
    <br/>host = <xsl:value-of select="@host"/> 
  </xsl:if>

  <xsl:if test="@qty">
    <br/>qty = <xsl:value-of select="@qty"/> 
  </xsl:if>

  </li>
</xsl:template>


<xsl:template match="sleep">
  <li>
  sleep for <xsl:value-of select="@milisec"/> 
  </li>
</xsl:template>


<xsl:template match="runScript">
  <li>
  runs the script <xsl:value-of select="@scriptId"/> 
  <xsl:if test="@host">
     at <xsl:value-of select="@host"/>
  </xsl:if>

  <xsl:if test="@creationMethod">
    (as a <xsl:value-of select="@creationMethod"/>)
  </xsl:if>
  </li>
</xsl:template>


</xsl:stylesheet>

