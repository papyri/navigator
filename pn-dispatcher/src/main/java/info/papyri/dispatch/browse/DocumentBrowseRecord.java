package info.papyri.dispatch.browse;

import java.net.URL;

/**
 * @author thill
 */
public class DocumentBrowseRecord extends BrowseRecord implements Comparable {

  private DocumentCollectionBrowseRecord documentGroupRecord;
  private String itemId;
  private URL url;
  private String place;
  private String date;
  private String language;
  private String translationLanguages;
  private String hasImage;
  private String invNum;

  public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, String itemId, URL url, String place, String date, String lang, Boolean hasImg, String trans, String invNum) {

    // TODO: this will have to be changed depending on what users want to see in the records

    this.documentGroupRecord = dgr;
    this.itemId = itemId;
    this.url = url;
    this.place = place;
    this.date = date;
    this.language = lang;
    this.translationLanguages = trans;
    this.hasImage = hasImg ? "Yes" : "No";
    this.invNum = invNum;

  }



  @Override
  public String getHTML() {

    StringBuilder anchor = new StringBuilder();
    anchor.append("<a href='");
    anchor.append(url);
    anchor.append("'>");
    anchor.append(getDisplayId());
    anchor.append("</a>");
    StringBuilder html = new StringBuilder("<tr><td class=\"identifier\">" + anchor + "</td>");
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
    html.append(hasImage);
    html.append("</td>");
    html.append("</tr>");
    return html.toString();

  }

  public String getDisplayId() {
      
     if(documentGroupRecord.getCollection().toUpperCase().equals("APIS") && invNum != null){
         
         return invNum;
     } 
      
    StringBuilder displayName = new StringBuilder();
    displayName.append(this.documentGroupRecord.getSeries());
    displayName.append(" ");
    displayName.append(this.documentGroupRecord.getVolume().equals("0") ? "" : this.documentGroupRecord.getVolume());
    displayName.append(" ");
    displayName.append(this.itemId);
    String rawName = displayName.toString();
    return rawName.replaceAll("_", " ");
  
  }

  @Override
  public int compareTo(Object o) {

    DocumentBrowseRecord comparandum = (DocumentBrowseRecord) o;
    String thisId = this.getDisplayId() != null ? this.getDisplayId() : "";
    String thatId = comparandum.getDisplayId() != null ? comparandum.getDisplayId() : "";

    thisId = thisId.replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");
    thatId = thatId.replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");

    if (thisId.isEmpty()) {
      thisId = "0";
    }
    if (thatId.isEmpty()) {
      thatId = "0";
    }

    long thisIdNo = Long.parseLong(thisId);
    long thatIdNo = Long.parseLong(thatId);

    if (thisIdNo > thatIdNo) {

      return 1;

    } else if (thisIdNo < thatIdNo) {

      return -1;

    }
    return this.getDisplayId().compareToIgnoreCase(comparandum.getDisplayId());

  }
}
