package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author thill
 */
public class DocumentBrowseRecord extends BrowseRecord implements Comparable {

  private DocumentCollectionBrowseRecord documentGroupRecord;
  private String displayId;
  private String place;
  private String date;
  private String language;
  private String hasTranslation;
  private String hasImage;
  private String hgv_identifier;
  private String invNum;

  public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, String itemId, String place, String date, String lang, Boolean hasImg, Boolean hasTrans, String hgv) {

    // TODO: this will have to be changed depending on what users want to see in the records

    this.documentGroupRecord = dgr;
    this.displayId = itemId;
    this.place = place;
    this.date = date;
    this.language = lang;
    this.hasTranslation = hasTrans ? "Yes" : "No";
    this.hasImage = hasImg ? "Yes" : "No";
    this.hgv_identifier = hgv;

  }

  public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, String itemId, String place, String date, String lang, Boolean hasImg, Boolean hasTrans) {

    // TODO: this will have to be changed depending on what users want to see in the records

    this(dgr, itemId, place, date, lang, hasImg, hasTrans, "");


  }

  @Override
  public String getHTML() {

    StringBuilder displayName = new StringBuilder();
    displayName.append(this.documentGroupRecord.getSeries());
    displayName.append(" ");
    displayName.append(this.documentGroupRecord.getVolume().equals("0") ? "" : this.documentGroupRecord.getVolume());
    displayName.append(" ");
    displayName.append(this.displayId);
    StringBuilder anchor = new StringBuilder();
    anchor.append("<a href='");
    anchor.append(this.assembleLink());
    anchor.append("'>");
    anchor.append(displayName.toString().replaceAll("_", " "));
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
    html.append(hasTranslation);
    html.append("</td>");
    html.append("<td class=\"has-images\">");
    html.append(hasImage);
    html.append("</td>");
    html.append("</tr>");
    return html.toString();

  }

  @Override
  public String assembleLink() {

    String coll = documentGroupRecord.getCollection();
    StringBuilder url = new StringBuilder();
    url.append("/");
    url.append(coll);
    url.append("/");
    StringBuilder item = new StringBuilder();

    if ("ddbdp".equals(coll)) {

      item.append(documentGroupRecord.getSeries());
      item.append(";");
      item.append("0".equals(documentGroupRecord.getVolume()) ? "" : documentGroupRecord.getVolume());
      item.append(";");
      item.append(this.displayId);


    } else if ("hgv".equals(coll)) {

      item.append(this.hgv_identifier);


    } else {    // if APIS

      item.append(this.displayId);



    }
    url.append(item);
    url.append("/");
    return url.toString().replaceAll("\\s", "");

  }

  public String getDisplayId() {
    return this.displayId;
  }

  @Override
  public int compareTo(Object o) {

    DocumentBrowseRecord comparandum = (DocumentBrowseRecord) o;
    String thisId = this.displayId != null ? this.displayId : "";
    String thatId = comparandum.getDisplayId() != null ? comparandum.getDisplayId() : "";

    thisId = this.displayId.replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");
    thatId = comparandum.getDisplayId().replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");

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
    return this.displayId.compareToIgnoreCase(comparandum.getDisplayId());

  }
}
