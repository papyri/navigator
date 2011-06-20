package info.papyri.dispatch.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author thill
 */

 
    
    public class DocumentBrowseRecord extends BrowseRecord implements Comparable{
        
        private DocumentCollectionBrowseRecord documentGroupRecord;
        private ArrayList<String> documentIds;
        private String displayId;
        private String place;
        private String date;
        private String language;        
        private String hasTranslation;
        private String hgv_identifier;
        private String ddbdpIds;
        private String hgvIds;
        private String apisIds;

    public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, ArrayList<String> itemIds, String itemId, String ddb, String hgv0, String apis, String place, String date, String lang, Boolean hasTrans, String hgv){
            
            // TODO: this will have to be changed depending on what users want to see in the records
            
            this.documentGroupRecord = dgr;
            this.documentIds = itemIds;
            this.ddbdpIds = ddb;
            this.hgvIds = hgv0;
            this.apisIds = apis;
            this.place = place;
            this.date = date;
            this.language = lang;
            this.hasTranslation = hasTrans ? "Yes" : "No";
            this.hgv_identifier = hgv;
            determineDisplayId(itemId);
            
        }
        
        public DocumentBrowseRecord(DocumentCollectionBrowseRecord dgr, ArrayList<String> itemIds, String itemId, String ddb, String hgv, String apis, String place, String date, String lang, Boolean hasTrans){
            
            // TODO: this will have to be changed depending on what users want to see in the records
            
            this(dgr, itemIds, itemId, ddb, hgv, apis, place, date, lang, hasTrans, "");
            
            
        }
        
        private void determineDisplayId(String itemId){
            
            if(this.documentIds.size() == 1){
                
                this.displayId = documentIds.remove(0);
                return;
                
            }
            Collections.sort(this.documentIds);
            Iterator<String> dit = documentIds.iterator();
            int i;
            for(i = 0; i < documentIds.size(); i++){
                
               if(itemId.matches(".*" + documentIds.get(i) + ".*")){
                    
                    this.displayId = documentIds.get(i);
                    break;
                    
                }
                
                
            }
            
            this.documentIds.remove(i);
            
        }
        
        @Override
        public String getHTML(){
            
            if(this.displayId.equals("0")) return ""; //TODO: Work out why zero-records occur and fix this bodge if necessary
            String displayName = this.documentGroupRecord.getSeries() + " " + this.documentGroupRecord.getVolume() + " " + this.displayId;
            String anchor = "<a href='" + this.assembleLink() + "'>" + displayName + "</a>";
            String html = "<tr class=\"identifier\"><td>" + anchor + "</td>";
            html += "<td class=\"display-place\">" + place + "</td>";
            html += "<td class=\"display-date\">" + date + "</td>";
            html += "<td class=\"ddbdp-ids\">" + ((ddbdpIds.length() > 0) ? ddbdpIds : "None") + "</td>";
            html += "<td class=\"hgv-ids\">" + ((hgvIds.length() > 0) ? hgvIds : "None") + "</td>";
            html += "<td class=\"apis-ids\">" + ((apisIds.length() > 0) ? apisIds : "None") + "</td>";
            html += "<td class=\"language\">" + language + "</td>";
            html += "<td class=\"has-translation\">" + hasTranslation + "</td>";
            html += "</tr>";
            return html;
            
        }
        
        @Override
        public String assembleLink(){
            
           String coll = documentGroupRecord.getCollection();
           String url = "http://localhost/" + coll + "/";
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

            thisId = this.displayId.replace("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");
            thatId = comparandum.getDisplayId().replace("[^\\d]", "").replaceFirst("^0+(?!$)", "").replaceAll("[\\s]", "");

            if(thisId.isEmpty()) thisId = "0";
            if(thatId.isEmpty()) thatId = "0";

            int thisIdNo = Integer.parseInt(thisId);
            int thatIdNo = Integer.parseInt(thatId);
            
            return thisIdNo - thatIdNo;
            
        }
        
        
    }
    