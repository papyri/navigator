package info.papyri.dispatch.browse;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.LanguageCode;
import info.papyri.dispatch.browse.facet.StringSearchFacet.ClauseRole;
import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchTerm;
import info.papyri.dispatch.browse.facet.StringSearchFacet.SearchType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * The <code>DocumentBrowseRecord</code> class stores summary information regarding
 * documents retrieved during browsing or search.
 * 
 * 
 * @author thill
 */
public class DocumentBrowseRecord extends BrowseRecord implements Comparable {

  private ArrayList<String> itemIds = new ArrayList<String>();
  private String preferredId;
  private URL url;
  private String documentTitle;
  private String place;
  private String date;
  private String language;
  private String translationLanguages;
  private ArrayList<String> imagePaths;
  private Boolean hasIllustration;
  private String highlightString;
  private String solrQueryString;
  private Long position;
  private Long total;
  //private ArrayList<SearchTerm> highlightTerms;
  
  private static IdComparator documentComparator = new IdComparator();
  
  public DocumentBrowseRecord(String prefId, ArrayList<String> ids, URL url, ArrayList<String> titles, String place, String date, String lang, ArrayList<String> imgPaths, String trans, Boolean illus, ArrayList<SearchTerm> sts) {

    this.preferredId = tidyPreferredId(prefId);
    this.itemIds = ids;
    this.url = url;
    this.documentTitle = this.tidyTitles(titles);
    this.place = place;
    this.date = date;
    this.language = tidyAncientLanguageCodes(lang);
    this.translationLanguages = tidyModernLanguageCodes(trans);
    this.imagePaths = imgPaths;
    this.hasIllustration = illus;
    this.highlightString = buildHighlightString(sts);
    
  }
  
  String buildHighlightString(ArrayList<SearchTerm> searchTerms){
      
      StringBuilder hilites = new StringBuilder();
      ArrayList<String> regexes = new ArrayList<String>();
      
      Iterator<SearchTerm> stit = searchTerms.iterator();
      while(stit.hasNext()){
      
          SearchTerm term = stit.next();
                
          try{
          
              String searchString = term.buildTransformedString(); 
              hilites.append(searchString);
              hilites.append(" ");
                  
              }
          
           catch (Exception e){} 
          
      }
           
      return hilites.toString().trim();
      
  }

  String transformSubstringSearchToHighlightPattern(String origSearch){
      
      origSearch = transformWildcardToRegex(origSearch);
      if(!Character.toString(origSearch.charAt(0)).equals("^")) origSearch = "[\\s]([\\S]*?)" + origSearch;
      if(!Character.toString(origSearch.charAt(origSearch.length() - 1)).equals("^")) origSearch += "([\\S]*?)[\\s]";
      origSearch = origSearch.replaceAll("\\^", "[\\\\s]");
      
      return origSearch;
      
  }
  
  ArrayList<String> getExpandedRegexes(ArrayList<String> regexes){
  
      ArrayList<String> expandedStrings = new ArrayList<String>();
      FileUtils util = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
      String slurpString = util.loadTextFromId(url.toExternalForm());
      Iterator<String> rit = regexes.iterator();
      while(rit.hasNext()){
          
          String regex = rit.next();
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(slurpString);
          while(matcher.find()){
              
              String found = matcher.group();
              expandedStrings.add(found);
              
              
          }
          
      }
     
      return expandedStrings;
      
  }
  
