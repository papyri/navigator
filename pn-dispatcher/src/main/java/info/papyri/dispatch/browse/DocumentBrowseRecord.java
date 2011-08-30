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
  private String place;
  private String date;
  private String language;
  private String translationLanguages;
  private ArrayList<String> imagePaths;
  private Boolean hasIllustration;
  
  private static IdComparator documentComparator = new IdComparator();

 
  
  // TODO: Change images display so that icons/links to the original images are displayed instead of a simple 'yes'/'no' value
  // TODO: Change language display so that codes displayed instead of expanded strings.
  
  public DocumentBrowseRecord(String prefId, ArrayList<String> ids, URL url, String place, String date, String lang, ArrayList<String> imgPaths, String trans, Boolean illus) {

    this.preferredId = tidyPreferredId(prefId);
    this.itemIds = ids;
    this.url = url;
    this.place = place;
    this.date = date;
    this.language = tidyAncientLanguageCodes(lang);
    this.translationLanguages = tidyModernLanguageCodes(trans);
    this.imagePaths = imgPaths;
    this.hasIllustration = illus;
  }

  @Override
  public String getHTML() {

    StringBuilder anchor = new StringBuilder();
    anchor.append("<a href='");
    anchor.append(url.toString().substring("http://papyri.info".length()));
    anchor.append("'>");
    anchor.append(getDisplayId());
    anchor.append("</a>");
    StringBuilder html = new StringBuilder("<tr class=\"result-record\"><td class=\"identifier\" title=\"");
    html.append(getAlternativeIds());
    html.append("\">");
    html.append(anchor.toString());
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
