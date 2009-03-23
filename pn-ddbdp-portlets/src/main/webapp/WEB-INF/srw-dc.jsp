<%@ page language="java" pageEncoding="UTF-8" session="false" contentType="text/xml; charset=UTF-8" import="java.io.IOException,java.util.*,info.papyri.ddbdp.servlet.*" %><%!
String getUTF8(String parm){
    if(parm == null) return "";
    try{
        return new String(parm.getBytes("ISO-8859-1"),"UTF-8");
    }
    catch(IOException ioe) {
        return "";
    }
}
%><sru:searchRetrieveResponse
 xmlns:sru="http://www.loc.gov/zing/srw/"
 xmlns:dc="http://purl.org/dc/elements/1.1/"
 xmlns:diag="http://www.loc.gov/zing/srw/diagnostic/">
      <sru:version>1.1</sru:version>
<% String [] fnames = (String[])request.getAttribute(Sru.FILENAMES_ARRAY_ATTR);
String [] fragments = (String[])request.getAttribute(Sru.FRAGMENTS_ARRAY_ATTR);
String next = (String)request.getAttribute(Sru.NEXT_RECORD_POS_ATTR);
String numRecs = (String)request.getAttribute(Sru.NUM_RECS_ATTR);
boolean doFrags = (fragments != null && fragments.length == fnames.length);
    out.print("<sru:numberOfRecords>");
    out.print((fnames == null)?"0":numRecs);
    out.println("</sru:numberOfRecords>");
    out.println("<sru:records>");
    for(int i=0;i<fnames.length;i++){
        out.println("<sru:record>");
        out.println("<sru:recordSchema>info:srw/schema/1/dc-v1.1</sru:recordSchema>");
        out.println("<sru:recordPacking>xml</sru:recordPacking>");
        out.println("<sru:recordData>");
        out.println("<dc:identifier>" + fnames[i] + "</dc:identifier>");
        if(doFrags){
            out.println("<dc:description>" + fragments[i] + "</dc:description>");
        }
        out.println("</sru:recordData>");
        out.println("</sru:record>");
    }
    out.println("</sru:records>");
    if(next != null){
    out.print("<sru:nextRecordPosition>");
    out.print(next);
    out.println("</sru:nextRecordPosition>");
    }
%>
<sru:echoedSearchRetrieveRequest>
    <sru:version>1.1</sru:version>
    <sru:query><%=request.getAttribute(Sru.CQL_ATTR)%></sru:query>
    <sru:recordScema>dc</sru:recordScema>
    <sru:baseUrl>http://<%= request.getServerName() %>:<%= request.getServerPort() %><%=request.getContextPath() %>/sru</sru:baseUrl>
    <%=request.getAttribute(Sru.XQL_ATTR)%>
</sru:echoedSearchRetrieveRequest>
</sru:searchRetrieveResponse>