package info.papyri.tests;

import java.io.IOException;
import java.io.StringReader;
import org.xml.sax.SAXException;

import junit.framework.TestCase;
import org.apache.lucene.analysis.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import info.papyri.epiduke.lucene.*;
import info.papyri.epiduke.lucene.analysis.*;
import info.papyri.epiduke.lucene.spans.SubstringSpanTermQuery;
import info.papyri.epiduke.sax.TEIHandler;
import info.papyri.epiduke.sax.TEILineHandler;

import edu.unc.epidoc.transcoder.UnicodeParser;
import edu.unc.epidoc.transcoder.UnicodeCConverter;

public class AnalyzerTest extends GreekTestsBase {
    
    private static final String srcC = "\u03A3\u03B9\u03B4\u1FF6\u03BD\u03BF\u03C2\u1F8C";
    private static final String srcD = "\u03A3\u03B9\u03B4\u03C9\u1FC0\u03BD\u03BF\u03C3\u0391\u1FCF\u037A";
    private static final String lowerCaseC = "\u03C3\u03B9\u03B4\u1FF6\u03BD\u03BF\u03C3\u1F84";
    private static final String lowerCaseD = "\u03C3\u03B9\u03B4\u03C9\u1FC0\u03BD\u03BF\u03C3\u03B1\u1FCF\u037A";
    private static final String unaccented = "\u03A3\u03B9\u03B4\u03C9\u03BD\u03BF\u03C3\u0391";
    private static final String analyzed = "\u03C3\u03B9\u03B4\u03C9\u03BD\u03BF\u03C3\u03B1";
    public void setUp(){
        
    }
    public void tearDown(){
        
    }
    public void testDiacriticFilterFormC(){
        StringReader src = new StringReader(AnalyzerTest.srcC);
        TokenFilter filter = new AncientGreekAccentFilter(new AncientGreekTokenizer(src));
        try{
            String actual = filter.next().termText();
            String expected = unaccented;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
    }
    
    public void testDiacriticFilterFormD(){
        StringReader src = new StringReader(AnalyzerTest.srcD);
        TokenFilter filter = new AncientGreekAccentFilter(new AncientGreekTokenizer(src));
        try{
            String actual = filter.next().termText();
            String expected = unaccented;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
    }
    
    public void testLowerCaseFilterFormC(){
        StringReader src = new StringReader(AnalyzerTest.srcC);
        TokenFilter filter = new AncientGreekLowerCaseFilter(new AncientGreekTokenizer(src));
        try{
            String actual = filter.next().termText();
            String expected = lowerCaseC;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
    }
    
    public void testLowerCaseFilterFormD(){
        StringReader src = new StringReader(AnalyzerTest.srcD);
        TokenFilter filter = new AncientGreekLowerCaseFilter(new AncientGreekTokenizer(src));
        try{
            String actual = filter.next().termText();
            String expected = lowerCaseD;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
    }
    public void testAllAnalyzerFormD() {
        StringReader src = new StringReader(AnalyzerTest.srcD);
        TokenFilter filter = new AncientGreekAccentFilter(new AncientGreekLowerCaseFilter(new AncientGreekTokenizer(src)));
        try{
            String actual = filter.next().termText();
            String expected = analyzed;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
        
    }
    public void testAllAnalyzerFormC() {
        StringReader src = new StringReader(AnalyzerTest.srcC);
        TokenFilter filter = new AncientGreekAccentFilter(new AncientGreekLowerCaseFilter(new AncientGreekTokenizer(src)));
        try{
            String actual = filter.next().termText();
            String expected = analyzed;
            assertEquals(expected,actual);
        }
        catch (IOException ioe){
            fail(ioe.toString());
        }
        
    }
    
    public void testRotationTokenizer() throws IOException{
    	AncientGreekTokenizer t = new AncientGreekTokenizer(new StringReader("scat cat"));
        SubstringRotationTokenStream s = new SubstringRotationTokenStream(t);
        Token next = null;
        int i = 0;
        String [] expected = new String[]{"$scat$","$$scat","t$$sca","at$$sc","cat$$s","scat$$","$cat$","$$cat","t$$ca","at$$c","cat$$"};
        while ((next = s.next()) != null){
            assertEquals(expected[i++],next.termText());
        }
    }
    
    public void testTranscoderDeltaLenisToFormC() throws Exception {
        String srcD = "\u03B4\u0313";
        UnicodeParser parser = new UnicodeParser();
        UnicodeCConverter coder = new UnicodeCConverter();
        parser.setString(srcD);
        try{
            coder.convertToString(parser);
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        
    }
    public void testTranscoderOmegaISubToFormC() throws Exception {
        String srcD = "\u03C9\u0345";
        UnicodeParser parser = new UnicodeParser();
        UnicodeCConverter coder = new UnicodeCConverter();
        parser.setString(srcD);
        try{
            coder.convertToString(parser);
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        
    }
    


}
