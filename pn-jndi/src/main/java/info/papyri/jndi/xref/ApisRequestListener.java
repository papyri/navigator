package info.papyri.jndi.xref;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.spi.NamingManager;
import javax.naming.Name;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class ApisRequestListener implements ServletRequestListener {
    private Context apisResolver;
    private static final boolean DEBUG = "true".equals(System.getProperty("pn.jndi.debug"));
    public ApisRequestListener(){
        
    }

    public void requestDestroyed(ServletRequestEvent arg0) {
        // TODO Auto-generated method stub

    }

    public void requestInitialized(ServletRequestEvent arg0) {
        ServletRequest req = arg0.getServletRequest();
        String cn = req.getParameter("controlName");

        String collection = req.getParameter("collection");
        if (collection == null || "".equals(collection.trim())) collection = "APIS:metadata:apis:controlname";
        if (cn != null){
            cn = cn.trim();
            try{
                cn = new String(cn.getBytes("ISO-8859-1"),"UTF-8");
                Hashtable env = new Hashtable();
                    env.put(Context.URL_PKG_PREFIXES, "info.papyri");
                Context c = NamingManager.getURLContext("jndi", env);
                Name xName = getName(cn);
                c = (Context)c.lookup(xName);
                if (c == null) return;
                String apis = (String)c.lookup("apis");
                String hgv = (String)c.lookup("hgv");
                String ddbdp = (String)c.lookup("ddbdp");
                String ldab = (String)c.lookup("ldab");
                String display = (String)c.lookup("display");
                req.setAttribute("apis", apis);
                req.setAttribute("hgv", hgv);
                req.setAttribute("ddbdp", ddbdp);
                req.setAttribute("ldab", ldab);
                req.setAttribute("display", display);
                String primary = (String)c.lookup("primary");
                String supplement = (String)c.lookup("supplemental");
                String primaryq = (String)c.lookup("primary/query");
                String supplementq = (String)c.lookup("supplemental/query");
                String apisq = (String)c.lookup("apis/query");
                String hgvq = (String)c.lookup("hgv/query");
                String ddbdpq = (String)c.lookup("ddbdp/query");
                String ldabq = (String)c.lookup("ldab/query");
                req.setAttribute("apis/query", apisq);
                req.setAttribute("hgv/query", hgvq);
                req.setAttribute("ddbdp/query", ddbdpq);
                req.setAttribute("ldab/query", ldabq);

                req.setAttribute("primary/query", primaryq);
                req.setAttribute("supplemental/query", supplementq);
                req.setAttribute("primary", primary);
                req.setAttribute("supplemental", supplement);
                if (DEBUG){
                    System.out.print("cn = " + cn);
                    System.out.print(";apis = " + apis);
                    System.out.print(";hgv = " + hgv);
                    System.out.print(";ddbdp = " + ddbdp);
                    System.out.print(";ldab = " + ldab);
                    System.out.print(";primary = " + primary);
                    System.out.println(";supplemental = " + supplement);
                }
                
                                
            }
            catch (Exception e){e.printStackTrace();}
            
        }
    }
    
    private static Name getName(String cn) throws InvalidNameException{
        Properties syntax = new Properties();
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ":");
        return new CompoundName(cn,syntax);
    }

}
