package info.papyri.tests;

import java.io.*;

public class UnicodeDebugger {
	public static void debug(InputStream xml, OutputStream out,String charset) throws IOException {
	    int len = -1;
	    char [] chars = new char[32];
	    InputStreamReader reader = new InputStreamReader(xml,charset);
	    PrintStream ps = new PrintStream(out); 
	    while ((len = reader.read(chars)) != -1){
	    	char [] tempChars = new char[32];
	    	java.util.Arrays.fill(tempChars,' ');
	    	System.arraycopy(chars,0,tempChars,0,len);
	    	String temp = new String(tempChars);
	    	ps.print(temp.replaceAll("\\s", " "));
	    	ps.print("      ");
	    	char [] hexChars = new char[5];
	    	for (char c:chars){
	    		java.util.Arrays.fill(hexChars,' ');
	    		String hex = Integer.toHexString(c);
	    		System.arraycopy(hex.toCharArray(),0,hexChars,0,hex.length());
	    		ps.print( hexChars);
	    	}
	    	ps.println();
	    	
	    }
		
	}
	
	public static void main(String[] args) throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.load(GreekFullTermTest.class.getResourceAsStream("/index.properties"));
		String test = props.getProperty("doc.root") + "/p.col/p.col.18/p.col.18.788.xml";
		debug(new FileInputStream(test),System.out,"UTF-8");
	}

}
