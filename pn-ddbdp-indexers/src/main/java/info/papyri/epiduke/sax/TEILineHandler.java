package info.papyri.epiduke.sax;

import org.xml.sax.Attributes;

import info.papyri.metadata.NamespacePrefixes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class TEILineHandler extends TEIHandler {

    private Stack<String> path = new Stack<String>();
    private Stack<Boolean> collect = new Stack<Boolean>();
    private StringBuffer text = new StringBuffer();
//    private StringBuffer line = new StringBuffer();
//    private ArrayList<String> lines = null;
    private Set<String> lbTags = new HashSet<String>();
    private Set<String[]> textTags = new HashSet<String[]>();
    private final boolean cascade;
    private String ddbdpId;
    private String collection;
    private String volume;
    private String document;
    private String lineNum = null;

    public TEILineHandler() {
        this(false);
    }

    public TEILineHandler(boolean cascade) {
        this.cascade = cascade;
    }

    public void addLineBreakTag(String tagName) {
        if (!lbTags.contains(tagName)) {
            lbTags.add(tagName);
        }
    }

    public void addTextPattern(String pattern) {
        if (pattern.endsWith("/")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        String[] pArray = pattern.split("\\/");
        if (!textTags.contains(pArray)) {
            textTags.add(pArray);
        }
    }

    public String getText() {
        String result = text.toString();
        result = result.replaceAll("\\-\\$", "");
        result = result.replaceAll("-\\s+\\$", "");
        result = result.replaceAll("\\$", "");
        result = result.replaceAll("[\\[\\]\\{\\}<>]\\d?", "");
        return result;
    }

    public Iterator<String> getLines() {
        String allText = text.toString();
        String[] lines = allText.split("\\$");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim().replaceAll("\\s+", " ");
            lines[i] = lines[i].replaceAll("[\\[\\]\\{\\}<>]\\d?", "");
        }
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].endsWith("-") && i < (lines.length - 1)) {
                int firstPartOffset = lines[i].lastIndexOf(" ") + 1;
                String firstPart = lines[i].substring(firstPartOffset);
                int secondPartOffset = lines[i + 1].indexOf(" ");
                if (secondPartOffset == -1) {
                    secondPartOffset = lines[i + 1].length();
                }
                String secondPart = lines[i + 1].substring(0, secondPartOffset);
                String replace = (firstPart + secondPart).replaceAll("\\-", "");
                lines[i] = lines[i].substring(0, firstPartOffset) + replace;
                lines[i + 1] = replace + lines[i + 1].substring(secondPartOffset);
            }
        }
        return java.util.Arrays.asList(lines).iterator();
    }

    public String getCollection() {
        return this.collection;
    }

    public String getDdbdpId() {
        return this.ddbdpId;
    }

    public String getVolume() {
        return this.volume;
    }

    public String getDocument() {
        return this.document;
    }

    public void characters(char[] cdata, int start, int length) throws SAXException {
        if (collect.peek().booleanValue()) {
            if (lineNum != null) {
                boolean found = false;
                for (int i = start; i < start + length; i++) {
                    whitespace:
                    if (Character.isWhitespace(cdata[i])) {
                        char[] first = new char[i - start];
                        char[] second = new char[length - first.length];
                        System.arraycopy(cdata, start, first, 0, first.length);
                        System.arraycopy(cdata, start + first.length, second, 0, second.length);
                        text.append(first);
                        text.append(" &LINE-");
                        text.append(lineNum);
                        text.append("; ");
                        text.append(second);
                        lineNum = null;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    text.append(cdata, start, length);
                }
            } else {
                text.append(cdata, start, length);
            }
//			line.append(cdata,start,length);
        }
    }

    public void endDocument() throws SAXException {
    }

    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        if (lbTags.contains(arg1)) {
            text.append("$");
        }
        path.pop();
        collect.pop();
    }

    public void endPrefixMapping(String arg0) throws SAXException {
        // TODO Auto-generated method stub
    }

    public void ignorableWhitespace(char[] cdata, int start, int length)
            throws SAXException {
    }

    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
    }

    public void setDocumentLocator(Locator arg0) {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    public void startDocument() throws SAXException {
        text = new StringBuffer();
        this.collection = null;
        this.volume = null;
        this.document = null;
    }

    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        path.add(arg1);
        String[] currentPath = path.toArray(info.papyri.util.ArrayTypes.STRING);
        Boolean collectText = Boolean.FALSE;
        for (String[] pattern : textTags) {
            if (currentPath.length < pattern.length) {
                continue;
            }
            String[] compare = null;
            if (cascade) {
                compare = new String[pattern.length];
            } else {
                compare = new String[currentPath.length];
            }
            System.arraycopy(currentPath, 0, compare, 0, compare.length);
            if (java.util.Arrays.equals(pattern, compare)) {
                collectText = Boolean.TRUE;
                break;
            }
        }
        collect.push(collectText);
        if ("rdg".equals(arg1) || "del".equals(arg1) || "sic".equals(arg1)) {
            text.append(" ~");
        }
        if ("TEI.2".equals(arg1)) {
            String idAtt = arg3.getValue("id");
            String nAtt = arg3.getValue("n");
            this.ddbdpId = NamespacePrefixes.DDBDP + nAtt.replace(';', ':');
            String[] nParts = TEILineHandler.getNameParts(idAtt, nAtt);
            this.collection = nParts[COLL_IX];
            this.volume = nParts[VOL_IX];
            this.document = nParts[DOC_IX];
        }
        if ("lb".equals(arg1)) {
            String n = arg3.getValue("n");
            lineNum = n;
            char last = ' ';

            for (int i = text.length() - 1; i > 0; i--) {
                if (Character.isWhitespace(text.charAt(i))) {
                    continue;
                }
                last = text.charAt(i);
                break;
            }
            if (lineNum != null && text.length() > 0 && last != '-') {
                //text.s
                text.append(" &LINE-");
                text.append(lineNum);
                text.append("; ");
                lineNum = null;
            }
            if (lineNum != null && text.length() == 0) {
                text.append(" &LINE-");
                text.append(lineNum);
                text.append("; ");
                lineNum = null;
            }
        }
    }
    private static final Pattern DIGIT_PREFIX = Pattern.compile("(^\\d+).*$");

    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        // TODO Auto-generated method stub
    }

    public static final int COLL_IX = 0;
    public static final int VOL_IX = 1;
    public static final int DOC_IX = 2;

    public static final String[] getNameParts(String id, String n) {
        String[] result = new String[3];
        String[] nFrags = n.split(";");
        result[COLL_IX] = nFrags[0];  //result[COLL_IX] == "0001"
        try {
            if (nFrags.length == 3) {
                result[DOC_IX] = nFrags[2].replace(',', '-').replace('/', '_'); //result[DOC_IX] == "1145"
                if (nFrags[1] != null && !nFrags[1].equals("")) {  //true
                    result[VOL_IX] = nFrags[1];  // result[VOL_IX] == "4"
                    result[COLL_IX] = id.substring(0, id.indexOf("." + result[VOL_IX]));  // result[COLL_IX] == "bgu4"
                } else {
                    result[COLL_IX] = id.substring(0, id.indexOf("." + result[DOC_IX]));
                }
            } else if (nFrags.length == 2) {
                result[DOC_IX] = nFrags[1].replace(',', '-').replace('/', '_');
                result[COLL_IX] = id.substring(0, id.indexOf("." + result[DOC_IX]));
            }
        } catch (Throwable t) {
            System.out.println("id=\"" + id + "\", n=\"" + n + "\"");
            throw new RuntimeException(t);
        }
        if (result[VOL_IX] == null || "".equals(result[VOL_IX])) {
            result[VOL_IX] = "0000";
        } else {
            Matcher m = DIGIT_PREFIX.matcher(result[VOL_IX]);
            if (m.matches()) {
                String orig = m.group(1);
                if (orig.length() <= 4) {
                    char[] c = orig.toCharArray();
                    char[] c2 = new char[]{'0', '0', '0', '0'};
                    System.arraycopy(c, 0, c2, 4 - c.length, c.length);
                    String mod = new String(c2);
                    result[VOL_IX] = mod + result[VOL_IX].substring(c.length);
                }
            }
        }
        return result;
    }
}
