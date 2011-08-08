/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author hcayless
 */
public class SolrUtils {
  
  private String solrUrl;
  private static String morphSearch = "morph-search/";
  
  public SolrUtils(ServletConfig config) {
    solrUrl = config.getInitParameter("solrUrl");
  }
  
  public String expandLemmas(String query) throws MalformedURLException, SolrServerException {
    SolrServer solr = new CommonsHttpSolrServer(solrUrl + morphSearch);
    StringBuilder exp = new StringBuilder();
    SolrQuery sq = new SolrQuery();
    String[] lemmas = query.split("\\s+");
    for (String lemma : lemmas) {
      exp.append(" lemma:");
      exp.append(lemma);
    }
    sq.setQuery(exp.toString());
    sq.setRows(1000);
    QueryResponse rs = solr.query(sq);
    SolrDocumentList forms = rs.getResults();
    Set<String> formSet = new HashSet<String>();
    if (forms.size() > 0) {
      for (int i = 0; i < forms.size(); i++) {
        formSet.add(FileUtils.stripDiacriticals((String)forms.get(i).getFieldValue("form")).replaceAll("[_^]", "").toLowerCase());
      }
    }
    return FileUtils.interpose(formSet, " OR ");
  }
  
}
