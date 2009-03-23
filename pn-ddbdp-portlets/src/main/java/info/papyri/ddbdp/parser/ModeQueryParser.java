package info.papyri.ddbdp.parser;
import org.mozilla.javascript.*;

import java.lang.reflect.Method;
import info.papyri.ddbdp.servlet.Sru;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spans.*;

public class ModeQueryParser extends NodeTransformer implements QueryFunctions {
    private static final Logger LOG = Logger.getLogger(ModeQueryParser.class);
    private static final String src2 = "term(\"foo bar\")";
    private static final String src3 = "Integer.parseInt(\"9\")";
    public static void main(String [] args) throws Exception {
        ErrorReporter rep = new DefaultReporter();
        //CompilerEnvirons env = new CompilerEnvirons();
        //env.
        Parser p = new Parser(new CompilerEnvirons(),rep);
        //p.
        ScriptOrFnNode root = p.parse(src2, "http://somesuch.info", 1);
        Method m = ScriptOrFnNode.class.getDeclaredMethod("flattenSymbolTable", new Class[]{boolean.class});
        m.setAccessible(true);
        m.invoke(root, new Object[]{Boolean.FALSE});
        LOG.debug("root.getFunctionCount(): " + root.getFunctionCount());
        NodeTransformer trans = new ModeQueryParser();
        trans.transform(root);
        Node child = root.getFirstChild();
        LOG.debug("root: " + root);
        LOG.debug("root.firstChild: " + child);
        LOG.debug(root.getNextTempName());
        //org.mozilla.javascript.Function pos = new Function();
        Context cx = Context.enter();

        ScriptableObject pcx = new QueryExecContext();
        //cx.initStandardObjects(pcx, true); // seal to prevent manipulation of context properties
        // security question: Can we eliminate all calls/creations except:
        // Object, Number, and String?
        Object result = cx.evaluateString(pcx, src2, "http://somesuch.info", 1, null);
        SpanNearQuery rQuery = (SpanNearQuery)result;
        LOG.debug("slop: " + rQuery.getSlop());
    }
    
    private int mode = 0;
    public int getMode(){
        return mode;
    }

    protected void visitCall(Node node, ScriptOrFnNode tree) {
        LOG.debug("visitCall: " + node.getClass().getName());
        if(LOG.isDebugEnabled())debug(node, tree, false);
        Node child = node.getFirstChild();
        while(child != null){
            if(LOG.isDebugEnabled())debug(child,tree, true);
            if(child.getType() == Token.NAME){
                String n = child.getString();
                if(FUNC_BETA.equals(n)){
                    mode |= MODE_BETA;
                }
                else if(CONST_IGNORE_ALL.equals(n)){
                    mode |= MODE_FILTER_CAPITALS_AND_DIACRITICS;
                }
                else if(CONST_IGNORE_CAPS.equals(n)){
                    mode |= MODE_FILTER_CAPITALS;
                }
                else if(CONST_IGNORE_MARKS.equals(n)){
                    mode |= MODE_FILTER_DIACRITIC;
                }
                else if(CONST_LEMMAS.equals(n)){
                    mode = MODE_LEMMAS;
                }
            }
            child=child.getNext();
        }
    }

    private static void debug(Node node, ScriptOrFnNode tree, boolean indent){
        String offset = indent?"\t":"";
        if(node == null){
            LOG.debug(offset + "NULL NODE");
            return;
        }
        int type = node.getType();
        LOG.debug(offset + "node.type: " + type);
        if(type == Token.NAME){
            LOG.debug(offset + "\tname: " + node.getString());
        }
    }
}
