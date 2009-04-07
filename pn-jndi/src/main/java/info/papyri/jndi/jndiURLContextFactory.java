package info.papyri.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

/**
 *
 * @author hcayless
 */
public class jndiURLContextFactory implements InitialContextFactory, ObjectFactory{


    public Context getInitialContext(Hashtable<?, ?> arg0)
            throws NamingException {
        // TODO Auto-generated method stub
        if (jndiURLContext.debug) System.out.println("getInitialContext");
        return new InitialContext(arg0);
    }

    public Object getObjectInstance(Object arg0, Name arg1, Context arg2,
            Hashtable<?, ?> arg3) throws Exception {
        String a0m = (arg0 == null)?"arg0 = null":arg0.getClass().getName() + " : " + arg0.toString();
        String a1m = (arg1 == null)?"arg1 = null":arg1.getClass().getName() + " : " + arg1.toString();
        String a2m = (arg2 == null)?"arg2 = null":arg2.getClass().getName() + " : " + arg2.toString();
        String a3m = (arg3 == null)?"arg3 = null":arg3.getClass().getName() + " : " + arg3.toString();
        return new jndiURLContext();
    }
}
