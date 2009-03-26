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
<%@ include file="decorations_portlet_init.jsp" %>

<%@page import="javax.portlet.WindowState"%>
 
  <%-- BEGIN PORTLET SCOPE CONSTANTS --%>
    <%
	  /**
	   * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
	   */
    
      // TODO find out where is this set? or how is it set?
      String _hidePortletAttrKey = "js_" + _jpt.getCurrentFragment().getId() + "_HideDecorator";
      Boolean _hidePortlet = (Boolean) renderRequest.getAttribute(_hidePortletAttrKey);
      pageContext.setAttribute("hidePortlet", _hidePortlet, DEFAULT_SCOPE);
      
      String _wsStr = _jpt.getMappedWindowState().toString();
      pageContext.setAttribute("wsStr", _wsStr, DEFAULT_SCOPE);
 
      WindowState _ws = _jpt.getMappedWindowState();
      pageContext.setAttribute("ws", _ws, DEFAULT_SCOPE);
  
      String _pTitle = _jpt.getTitle(_jpt.getCurrentPortletEntity(), _jpt.getCurrentFragment());
      pageContext.setAttribute("pTitle", _pTitle, DEFAULT_SCOPE);
      
      pageContext.setAttribute("isMinimized",_ws.toString().equalsIgnoreCase("minimized"), DEFAULT_SCOPE);
    %>

    <c:if test="${wsStr eq 'solo'}" var="solo" scope="page"/>

  <%-- END PORTLET SCOPE CONSTANTS --%>

                      <div id="<c:out value='${frag.id}'/>" class="portlet <c:out value='${decoration.baseCSSClass}'/>"> <!-- B: portlet div -->

                        <!--  portlet title bar -->
                        <c:if test="${not solo and not hidePortlet}">
                          <div class="PTitle"> <!--  B: PTitle div -->
                            <div class="PTitleContent">
                              <c:out value="${pTitle}"/>
                            </div>
                            <c:out escapeXml="false" value="${PortletActionBar}"/>
                          </div><!--  E: PTitle div -->
                        </c:if>                       
                        
                        <!--  finally portlet content -->
                        <c:if test="${not isHidden and not hidePortlet and not isMinimized}">

                          <c:if test="${not solo}">
                            <div class="PContentBorder"> <!-- B: div PContentBorder -->
                          </c:if>

                              <div class="PContent"> <!-- B: P rendered Content -->
                                <span style="line-height:0.005px;">&nbsp;</span>
                                <c:out escapeXml="false" value="${frag.renderedContent}"/>
                              </div> <!-- E: P rendered Content -->

                          <c:if test="${not solo}">
                            </div> <!-- E: div PContentBorder -->
                          </c:if>

                        </c:if>
                        
                      </div> <!-- E: portlet div -->
                      