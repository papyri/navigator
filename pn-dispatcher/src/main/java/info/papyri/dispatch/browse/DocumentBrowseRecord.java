package info.papyri.dispatch.browse;

import info.papyri.dispatch.LanguageCode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;


/**
 * The <code>DocumentBrowseRecord</code> class stores summary information regarding
 * documents retrieved during browsing or search.
 * 
 * 
 * @author thill
 */
public class DocumentBrowseRecord extends BrowseRecord implements Comparable {

  private String preferredId;
  private ArrayList<String> itemIds = new ArrayList<String>();
  private URL url;
  private String documentTitle;
  private String place;
  private String date;
  private String language;
  private String translationLanguages;
  private ArrayList<String> imagePaths;
  private Boolean hasIllustration;
  private String highlightString;
  
  private static IdComparator documentComparator = new IdComparator();
  
  public DocumentBrowseRecord(String prefId, ArrayList<String> ids, URL url, ArrayList<String> titles, String place, String date, String lang, ArrayList<String> imgPaths, String trans, Boolean illus, String hlite) {

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
    this.highlightString = hlite;
    
  }

  @Override
  public String getHTML() {

    StringBuilder anchor = new StringBuilder();
    anchor.append("<a href='");
    anchor.append(url.toString().substring("http://papyri.info".length()));
    anchor.append(this.getHighlightString());
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
    return html.toString();

  }

  // TODO: Change in line with trac http://idp.atlantides.org/trac/idp/ticket/828
  
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
      shortTitle += " &hellip;";
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
}
