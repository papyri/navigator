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
  private String imagePath;
  private static IdComparator documentComparator = new IdComparator();
  private static String cameraImgPath = "/images/camera.gif";
  private static String extLinkImgPath = "/images/extlink.gif";
  private static String cameraImgHeight = "28px";
  private static String cameraImgWidth = "35px";
  private static String extLinkImgHeight = "20px";
  private static String extLinkImgWidth = "20px";
 
  
  // TODO: Change images display so that icons/links to the original images are displayed instead of a simple 'yes'/'no' value
  // TODO: Change language display so that codes displayed instead of expanded strings.
  
  public DocumentBrowseRecord(String prefId, ArrayList<String> ids, URL url, String place, String date, String lang, Boolean hasImg, String trans) {

    this.preferredId = tidyPreferredId(prefId);
    this.itemIds = ids;
    this.url = url;
    this.place = place;
    this.date = date;
    this.language = tidyAncientLanguageCodes(lang);
    this.translationLanguages = tidyModernLanguageCodes(trans);
    this.imagePath = hasImg ? "Yes" : "No";
    
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
    html.append(imagePath);
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
      
      if(!imagePath.contains("http://")) return "None";
      
      StringBuilder html = new StringBuilder("<a href=\"");
      html.append(imagePath);
      html.append("\"><img alt=\"Image icon\" height=\"");
      html.append(String.valueOf(cameraImgHeight));
      html.append("\" width=\"");
      html.append(String.valueOf(cameraImgWidth));
      html.append("\" src=\"");
      html.append(cameraImgPath);
      html.append("\"/>");
      
      if(!imagePath.contains("papyri.info")){
          
          html.append("<img alt=\"External link icon\" height=\"");
          html.append(String.valueOf(extLinkImgHeight));
          html.append("\" width=\"");
          html.append(String.valueOf(extLinkImgWidth));
          html.append("\" src=\"");
          html.append(extLinkImgPath);
          html.append("\"/>");
          
      }
      
      html.append("</a>");
      
      return html.toString();
  }
}
