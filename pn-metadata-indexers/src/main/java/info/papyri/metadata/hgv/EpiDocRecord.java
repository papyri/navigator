package info.papyri.metadata.hgv;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.papyri.metadata.CoreMetadataRecord;

public class EpiDocRecord extends CoreMetadataRecord {

    private HashMap<URL,String> webImages = new HashMap<URL,String>();
    private Set<String> xrefs = new HashSet<String>();
    private Set<String> ids = new HashSet<String>();
    
     @Override
     public void addIdentifier(String identifier) {
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
}
