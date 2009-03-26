<?xml version="1.0"?>
<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<xslt:transform version="1.0"
                 xmlns:xslt="http://www.w3.org/1999/XSL/Transform">
<!-- Simple template to merge two database schemas into one  -->
<xslt:param name="fileTwo" />
<xslt:template match="/">

   <xslt:message>
      <xslt:text />Merging input with '<xslt:value-of select="$fileTwo"/>
      <xslt:text>'</xslt:text>
   </xslt:message>
	<xslt:if test="string($fileTwo)=''">
      <xslt:message terminate="yes">
         <xslt:text>No input file specified (parameter 'fileTwo')</xslt:text>
      </xslt:message>
   </xslt:if>
	<database name="generic">
	<xslt:apply-templates />
	</database>
	</xslt:template>
	<xslt:template match="database">
      <xslt:apply-templates />
      <xslt:apply-templates select="document($fileTwo)/database/table"/> 
	</xslt:template>
	
	<xslt:template match="@*|node()">
	<xslt:copy>
      <xslt:apply-templates select="@*|node()"/>
	</xslt:copy>
	</xslt:template>
</xslt:transform>
