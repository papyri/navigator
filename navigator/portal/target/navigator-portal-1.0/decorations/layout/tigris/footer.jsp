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
<%@ include file="../initLayoutDecorators.jsp" %>
  <%
	  /**
	   * @author <a href="mailto:kmoh.raj@gmail.com">Mohan Kannapareddy</a>
	   */
      //contextPath
      String _cPath = (String) request.getContextPath();
      pageContext.setAttribute("imgFooter", _cPath + "/" +
              getLayoutResource(_layoutDecoration,"images/Jetspeed_blue_sm.png"), PAGE_SCOPE);
  %>



          </td>   <!--  E: all portlet content -->
        </tr>    <!--  E: Main row -->
      </table> <!--  S: ALL CONTENT TABLE -->

    </div> <!-- END: body div wrapper -->
    <p>
      <img src="<c:out escapeXml='false' value='${imgFooter}'/>" alt="Jetspeed 2 Powered" border="0" />
    </p>
  </body>
</html>

