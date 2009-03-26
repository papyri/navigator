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
<%@ include file="../initTemplatesLayoutNormal.jsp" %>

  <%
      /**
      * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
      */
      // columnLayout already inhertited

      // our current index in the loop
      int loopIndex = ((Integer) renderRequest.getAttribute("loopIndex")).intValue();
      
      // get the fragment in the loop
      ContentFragment indexFrag = (ContentFragment) renderRequest.getAttribute("indexFrag");
      LayoutCoordinate _coords = _columnLayout.getCoordinate(indexFrag);
      pageContext.setAttribute("coords",_coords, PAGE_SCOPE);
      
      int _col = _coords.getX();
      pageContext.setAttribute("col", _col, PAGE_SCOPE);
      int _row = _coords.getY();
      pageContext.setAttribute("row", _row, PAGE_SCOPE);
  
      pageContext.setAttribute("lastRow",_columnLayout.getLastRowNumber(_col), PAGE_SCOPE);

      String columnFloat = _columnLayout.getColumnFloat(loopIndex);
      pageContext.setAttribute("columnFloat",columnFloat, PAGE_SCOPE);
      
      String columnWidth = _columnLayout.getColumnWidth(loopIndex);
      pageContext.setAttribute("columnWidth",columnWidth, PAGE_SCOPE);

      //get all the portlet move images
      pageContext.setAttribute("movePortletLeft", _cPath + "/" +
                getLayoutResource(_rootDecorator,"images/movePortletLeft.gif"), PAGE_SCOPE);
      
      pageContext.setAttribute("movePortletRight", _cPath + "/" +
              getLayoutResource(_rootDecorator,"images/movePortletRight.gif"), PAGE_SCOPE);

      pageContext.setAttribute("movePortletUp", _cPath + "/" +
              getLayoutResource(_rootDecorator,"images/movePortletUp.gif"), PAGE_SCOPE);

      pageContext.setAttribute("movePortletDown", _cPath + "/" +
              getLayoutResource(_rootDecorator,"images/movePortletDown.gif"), PAGE_SCOPE);
      
      pageContext.setAttribute("imgClose", _cPath + "/" +
              getLayoutResource(_rootDecorator,"images/close.gif"), PAGE_SCOPE);
      
      
      pageContext.setAttribute("allowChangePortletDecorator",
              getBoolProperty(_rootDecorator,"allow.change.portlet.decorator",true), PAGE_SCOPE);
  %>

  <%-- current loop index passed in request --%>
  <c:set var="curIndex" value="${requestScope.loopIndex}"/>

  <%-- the current loop fragment passed in request --%>
  <c:set var="f" value="${requestScope.indexFrag}" scope="request"/>

  <%-- lastColumn passed in request --%>
  <c:set var="lc" value="${requestScope.lastColumn}" scope="page"/>
 

  <c:set var="actionName">
    <portlet:namespace/><c:out value="${requestScope.dcnt}"/>
  </c:set>

    
      <%-- START PORTLET EDITING MODE POSITIONING --%>
      
          <div class="portlet-edit-bar">  <!--  B: div  portlet-edit-bar portlet positioning -->
            <form name="f<c:out value='${actionName}'/>" 
                  action="<portlet:actionURL/>" method="post">
                  
              <input type="hidden" name="fragment" 
                     value="<c:out value='${f.id}'/>">
              <input type="hidden" name="move" value="">
              <input type="hidden" name="remove" value="">
              <c:choose>
              
                <c:when test="${f.type eq 'portlet' }">
                  <c:set var="fragmentType" value="Portlet" scope="page"/>
                  <c:if test="${allowChangePortletDecorator}">
                      <select id="decorator" name="decorator" 
                              onChange="f<c:out value='${actionName}'/>.submit()">
                        <option value=""/>
                        <c:forEach var="pd" items="${portletDecorationsSet}">
                          <c:set var="_sel" value=""/>
                          <c:if test="${f.decorator eq pd}">
                            <c:set var="_sel" value='selected="selected"'/>
                          </c:if>
                          <option value="<c:out value='${pd}'/>" 
                                  <c:out escapeXml="false" value="${_sel}"/>>
                                  <c:out value="${pd}"/>
                        </c:forEach>
                    </select>
                  </c:if>
                </c:when>
                
                <c:otherwise>
                  <c:set var="fragmentType" value="Layout" scope="page"/>
                </c:otherwise>
                
              </c:choose>
              <c:set var="fmtKeyLeft" value="portal.page.editing.move${fragmentType}Left"/>
              <c:set var="fmtKeyRight" value="portal.page.editing.move${fragmentType}Right"/>
              <c:set var="fmtKeyUp" value="portal.page.editing.move${fragmentType}Up"/>
              <c:set var="fmtKeyDown" value="portal.page.editing.move${fragmentType}Down"/>
              <c:set var="fmtKeyRemove" value="portal.page.editing.remove${fragmentType}"/>
              
              <%-- Move Portlet Left --%>
              <c:if test="${lc gt 0 and col gt 0}">
                <a href="#" onClick="f<c:out value='${actionName}'/>.move.value='3';f<c:out value='${actionName}'/>.submit();return false;" class="move-portlet-left">
                  <img src="<c:out value='${movePortletLeft}'/>" border="0" title="<fmt:message key='${fmtKeyLeft}'/>"/>
                </a>
              </c:if>
              
              <c:if test="${row gt 0}">
                <a href="#" onClick="f<c:out value='${actionName}'/>.move.value='1';f<c:out value='${actionName}'/>.submit();return false;" class="move-portlet-up">
                  <img src="<c:out value='${movePortletUp}'/>" border="0" title="<fmt:message key='${fmtKeyUp}'/>"/>
                </a>
              </c:if>

              <%-- Remove Portlet BUG: report typo in class --%>
              <a href="#" onClick="f<c:out value='${actionName}'/>.remove.value='y';f<c:out value='${actionName}'/>.submit();return false;" class="remove-portlet-from-page">
                <img src="<c:out value='${imgClose}'/>" border="0" title="<fmt:message key='${fmtKeyRemove}'/>"/>
              </a>       
              <%-- Move Portlet Down --%>
              <c:if test="${row lt lastRow}">
                <a href="#" onClick="f<c:out value='${actionName}'/>.move.value='2';f<c:out value='${actionName}'/>.submit();return false;" class="move-portlet-down">
                  <img src="<c:out value='${movePortletDown}'/>" border="0" title="<fmt:message key='${fmtKeyDown}'/>"/>
                </a>
              </c:if>

              <%-- Move Portlet Left --%>
              <c:if test="${lc gt 0 and col lt lc}">
                <a href="#" onClick="f<c:out value='${actionName}'/>.move.value='4';f<c:out value='${actionName}'/>.submit();return false;" class="move-portlet-right">
                  <img src="<c:out value='${movePortletRight}'/>" border="0" title="<fmt:message key='${fmtKeyRight}'/>"/>
                </a>
              </c:if>
            </form>      
          </div> <!--  B: div  portlet-edit-bar portlet positioning -->
  
  
  