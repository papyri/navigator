package info.papyri.navigator.portlet;

import info.papyri.index.LuceneIndex;
import info.papyri.metadata.CoreMetadataFields;
import info.papyri.metadata.NamespacePrefixes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import util.NumberConverter;
import util.OutOfRangeException;
import util.lucene.ConstantBitsetFilter;
import util.lucene.SimpleCollector;


public class MetadataSearchPortlet extends GenericPortlet {
    public static final String UNDEFINED_DATE = "none";
    public static final String UNDEFINED_DATE_INDEX = "-Z";
    private static final Logger LOG = Logger.getLogger(MetadataSearchPortlet.class);
    private static final int XREF_DOC_TYPE = 0;
    private static final int APIS_DOC_TYPE = 1;
    private static final int HGV_DOC_TYPE = 2; 
    private static final String XREF_TAG = "XREF:";
    private static final String HGV_TAG = "HGV:";
    private static final String APIS_TAG = "APIS:";
    public static final String SORT_BY = "pn-sort";
    public static final String XREF_DOC = XREF_TAG + "doc";
    public static final String XREF_PAGE_DOC_NUMBER = XREF_TAG + "docNumber";
    public static final String PN_NUM_DOCS_PER_PAGE = "pn:numPerPage";
    public static final String PN_QUERY = "pn:query";
    public static final String PN_MSG = "pn:message";
    public static final String PN_PROVENANCE_REC = "pn:rec:provenance";
    public static final String PN_LANG_REC = "pn:rec:language";
    public static final String PN_SERIES_REC = "pn:rec:series";
    
    private static final Set<String> STOP_WORDS = StopFilter.makeStopSet(StopAnalyzer.ENGLISH_STOP_WORDS);
    private static final Term DATE1_TEMPLATE = new Term(CoreMetadataFields.DATE1_I,"");
    private static final Term DATE2_TEMPLATE = new Term(CoreMetadataFields.DATE2_I,"");
    private static final Term MINIMUM_BEGIN_DATE = DATE1_TEMPLATE.createTerm(getSafeDate(NumberConverter.MIN_INT,1,1));
    private static final Term MINIMUM_END_DATE = DATE2_TEMPLATE.createTerm(getSafeDate(NumberConverter.MIN_INT,1,1));
    private static final Term MAXIMUM_BEGIN_DATE = DATE1_TEMPLATE.createTerm(getSafeDate(NumberConverter.MAX_INT,12,31));
    private static final Term MAXIMUM_END_DATE = DATE2_TEMPLATE.createTerm(getSafeDate(NumberConverter.MAX_INT,12,31));
    private static final Term ID_TEMPLATE = new Term(CoreMetadataFields.DOC_ID,"");
    private static final Term ALL_TEMPLATE = new Term(CoreMetadataFields.ALL,"");
    private static final Term ALL_NO_TRANS_TEMPLATE = new Term(CoreMetadataFields.ALL_NO_TRANS,"");
    private static final Term INVENTORY_TEMPLATE = new Term(CoreMetadataFields.INV,"");
    private static final Term PROVENANCE_TEMPLATE = new Term(CoreMetadataFields.PROVENANCE,"");
    private static final Term PROVENANCE_NOTE_TEMPLATE = new Term(CoreMetadataFields.PROVENANCE_NOTE,"");
    private static final Term PUBLICATION_TEMPLATE = new Term(CoreMetadataFields.BIBL_PUB,"");
    private static final Term LANG_TEMPLATE = new Term(CoreMetadataFields.LANG,"");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final String getSafeDate(int y, int m, int d){
        try{
            return NumberConverter.encodeDate(y,m,d);
        }
        catch (OutOfRangeException e){
            LOG.error(e.toString(),e);
            return "";
        }
    }
    
     protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        if (LuceneIndex.SEARCH_XREF == null){
            PrintWriter writer = response.getWriter();
            writer.println("{\"error\":\"XREF index unavailable\"}");
            return;
        }
        
        boolean getResults = "results".equals(request.getParameter("pn-display-mode"));
        if (!getResults){
            PortletRequestDispatcher rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/searchPortlet.jsp");
            rd.include(request, response);
            response.flushBuffer();
            return;
        }
        StringBuffer queryBuffer = new StringBuffer();
        
