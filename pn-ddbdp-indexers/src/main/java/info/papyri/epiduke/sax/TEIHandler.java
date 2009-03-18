package info.papyri.epiduke.sax;

import org.xml.sax.Attributes;
import java.util.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


public class TEIHandler implements ContentHandler {
    private Stack<String> path = new Stack<String>();
    private Stack<Boolean> collect = new Stack<Boolean>();

    private StringBuffer text = new StringBuffer();
    private StringBuffer line = new StringBuffer();
    private ArrayList<String> lines = null;
    private Set<String> lbTags = new HashSet<String>();
    private Set<String[]> textTags = new HashSet<String[]>();
    private final boolean cascade;
    public TEIHandler(){
        this(false);
    }
    
    public TEIHandler(boolean cascade){
        this.cascade = cascade;
    }
    
    public void addLineBreakTag(String tagName){
    	if (!lbTags.contains(tagName))lbTags.add(tagName);
    }
    
    public void addTextPattern(String pattern){
    	if (pattern.endsWith("/")) pattern = pattern.substring(0,pattern.length() -1);
        String [] pArray = pattern.split("\\/");
    	if (!textTags.contains(pArray)) textTags.add(pArray);
    }
    
    public String getText(){
    	return text.toString();
    }
    
    public Iterator<String> getLines(){
    	return lines.iterator();
    }

    public void characters(char[] cdata, int start, int length) throws SAXException {
		if (collect.peek().booleanValue()){
			text.append(cdata,start,length);
			line.append(cdata,start,length);
		}
	}

	public void endDocument() throws SAXException {
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if (collect.peek().booleanValue() && lbTags.contains(arg1)){
            if (text.charAt(text.length()-1) == '-') text.deleteCharAt(text.length()-1);
		}
        collect.pop();
		path.pop();
	}

	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	public void ignorableWhitespace(char[] cdata, int start, int length)
			throws SAXException {
	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub

	}

	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	public void startDocument() throws SAXException {
        lines = new ArrayList<String>();
        text = new StringBuffer();
	}

	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		if (lbTags.contains(arg1)){
			lines.add(line.toString());
			line = new StringBuffer();
		}

		path.add(arg1);
        String [] currentPath = path.toArray(info.papyri.util.ArrayTypes.STRING);
       Boolean collectText = Boolean.FALSE; 
        for (String [] pattern: textTags){
            if (currentPath.length < pattern.length) continue;
            String [] compare = null;
            if (cascade){
            	compare = new String [pattern.length];
            }
            else{
            	compare = new String [currentPath.length];
            }
            System.arraycopy(currentPath, 0, compare, 0, compare.length);
            if (java.util.Arrays.equals(pattern,compare)){
                collectText = Boolean.TRUE;
                break;
            }
        }
        collect.push(collectText);
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

}
