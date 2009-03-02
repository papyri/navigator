package info.papyri.digester;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;

public class MergeMetadataRecord extends CoreMetadataRecord {

    private HashMap<URL,String> webImages = new HashMap<URL,String>();
    private Set<String> xrefs = new HashSet<String>();
    private Set<String> ids = new HashSet<String>();
    private HashSet<String> displayDates = new HashSet<String>();

     @Override
     public void addIdentifier(String identifier) {
         if(identifier.startsWith(NamespacePrefixes.INV)){
             this.setInventoryNumber(identifier.substring(NamespacePrefixes.INV.length()).replaceAll("%20", " "));
         }
         this.ids.add(identifier);
     }

     @Override
     public void addWebImage(String caption, URL uri) {
         webImages.put(uri, caption);
     }

     @Override
     public Map<URL, String> getWebImages() {
         return webImages;
     }

     @Override
     public void addXref(String xref) {
         xrefs.add(xref);
     }
     
     public Set<String> getXrefs(){
         return xrefs;
     }
     public void addPublication(String newPublication) {
         if(newPublication==null)return;
         if((newPublication = newPublication.trim()).equals(""))return;
         super.addPublication(newPublication.replaceAll("[$]",""));
     }     
     public String getFreeformPublication(){
         return "";
     }
     public void addDisplayDate(String date){
         this.displayDates.add(date);
     }
     public Set<String> getDisplayDates(){
         return this.displayDates;
     }
 }