        String pubSeries = request.getParameter("pubnum_series");
        if (pubSeries == null){
            pubSeries = "";
        }
        else{
            pubSeries = util.jsp.el.Functions.decode(pubSeries);
        }
        String pubVol = request.getParameter("pubnum_vol");
        if (pubVol == null) { 
            pubVol = "";
        }
        else{
            pubVol = pubVol.trim();
            if (DIGITS.matcher(pubVol.trim()).matches()){
                pubVol = NumberConverter.getRoman(pubVol);
            }
        }
        String pubDoc = request.getParameter("pubnum_doc");
        if (pubDoc == null) { 
            pubDoc = "";
        }
        else{
            pubDoc = pubDoc.trim();
        }
        String apisInst = request.getParameter("apisnum_inst");
        if (apisInst == null) apisInst = "";
        String apisNum = request.getParameter("apisnum_num");
        String invNum = request.getParameter("invnum_num");
        if (invNum != null) invNum = invNum.toLowerCase();
        String inst = request.getParameter("institution");
        if (inst == null) inst = "";
        if ((inst.length() != 0 && apisInst.length() != 0 && !apisInst.equals(inst))){
            request.setAttribute(PN_MSG, "Two conflicting APIS institution filters were requested: \"" + apisInst + "\" and \"" + inst + "\"");
            PortletRequestDispatcher rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/searchPortlet.jsp");
            rd.include(request, response);
            response.flushBuffer();
            return;
        }

        
        
        String provenance = request.getParameter("provenance");
        String provenanceNote = request.getParameter("provenanceNote");
        String [] lang = request.getParameterValues("lang");
        String chkIncludeTranslations = request.getParameter("chkIncludeTranslations");
        String onOrAfterDate = request.getParameter("beginDate");
        if (onOrAfterDate != null) onOrAfterDate = onOrAfterDate.trim();
        else onOrAfterDate = "";
        int onOrAfterDateEra = ("CE".equals(request.getParameter("beginDateEra"))?1:-1);
        String onOrBeforeDate = request.getParameter("endDate");
        if (onOrBeforeDate != null) onOrBeforeDate = onOrBeforeDate.trim();
        else onOrBeforeDate = "";
        int onOrBeforeDateEra = ("CE".equals(request.getParameter("endDateEra"))?1:-1);
        boolean dateRangeQuery = (!onOrAfterDate.equals(onOrBeforeDate) || !(onOrAfterDateEra == onOrBeforeDateEra));

      
        if (apisInst == null || apisInst.length() == 0){
            if (inst == null || inst.length() == 0){
                apisInst = "*";
            }
            else {
                apisInst = inst;
            }
        }
        if (apisNum == null || apisNum.length() == 0) apisNum = "*";

        String apisCNPattern = NamespacePrefixes.APIS + apisInst + ":" + apisNum.toLowerCase();
        

        boolean queryApisControlName = !(NamespacePrefixes.APIS + "*:*").equals(apisCNPattern);
        
        boolean queryPublication = false;
        queryPublication = (queryPublication || (!"".equals(pubSeries) && !"*".equals(pubSeries)));
        queryPublication = (queryPublication || (!"".equals(pubVol) && !"*".equals(pubVol)));
        queryPublication = (queryPublication || (!"".equals(pubDoc) && !"*".equals(pubDoc)));
        
        boolean imageFirst = "on".equals(request.getParameter("req_img"));
        boolean translationFirst = "on".equals(request.getParameter("req_trans"));
        boolean pubFirst = "on".equals(request.getParameter("req_pub"));
        BitSet resultBits = new BitSet(LuceneIndex.INDEX_XREF.maxDoc());
        boolean filtered = false;
        SimpleCollector temp = new SimpleCollector(LuceneIndex.SEARCH_XREF);
        
