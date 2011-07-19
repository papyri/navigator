package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author thill
 */

 
    
    public class DocumentBrowseRecord extends BrowseRecord implements Comparable{
        
        private DocumentCollectionBrowseRecord documentGroupRecord;
        private String displayId;
        private String place;
        private String date;
        private String language;        
        private String hasTranslation;
        private String hasImage;
        private String hgv_identifier;

    public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, String itemId, String place, String date, String lang, Boolean hasImg, Boolean hasTrans, String hgv){
            
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
        
   public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, String itemId, String place, String date, String lang, Boolean hasImg, Boolean hasTrans){
            
            // TODO: this will have to be changed depending on what users want to see in the records
            
            this(dgr, itemId, place, date, lang, hasImg, hasTrans, "");
            
            
    }
        
        
        @Override
        public String getHTML(){
            
            String displayName = this.documentGroupRecord.getSeries() + " " + (this.documentGroupRecord.getVolume().equals("0") ? "" : this.documentGroupRecord.getVolume()) + " " + this.displayId;
            displayName = displayName.replaceAll("_", " ");
            String anchor = "<a href='" + this.assembleLink() + "'>" + displayName + "</a>";
            String html = "<tr><td class=\"identifier\">" + anchor + "</td>";
            html += "<td class=\"display-place\">" + place + "</td>";
            html += "<td class=\"display-date\">" + date + "</td>";
            html += "<td class=\"language\">" + language + "</td>";
            html += "<td class=\"has-translation\">" + hasTranslation + "</td>";
            html += "<td class=\"has-images\">" + hasImage + "</td>";
            html += "</tr>";
            return html;
            
        }
        
        @Override
        public String assembleLink(){
            
           String coll = documentGroupRecord.getCollection();
           String url = "/" + coll + "/";
           String item = "";
           
           if("ddbdp".equals(coll)){
               
               item += documentGroupRecord.getSeries() + ";";
               item += ("0".equals(documentGroupRecord.getVolume()) ? "" : documentGroupRecord.getVolume()) + ";";
               item += this.displayId;
               
               
           }
           else if("hgv".equals(coll)){
               
               item += this.hgv_identifier;
               
               
           }
           else{    // if APIS
               
               item += documentGroupRecord.getSeries() + ".";
               item += coll + ".";
               item += this.displayId;
               
               
           }
           url += item;
           url += "/";
           url = url.replaceAll("\\s", "");
           return url;
                      
        }
        
        public String getDisplayId(){ return this.displayId; }

        @Override
        public int compareTo(Object o) {
           
            DocumentBrowseRecord comparandum = (DocumentBrowseRecord)o;
            String thisId = this.displayId != null ? this.displayId : "";
            String thatId = comparandum.getDisplayId() != null ? comparandum.getDisplayId() : "";

            thisId = this.displayId.replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");
            thatId = comparandum.getDisplayId().replaceAll("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");

            if(thisId.isEmpty()) thisId = "0";
            if(thatId.isEmpty()) thatId = "0";

            long thisIdNo = Long.parseLong(thisId);
            long thatIdNo = Long.parseLong(thatId);
            
            if(thisIdNo > thatIdNo){
                
                return 1;
                
            }
            else if(thisIdNo < thatIdNo){
                
                return -1;
                
            }
            return this.displayId.compareToIgnoreCase(comparandum.getDisplayId());
            
        }
        
        
    }
    