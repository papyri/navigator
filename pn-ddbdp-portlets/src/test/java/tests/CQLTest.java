package tests;

import junit.framework.TestCase;
import java.util.Vector;
import org.z3950.zing.cql.*;

import java.io.IOException;
import info.papyri.ddbdp.servlet.Search;
public class CQLTest extends TestCase {
    private final static String ISO = "ISO-8859-1";
    public void testParse() throws IOException, CQLParseException {
        String test = "%ce%ba%ce%b1%cf%84%ce%b1%ce%bb%ce%b5";
        test = java.net.URLDecoder.decode(test,ISO);
        String u = "καταλε";
        byte [] bytes = test.getBytes(ISO);
        
        String i = new String(bytes,"UTF-8");
        assertEquals(u, i);
        
        String input = "%28%28cql.keywords%3D%3D%2FignoreCapitals%2FignoreAccents+%22%CE%B4%CF%81%CE%B1%CF%87%22+prox%2Funit%3Dword%2Fdistance%3C%3D1+%22%CF%80%CE%BF%CF%81%CE%B5%CF%85%22%29+AND+%28cql.keywords%3D%3D%2FignoreCapitals%2FignoreAccents%22%CE%B5%CE%BB%CE%B1%CE%B2%CE%BF%CF%83%CE%B1%CE%BD%22%29%29+sortBy+dc.identifier/sort.ascending";
        CQLParser parser = new CQLParser();
        input = java.net.URLDecoder.decode(input,ISO);
        input = new String(input.getBytes(ISO),"UTF-8");
        System.out.println(input);
        //input = Search.getSafeUTF8(input);
        CQLSortNode sort = (CQLSortNode)parser.parse(input);
        
        try{ java.lang.reflect.Field keysField = sort.getClass().getDeclaredField("keys");
        boolean access = keysField.isAccessible();
        keysField.setAccessible(true);
        Object modVect = keysField.get(sort);
        Vector<ModifierSet> keys = (Vector<ModifierSet>)modVect;
        for (ModifierSet key:keys){
            Vector<Modifier> mods = key.getModifiers();
            for(Modifier mod:mods){
                System.out.println(mod.getType() + " " + key.getBase());
            }
        }
        keysField.setAccessible(access);
        }
        catch(Throwable t){
            t.printStackTrace();
        }
        CQLBooleanNode root = (CQLBooleanNode)(sort.subtree);
        System.out.println(root.getClass().getName());
        if (root.left instanceof CQLProxNode){
            CQLProxNode pNode = (CQLProxNode)root.left;
            CQLTermNode left = (CQLTermNode)pNode.left;
            CQLTermNode right = (CQLTermNode)pNode.right;
            System.out.println(left.getIndex() + " " + left.getRelation().getBase() + " " + left.getTerm());
            System.out.println(right.getIndex() + right.getRelation().toString() + right.getTerm());
            Vector<Modifier> mods = pNode.ms.getModifiers();
            for(Modifier mod:mods){
                System.out.println("pNode.ms " + mod.getType() + " " + mod.getValue());
            }
            mods = left.getRelation().getModifiers();
            for(Modifier mod:mods){
                System.out.println("left.getRelation()" + mod.getType() + " " + mod.getValue());
            }
            
        }
    }
}