        if (queryPublication){
            StringBuffer pubPat = new StringBuffer();
            boolean series = !(pubSeries == null || pubSeries.length() == 0 && !"*".equals(pubSeries));
            boolean volume = !(pubVol == null || pubVol.length() == 0 && !"*".equals(pubVol));
            boolean document = !(pubDoc == null || pubDoc.length() == 0 && !"*".equals(pubDoc));
            if (!series) pubPat.append("*");
            else pubPat.append(pubSeries);
            pubPat.append(' ');
            if (!volume) pubPat.append("*");
            else pubPat.append(pubVol.toUpperCase());
            pubPat.append(' ');
            if (!document){
                if (volume && !"*".equals(pubVol)) pubPat.append("*");
            }
            else pubPat.append(pubDoc);
            
            String publicationPattern = pubPat.toString();
            publicationPattern = publicationPattern.replaceAll("\\s+", " ").trim();
            if (LOG.isDebugEnabled()) LOG.debug("searching " + publicationPattern);
            Query publicationQuery = getPublicationIDQuery(publicationPattern);
            
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(publicationQuery,temp);
            if(temp.get().cardinality()==0){
                LuceneIndex.SEARCH_XREF.search(getPublicationIDQuery(publicationPattern + " *"),temp);
            }
            if (temp.get().cardinality() == 0 && (!volume && document)){
                publicationQuery = getPublicationIDQuery((pubSeries + " " + pubDoc).replaceAll("\\s+", " ").trim());
                if (LOG.isDebugEnabled()) LOG.debug("searching " + (pubSeries + " " + pubDoc).replaceAll("\\s+", " ").trim());
                LuceneIndex.SEARCH_XREF.search(publicationQuery,temp);
            }
            if (filtered){
                resultBits.and(temp.get());
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(publicationQuery.toString());
        }
        int beforeDateInt = Integer.MIN_VALUE;
        if (dateRangeQuery && onOrBeforeDate != null && onOrBeforeDate.length() > 0){
            try{
                Query onOrBeforeQuery = null;
                beforeDateInt = Integer.parseInt(onOrBeforeDate) * onOrBeforeDateEra;
                String bDateQuery = NumberConverter.encodeDate(beforeDateInt,12,31);
                Term bTerm = new Term(CoreMetadataFields.DATE1_I,bDateQuery);
                onOrBeforeQuery = new ConstantScoreRangeQuery(MINIMUM_BEGIN_DATE.field(),MINIMUM_BEGIN_DATE.text(),bDateQuery,false,true);
                temp.reset();
                LuceneIndex.SEARCH_XREF.search(onOrBeforeQuery,temp);
                if (filtered){
                    resultBits.and(temp.get());
                }
                else {
                    resultBits.or(temp.get());
                    filtered = true;
                }
                if(queryBuffer.length()>0) queryBuffer.append(" ; ");
                queryBuffer.append(onOrBeforeQuery.toString());
            }
            catch (NumberFormatException nfe){
                LOG.error("NFE for Date parseInt(\"" + onOrBeforeDate + "\")");
            }
            catch (OutOfRangeException e){
                LOG.error("OutOfRange for date(\"" + onOrBeforeDate + "\")");
            }
        }
        int afterDateInt = Integer.MAX_VALUE;
        
        if (dateRangeQuery && onOrAfterDate != null && onOrAfterDate.length() > 0){
            try{
                afterDateInt = Integer.parseInt(onOrAfterDate) * onOrAfterDateEra;
                if (
                        (beforeDateInt !=   Integer.MIN_VALUE && beforeDateInt < 0 && afterDateInt > -1) ||
                        (afterDateInt > beforeDateInt && beforeDateInt > -1) ||
                        (beforeDateInt < afterDateInt && afterDateInt < 0 )){
                    request.setAttribute(PN_MSG, "The end date of date range cannot be after the beginning date.");
                    PortletRequestDispatcher rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/searchPortlet.jsp");
                    rd.include(request, response);
                    response.flushBuffer();
                    return;
                }

                String dateQuery = NumberConverter.encodeDate(afterDateInt,1,1);
                ConstantScoreRangeQuery bRQ = 
                    new ConstantScoreRangeQuery(MAXIMUM_BEGIN_DATE.field(),dateQuery,MAXIMUM_BEGIN_DATE.text(),true,false);
                ConstantScoreRangeQuery bRQ2 = 
                    new ConstantScoreRangeQuery(MAXIMUM_END_DATE.field(),dateQuery, MAXIMUM_END_DATE.text(),true,false);
                BooleanQuery bQ = new BooleanQuery();
                bQ.add(bRQ, BooleanClause.Occur.SHOULD);
                bQ.add(bRQ2, BooleanClause.Occur.SHOULD);
                bQ.setMinimumNumberShouldMatch(1);
                temp.reset();
                LuceneIndex.SEARCH_XREF.search(bQ,temp);
                if (filtered){
                    resultBits.and(temp.get());
                }
                else {
                    resultBits.or(temp.get());
                    filtered = true;
                }
                if(queryBuffer.length()>0) queryBuffer.append(" ; ");
                queryBuffer.append(bQ.toString());
        }
        catch (NumberFormatException nfe){
            LOG.error("NFE for parseInt(\"" + onOrAfterDate + "\")");
        }
        catch (OutOfRangeException e){
            LOG.error("OutOfRange for date(\"" + onOrBeforeDate + "\")");
        }
        }
        
        if (!dateRangeQuery && onOrBeforeDate.length() > 0){
            beforeDateInt = Integer.parseInt(onOrBeforeDate) * onOrBeforeDateEra;
            try{
            String startQuery = NumberConverter.encodeDate(beforeDateInt,0,0);
            String endQuery = NumberConverter.encodeDate(beforeDateInt,12,31);
            
            Query query1 = new RangeQuery(DATE1_TEMPLATE.createTerm(startQuery),DATE1_TEMPLATE.createTerm(endQuery),true);
            Term nullDate2 = new Term(CoreMetadataFields.DATE2_I, UNDEFINED_DATE_INDEX);
            Query query2 = new RangeQuery(DATE2_TEMPLATE.createTerm(startQuery),DATE2_TEMPLATE.createTerm(endQuery),true);
            Query undefQuery = new TermQuery(nullDate2);
            BooleanQuery query = new BooleanQuery();
            query.add(query1,BooleanClause.Occur.MUST);
            query.add(query2,BooleanClause.Occur.SHOULD);
            query.add(undefQuery,BooleanClause.Occur.SHOULD);
            query.setMinimumNumberShouldMatch(1);

            temp.reset();
            LuceneIndex.SEARCH_XREF.search(query,temp);

            if (filtered){
                resultBits.and(temp.get());
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(query.toString());
            }
            catch (OutOfRangeException e){
                LOG.error("OutOfRange for date(\"" + onOrBeforeDate + "\")");
            }

        }

        if (provenance != null && !"".equals(provenance.trim())){
            Query provenanceQuery = null;
            provenanceQuery = new TermQuery(PROVENANCE_TEMPLATE.createTerm(provenance));
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(provenanceQuery,temp);
            if (filtered){
                resultBits.and(temp.get());
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(provenanceQuery.toString());
        }

        if (provenanceNote != null && !"".equals(provenanceNote.trim())){
            Query provenanceQuery = null;
            String provenanceIndex = info.papyri.metadata.provenance.ProvenanceControl.match(provenanceNote);
            TermQuery pNoteQuery = new TermQuery(PROVENANCE_NOTE_TEMPLATE.createTerm(provenanceNote.toLowerCase()));
            if (provenanceIndex != null){
                BooleanQuery q = new BooleanQuery();
                TermQuery pIndexQuery = new TermQuery(PROVENANCE_TEMPLATE.createTerm(provenanceIndex.toLowerCase()));
                q.add(pIndexQuery,BooleanClause.Occur.SHOULD);
                q.add(pNoteQuery,BooleanClause.Occur.SHOULD);
                q.setMinimumNumberShouldMatch(1);
                provenanceQuery = q;
            }
            else {
                provenanceQuery = pNoteQuery;
            }
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(provenanceQuery,temp);
            if (filtered){
                resultBits.and(temp.get());
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(provenanceQuery.toString());
        }

        if (lang != null){
            BooleanQuery langQuery = new BooleanQuery();
            for (String langVal: lang){
                langVal = langVal.trim();
                if (langVal.length() > 0)
                langQuery.add(new TermQuery(LANG_TEMPLATE.createTerm(langVal)),BooleanClause.Occur.MUST);
            }
            if (langQuery.getClauses().length > 0){
                temp.reset();
                LuceneIndex.SEARCH_XREF.search(langQuery,temp);
                if (filtered){
                    resultBits.and(temp.get());
                }
                else {
                    resultBits.or(temp.get());
                    filtered = true;
                }
                if(queryBuffer.length()>0) queryBuffer.append(" ; ");
                queryBuffer.append(langQuery.toString());
            }
        }
        
        String all = request.getParameter("keyword");
        String [] keywords = new String[0];
        if(all != null && !"".equals(all)){
            int quotes = 0;
            int quotePos = -1;
            ArrayList<String> kws = new ArrayList<String>();
            while((quotePos = all.indexOf("\"",quotePos + 1))!= -1){
                quotes++;
            }

            if (quotes > 0 && quotes % 2 == 0){
                String[] parts = all.split("\"");
                for (int i =0;i<parts.length;i++){
                   if (i % 2 == 1){
                       kws.add(parts[i]);    
                   }
                   else {
                       kws.addAll(Arrays.asList(parts[i].split("\\s+")));
                   }
                }
                keywords = kws.toArray(keywords);
            }
            else {
                keywords = all.split("\\s+");
            }
        }
        if (keywords.length > 0){
            BooleanQuery keyword = new BooleanQuery();
            Term template = (chkIncludeTranslations != null)? ALL_TEMPLATE:ALL_NO_TRANS_TEMPLATE;
            for (String val: keywords){
                val = val.trim();
                if ("".equals(val)) continue;
                if (val.endsWith("*")){ 
                    keyword.add(new PrefixQuery(template.createTerm(val.toLowerCase().substring(0,val.length() - 1))), BooleanClause.Occur.MUST);
                    
                }
                else if (val.startsWith("-")){
                    keyword.add(new TermQuery(template.createTerm(val.substring(1).toLowerCase())), BooleanClause.Occur.MUST_NOT);
                }
                else if (val.indexOf(" ") > -1){
                    PhraseQuery query = new PhraseQuery();
                    StandardAnalyzer sa = new StandardAnalyzer();
                    TokenStream tknzr = sa.tokenStream(null,new StringReader(val));
                    org.apache.lucene.analysis.Token t = null;
                    while ((t = tknzr.next()) != null){
                        String text = t.termText();
                        if (STOP_WORDS.contains(text)) continue;
                        query.add(template.createTerm(text));    
                    }
                    if (query.getTerms().length > 0) keyword.add(query, BooleanClause.Occur.MUST);
                }
                else {
                    val = val.toLowerCase().trim();
                    if (!STOP_WORDS.contains(val)) keyword.add(new TermQuery(template.createTerm(val.toLowerCase().trim())), BooleanClause.Occur.MUST);
                }
            }
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(keyword,temp);
            if (filtered){
                resultBits.and(temp.get());
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(keyword.toString());
        }
        
        
        if (queryApisControlName){
            Query apisCNQuery = null;
            apisCNQuery = (apisCNPattern.indexOf('*') != -1)
            ?new WildcardQuery(ID_TEMPLATE.createTerm(apisCNPattern))
            :new TermQuery(ID_TEMPLATE.createTerm(apisCNPattern));
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(apisCNQuery,temp);

            if (filtered){
                resultBits.and(temp.get());    
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(apisCNQuery.toString());
        }
        if (invNum != null && !"".equals(invNum.trim())){
            String invPattern = invNum.trim().replaceAll("\\s+", "%20");
            Query iQuery = (new WildcardQuery(INVENTORY_TEMPLATE.createTerm(NamespacePrefixes.INV + "*" + invPattern + "*")));
            temp.reset();
            LuceneIndex.SEARCH_XREF.search(iQuery,temp);
            if (filtered){
                resultBits.and(temp.get());    
            }
            else {
                resultBits.or(temp.get());
                filtered = true;
            }
            if(queryBuffer.length()>0) queryBuffer.append(" ; ");
            queryBuffer.append(iQuery.toString());
        }
        
        if (resultBits.cardinality() > 0){
            String sortBy = request.getParameter(SORT_BY);
            Hits results = getResults(resultBits, sortBy,imageFirst,translationFirst,pubFirst);
            request.setAttribute(NavigatorPortlet.XREF_RESULTS, results);
        }
        request.setAttribute(NavigatorPortlet.XREF_NUM_RESULTS, Integer.valueOf(resultBits.cardinality()));
        request.setAttribute(NavigatorPortlet.XREF_REQ_URL, response.createRenderURL().toString());
        if (queryBuffer.length() > 0){
            request.setAttribute(PN_QUERY,queryBuffer.toString());
        }
        PortletRequestDispatcher rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/searchPortlet.jsp");
        rd.include(request, response);
        response.flushBuffer();
    }
     
     private static final byte SORT_HAS_IMG = 1;
     private static final byte SORT_HAS_TRANS = 2;
     private static final byte SORT_HAS_PUB = 4;
    
    private Hits getResults(BitSet xrefBits, String sortBy, boolean imgFirst, boolean pubFirst, boolean transFirst) throws IOException {
        Filter bitFilter = new ConstantBitsetFilter(LuceneIndex.INDEX_XREF,xrefBits);
        Query dummy = new ConstantScoreQuery(bitFilter);
        Sort sort = null;
        SortField qSort = null;
        if (sortBy != null && sortBy.length() > 0){
            switch (sortBy.charAt(0)){
            case 'D':
                qSort = new SortField(CoreMetadataFields.SORT_DATE,SortField.STRING);
                LOG.debug("sorting metadata search results by date");
                break;
            case 'P':
                qSort = new SortField(CoreMetadataFields.BIBL_PUB,SortField.STRING);
                LOG.debug("sorting metadata search results by publication");
                break;
            default:
                qSort = new SortField(CoreMetadataFields.DOC_ID,SortField.STRING);
                LOG.debug("sorting metadata search results by doc id (unkown sort switch " + sortBy + ")");
            }
        }
        else {
            qSort = new SortField(CoreMetadataFields.DOC_ID,SortField.STRING);
            LOG.debug("sorting  by doc id (no sort switch)");
        }
        byte sortCase = 0;
        SortField imgSort = null;
        SortField transSort = null;
        SortField pubSort = null;
        if (imgFirst){
            sortCase |= SORT_HAS_IMG;
            imgSort = new SortField(CoreMetadataFields.SORT_HAS_IMG,SortField.STRING);
        }
        if (transFirst){
            sortCase |= SORT_HAS_TRANS;
            transSort = new SortField(CoreMetadataFields.SORT_HAS_TRANS,SortField.STRING);
        }
        if (pubFirst){
            sortCase |= SORT_HAS_PUB;
            pubSort = new SortField(CoreMetadataFields.SORT_HAS_PUB,SortField.STRING);
        }
        switch (sortCase){
        case SORT_HAS_IMG:
            sort = new Sort(new SortField[]{imgSort,qSort});
            LOG.debug("sort hits with images to top");
            break;
        case SORT_HAS_TRANS:
            sort = new Sort(new SortField[]{transSort,qSort});
            LOG.debug("sort hits with translations to top");
            break;
        case (SORT_HAS_IMG | SORT_HAS_TRANS):
            sort = new Sort(new SortField[]{imgSort, transSort, qSort});
            LOG.debug("sort hits with images and translations to top");
            break;
        case SORT_HAS_PUB:
            sort = new Sort(new SortField[]{pubSort,qSort});
            LOG.debug("sort hits with publications to top");
            break;
        case (SORT_HAS_IMG | SORT_HAS_PUB):
            sort = new Sort(new SortField[]{imgSort,pubSort,qSort});
            LOG.debug("sort hits with images and publications to top");
            break;
        case (SORT_HAS_TRANS | SORT_HAS_PUB):
            sort = new Sort(new SortField[]{transSort,pubSort,qSort});
            LOG.debug("sort hits with translations and publications to top");
        break;
        case (SORT_HAS_IMG | SORT_HAS_TRANS | SORT_HAS_PUB):
            sort = new Sort(new SortField[]{imgSort, transSort,pubSort, qSort});
            LOG.debug("sort hits with images, translations and publications to top");
        break;
        default:
            sort = new Sort(new SortField[]{qSort});
            LOG.debug("no sort to top; query sort only");
            break;
    }
        
        Hits results = LuceneIndex.SEARCH_XREF.search(dummy,sort);
        return results;
    }
    
    private static Query getPublicationIDQuery(String pattern) throws IOException {
            return new WildcardQuery(new Term("bib:publication",pattern));
    }
    
    private static String scrubHTML(String val){
        val = val.replaceAll("\"","&quot;").replaceAll("'","&apos;").replaceAll("\\(", "[").replaceAll("\\)", "]");
        return val;
    }
    
    private static void getSetValues(BitSet docs, IndexSearcher index, String [] fields,TreeSet<String>[] values){
        try{
            int pos = -1;
            while ((pos = docs.nextSetBit(pos + 1)) != -1){
                Document doc = index.doc(pos);
                for (int i =0;i<fields.length;i++){
                    String [] vals = doc.getValues(fields[i]);
                    if (vals != null){
                        for (String val:vals){
                            if (!values[i].contains(val)) values[i].add(val);
                        }
                    }
                }
            }
        }
        catch (IOException ioe){
            
        }
        
        
    }
 


}
