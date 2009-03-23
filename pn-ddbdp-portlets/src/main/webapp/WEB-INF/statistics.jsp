<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.*" %><%!
String getQueryDesc(String [] terms, String op){
    StringBuffer result = new StringBuffer();
    if(terms.length > 0 && !"".equals(terms[0])){
        result.append('"');
        result.append(terms[0]);
        result.append('"');
    }
    if(terms.length > 0 && !"".equals(terms[0])){
        result.append(' ');
        result.append(op);
        result.append(' ');
        result.append('"');
        result.append(terms[1]);
        result.append('"');
    }
    return result.toString();
}
%><html>
<head>
<title>DDbDP Statistics</title>
</head>
<%

%>
<body>

</body>
</html>