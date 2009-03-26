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
<%@ include file="initTemplatesLayoutCustomizerNormal.jsp" %>



    <%
      /**
      * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
      */

      //chooser url
      String _chooserUrl = _jpt.getBasePath() + 
                                  "/system/customizer/selector.psml?jspage=" +
                                  _psrc.getPage().getPath() +
                                  "&jslayoutid=" +
                                  _myFragment.getId();
      pageContext.setAttribute("chooserUrl", renderResponse.encodeURL(_chooserUrl), 
              PAGE_SCOPE);
      
      // all images paths
      
      pageContext.setAttribute("imgSelect", _cPath + "/" + getLayoutResource(_rootDecorator,"images/select.gif"), 
              PAGE_SCOPE);
    
    %>
    <!--  setup the message bundle -->
    
    
    <c:set var="chooser" scope="page" value="${jpt.basePath}/system/customizer/selector.psml?jspage=${profiledPage}&jslayoutid=${myFragment.id}"/>

        <!--  BEGIN: customizer content -->

    <div class="layout-edit-bar">   <!-- B: div layout-edit-bar -->

          <fieldset>    
            <div class="layout-edit-bar-left"> <!-- B: div layout-edit-bar-left -->
                <div class="layout-title">
                  <b><fmt:message key="portal.page.customizer.name"/></b>
                </div>
            </div> <!-- E: div layout-edit-bar-left -->
            <div class="layout-edit-bar-right"> <!-- B: div layout-edit-bar-right -->
              <c:if test="${allowAddPortlet}">
                <label><fmt:message key="portal.page.editing.portlet"/></label>
                <a href="<c:out escapeXml='false' value='${chooserUrl}'/>">
                  <img src="<c:out value="${imgSelect}"/>"
                       border="0"
                       title="<fmt:message key="portal.page.editing.addportlet"/>"/>
                </a>
              </c:if>
            </div> <!-- E: div layout-edit-bar-right -->
          </fieldset>

        <!--  END: customizer content -->
        
        <!-- BEGIN: All editing modes -->
        <form name="f<portlet:namespace/>" action="<portlet:actionURL/>" method="post"> <!--  B: main form -->
          <!-- add and change layout -->
          <fieldset> <!-- B: add change layout -->
              <div class="layout-edit-bar-left"> <!-- B: customizer layout-edit-bar-left -->

            <%-- B: addLayout or changeLayout block--%>
            <c:if test="${allowAddLayout or allowChangeLayout}">
                 <div class="layout-title"><b><fmt:message key="portal.page.editing.layoutConfiguration"/></b></div>   

                 <div class="layout-content"> <!-- B: div class layout-content -->
            </c:if>

            <%-- allowChangePageTheme --%>
            <c:if test="${allowChangePageTheme}">
                  <div> <!-- B: div allowChangePageTheme -->
                    <label for="theme"><fmt:message key="portal.page.editing.theme"/></label>
                    <select id='theme' name='theme' onChange="f<portlet:namespace/><portlet:actionURL/>">
                      <c:forEach var="td" items="${pageDecorations}">
                        <c:set var="_sel" value=""/>
                        <c:if test="${rootDecorator.name eq td}">
                          <c:set var="_sel" value='selected="selected"'/>
                        </c:if>
                        <option value="<c:out value='${td}'/>" <c:out escapeXml="false" value="${_sel}"/>/>
                        <c:out value="${td}"/>
                      </c:forEach>
                    </select>   
                  </div> <!-- E: div allowChangePageTheme -->
            </c:if>
            
            <%-- add/changeLayout --%>
            <c:if test="${allowAddLayout or allowChangeLayout}">
                  <div> <!-- B: div add change layout -->
                    <label for="layout"><fmt:message key="portal.page.editing.layout"/></label>
                    <select id='layout' name='layout'>
                      <c:forEach var="ld" items="${layoutDecorationsList}">
                        <c:set var="_sel" value=""/>
                        <c:if test="${myFragment.name eq ld.name}">
                          <c:set var="_sel" value='selected="selected"'/>
                        </c:if>
                        <option value="<c:out value='${ld.name}'/>" 
                                <c:out escapeXml="false" value="${_sel}"/>/>
                                <c:out value="${ld.name}"/>
                      </c:forEach>
                    </select>
            </c:if>
            <c:if test="${allowChangeLayout}">
                    <input type="submit" name="jsChangeLayout" value="<fmt:message key='portal.page.editing.changelayout'/>" />
            </c:if>
            <c:if test="${allowAddLayout}">
                    <input type="submit" name="jsAddLayout" value="<fmt:message key='portal.page.editing.addlayout'/>" />
            </c:if>
            <c:if test="${allowAddLayout or allowChangeLayout}">
                  </div> <!-- E: div add change layout -->
            </c:if>

            <%-- allow portlet decorations --%>
            <%-- TODO figure out what myFragment should be ?? --%>
            <c:if test="${allowChangePortletDecorator}">
                  <div> <!--  B: allowChangePortletDecorator -->
                    <input type="hidden" name="fragment" value="<c:out value='${myFragment.id}'/>">
                    <input type="hidden" name="move" value="">
                    <input type="hidden" name="remove" value="">
                    <label for="decorators"><fmt:message key='portal.page.editing.portlet.decorator'/></label>
                    <select id="decorators" name="decorators">
                      <option value=""/>
                        <c:forEach var="pd" items="${portletDecorationsSet}">
                          <c:set var="_sel" value=""/>
                          <c:if test="${myFragment.decorator eq pd}">
                            <c:set var="_sel" value='selected="selected"'/>
                          </c:if>
                          <option value="<c:out value='${pd}'/>" 
                                  <c:out escapeXml="false" value="${_sel}"/>/>
                                  <c:out value="${pd}"/>
                        </c:forEach>
                    </select>
                    <input type="submit" name="jsSubmitTheme" value="<fmt:message key='portal.page.editing.changethemeall'/>"/>
                  </div> <!--  E: allowChangePortletDecorator -->
            </c:if>
            
            <c:if test="${allowAddLayout or allowChangeLayout}">
                 </div> <!-- E: div class layout-content -->
            </c:if>
            
            <%-- E: addLayout or changeLayout block--%>
              </div> <!-- E: customizer layout-edit-bar-left -->
          </fieldset><!-- E: add change layout -->
          
          <%-- All portal page customizers  --%>
          <fieldset style="float:left;width:49%;margin:0px;padding:0px;"> <!-- B: portal page customizer fieldset -->

            <div class="layout-edit-bar-left"> <!-- B: page customizer layout-edit-bar-left -->

            <%-- B: AddPage ChangePage PageTheme customizer block--%>
            <c:if test="${allowAddPage or allowChangePageName or allowDeletePage}">
                 <div class="page-title"><b><fmt:message key="portal.page.editing.pageConfiguration"/></b></div>   
                 <div class="page-content"> <!-- B: div class page-content -->
            </c:if>

            <c:if test="${allowAddPage or allowChangePageName}">
                    <div>
                      <label for="pagename"><fmt:message key="portal.page.editing.page"/></label>
                      <input id="pagename" name="jsPageName"/>
                    </div>
                    <div>
                      <label for="pagetitle"><fmt:message key="portal.page.editing.page.title"/></label>
                      <input id="pagetitle" name="jsPageTitle"/>
                    </div>
                    <div>
                      <label for="pageshorttitle"><fmt:message key="portal.page.editing.page.shorttitle"/></label>
                      <input id="pageshorttitle" name="jsPageShortTitle"/>
                    </div>
                    <div> <!--  B: test allowAddPage or allowChangePageName -->
            </c:if>
            <c:if test="${allowAddPage}">
                      <input type="submit" name="jsSubmitPage" value="<fmt:message key='portal.page.editing.addpage'/>"/>
            </c:if>                    
            <c:if test="${allowChangePageName}">
                      <input type="submit" name="jsChangePageName" value="<fmt:message key='portal.page.editing.changepagename'/>"/>
            </c:if>                    
            <c:if test="${allowAddPage or allowChangePageName}">
                    </div> <!--  E: test allowAddPage or allowChangePageName -->
            </c:if>
                                
            <c:if test="${allowNavigatePage}">
                    <div>
                      <label><fmt:message key="portal.page.editing.pagenavigation"/></label>
                      <input id="move-page-left" type="submit" name="jsMovePageLeft" value="<fmt:message key='portal.page.editing.movePageLeft'/>" />
                      <input id="move-page-right" type="submit" name="jsMovePageRight" value="<fmt:message key='portal.page.editing.movePageRight'/>" />
                    </div>
            </c:if>                    

            <c:if test="${allowDeletePage}">
                    <div>
                      <label for="deletepage"><fmt:message key="portal.page.editing.deleteThisPage"/></label>
                      <input id="deletepage" type="submit" name="jsDeletePage" value="<fmt:message key='portal.page.editing.deletePage'/>" onclick="if(window.confirm('<fmt:message key="portal.page.editing.confirmDeleteThisPage"/>')){return true;}return false;"/>
                    </div>
            </c:if>                    


            <c:if test="${allowAddPage or allowChangePageName or allowDeletePage}">
                 </div>                      <!-- E: div class page-content -->
            </c:if>
            <%-- E: AddPage ChangePage PageTheme customizer block--%>
            </div> <!-- E: page customizer layout-edit-bar-left -->
          </fieldset> <!-- E: portal page customizer fieldset -->

            <%-- BEGIN FOLDER CUSTOMIZATION --%>
            
          <fieldset style="width:49%;margin:0px;padding:0px;">
            <div class="layout-edit-bar-left"> <!-- B: folder customizer layout-edit-bar-left -->
            
            <c:if test="${allowAddFolder or allowChangeFolderName or allowDeleteFolder}">
                 <div class="folder-title"><b><fmt:message key="portal.folder.editing.folderConfiguration"/></b></div>   
                 <div class="folder-content"> <!-- B: div class folder-content -->
            </c:if>
            
            <c:if test="${allowAddFolder or allowChangeFolderName}">
                    <div>
                      <label for="foldername"><fmt:message key="portal.folder.editing.folder"/></label>
                      <input id="foldername" name="jsFolderName"/>
                    </div>
                    <div>
                      <label for="foldertitle"><fmt:message key="portal.folder.editing.folder.title"/></label>
                      <input id="foldertitle" name="jsFolderTitle"/>
                    </div>
                    <div>
                      <label for="foldershorttitle"><fmt:message key="portal.folder.editing.folder.shorttitle"/></label>
                      <input id="foldershorttitle" name="jsFolderShortTitle"/>
                    </div>
                    <div> <!--  B: test allowAddFolder or allowChangeFolderName -->
            </c:if>
            <c:if test="${allowAddFolder}">
                      <input type="submit" name="jsSubmitFolder" value="<fmt:message key='portal.folder.editing.addfolder'/>"/>
            </c:if>
            <c:if test="${allowChangeFolderName}">
                      <input type="submit" name="jsChangeFolderName" value="<fmt:message key='portal.folder.editing.changefoldername'/>"/>
            </c:if>
            <c:if test="${allowAddFolder or allowChangeFolderName}">
                    </div> <!--  E: test allowAddFolder or allowChangeFolderName -->
            </c:if>

            <c:if test="${allowNavigateFolder}">
                    <div>
                      <label><fmt:message key="portal.folder.editing.foldernavigation"/></label>
                      <input id="move-folder-left" type="submit" name="jsMoveFolderLeft" value="<fmt:message key='portal.folder.editing.moveFolderLeft'/>" />
                      <input id="move-folder-right" type="submit" name="jsMoveFolderRight" value="<fmt:message key='portal.folder.editing.moveFolderRight'/>" />
                    </div>
            </c:if>

            <c:if test="${allowDeleteFolder}">
                    <div>
                      <label for="deletefolder"><fmt:message key="portal.folder.editing.deleteThisFolder"/></label>
                      <input id="deletefolder" type="submit" name="jsDeleteFolder" value="<fmt:message key='portal.folder.editing.deleteFolder'/>" onclick="if(window.confirm('<fmt:message key="portal.folder.editing.confirmDeleteThisFolder"/>')){return true;}return false;"/>
                    </div>
            </c:if>
            
            <c:if test="${allowAddFolder or allowChangeFolderName or allowDeleteFolder}">
                 </div>                      <!-- E: div class folder-content -->
            </c:if>
            </div> <!-- E: folder customizer layout-edit-bar-left -->
          </fieldset> <!-- E: folder customizer fieldset -->
          
            <%-- END FOLDER CUSTOMIZATION --%>
          <br style="clear:both;">
        </form>   <!--  E: main form -->    

    </div>                            <!-- E: div layout-edit-bar -->
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        