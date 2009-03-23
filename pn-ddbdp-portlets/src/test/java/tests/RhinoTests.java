package tests;

import info.papyri.ddbdp.parser.DefaultReporter;
import info.papyri.ddbdp.parser.ModeQueryParser;
import info.papyri.ddbdp.parser.QueryExecContext;
import info.papyri.ddbdp.parser.QueryFunctions;

import org.apache.lucene.search.spans.SpanNearQuery;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.NodeTransformer;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.ScriptableObject;

import junit.framework.TestCase;

public class RhinoTests extends TestCase {
    private static final String src = "then(term(\"foo\",IA),term(\"bar\",IA),3)";
    private static final String src2 = "term(\"foo bar\")";
    private static final String parseInt = "Integer.parseInt(\"9\")";
    private static final String function = "function f(){return true;};f()";
    private static final String java = "java";
    private static final String imprt = "importPackage(java.io)";
    private static final String file = "java.io.File";
    private static final String date = "new java.util.Date()";
    private static final String number = "42";
    private static final String print = "print(\"print\")";
    private static final String string = "\"string\"";
    private static final String object = "obj = { run: function () { return ('running'); } }";
    private  Parser p = new Parser(new CompilerEnvirons(),new DefaultReporter());
    private Context cx = Context.enter();
    private QueryExecContext pcx = new QueryExecContext();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        //not init'ing the standard objects appears to lock out Java calls, which would be nice
        //cx.initStandardObjects(pcx, true); // seal to prevent manipulation of context properties
        // security question: Can we eliminate all calls/creations except:
        // Object, Number, and String?
    }
    
    public void testThings(){
        ScriptOrFnNode root = p.parse(src, "http://somesuch.info", 1);
        System.out.println("root.getFunctionCount(): " + root.getFunctionCount());
        NodeTransformer trans = new ModeQueryParser();
        trans.transform(root);
        Node child = root.getFirstChild();
        System.out.println(root);
        System.out.println(child);
        System.out.println(root.getNextTempName());
        //org.mozilla.javascript.Function pos = new Function();

        Object result = cx.evaluateString(pcx, src, "http://somesuch.info", 1, null);
        SpanNearQuery rQuery = (SpanNearQuery)result;
        System.out.println(rQuery.getSlop());
    }
    
    public void testPosPhrase(){
        Object result = cx.evaluateString(pcx, src2, "http://somesuch.info", 1, null);
        SpanNearQuery rQuery = (SpanNearQuery)result;
        System.out.println(rQuery.getSlop());
    }
    
    public void testJavaParseIntFails(){
        try{
            Object result = cx.evaluateString(pcx, parseInt, "http://somesuch.info", 1, null);
            fail("Java calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EcmaError e){}
    }
    
    public void testJavaFails(){
        try{
            Object result = cx.evaluateString(pcx, java, "http://somesuch.info", 1, null);
            fail("Java calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EcmaError e){}
    }
    
    public void testJavaFileFails(){
        try{
            Object result = cx.evaluateString(pcx, file, "http://somesuch.info", 1, null);
            fail("Java calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EcmaError e){}
    }
    
    public void testJavaImportFails(){
        try{
            Object result = cx.evaluateString(pcx, imprt, "http://somesuch.info", 1, null);
            fail("Java calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EcmaError e){}
    }
    
    public void testFunctionDecl(){
            Object result = cx.evaluateString(pcx, function, "http://somesuch.info", 1, null);
    }
    
    public void testJSString(){
        Object result = cx.evaluateString(pcx, string, "http://somesuch.info", 1, null);
        assertTrue("Unexpected result: \"" + result + "\"","string".equals(result.toString()));
    }

    public void testJSNumber(){
        Object result = cx.evaluateString(pcx, number, "http://somesuch.info", 1, null);
        assertTrue("Unexpected result: \"" + result + "\"","42".equals(result.toString()));
    }
    public void testJSObjectFails(){
        try{
            Object result = cx.evaluateString(pcx, object, "http://somesuch.info", 1, null);
            fail("JSObject constructor calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EvaluatorException e){}
    }
    public void testJSPrintFails(){
        try{
            Object result = cx.evaluateString(pcx, print, "http://somesuch.info", 1, null);
            fail("Java calls cannot be allowed to succeed; returned \"" + result + "\"");
        }
        catch(org.mozilla.javascript.EcmaError e){}
    }
    
    public void testBeta(){
        String  src = "beta(\"^kai\")";
        String expected =  "^και";
        ScriptOrFnNode root = p.parse(src, "http://somesuch.info", 1);
        String actual = cx.evaluateString(pcx, src, "http://somesuch.info", 1, null).toString();
        assertEquals(expected, actual);
    }
    
    public void testModeParser(){
        ModeQueryParser mqp = new ModeQueryParser();
        String  src = "then(beta(\"foo\",IM),beta(\"bar\",IM),3)";
        ScriptOrFnNode root = p.parse(src, "http://somesuch.info", 1);
        mqp.transform(root);
        int mode = mqp.getMode();
        assertEquals(QueryFunctions.MODE_BETA_FILTER_DIACRITICS,mode);
    }
}
