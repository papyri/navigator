package info.papyri.tests.perseus;

import info.papyri.antlr.DDBDPResult;
import info.papyri.antlr.DDBDPResultSet;

import java.net.URL;
import org.w3c.dom.*;

import java.util.*;

import org.apache.commons.digester.*;


import info.papyri.*;


public class DigesterTest {

	/**
	 * @param args
	 */
	private static final String TEST_QUERY =
		"http://www.perseus.tufts.edu/hopper/xmlsearch.jsp?target=greek&q=*%29abouqi%2Fw%7C&doc=Perseus%3Acollection%3ADDBDP&expand=yes";

	public static void main(String[] args) {
		try {
			URL queryURL = new URL(TEST_QUERY);
			Digester digest = new Digester();
			
			NodeCreateRule ncd = new NodeCreateRule(Node.ELEMENT_NODE);
			digest.addRule("results/result/text", ncd);
			
			digest.addObjectCreate("results", DDBDPResultSet.class);
			digest.addObjectCreate("results/result", DDBDPResult.class);
			digest.addSetNext("results/result", "addResult",DDBDPResult.class.getName());
			digest.addCallMethod("results/result/query", "setQuery",1);
			digest.addCallParam("results/result/query",0);
			//digest.addCallMethod("results/result/text", "addText",1);
			//digest.addCallParam("results/result/text",0);
			//digest.addCallMethod("results/result/text/span","addText",1);
			digest.addSetNext("results/result/text","addText",Element.class.getName());
			DDBDPResultSet resultSet = (DDBDPResultSet) digest.parse(queryURL.openStream());
			Map<String, DDBDPResult> results = resultSet.results;
			Iterator<String> keys = results.keySet().iterator();
			while (keys.hasNext()){
				String key = keys.next();
				System.out.println("key: " + key);
				System.out.println("\tvalue: " + results.get(key));
			}
		}
		catch (Exception s){
			
		}
		}

}