  String transformProximitySearchToHighlightPattern(String origSearch){
      
      origSearch = transformWildcardToRegex(origSearch);
      String[] searchBits = origSearch.split("\\s+");
      Pattern opPattern = Pattern.compile("(\\d+)(w|n)");
      String regexReplace = "";
      String operation = "w";
      StringBuilder regex = new StringBuilder();
      
      for(int i = 0; i < searchBits.length; i++){
          
          String bit = searchBits[i];
          Matcher opMatcher = opPattern.matcher(bit);
          if(opMatcher.matches()){
          
              String length = opMatcher.group(1);
              regexReplace = "\\s(\\S+\\s){1," + length + "}\\s";
              operation = opMatcher.group(2);
              regex.append(regexReplace);
              
          }
          else{
              
              regex.append(bit);
              
          }
          
      }
      
      if(!operation.equals("w")){
          
          regex.append("|");
          for(int i = searchBits.length - 1; i >= 0; i--){
              
              String bit = searchBits[i];
              Matcher opMatcher = opPattern.matcher(bit);
              if(opMatcher.matches()){
                  
                  regex.append(regexReplace);
                  
              }
              else{
                  
                  regex.append(bit);
                  
              }
              
              
          }
          
          
      }
      
      return regex.toString();
  }
  
  String transformUserDefinedSearchToHighlightPattern(String origSearch){
      
      origSearch = origSearch.substring(origSearch.indexOf(":") + 1);
      origSearch = transformWildcardToRegex(origSearch);
      
      return origSearch;
      
  }
  
  String transformWildcardToRegex(String origSearch){
      
      origSearch = origSearch.replaceAll("\\?", ".");
      origSearch = origSearch.replaceAll("\\*", ".*");
      return origSearch;
      
  }
  
  Boolean containsWildcard(String wild){
      
      return (!wild.contains("?") && !wild.contains("*"));  
      
  }
    
  String regexify(SearchType searchType, String searchString){
      
      if(searchType == SearchType.USER_DEFINED) return transformUserDefinedSearchToHighlightPattern(searchString);
      if(searchType == SearchType.SUBSTRING) return transformSubstringSearchToHighlightPattern(searchString);
      if(searchType == SearchType.PROXIMITY) return transformProximitySearchToHighlightPattern(searchString);
      return transformWildcardToRegex(searchString);
      
  }
  
  Boolean needsRegexification(SearchType searchType, String searchString){
      
      if(containsWildcard(searchString)) return true;
      if(searchType == SearchType.REGEX) return true;
      if(searchType == SearchType.PROXIMITY) return true;
      if(searchType == SearchType.SUBSTRING) return true;
      return false;
      
  }
  
  @Override
  public String getHTML() {

    StringBuilder anchor = new StringBuilder();
    anchor.append("<a href='");
    anchor.append(generateLink());    
    anchor.append("'>");
    anchor.append(getDisplayId());
    anchor.append("</a>");
    StringBuilder html = new StringBuilder("<tr class=\"result-record\"><td class=\"identifier\" title=\"");
    html.append(getAlternativeIds());
    html.append("\">");
    html.append(anchor.toString());
    html.append("</td>");
    html.append("<td class=\"doc-title\">");
    html.append(this.getTitleHTML());
    html.append("</td>");
    html.append("<td class=\"display-place\">");
    html.append(place);
    html.append("</td>");
    html.append("<td class=\"display-date\">");
    html.append(date);
    html.append("</td>");
    html.append("<td class=\"language\">");
    html.append(language);
    html.append("</td>");
    html.append("<td class=\"has-translation\">");
    html.append(translationLanguages);
    html.append("</td>");
    html.append("<td class=\"has-images\">");
    html.append(getImageHTML());
    html.append("</td>");
    html.append("</tr>");
    html.append(this.getKWIC());
    return html.toString();

  }
  
  public String getDisplayId() {
     
    return preferredId;
    
  }
  
  private String tidyPreferredId(String prefId){
      
      String newId = prefId.replace(" 0 ", " ");
      newId = newId.replace("hgv ", "");
      newId = newId.replace("ddbdp ", "");
      newId = newId.replace("apis ", "");
      
      return newId;
      
  }
 
  
  private String tidyModernLanguageCodes(String languageCodes){
              
        String[] codes = languageCodes.split(",");
        
        // eliminate duplicates
        
        ArrayList<String> previousCodes = new ArrayList<String>();
        
        for(int i = 0; i < codes.length; i++){
            
            String code = codes[i].trim();
            
            if(!previousCodes.contains(code))  previousCodes.add(code);
            
        }

        String tidiedCodes = "";

        Iterator<String> pcit = previousCodes.iterator();
        while(pcit.hasNext()){
            
            tidiedCodes += pcit.next();
            if(pcit.hasNext()) tidiedCodes += ", ";
            
        }
        
        return tidiedCodes;
           
  }
  
