package info.papyri.navigator.portlet;

import info.papyri.index.DBUtils;
import info.papyri.index.LuceneIndex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;

import util.NumberConverter;


public class XREFSearchPortlet extends GenericPortlet {

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        super.destroy();
    }

    @Override
    protected void doView(RenderRequest arg0, RenderResponse arg1) throws PortletException, IOException {
        if (LuceneIndex.SEARCH_XREF == null){
            PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/indexing.jsp");
            rd.include(arg0,arg1);
            return;
        }
//        String pattern = arg0.getParameter("xrefPattern");
//        String schema = arg0.getParameter("xrefSchema");
//        String altSeries = arg0.getParameter("pubnum_series");
//        String altVol = arg0.getParameter("pubnum_vol");
//        String altDoc = arg0.getParameter("pubnum_doc");
//        String altInst = (arg0.getParameter("apisnum_inst") == null)?"":arg0.getParameter("apisnum_inst");
//        String altNum = arg0.getParameter("apisnum_num");
//
//        boolean apisSubmitted = (schema != null) && ("altApis".equals(schema));
//        boolean altSubmitted = (schema != null) && (altSeries != null || altVol != null || altDoc != null || altInst != null || altNum != null); 
//        
//        boolean html = !"xml".equals(arg0.getParameter("xrefOutput"));
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/xrefsearch.jsp");
        rd.include(arg0,arg1);
        if (arg0.getParameter("xrefSchema") == null || "".equals(arg0.getParameter("xrefSchema"))) return;
//        rd = getPortletContext().getRequestDispatcher("/WEB-INF/xrefresults.jsp");
        rd = getPortletContext().getRequestDispatcher("/WEB-INF/xrefexhibit.jsp");
//        Query q = null;
//        System.out.println("schema =\"" + schema + "\"");
//        System.out.println("pattern =\"" + pattern + "\"");
//        if (!apisSubmitted && !altSubmitted) return;
//        if (apisSubmitted){
//            pattern = ("".equals(altInst))?"*":altInst;
//            pattern += ".apis.";
//            pattern +=  ("".equals(altNum))?"*":altNum;
//        }
//
//        if (pattern == null) pattern = "";
//        if (schema == null) schema = "";
//        if (altSeries == null) altSeries = "";
//        if (altVol == null) altVol = "";
//        if (altDoc == null) altDoc = "";
//        if (altInst == null) altInst = "";
//        if (altNum == null) altNum = "";
//        
//        
//        boolean wild = (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1);
//        
//        if (!wild)pattern += "*";
//
//        if (apisSubmitted){
//            q = new WildcardQuery(new Term("APIS:metadata:apis:controlname",pattern));  
//        }
//        else if (altSubmitted){
//            String pubPattern = altSeries;
//            pubPattern += ("".equals(altVol))?"": " " +  altVol;
//            pubPattern += ("".equals(altDoc))?"": " " +  altDoc;
//            if (pubPattern.indexOf('*') == -1 && pubPattern.indexOf('?') == -1) pubPattern += "*";
//            System.out.println("pubPattern =\"" + pubPattern + "\"");
//            q = new WildcardQuery(new Term("APIS:publication",pubPattern));  
//            
//        }
//        else if ("altApis".equals(schema)){
//            String pubPattern = "" + arg0.getParameter("apisnum_inst") + ".apis." +  arg0.getParameter("apisnum_num"); 
//            System.out.println("pubPattern =\"" + pubPattern + "\"");
//            q = new WildcardQuery(new Term("APIS:metadata:apis:controlname",pubPattern));  
//            
//        }
//        
//        Hits h = LuceneIndex.SEARCH_XREF.search(q);
//        int hits = h.length();
//        if (hits > 0){
            String jsURI = "/apisplusPortals/numbers?";
            
            Enumeration parmNames = arg0.getParameterNames();
            while(parmNames.hasMoreElements()){
                String parmName = parmNames.nextElement().toString();
                String [] vals = arg0.getParameterValues(parmName);
                for (int i=0;i<vals.length;i++){
                    jsURI += parmName + "=" + vals[i];
                    if (i < vals.length - 1) jsURI += "&";
                }
                if (parmNames.hasMoreElements()) jsURI += "&";
            }
            arg0.setAttribute("JS_URI", jsURI);
            rd.include(arg0, arg1);
//        }
//        
//        if (html){
//            writeHTML(rd,arg0,arg1,h);
//        }
//        else {
//            writeXML(rd,arg0,arg1,h);
//        }
    }
    
    private static void writeHTML(PortletRequestDispatcher rd, RenderRequest request, RenderResponse response, Hits hits)
    throws IOException, PortletException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<table style=\"width:auto\">");
        writer.println("<tr><th class=\"rowheader\">APIS</th><th class=\"rowheader\">papyri.info</th><th class=\"rowheader\">HGV</th><th class=\"rowheader\">DDBDP</th><th class=\"rowheader\">(reserved)</th></tr>");
        Iterator i = hits.iterator();
        while(i.hasNext()){
            Document d =((Hit)i.next()).getDocument();
            request.setAttribute(NavigatorPortlet.DOC_ATTR, d);
            rd.include(request, response);
        }
        writer.println("</table>");
    }

    private static void writeXML(PortletRequestDispatcher rd, RenderRequest request, RenderResponse response, Hits hits)
    throws IOException, PortletException {
        response.setContentType("text/xml");
        PrintWriter writer = response.getWriter();
        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        writer.println("<documents");
        writer.println("xmlns=\"\"http://apis.edu/numberserver\"");
        writer.println("xmlns:xsd=\"\"http://www.w3.org/2001/XMLSchema\"");
        writer.println(">");
        writer.println("<tr><th>Identifier</th><th>APIS</th><th>HGV</th><th>DDBDP</th><th>LDAB</th></tr>");
        Iterator i = hits.iterator();
        while(i.hasNext()){
            Document d =((Hit)i.next()).getDocument();
            request.setAttribute(NavigatorPortlet.DOC_ATTR, d);
            rd.include(request, response);
        }
        writer.println("</table>");
    }

    @Override
    public void init(PortletConfig config) throws PortletException {
        try{
            super.init(config);
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }
    
    public static String normalizePub(String series, String volume, String document){
        series = normalizeSeries(series);
        volume = normalizeVolume(volume);
        document = normalizeDocument(document);

        
        
      if ("*".equals(volume) && "*".equals(document)){
          return series + " " + volume;
      }else{
          return series + " " + volume + " " + document;    
      }
    }
    
    public static String normalizeSeries(String series){
        series = series.trim();
        if (series.indexOf('*') != -1){
            return series;
        }
        if ("".equals(series)){
            return "*";
        }
        else {
            String normal = series.replaceAll(" ","");
            char [] lc = new char[0];
            normal = normal.replaceAll("\\.","\\. ").trim();
            normal = normal.replaceFirst("\\. ", ".");
            String [] seriesC = normal.split("\\.(\\s)?");
            switch (seriesC.length){
            case 3:
                lc = seriesC[2].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[2],new String(lc));
            case 2:
                lc = seriesC[1].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[1],new String(lc));
            case 1:
                lc = seriesC[0].toLowerCase().toCharArray();
                lc[0] = Character.toUpperCase(lc[0]);
                normal = normal.replaceAll(seriesC[0],new String(lc));
            default:
                break;
            }
            series = normal;
        }
        return series;
    }

    public static String normalizeVolume(String volume){
        if (volume == null || "".equals(volume.trim())) return "*";
        return NumberConverter.getRoman(volume);
    }
    
    public static String normalizeDocument(String document){
        if (document == null || "".equals(document.trim())) return "*";
        return document.trim();
    }
}
