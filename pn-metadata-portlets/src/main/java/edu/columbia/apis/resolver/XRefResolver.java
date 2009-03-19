package edu.columbia.apis.resolver;

import info.papyri.metadata.NamespacePrefixes;
import info.papyri.navigator.portlet.*;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ResolveResult;
import javax.naming.spi.Resolver;

import org.apache.log4j.Logger;
import org.apache.lucene.document.*;

import java.util.WeakHashMap;

public class XRefResolver implements Resolver {
    final static Logger LOG = Logger.getLogger(XRefResolver.class);
    final WeakHashMap<String, ResolveResult> cache = new WeakHashMap<String, ResolveResult>();
    public XRefResolver(){

    }

    public ResolveResult resolveToClass(Name arg0, Class<? extends Context> arg1)
    throws NamingException {
        int last = arg0.size() - 1;
        String ln = arg0.get(last);
        ln = ln.trim().replaceAll("\\s+","%20");
        arg0.remove(last);
        arg0.add(ln);
        String name = arg0.toString();
        return resolveToClass(name, arg1);        
    }

    public ResolveResult resolveToClass(String name,
            Class<? extends Context> arg1) throws NamingException {

        if (!arg1.getName().equals("edu.columbia.apis.apisURLContext")){
            throw new NamingException("Unexpected resolution class: " + arg1.getName());
        }
        name = name.trim().replaceAll("\\s+","%20");
        ResolveResult result = cache.get(name);
        if(result != null) return result;
        if (LOG.isDebugEnabled()){
            LOG.debug("cn = \"" + name + "\" collection = default(apis)");
            LOG.debug("resolver(String): cn = ");
            byte [] bytes = name.getBytes();
            StringBuffer buf = new StringBuffer();
            for (byte b:bytes){
                buf.append("0x" + Integer.toHexString(b) + " ");
            }
            LOG.debug(buf.toString());
        }
        Document doc = XREFPortlet.getDocumentByControlName(name);
        if(name.startsWith(NamespacePrefixes.APIS)){
            result = new ResolveResult(new ApisXRefURLContext(name,doc),"");
        }
        else if(name.startsWith(NamespacePrefixes.HGV)){
            result = new ResolveResult(new HgvXRefURLContext(name,doc),"");
        }
        else if(name.startsWith(NamespacePrefixes.DDBDP)){
            result = new ResolveResult(new DdbXRefURLContext(name,doc),"");
        }
        else if(name.startsWith(NamespacePrefixes.TM)){
            result = new ResolveResult(new LdabXRefURLContext(name,doc),"");
        }
        else result = new ResolveResult(new XRefURLContext(name,doc),"");
        cache.put(name, result);
        return result;
    }

}
