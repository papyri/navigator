<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/html; charset=UTF-8" import="java.util.*,info.papyri.ddbdp.servlet.IndexEventPropagator,info.papyri.ddbdp.servlet.ScriptSearch,javax.portlet.RenderRequest,info.papyri.util.JetspeedUrlRewriter" %><%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%><%@page import="info.papyri.ddbdp.servlet.IndexEventPropagator"%>
<%@page import="info.papyri.ddbdp.portlet.SearchPortlet,org.apache.lucene.document.Document"%>
<%@page import="info.papyri.metadata.CoreMetadataFields"%><%! String CSS_ATTR = "jsp:private:css"; %>
<portlet:defineObjects/>
<%
boolean nofrags = "on".equals(request.getParameter("nofrags"));
RenderRequest rReq = (RenderRequest)renderRequest;
String fname = (String)rReq.getAttribute(SearchPortlet.FNAME_ATTR);
String ddbName = fname.substring(0,fname.length()-4);
JetspeedUrlRewriter jur = new JetspeedUrlRewriter();
String id = jur.rewriteId(rReq.getAttribute(SearchPortlet.DDB_ID_ATTR));
String staticFile = jur.getStaticDir(ddbName, id);
String fragment = (String)rReq.getAttribute(SearchPortlet.FRAGMENT_ATTR);
Document doc = (Document)rReq.getAttribute(SearchPortlet.RESULT_ITEM_ATTR);
String css = (String)rReq.getAttribute(CSS_ATTR);
if(css == null){
    css="";
    rReq.setAttribute(CSS_ATTR,"style=\"background-color:#eed;\"");
}
else{
    rReq.setAttribute(CSS_ATTR,null);
}
out.print("<tbody class=\"searchresult\" " + css + "><tr>");
if(CoreMetadataFields.SORTABLE_YES_VALUE.equals(doc.get(CoreMetadataFields.SORT_HAS_IMG))){
    out.print("<td><img src=\"/pn-portals/iavail.gif\" /></td>");
    //out.print("<td>" + doc.get(CoreMetadataFields.SORT_HAS_IMG) + "</td>");
}
else{
    out.print("<td>&nbsp;</td>");
    //out.print("<td>" + doc.get(CoreMetadataFields.SORT_HAS_IMG) + "</td>");
}
        out.println("<td style=\"font-weight:bold;font-size:1.1em;\"><a href=\"text/" + id + "\" >" +  ddbName + "</a><a target=\"_new\" href=\"/idp_static/current/data/aggregated/html" + staticFile + ".html\">[html]</a></td>");
        out.print("<td>");
        String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
        HashSet<String> pubSet = new HashSet<String>();
        
        if(pubs != null){
            for(String pub:pubs)pubSet.add(pub);
        }
        for(String pub:pubSet)out.print(pub + "<br/>");
        out.print("</td>");
        String date = doc.get(CoreMetadataFields.DATE1_D);
        if(date == null) date = "&nbsp;";
        String title = doc.get(CoreMetadataFields.TITLE);
        if(title == null) title = "none";
        String [] places = doc.getValues(CoreMetadataFields.PROVENANCE_NOTE);
        HashSet<String> placeSet = new HashSet<String>();
        if(places != null) for(String place:places)placeSet.add(place);
        out.print("<td>" + title + "</td>");
        out.print("<td>" + date + "</td>");
        out.print("<td>");
        for(String place:placeSet)out.print(place + "<br/>");
        out.print("</td>");
          out.println("</tr>");
        if(!nofrags){
            out.println("<tr " + css + "><td colspan=\"6\">" + fragment + "</td></tr>");
        }
        out.println("</tbody>");
    %>