<%--
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
--%>
<%@ include file="../../../initTemplatesLayoutNormal.jsp" %>

  <%--  Check if I am root fragment --%>
  <c:set var="lastColumn" value="${numberOfColumns - 1}" scope="request"/>

  <% 
    /**
    * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
    */

      LayoutDecoration layoutDecoration = (LayoutDecoration)_myFragment.getDecoration();
      // get the layout decoration header file
      String headerJSP = layoutDecoration.getHeader();
      if (!(headerJSP.startsWith("/")))
      {
    	  headerJSP = "/" + headerJSP;
      }
      pageContext.setAttribute("layoutHeaderJSP", headerJSP, PAGE_SCOPE);
      
      // get the layout decoration footer file
      String footerJSP = layoutDecoration.getFooter();
      if (!(footerJSP.startsWith("/")))
      {
        footerJSP = "/" + footerJSP;
      }
      pageContext.setAttribute("layoutFooterJSP", footerJSP, PAGE_SCOPE);


  %>

  
  <%-- ***** BEGIN header.jsp ***** --%>
    <c:choose>
      <c:when test="${isRoot}">
        <c:import url="${layoutHeaderJSP}">
        </c:import>
        <c:set var="layoutClass" value="portal-nested-layout" scope="page"/>
      </c:when>
      <c:otherwise>
        <c:set var="layoutClass" value="portal-layout" scope="page"/>
      </c:otherwise>
    </c:choose>
  <%-- ***** END header.jsp ***** --%>
  
  

      <div id="<portlet:namespace/>"
           class="<c:out value='${layoutClass} ${layoutClass}-${layoutType }'/>"> <!-- S:div main -->
        
        <c:if test="${editing}">
          <c:set var="customizerUrl" value="/WEB-INF/templates/templateLayoutCustomizer.jsp"/>
          <c:import url="${customizerUrl}"/>
        </c:if>

        
                 <c:set var="dcnt" value="0" scope="request"/>
        <c:forEach var="column" items="${columnLayout.columns}" varStatus="columnLoop" >  <!--  S:columnLayouts  -->

          <%-- req paramter to positioner  --%>
          <c:set var="loopIndex" value="${columnLoop.index}" scope="request"/>
          <%-- BEGIN setup div attributes --%>
          <%
              int _loopIndex = ((Integer) pageContext.getAttribute("loopIndex", REQ_SCOPE)).intValue();
      
              String _columnFloat = _columnLayout.getColumnFloat(_loopIndex);
              pageContext.setAttribute("columnFloat",_columnFloat);
              
              String _columnWidth = _columnLayout.getColumnWidth(_loopIndex);
              pageContext.setAttribute("columnWidth",_columnWidth);
           %>
           <c:set var="divId" value="column_${myFragment.id}_${loopIndex}" />
           <c:set var="divClass" value="portal-layout-column portal-layout-column-${layoutType}-${loopIndex}"/>
           <c:set var="divStyle" value="float:${columnFloat}; width:${columnWidth};" />
           <%-- END setup div attributes --%>
      
      
             <div id="<c:out value='${divId}'/>"
                  class="<c:out value='${divClass}'/>"
                  style="<c:out value='${divStyle}'/>">  <!--  S: content for each column -->
      
                 <%-- BEGIN Render Fragments in Column --%>
                 <c:forEach var="frag" items="${column}">
                   <c:set var="indexFrag" value="${frag}" scope="request"/>
                   <c:set var="dcnt" value="${dcnt + 1}" scope="request"/>
                   
                   <%
                      ContentFragment _frag = (ContentFragment) pageContext.getAttribute("frag",PAGE_SCOPE);

                      LayoutCoordinate _coords = _columnLayout.getCoordinate(_frag);
                      pageContext.setAttribute("coords",_coords);
                      int _col = _coords.getX();
                      pageContext.setAttribute("col",_col);
                      int _row = _coords.getY();
                      pageContext.setAttribute("row",_row);
                      
                      pageContext.setAttribute("lastRow",_columnLayout.getLastRowNumber(_col));
                      
                   %>
                   <c:set var="pClass" value="portal-layout-cell portal-layout-cell-${layoutType}-${row}-${col}" />
                    <c:if test="${editing}">
                      <c:set var="portletCustomizerUrl" value="/WEB-INF/templates/layout/positionPortletCustomizer.jsp"/>
                      <c:import url="../../positionPortletCustomizer.jsp" />
                    </c:if>
                    <!-- BEGIN PORTLET CONTENT -->
                    <div class="<c:out value='${pClass}' />">
                    
                      <%-- do the rendering --%>
                      <%
                        //inform JPT that we are current.
                        _jpt.setCurrentFragment(_frag);
                      
                      %>
                      
                      <c:choose>
                        <c:when test="${frag.type eq 'portlet'}">

                          <%
                              // TODO hack alert, we need to do this because PortletDecorationImpl has
                              // decorator.vm hard coded sigh!     
                              String _tempPath = _frag.getDecoration().getBasePath();
                              String _portletJSP = _tempPath.substring(0,_tempPath.lastIndexOf('/')) +
                                                   "/decorator.jsp";
                              pageContext.setAttribute("fragTemplateUrl", _portletJSP, PageContext.PAGE_SCOPE);
                          %>
                          
                          <c:import url="${fragTemplateUrl}">
                          </c:import>
                        </c:when>
                        <c:otherwise>
                          <c:out value="${frag.renderedContent}" />
                        </c:otherwise>
                      </c:choose>
                    </div>
                    <!-- END PORTLET CONTENT -->
                 
                 </c:forEach>
                 <%-- END Render Fragments in Column --%>
             
             </div> <!--  E: content for each column -->
           
        </c:forEach> <!--  E:columnLayouts  -->
      
      </div> <!-- E:div main -->

  
  <%-- ***** BEGIN footer.jsp ***** --%>
  <c:import url="${layoutFooterJSP}"></c:import>
  <%-- ***** END footer.jsp ***** --%>
  