  private String tidyAncientLanguageCodes(String rawLanguageCodes){
      
      String filteredLanguages = LanguageCode.filterModernLanguageCodes(rawLanguageCodes);
      
      String[] splitLanguages = filteredLanguages.split(",");
      
      Collections.sort(Arrays.asList(splitLanguages));
      
      String alphabetized = "";
      
      for(int i = 0; i < splitLanguages.length; i++){
          
          alphabetized += splitLanguages[i];
          
          if(i < splitLanguages.length - 1) alphabetized += ", ";
          
      }
      
      return alphabetized;
      
  }
  
  private String getAlternativeIds(){
      
      String allIds = "";
      
      Iterator<String> ait = itemIds.iterator();
      
      while(ait.hasNext()){
      
          String id = ait.next();
          id = id.replaceAll(" 0 ", " ");
          id = id.replaceAll("ddbdp", "DDbDp:");
          id = id.replaceAll("hgv", "HGV:");
          id = id.replaceAll("apis", "APIS:");
          allIds += " = ";
          allIds += id;
          
      }
      
      if("".equals(allIds)) return "No other identifiers";
      
      return allIds;
  }
  
  private String tidyTitles(ArrayList<String> rawTitles){
      
      ArrayList<String> cleanTitles = new ArrayList<String>();
      for(String rawTitle : rawTitles){
          
          String trimTitle = rawTitle.trim();
          if(!cleanTitles.contains(trimTitle)) cleanTitles.add(trimTitle);
          
      }
      
      String trueTitle = "";
      
      for(String title : cleanTitles){
          
          String compTitle = title.toLowerCase();
          compTitle = compTitle.replaceAll("[\\. ]", "");
      
          Boolean titleNotRedundant = true;
          
          if(preferredId != null){
              
              String compPrefId = preferredId.toLowerCase();
              compPrefId = compPrefId.replaceAll("[\\. ]", "");
              if(compPrefId.equals(compTitle)) titleNotRedundant = false;
              
          }
          for(String id : itemIds){
              
              String compId = id.toLowerCase();
              compId = compId.replaceAll("[\\. ]", "");
              if(compId.equals(compTitle)) titleNotRedundant = false;
          
          }
          
          if(titleNotRedundant) trueTitle += title + ", ";
          
      }
     
     trueTitle = trueTitle.trim();
     if(trueTitle.length() > 0) trueTitle = trueTitle.substring(0, trueTitle.length() - 1);
     return trueTitle;
      
  }
  
  private String getTitleHTML(){
      
      if("".equals(documentTitle)){
          
          return "<div class=\"title-none\">---</div>";
          
      }
      
      StringBuilder html = new StringBuilder();
      
      html.append("<div class=\"title-long\">");
      html.append(documentTitle);
      html.append("</div><!-- closing .title-long -->");
      
      html.append("<div class=\"title-short\" title =\"");
      html.append(documentTitle);
      html.append("\">");
      
      String[] titleBits = documentTitle.split(" ");
      String shortTitle = titleBits[0];
      if(shortTitle.length() < documentTitle.length()) shortTitle += " &hellip;";
      html.append(shortTitle);
      html.append("</div><!-- closing .title-short -->");
      
      return html.toString();      
      
      
  }

  /**
   * Allows sorting so that it occurs (in order of priority)
   * 
   * (1) DDbDP before HGV, HGV before APIS
   * (2) Numeric by id
   * (3) Alphabetic by letters occurring after the numeric portion
   * 
   * @param o
   * @return 
   */
  
  @Override
  public int compareTo(Object o) {

    DocumentBrowseRecord comparandum = (DocumentBrowseRecord) o;
    String thisId = this.getDisplayId() != null ? this.getDisplayId() : "";
    String thatId = comparandum.getDisplayId() != null ? comparandum.getDisplayId() : "";

    return documentComparator.compare(thisId, thatId);

  }
  
