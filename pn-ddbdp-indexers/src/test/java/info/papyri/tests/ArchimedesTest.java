package info.papyri.tests;

import junit.framework.TestCase;
import org.apache.xmlrpc.client.*;
import java.net.URL;
import java.util.HashMap;
public class ArchimedesTest extends TestCase {
    public void testRPC() throws Exception {
        String greek1 = "meri/dos";
        String unic1 = "μερ\u1F77δος";
        String greek2 = "strathgw=|";
        String headword = "μερίς";
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://archimedes.mpiwg-berlin.mpg.de:8098/RPC2"));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        Object [] params = new Object[]{"-GRC",new String[] {unic1,greek1, greek2}};
        Object result =client.execute("lemma", params);
        HashMap resultMap = (HashMap)result;
        Object [] result1 = (Object[])resultMap.get(greek1);
        Object [] result2 = (Object[])resultMap.get(greek2);
        Object [] unicObj1 = (Object[])resultMap.get(unic1);
        if(result1 != null){
            printObjArray(result1);
        }
        if(result2 != null){
            printObjArray(result2);
        }
        if(unicObj1 != null){
            printObjArray(unicObj1);
        }
    }
    static void printObjArray(Object[] objs){
        for(Object obj:objs){
            System.out.println(obj.getClass().getName() + " : " + obj);
        }
    }
}
