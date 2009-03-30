<%--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.0//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic10.dtd">
<%@ page language="java" import="org.apache.jetspeed.login.LoginConstants" session="true" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.apache.jetspeed.login.resources.LoginViewResources" />

<html>
<body>
<div>
<form id="login" method="POST" action="<%= response.encodeURL("j_security_check") %>">
<fmt:message key="username.label"/><input type="text" name="j_username" value="<%= session.getAttribute(LoginConstants.USERNAME) %>"/><br/>
<fmt:message key="password.label"/><input type="password" name="j_password" value="<%= session.getAttribute(LoginConstants.PASSWORD) %>"/><br/>
<input type="submit" value="<fmt:message key="login.button"/>"/>
</form>
</div>
</body>
</html>
