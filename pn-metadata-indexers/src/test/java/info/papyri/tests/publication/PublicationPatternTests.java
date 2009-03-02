package info.papyri.tests.publication;

import junit.framework.*;
import java.util.regex.*;

import info.papyri.data.publication.StandardPublication;

public class PublicationPatternTests extends TestCase {
    Pattern plainPub;
    Pattern embeddedPub;
    
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        //plainPub = Pattern.compile("^(PO)\\.[A-Z][a-zA-Z]*\\.\\s[MDCLXVI]+\\s\\d+");
        plainPub = Pattern.compile("\\b([PO]\\.[A-Z][a-zA-Z]*\\.\\s[MDCLXVI]+\\s\\d+(\\s[a-z])?).*");
    }
    
    public void testPlain(){
        Matcher m = plainPub.matcher("P.Col. IV 60");
        if (!m.matches()) fail();
    }
    
    public void testNormalNoVolume(){
        String actual = StandardPublication.normalizePub("p.oxy.", null, "744");
        String expected = "P.Oxy. * 744";
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
    }
    
    public void testNormalNoDocument(){
        String actual = StandardPublication.normalizePub("p.col.", "III", null);
        String expected = "P.Col. III *";
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
    }
    public void testNormalNoVolumeDocument(){
        String actual = StandardPublication.normalizePub("p.cair.zen.", null, null);
        String expected = "P.Cair. Zen. *";
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
    }
    public void testNormalArabicVolume(){
        String actual = StandardPublication.normalizePub("p.col.", "3", "2");
        String expected = "P.Col. III 2";
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
    }
    
    public void testSBExtraction(){
        String raw = "Lewis N, BASP 9, 33-36, 1972 -- SB XII 10797";
        String expected = "SB XII 10797";
//        String pattern = "(SB\\s[XIV]+\\s\\d+)";
        String pattern = "(^.*)(SB\\s[XIV]{1,4}\\s\\d{1,5})(.*$)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(raw);
        String actual = null;
        if (matcher.matches()){
            actual = matcher.group(2);
        }
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
        // (^.*)(SB\\s[XIV]{1,4}\\s\\d{1,5}(\\.\\d)?)(.*$)
    }
    public void testSBExtractionSubDoc(){
        String raw = "Husselman EM, TAPA 88, 142-5, 1957 -- SB VIII 9642.4 -- BL VII, 213";
        String expected = "SB VIII 9642 4";
//        String pattern = "(SB\\s[XIV]+\\s\\d+)";
        String pattern = "(^.*)(SB\\s[XIV]{1,4}\\s\\d{1,5}(\\.\\d)?)(.*$)";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(raw);
        String actual = null;
        if (matcher.matches()){
            actual = matcher.group(2);
            actual = actual.replace('.', ' ');
        }
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
        // (^.*)(SB\\s[XIV]{1,4}\\s\\d{1,5}(\\.\\d)?)(.*$)
    }
    
    public void testPlainPub(){
        String startP = "^(.*?)";
        String endP = "(.*?$)";
        String seriesP = "(([PO]\\.([A-Z][a-zA-Z]*\\.?))(\\s[A-Z][a-zA-Z]*\\.?)?|(SB))";
        String volP = "(\\s[MDCLXVI]+)";
        String docP = "(\\s\\d+((\\.\\d)?|(\\s?[a-z]))?)";
        String sideP = "(\\s((V(erso)?)|(R(ecto)?))(\\s[a-z0-9])?)?";
        Pattern plainPub = Pattern.compile(startP +"(" + seriesP + volP + docP + sideP + ")" +  endP);
            //Pattern.compile("(([([PO]\\.([A-Z][a-zA-Z]*\\.))(SB)]+\\s[MDCLXVI]+\\s\\d+(\\s[a-z])?(\\s[RV])?(Recto\\s)?(Verso\\s)?\\b)?(\\sdescr\\.)?)(.*)");
        String expected = "P.Col. V 1 Verso 2";
        Matcher m = plainPub.matcher("foo " + expected + " -- perflickity");
        String actual = "";
        if (m.matches()){
            int groups = m.groupCount();
            for (int i=0;i<groups;i++){
                //System.out.println(m.group(i));
            }
            actual = m.group(2);
        }
        else {
            fail("Pattern did not match.");
        }
        assertEquals("E: " + expected + " A: " + actual,expected,actual);

    }
    
    public void testCUPub(){
        String docPatt = "(^.*?)(1)([ab])(\\s?[+-]\\s?)(1)([ab])(.*?$)";
        String original = "P.Col. II 1 Recto 1a-1b";
        Matcher m = Pattern.compile(docPatt).matcher(original);
        String expected = "P.Col. II 1 Recto 1 a";
        String actual = "";
        if (m.matches()){
            String base = m.group(1);
            String doc = m.group(2);
            actual = base + m.group(2) + " " + m.group(3);
        }
        assertEquals(expected,actual);
    }
    
    public void testMichKol(){
        // (\\s\\()([a-z])(\\))
        Pattern pmichKol = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)(,\\scol\\.\\s)([XVI]+)((\\s\\()([a-z])(\\)))?(.*?$)");
        String data = "Boak AER, PMich II, 128, col. I, 1933 --";
        String expected = "P.Mich. II 128 Kol. I";
        Matcher m = pmichKol.matcher(data);
        String actual = "";
        if (m.matches()){
            actual = "P.Mich. " + m.group(3) + " " + m.group(5) + " Kol. " + m.group(7);
            if (m.groupCount() > 9 && m.group(10) != null) {
                actual += " " + m.group(10);
            }
        }
        assertEquals(expected,actual);
    }
    
    public void testMichKolTile(){
        Pattern pmichKol = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)(,\\scol\\.\\s)([XVI]+)((\\s\\()([a-z])(\\)))?(.*?$)");
        String data = "Boak AER, PMich II, 128, col. I (b), 1933 --";
        String expected = "P.Mich. II 128 Kol. I b";
        Matcher m = pmichKol.matcher(data);
        String actual = "";
        if (m.matches()){
            actual = "P.Mich. " + m.group(3) + " " + m.group(5) + " Kol. " + m.group(7);
            if (m.groupCount() > 9 && m.group(10) != null) {
                actual += " " + m.group(10);
            }
        }
        assertEquals(expected,actual);
    }
    
    public void testMichTile(){
        Pattern pmichTile = Pattern.compile("(^.*?)(PMich\\s)([XVI]+)(,\\s)(\\d+)(,\\stext\\s)([a-z0-9+])(,.*?$)");
        String data = "Husselman EM, PMich V, 322, text a, 1944 -- BL IX, 160 (l. 37).";
        String expected = "P.Mich. V 322 a";
        Matcher m = pmichTile.matcher(data);
        String actual = "";
        if (m.matches()){
            actual = "P.Mich. " + m.group(3) + " " + m.group(5) + " " + m.group(7);
        }
        assertEquals(expected,actual);
    }
    
    public void testNYU(){
        Pattern nyuRange = Pattern.compile("(^.*?)(P\\.\\sNYU\\s)([XIV]+)(\\s?,\\s)(\\d+)(-)(\\d+)(,.*?$)");
        String data = "Naphtali Lewis, P. NYU I , 38-9, 1967";
        String [] expected = new String[] { "P.NYU I 38","P.NYU I 39"};
        Matcher m = nyuRange.matcher(data);
        String [] actual = new String[0];
        if (m.matches()){
            int start = Integer.parseInt(m.group(5));
            int end = Integer.parseInt(m.group(7));
            if (end < start) end += (start/10) * 10;
                 actual = new String[]{"P.NYU " + m.group(3) + " " + start,"P.NYU " + m.group(3) + " " + end}; 
        }
        for (int i=0;i<expected.length;i++){
            assertEquals(expected[i],actual[i]);
        }
    }
    
    public void testControlName(){
        Pattern APIS = Pattern.compile("^\\w+\\.apis\\.\\w+$");
        String data = "columbia.apis.p3";
        assertTrue(APIS.matcher(data).matches());
    }
    
    public void testSeriesParse(){
        Pattern indexable = Pattern.compile("^[\\w\\s\\.]+$");
        Pattern vol = Pattern.compile("(^.*?)(\\s[XIV\\d]+)(.*$)");
        String data = "P.Col. X 261";
        String actual = info.papyri.data.publication.PublicationMatcher.indexableSeries(data).get(0);
        String expected = "P.Col.";
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
        data = "BGU I 4";
        expected = "BGU";
        actual = info.papyri.data.publication.PublicationMatcher.indexableSeries(data).get(0);
        assertEquals("E: " + expected + " A: " + actual,expected,actual);
              }
    public void testPMich(){
        String raw = "SB VI 9242b";
        Matcher m = info.papyri.data.publication.MichiganScrubber.pmichSB.matcher(raw);
        assertTrue(m.matches());
        assertEquals("b",m.group(4));
    }
    
}