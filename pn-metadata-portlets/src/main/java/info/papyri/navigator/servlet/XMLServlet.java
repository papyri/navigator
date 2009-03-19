package info.papyri.navigator.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Iterator;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.papyri.index.LuceneIndex;
import info.papyri.metadata.*;
import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;

import util.NumberConverter;

public class XMLServlet extends HttpServlet {
    private final static Term TERM_TEMPLATE = new Term(CoreMetadataFields.DOC_ID,"");
    private final static int NONE = 0;
    private final static int APIS = 1;
    private final static int HGV = 2;
    private final static String [] IDS = new String[]{"none","apis","hgv"};
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        String cn = arg0.getParameter("controlName");
        cn = new String(cn.getBytes("ISO-8859-1"),"UTF-8");
        IndexSearcher searcher = null;
        int collId = NONE;
        if (cn.startsWith(NamespacePrefixes.APIS)){
            searcher = LuceneIndex.SEARCH_COL;
            collId = APIS;
        }
        else if (cn.startsWith(NamespacePrefixes.HGV)){
            searcher = LuceneIndex.SEARCH_HGV;
            collId = HGV;
        }
        arg1.setContentType("text/xml");
        PrintWriter pw = arg1.getWriter();
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.print("<modsCollection xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.loc.gov/mods/v3\" xsi:schemaLocation=\"http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-2.xsd\" ");
        pw.println(">");
        if (searcher != null){
            Query query = new TermQuery(TERM_TEMPLATE.createTerm(cn));
            Hits hitsCollection = searcher.search(query);
            Iterator<Hit> hits = hitsCollection.iterator();
            if (hitsCollection.length() == 0){
                pw.println("<!-- ");
                pw.println("no results for " + query.toString());
                pw.println("-->");
            }
            while (hits.hasNext()){
                Hit hit = hits.next();
                Document doc = hit.getDocument();
                apis(doc,pw);
            }
        }
        pw.print("</modsCollection>");
        pw.flush();
    }
    
    private static void apis(Document doc, PrintWriter out) throws IOException {
        String cn = doc.get(CoreMetadataFields.DOC_ID);
        String id = (cn == null)?"":cn.replaceAll("[\\s-]+","_");
        String location = info.papyri.navigator.portlet.XREFPortlet.getAPISlink(cn);
        String [] pubs = doc.getValues(CoreMetadataFields.BIBL_PUB);
        String [] bl = doc.getValues(CoreMetadataFields.PUB_ABOUT);
        String translation = doc.get(CoreMetadataFields.TRANSLATION_EN);
        String [] imageUrls = doc.getValues(CoreMetadataFields.IMG_URL);
        String [] imageCaptions = doc.getValues(CoreMetadataFields.IMG_CAPTION);
        String subjectData = doc.get(CoreMetadataFields.SUBJECT_I);
        String [] subject = (subjectData == null)?new String[0]:subjectData.split(";");
        String material = doc.get(CoreMetadataFields.MATERIAL);
        String title = doc.get(CoreMetadataFields.TITLE);
        String [] provenances = doc.getValues(CoreMetadataFields.PROVENANCE);
        String remarks = doc.get(CoreMetadataFields.GEN_NOTES);
        String [] date1 = doc.getValues(CoreMetadataFields.DATE1_I);
        String [] date2 = doc.getValues(CoreMetadataFields.DATE2_I);
        Term idTerm = new Term(CoreMetadataFields.DOC_ID,cn);
        String [] suppIds = getIds(idTerm,CoreMetadataFields.XREFS);
        out.print("<mods ID=\"");
        out.print(id);
        out.print("\" version=\"3.2\" >\n");
        if (title != null){
            out.print("<titleInfo><title>");
            out.print(title);
            out.println("</title></titleInfo>");
        }
        if (cn != null){
            out.println("<relatedItem type=\"host\" ID=\"apis_" + id + "\">");
            out.print("<identifier type=\"local\">");
            out.print(cn);
            out.println("</identifier>");
            out.print("<identifier type=\"uri\">");
            out.print(location.replaceAll("&", "&amp;"));
            out.println("</identifier>");
            out.println("</relatedItem>");
        }
        out.println("<relatedItem type=\"otherVersion\" ID=\"publication_" + id + "\">");
        if (pubs != null){
            for (String pub:pubs){
                out.println("<relatedItem type=\"constituent\">");
                out.print("<identifier type=\"publication\">");
                out.print(pub);
                out.println("</identifier>");
                out.println("<typeOfResource>text</typeOfResource>");
                out.println("</relatedItem>");
            }
        }
        out.println("</relatedItem>");
        if (suppIds.length > 0){
            // write the "software,multimedia" items
            out.println("<relatedItem type=\"isReferencedBy\" ID=\"" + id + "\">");
            out.println("<typeOfResource>software, multimedia</typeOfResource>");
            for (String suppId:suppIds){
                out.println("<relatedItem type=\"constituent\">");
                out.print("<identifier type=\"local\">");
                out.print(suppId);
                out.print("</identifier><identifier type=\"uri\">");
                out.print(info.papyri.navigator.portlet.XREFPortlet.getHGVlink(suppId).replaceAll("&", "&amp;"));
                out.println("</identifier>");
                out.println("</relatedItem>");
            }
            out.println("</relatedItem>");
        }

        if (imageUrls != null){
            
            out.println("<relatedItem type=\"isReferencedBy\">");
            out.println("<typeOfResource>still image</typeOfResource>");
            for (int i = 0;i<imageUrls.length - 1;i+=2){
                out.println("<relatedItem type=\"constituent\">");
                out.print("<location><url>");
                out.print(imageUrls[i].replaceAll("&", "&amp;"));
                out.println("</url></location>");
                out.print("<note>");
                out.print(imageCaptions[i].replaceAll("&", "&amp;"));
                out.println("</note>");
                out.println("</relatedItem>");
            }
            out.println("</relatedItem>");
        }
        out.println("<originInfo>");
        
        if (provenances != null){
            for (String provenance:provenances){
                out.print("<place><placeTerm>");
                out.print(provenance);
                out.println("</placeTerm></place>");
            }
        }
        if (date1 != null){
            boolean range = (date2 != null && date2.length == date1.length);
            for (int i=0;i<date1.length;i++){
                String type = "descriptive-metadata-" + i;
                int [] date1Parts = NumberConverter.decodeDate(date1[i]);
                if (range && !date1[i].equals(date2[i])){
                    int [] date2Parts = NumberConverter.decodeDate(date2[i]);
                    out.print("<dateOther encoding=\"w3cdtf\" point=\"begin\" type=\"" + type + "\">");
                    out.print(NumberConverter.w3cdtf(date1Parts[0], date1Parts[1], date1Parts[2]));
                    out.println("</dateOther>");
                    out.print("<dateOther encoding=\"w3cdtf\" point=\"end\" type=\"" + type + "\">");
                    out.print(NumberConverter.w3cdtf(date2Parts[0], date2Parts[1], date2Parts[2]));
                    out.println("</dateOther>");
                }
                else{
                    out.print("<dateOther encoding=\"w3cdtf\" type=\"" + type + "\">");
                    out.print(NumberConverter.w3cdtf(date1Parts[0], date1Parts[1], date1Parts[2]));
                    out.println("</dateOther>");
                }
            }
        }
        
        out.println("</originInfo>");
        if (subject != null){
            out.println("<subject>");
            for (String topic:subject){
                out.print("<topic>");
                out.print(topic);
                out.println("</topic>");
            }
            out.println("</subject>");
        }
        if (material != null){
            String lc = material.toLowerCase();
            if (lc.indexOf("papyr") != -1){
                out.print("<genre authority=\"aat\" type=\"papyri\" />");    
            }
            if (lc.indexOf("ostra") != -1){
                out.print("<genre authority=\"aat\" type=\"ostraka\" />");    
            }
        }
        if (bl != null ){
            out.print("<note type=\"errata\">");
            out.print(bl);
            out.println("</note>");
        }
        if (translation != null ){
            out.print("<note type=\"translation\">");
            out.print(translation);
            out.println("</note>");
        }
        if (remarks != null){
            out.print("<note type=\"remarks\">");
            out.print(remarks);
            out.print("</note>");
        }
        
        out.print("</mods>");
        }
    
    private static String [] getIds(Term sTerm, String rTerm) throws IOException{
        HashSet<String> result = new HashSet<String>();
        TermQuery q = new TermQuery(sTerm);
        Hits hc = LuceneIndex.SEARCH_XREF.search(q);
        Iterator<Hit> hits = hc.iterator();
        while (hits.hasNext()){
            Document doc = hits.next().getDocument();
            String [] matches = doc.getValues(rTerm);
            if (matches == null) continue;
            for (String m:matches){
                if (!"".equals(m) && !m.startsWith("none")) result.add(m);
            }
        }
        return result.toArray(new String[0]);
    }

}
