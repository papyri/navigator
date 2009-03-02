package info.papyri.antlr;

import java.util.*;
public class DDBDPResultSet {
	public final Map<String,DDBDPResult> results;
    public DDBDPResultSet() {
	  this.results = new HashMap<String,DDBDPResult>();
  }
  
  public void addResult(DDBDPResult result) {
	results.put(result.encQuery, result);
  }
}
