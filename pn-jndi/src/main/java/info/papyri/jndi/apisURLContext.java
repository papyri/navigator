package info.papyri.jndi;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.Resolver;

public class apisURLContext implements Context {
    public final static String DDBDP_DEFAULT = "http://www.perseus.tufts.edu/hopper/collection.jsp?collection=Perseus:collection:DDBDP";
    public final static String LDAB_DEFAULT = "http://ldab.arts.kuleuven.ac.be/ldab_text.php";
    public final static String APIS_DEFAULT = "http://www.columbia.edu/cgi-bin/cul/resolve?ATK2059";
    public final static String HGV_DEFAULT = "http://www.rzuser.uni-heidelberg.de/~gv0/gvz.html";
    private static Resolver resolver;
    static final boolean debug = "true".equals(System.getProperty("pn.jndi.debug"));
    public static void setResolver(Resolver resolver){
        apisURLContext.resolver = resolver;    
    }


    public Object addToEnvironment(String arg0, Object arg1) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public void bind(Name arg0, Object arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void bind(String arg0, Object arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void close() throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public Name composeName(Name arg0, Name arg1) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public String composeName(String arg0, String arg1) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public Context createSubcontext(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public Context createSubcontext(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public void destroySubcontext(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void destroySubcontext(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNameInNamespace() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NameParser getNameParser(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NameParser getNameParser(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NamingEnumeration<NameClassPair> list(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NamingEnumeration<NameClassPair> list(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NamingEnumeration<Binding> listBindings(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public NamingEnumeration<Binding> listBindings(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object lookup(Name arg0) throws NamingException {
        if (debug) System.out.println("apisURLContext looking up Name " + arg0);
        Object result = null;
        if (resolver != null){
            result = resolver.resolveToClass(arg0, apisURLContext.class).getResolvedObj();
            if (result != null) return result;
        }
        if (debug) System.out.println("apisURLContext returning " + result);
        return result;
    }

    public Object lookup(String arg0) throws NamingException {
        if (debug) System.out.println("apisURLContext looking up String " + arg0);
        Object result = null;
        if (resolver != null){
            result = resolver.resolveToClass(arg0, apisURLContext.class).getResolvedObj();
            if (result != null) return result;
        }
        if ("apis".equals(arg0)){
            result = APIS_DEFAULT;
        }
        else if ("hgv".equals(arg0)){
            result = HGV_DEFAULT;
        }
        else if ("ddbdp".equals(arg0)){
            result = DDBDP_DEFAULT;
        }
        else if ("ldab".equals(arg0)){
            result = LDAB_DEFAULT;
        }
        if (debug) System.out.println("apisURLContext returning " + result);
        return result;
    }

    public Object lookupLink(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object lookupLink(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public void rebind(Name arg0, Object arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void rebind(String arg0, Object arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public Object removeFromEnvironment(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    public void rename(Name arg0, Name arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void rename(String arg0, String arg1) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void unbind(Name arg0) throws NamingException {
        // TODO Auto-generated method stub
        
    }

    public void unbind(String arg0) throws NamingException {
        // TODO Auto-generated method stub
        
    }

}
