<%@ page language="java"%>
<%@ page session="false" contentType="text/html" import="info.papyri.navigator.portlet.*,org.apache.lucene.document.*,java.util.*,javax.portlet.*" %><div>
<%@ taglib uri="http://java.sun.com/portlet" 
prefix="portlet"%>
<%!
 final static String DDBDP =
    "http://www.perseus.tufts.edu/hopper/text.jsp?doc=Perseus:text:1999.05.QQserQQ:volume=QQvolQQ:document=QQdocQQ";
  String getDDBDPlink(Field f){
    if (f == null) return "";
    String [] vals = f.stringValue().split(";");
    if (vals.length < 3) return "";
    String series = vals[0];
    String volume = vals[1];
    String doc = vals[2];
    return DDBDP.replace("QQserQQ",series).replace("QQvolQQ",volume).replace("QQdocQQ",doc);
}
 final static String LDAB =
    "http://ldab.arts.kuleuven.ac.be/ldab_text_detail.php?tm=QQqueryQQ";
  String getLDABlink(Field f){
      if (f == null) return "";
      String val = f.stringValue();
      return LDAB.replace("QQqueryQQ",val);
}
 final static String APIS =
    "http://wwwapp.cc.columbia.edu/ldpd/app/apis/search?mode=search&apisnum_inst=QQinstQQ&apisnum_num=QQnumQQ&sort=date&resPerPage=25&action=search&p=1";
  String getAPISlink(Field f){
    if (f == null) return "";
    String [] vals = f.stringValue().split("\\.");
    if (vals.length < 3) return f.stringValue();
    String inst = vals[0];
    String num = vals[2];
    return APIS.replace("QQinstQQ",inst).replace("QQnumQQ",num);
}
 final static String HGV =
    "http://aquila.papy.uni-heidelberg.de/Hauptregister/FMPro?-db=hauptregister%5f&PublikationL=QQqueryQQ&-format=DFormVw.htm&-lay=Einzel&-max=1&-skip=0&-token=25&-find";
  String getHGVlink(Field f){
    if (f == null) return "";
    String val = f.stringValue().replace(" ","%20");
    return HGV.replace("QQqueryQQ",val);
}
%>
<portlet:defineObjects/>
<%
String apisId = request.getParameter("controlName").trim();
PortletRequest portletRequest = (PortletRequest)
request.getAttribute("javax.portlet.request");
  Document doc = (Document)portletRequest.getAttribute(NavigatorPortlet.DOC_ATTR);
if (doc != null){
%>
<h2 class="apis-portal-title">Identifier XRef for <%=apisId %></h2>
<table style="width:auto">
<tr><th class="rowheader">APIS</th><td><%if (doc.getField("APIS:metadata:apis:controlname") != null){
    %><a href="<%=getAPISlink(doc.getField("APIS:metadata:apis:controlname")) %>"><%=doc.get("APIS:metadata:apis:controlname")%></a>
    <%
    }
    else{ %>
    &nbsp;
    <%} %></td></tr>
<tr><th class="rowheader">HGV</th><td><%if (doc.getField("APIS:metadata:hgv:publikationl") != null){
    %><a href="<%=getHGVlink(doc.getField("APIS:metadata:hgv:publikationl")) %>"><%=doc.get("APIS:metadata:hgv:publikationl")%></a>
    <%
    }
    else{ %>
    &nbsp;
    <%} %></td></tr>
<tr><th class="rowheader">DDBDP</th><td><%if (doc.getField("APIS:metadata:ddbdp:ddbfull") != null){
    %><a href="<%=getDDBDPlink(doc.getField("APIS:metadata:ddbdp:ddbfull")) %>"><%=doc.get("APIS:metadata:ddbdp:ddbfull")%></a>
    <%
    }
    else{ %>
    &nbsp;
    <%} %></td></tr><!-- 
<tr><th class="rowheader">LDAB</th><td><%if (doc.getField("APIS:metadata:leuven:texid") != null){
    %><a href="<%=getLDABlink(doc.getField("APIS:metadata:leuven:texid")) %>"><%=doc.get("APIS:metadata:leuven:texid")%></a>
    <%
    }
     %>
    </td></tr>-->
</table>
</div>
<%
}
else {
%>
null document
<%
}
%>