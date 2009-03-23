<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/xml; charset=UTF-8" import="java.io.IOException,java.util.*,info.papyri.ddbdp.servlet.*" %><sru:scanResponse
 xmlns:sru="http://www.loc.gov/zing/srw/"
 xmlns:dc="http://purl.org/dc/elements/1.1/"
 xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/">
 <sru:diagnostics>
<diag:diagnostic>
    <diag:uri><%=request.getAttribute(Sru.DIAGNOSTIC_URI_ATTR)%></diag:uri>
    <diag:details><%=request.getAttribute(Sru.DIAGNOSTIC_DETAIL_ATTR)%></diag:details>
    <diag:message><%=request.getAttribute(Sru.DIAGNOSTIC_MESSAGE_ATTR)%></diag:message>
</diag:diagnostic>
</sru:diagnostics>
</sru:scanResponse>