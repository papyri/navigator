package info.papyri.resolver;

import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;
import info.papyri.navigator.portlet.*;

import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import info.papyri.jndi.apisURLContext;

import org.apache.log4j.Logger;
import org.apache.lucene.document.*;

public class XRefURLContext extends apisURLContext {
    final static Logger LOG = Logger.getLogger(XRefURLContext.class);
    final static Properties SYNTAX = getSyntax();
    final static Name OAI_ID = getName(NamespacePrefixes.ID_NS,SYNTAX);
    final static Name HGV_NAME = getName(NamespacePrefixes.HGV,SYNTAX);
    final Document doc;
    final String [] xrefs;
    final String name;
    final String pQuery;
    final String sQuery;
    final String pColl;
    final String sColl;
    XRefURLContext(Name name, Document doc){
        this(name.toString(),doc);
    }
    XRefURLContext(String name, Document doc){
        this.doc = (doc != null)?doc:new Document();
        String [] xrefs = doc.getValues(CoreMetadataFields.XREFS);
        if(xrefs == null){
            LOG.warn("Document had no xrefs: " + name);
            this.xrefs = new String[0];
        }
        else this.xrefs = xrefs;

        this.name = name;
        if (name.startsWith(NamespacePrefixes.APIS)){
           pQuery = "apis/query";
           pColl = "apis";
           sColl = "hgv";
           sQuery = "hgv/query";
        }
        else{
           pQuery = "hgv/query";
           pColl = "hgv";
           sColl = "apis";
           sQuery = "apis/query";
        }
    }
    
    Object lookupApis(String arg0){
        for(String xref:xrefs){
            if(xref.startsWith(NamespacePrefixes.APIS)){
                return xref;
            }
        }
        return null;
    }
    Object lookupHgv(String arg0){
        for(String xref:xrefs){
            if(xref.startsWith(NamespacePrefixes.HGV)){
                return xref;
            }
        }
        return null;
    }
    Object lookupTm(String arg0){
        for(String xref:xrefs){
            if(xref.startsWith(NamespacePrefixes.TM)){
                return xref;
            }
        }
        return null;
    }
    Object lookupDdb(String arg0){
        for(String xref:xrefs){
            if(xref.startsWith(NamespacePrefixes.DDBDP)){
                return xref;
            }
        }
        return null;
    }

    @Override
    public Object lookup(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        if (doc == null) return null;
        String id = doc.get(CoreMetadataFields.DOC_ID);
        if(id == null){
            LOG.error("Document had no id; name was " + this.name);
            return null;
        }
        Object result = null;

        if ("apis".equals(arg0)){
            result = lookupApis(arg0);
        }
        else if ("hgv".equals(arg0)){
            result = lookupHgv(arg0);
        }
        else if ("ddbdp".equals(arg0)){
            result = lookupDdb(arg0);
        }
        else if ("ldab".equals(arg0)){
            lookupTm(arg0);
        }
        else if ("apis/query".equals(arg0)){
            String name = (String)lookupApis(arg0);
            result = XREFPortlet.getAPISlink(name);
            if(result == null) result = XREFPortlet.APIS_DEFAULT;
        }
        else if ("hgv/query".equals(arg0)){
            String name = (String)lookupTm(arg0);
            result = XREFPortlet.getHGVlink(name);
            if(result == null) result = XREFPortlet.HGV_DEFAULT;
        }
        else if ("ddbdp/query".equals(arg0)){
            String name = (String)lookupDdb(arg0);
            if(name != null){
                result = XREFPortlet.getDDBDPlink(name);
            }
            else result = XREFPortlet.DDBDP_DEFAULT;
        }
        else if ("ldab/query".equals(arg0)){
            String name = (String)lookupTm(arg0);
            if(name != null){
                result = XREFPortlet.getLDABlink(name);
            }
            else result = XREFPortlet.LDAB_DEFAULT;
        }
        else if ("primary".equals(arg0)){
            result = lookup(pColl);
        }
        else if ("supplemental".equals(arg0)){
            result = lookup(sColl);
        }
        else if ("primary/query".equals(arg0)){
            result = lookup(pQuery);
        }
        else if ("supplemental/query".equals(arg0)){
            result = lookup(sQuery);
        }
        else if("display".equals(arg0)){
            String hgv = null;
            String inv = null;
            String apis = null;
            for(String xref:xrefs){
                if(xref.startsWith(NamespacePrefixes.HGV) && hgv==null){
                    String display = xref.substring(NamespacePrefixes.HGV.length());
                    display = display.replaceAll("%20"," ").replace(':', ' ');
                    return display;
                }
                if(xref.startsWith(NamespacePrefixes.APIS) && apis==null){
                    apis = xref;
                }
                if(xref.startsWith(NamespacePrefixes.INV) && inv==null){
                    inv = xref;
                }
            }
            String display = doc.get(CoreMetadataFields.BIBL_PUB);
            if(display != null) return display;
            if(inv != null){
                display = inv.substring(NamespacePrefixes.INV.length());
                display = display.replaceAll("%20"," ").replace(':',' ');
                return display;
             }
            if(apis != null){
                display = apis.substring(NamespacePrefixes.APIS.length());
                display = display.replace(":", ".apis");
                return display;
            }
        }
        if (LOG.isDebugEnabled()){
            LOG.debug(getClass().getName() + " key=" + arg0 + " result=" + result);
        }

        if (result != null && result.toString().indexOf("none.apis.") != -1) return "";
        
        return result;
    }
    
    private static Properties getSyntax(){
        Properties syntax = new Properties();
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ":");
        return syntax;
    }
    static Name getName(String collection,Properties syntax){
        try{
            return new CompoundName(collection,syntax);    
        }
        catch (InvalidNameException e){
            return null;
        }
    }
}
