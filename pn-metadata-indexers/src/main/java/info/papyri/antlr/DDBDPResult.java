package info.papyri.antlr;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharacterCodingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import info.papyri.data.*;

import java.sql.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.regex.*;

import java.net.URLEncoder;
import java.net.URLDecoder;

import info.papyri.util.DBUtils;

public class DDBDPResult {
    String decQuery = "";
    String encQuery = "";
    String pubColl = "";
    String document = "";
    String controlName = "";
    StringBuffer text = new StringBuffer();
    boolean foundMatch = false;
    private String pattern;
    private static final int COLL_BEGIN = "Perseus:text:1999.05.".length();
    public static final String getRegEx(String coll, String doc){
        return coll + ":\\w*:" + doc;
    }

    BetaCodeParser parser = new BetaCodeParser();
    
    public static String DEFAULT_CHARSET = "UTF-8";
    public static String encode(String in){
        try{
            return URLEncoder.encode(in,DEFAULT_CHARSET);
        }
        catch(Throwable t){
            return in;
        }
    }
    public static String decode(String in){
        try{
            return URLDecoder.decode(in,DEFAULT_CHARSET);
        }
        catch(Throwable t){
            return in;
        }
    }
    
    public DDBDPResult(String pattern){
        this.pattern = pattern;
    }
    public void setQuery(String query) {
        try{
            this.encQuery = query;
            this.decQuery = decode(query);
            String collQuery = this.decQuery.substring(0,this.decQuery.indexOf(":",COLL_BEGIN));
            this.pubColl = DBUtils.query(collQuery,true);
            this.document = this.decQuery.substring(this.decQuery.indexOf("document=") + "document=".length());
            if ("".equals(this.pubColl)|| this.pubColl == null) return;
            String pubRE = //getRegEx(pubColl,this.document);
                pubColl + ":*:" + document;
            Term qt = new Term("ddbdp_all",pubRE);
            Query q = new WildcardQuery(qt);

            //LuceneIndex.SEARCH_COL.explain(q, arg1)
            Hits hits = LuceneIndex.SEARCH_COL.search(q);
            
            switch (hits.length()){
            case 0:
                System.out.println("INFO: no " + qt.field() + " hits for " + qt.text());
                break;
            case 1:
                this.controlName = hits.doc(0).get("controlName");
                break;
            default:
                //@TODO apply additional criteria to disambiguate references
                break;
            }
            
        }
        catch (UnsupportedEncodingException whatever){}
        catch (SQLException se){}
        catch (IOException ie){}
        
    }

    public void addText(Element textElement) {
        try {
            Charset utfCharset = Charset.forName("UTF-8");
            CharsetEncoder encoder = utfCharset.newEncoder();
            NodeList cNodes = textElement.getChildNodes();
            int numC = cNodes.getLength();
            for (int i = 0; i < numC; i++) {
                if (foundMatch)break;
                Node c = cNodes.item(i);
                switch (c.getNodeType()) {
                case Node.TEXT_NODE:
                    String text = new String(encoder.encode(parser.parse(((Text) c).getNodeValue().toUpperCase())).array());
                    if (text.indexOf(pattern)!= -1){
                        addText("<span class=\"searchResult\">"
                                .getBytes("UTF-8"));
                        addText(text.getBytes());
                        addText("</span>".getBytes("UTF-8"));
                        foundMatch = true;
                    }

                    break;
                case Node.ELEMENT_NODE:
                    addText((Element)c);
                }
            }
        } catch (CharacterCodingException ce) {
            // ??
        } catch (IOException ue) {

        }
    }
    
    private void addText(byte [] utf8bytes) throws IOException{
        this.text.append(new String(utf8bytes,"UTF-8"));
    }
    
    public String getText(){
        return text.toString();
    }
    
    public String getQuery(boolean encoded){
        if (encoded) return this.encQuery;
        else return this.decQuery;
    }
    
    public String getCollection(){
        return this.pubColl;
    }
    
    public String getControlName(){
        return this.controlName;
    }
    
}