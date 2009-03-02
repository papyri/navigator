package info.papyri.metadata.apis;

import info.papyri.metadata.CoreMetadataRecord;
import info.papyri.metadata.NamespacePrefixes;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.net.URL;
public class OAIRecord extends CoreMetadataRecord {
   public OAIRecord(){
       
   }
   private HashMap<URL,String> webImages = new HashMap<URL,String>();
   private Set<String> xrefs = new HashSet<String>();
   private Set<String> ids = new HashSet<String>();
   
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

}
