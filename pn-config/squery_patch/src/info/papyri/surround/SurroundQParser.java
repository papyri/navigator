package info.papyri.surround;

// dependency jars
// apache-solr-core-3.5.0
// apache-solr-solrj-3.5.0
// lucene-core-3.5.0
// lucene-queryparser-3.4.0

import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.lucene.queryParser.surround.query.*;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SolrQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for lucene/contrib Surround query parser, bringing SpanQuery support
 * to Solr
 * 
 * <queryParser name="surround"
 * class="org.apache.solr.search.SurroundQParserPlugin" />
 * 
 * Examples of query syntax can be found in modules/queryparser/docs/surround
 * 
 * Note that the query string is not analyzed in any way
 * 
 */


public class SurroundQParser extends QParser {
  protected static final Logger LOG = LoggerFactory .getLogger(SurroundQParser.class);
  static final int DEFMAXBASICQUERIES = 1000;
  static final String MBQParam = "maxBasicQueries";
  
  String sortStr;
  SolrQueryParser lparser;
  int maxBasicQueries;

  public SurroundQParser(String qstr, SolrParams localParams,
      SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }

  @Override
  public Query parse()
      throws org.apache.lucene.queryParser.ParseException {
    SrndQuery sq;
    String qstr = getString();
    if (qstr == null)
      return null;
    String mbqparam = getParam(MBQParam);
    if (mbqparam == null) {
      this.maxBasicQueries = DEFMAXBASICQUERIES;
    } else {
      try {
        this.maxBasicQueries = Integer.parseInt(mbqparam);
      } catch (Exception e) {
        LOG.warn("Couldn't parse maxBasicQueries value " + mbqparam +", using default of 1000");
        this.maxBasicQueries = DEFMAXBASICQUERIES;
      }
    }
    // ugh .. colliding ParseExceptions
    try {
      sq = org.apache.lucene.queryParser.surround.parser.QueryParser
          .parse(qstr);
    } catch (org.apache.lucene.queryParser.surround.parser.ParseException pe) {
      throw new org.apache.lucene.queryParser.ParseException(
          pe.getMessage());
    }
    
    String defaultField = getParam(CommonParams.DF);
    if (defaultField == null) {
      defaultField = getReq().getSchema().getDefaultSearchFieldName();
    }

    BasicQueryFactory bqFactory = new BasicQueryFactory(this.maxBasicQueries);
    Query lquery = sq.makeLuceneQueryField(defaultField, bqFactory);
    return lquery;
  }

}