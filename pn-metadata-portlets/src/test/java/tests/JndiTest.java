package tests;

import java.util.Properties;
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.directory.*;
import junit.framework.TestCase;
public class JndiTest extends TestCase {
    final static Properties SYNTAX = getSyntax();
    private static Properties getSyntax(){
        Properties syntax = new Properties();
        syntax.put("jndi.syntax.direction", "left_to_right");
        syntax.put("jndi.syntax.separator", ";");
        syntax.put("jndi.syntax.trimblanks", "true");
        syntax.put("jndi.syntax.separator.ava", ",");
        syntax.put("jndi.syntax.separator.typeval", "=");
        return syntax;
    }
    private CompoundName getName(String publication) throws NamingException {
        return new CompoundName(publication,SYNTAX);
    }
    
    public void testJNDIPublicationBinding() throws NamingException {
        String omich1_1_1z12_src = "0033;1;1;line=1-12";
        String omich1_1_13z15_src = "0033;1;1;line=13-15";
        String omich1_1_113_src = "0033;1;113";
        String omich1_1_113_a_src = "0033;1;113;subdocument=a";
        CompoundName omich = getName("0033");
        CompoundName vol1 = getName("0033;1");
        CompoundName omich1_1_1z12 = getName(omich1_1_1z12_src);
        CompoundName omich1_1_13z15 = getName(omich1_1_13z15_src);
        CompoundName omich1_1_113 = getName(omich1_1_113_src);
        CompoundName omich1_1_113_a = getName(omich1_1_113_a_src);
        Hashtable env = new Hashtable();
        //env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.fscontext.RefFSContextFactory");
        InitialDirContext root = new InitialDirContext();
        //Context root = new InitialContext(env);
        root.bind(omich,new InitialDirContext());
    }
}