  private String getHighlightString(){
      
      if("".equals(highlightString)) return "";
      return "/?q=" + highlightString;
      
      
  }
  
  private String getKWIC(){
      
      StringBuilder html = new StringBuilder();
      try{
          FileUtils util = new FileUtils("/data/papyri.info/idp.data", "/data/papyri.info/pn/idp.html");
          List<String> kwix = util.highlightMatches(highlightString, util.loadTextFromId(url.toExternalForm()));
          html.append("<tr class=\"result-text\"><td class=\"kwic\" colspan=\"6\">");
          for(String kwic : kwix){

              html.append(kwic);
              html.append("<br/>\n");

          }
          html.append("</td></tr>");
      } catch (Exception e){
          
          // TODO: Need to do something sensible here with regard to highlighting
          
      }
      return html.toString();
      
  }
  
  private String getImageHTML(){
      
      String imageHTML = "";
      Iterator<String> ipit = imagePaths.iterator();
      Boolean hasExternalImgs = false;
      Boolean hasInternalImgs = false;
      String intIndicator = "<span class=\"internal-link-indicator\" title=\"Internal link: Image is available from papyri.info\">Img</span>";
      String extIndicator = "<span class=\"external-link-indicator\" title=\"External link: Links will take you out of papyri.info. Papyri.info thus cannot guarantee their presence or quality.\">Img (ext.)</span>";
      String printIndicator = "<span class=\"illustration-indicator\" title=\"Images reproduced in print publication\">Print</span>";
      while(ipit.hasNext()){
          
          String path = ipit.next();
          if(path.contains("papyri.info")){
              
              hasInternalImgs = true;
              
          }
          else{
              
              hasExternalImgs = true;
          }
          
          
      }
      
      if(hasInternalImgs){
          
          imageHTML += intIndicator;
          
      }
      if(hasExternalImgs){
          
          if(hasInternalImgs) imageHTML += ", ";
          imageHTML += extIndicator;
          
      }
      if(this.hasIllustration){
          
          if(hasInternalImgs || hasExternalImgs) imageHTML += ", ";
          imageHTML += printIndicator;
          
      }
      if(imageHTML.equals("")) imageHTML = "None";
      
      return imageHTML; 
  
  }
  
  private String generateLink(){
      
      StringBuilder html = new StringBuilder();
      
      html.append(url.toString().substring("http://papyri.info".length()));
      html.append(this.getHighlightString());      
      html.append(getSolrQueryString());
      
      return html.toString();
      
  }
  
  /**
   * Sets the Solr data required for the document to generate a Solr query string
   * returning itself and its immediate neighbours in the result set.
   * 
   * Used for linear browsing functionality
   * 
   * @param sq A Solr querystring retrieving the current record and its immediate neighbour(s)
   * @param position The position of this record in the current resultset
   * @param total The total number of records in the current resultset
   */
  
  public void setSolrData(SolrQuery sq, long position, long total){
      
      setSolrQueryString(sq);
      setPosition(position);
      setTotal(total);
      
      
  }
  
  private void setSolrQueryString(SolrQuery sq){
          
      int browserLimit = 2000;
      String sqs = sq.toString();
      
      if(sq == null || sqs.length() > browserLimit){
          
          solrQueryString = "";
          return;
          
      }
      
      solrQueryString = sqs;
      
  }
  
  /**
   * Formats the 
   * 
   * @return 
   */
  
  private String getSolrQueryString(){
      
      if("".equals(solrQueryString)) return "";
      String sq = ("".equals(highlightString) ? "?" : "&") + solrQueryString;
      sq += "&p=" + String.valueOf(position) + "&t=" + String.valueOf(total); 
      return sq;
      
  }

  private void setPosition(long p){
      
      position = p;
      
  }
  
  private void setTotal(long t){
      
      total = t;
      
  }
  
   
}
