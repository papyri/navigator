<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/xml; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.*" %><srw:explainResponse xmlns:srw="http://www.loc.gov/zing/srw/" xmlns:exp="http://explain.z3950.org/dtd/2.0/">
 <srw:version>1.2</srw:version>
 <srw:record>
   <srw:recordPacking>XML</srw:recordPacking>
   <srw:recordSchema>http://explain.z3950.org/dtd/2.1/</srw:recordSchema>
   <srw:recordData>

   <exp:explain>
     <exp:serverInfo protocol="SRU" version="1.2" transport="http"
                    method="GET POST">
        <exp:host><%=request.getServerName()%></exp:host>
        <exp:port>8080</exp:port>
        <exp:database>ddbdp-nav/sru</exp:database>
     </exp:serverInfo>
     <exp:databaseInfo>
       <exp:title lang="en" primary="true">DDbDP Test Database</exp:title>
     </exp:databaseInfo>
     <exp:indexInfo>
       <exp:set name="cql" identifier="info:srw/cql-context-set/1/cql-v1.2"/>
        <exp:index>
          <exp:map><exp:name set="cql">keywords</exp:name></exp:map>
        </exp:index>
        <exp:sortKeyword>identifier</exp:sortKeyword>
     </exp:indexInfo>
     <exp:schemaInfo>
        <exp:schema name="cql" identifier="info:srw/cql-context-set/1/cql-v1.2">
          <exp:title>CQL Context Set</exp:title>
        </exp:schema>
        <exp:schema name="dc" identifier="info:srw/schema/1/dc-v1.1">
          <exp:title>Simple Dublin Core</exp:title>
        </exp:schema>
     </exp:schemaInfo>
     <exp:configInfo>
         <exp:default type="numberOfRecords">1</exp:default>
         <exp:setting type="maximumRecords">50</exp:setting>
         <exp:supports type="proximity">
             <exp:configInfo>
             <exp:supports type="relationModifier">unit=word</exp:supports>
             <exp:supports type="relationModifier">distance</exp:supports>
             </exp:configInfo>
         </exp:supports>
         <exp:supports type="relationModifier">locale=grc.beta</exp:supports>
         <exp:supports type="relationModifier">ignoreCapitals</exp:supports>
         <exp:supports type="relationModifier">ignoreAccents</exp:supports>
     </exp:configInfo>
    </exp:explain>

   </srw:recordData>
 </srw:record>
</srw:explainResponse>