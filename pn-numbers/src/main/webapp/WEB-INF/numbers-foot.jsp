<%@ page language="java"%><%@ page session="false" pageEncoding="UTF-8" contentType="text/xml; charset=UTF-8" import="info.papyri.numbers.servlet.*" %>
                  <% if(request.getAttribute(Numbers.ATTR_OFFSET_URL) != null){
                  %>
                  <rdfs:seeAlso rdf:resource="<%=request.getAttribute(Numbers.ATTR_OFFSET_URL)%>"/>
                  <%}
                  %>
                  </rdf:Description>
</rdf:RDF>
         