package info.papyri.data.publication;

import info.papyri.metadata.CoreMetadataRecord;

import java.util.*;
import info.papyri.digester.offline.APISTuple;

public class PublicationScrubber {
    private Set<String> publications = new HashSet<String>();
    Set<String> corrected = new HashSet<String>();
    Set<String> corrections = new HashSet<String>();
    final protected String basePubInfo;
    final protected String controlName;
    final protected String inventory;
    final protected String notes;
    public PublicationScrubber(String pubInfo, APISTuple apis){
        this.basePubInfo = pubInfo;
        this.notes = (apis.getNotes()!=null)?apis.getNotes():"";
        this.controlName = apis.getControlName();
        this.inventory = apis.getInventory();
        Iterator<String> pubs = apis.getPublications();
        while (pubs.hasNext()) addPublication(pubs.next());
        init();
        
    }
    public PublicationScrubber(String pubInfo, CoreMetadataRecord coreData){
        this.basePubInfo = translateEntRefs(pubInfo);
        this.notes = (coreData.getGeneralNotes()!=null)?coreData.getGeneralNotes():"";
        this.controlName = coreData.getControlName();
        this.inventory = coreData.getInventoryNumber();
       addPublications(coreData.getPublication());
        init();
    }
    
    protected boolean addPublication(String p){
        for(String s:PublicationMatcher.requiredAlternates(this.publications)){
            this.publications.add(s);
        }
        return this.publications.add(p);
    }
    
    protected boolean addPublications(Collection<String> p){
        boolean result = this.publications.addAll(p);
        for(String each:p){
            addPublication(each);
        }
        return result;
    }
    
    protected boolean hasPublication(String pub){
        return this.publications.contains(pub);
    }
    
    protected boolean removePublication(String pub){
        return this.publications.remove(pub);
    }
    protected void clearPublications(){
        this.publications.clear();
    }

    private void init(){
        if (!this.basePubInfo.matches("^\\s*$")) {
            if (this.basePubInfo.indexOf('|') != -1 || this.basePubInfo.indexOf(';') != -1){
                String [] pubs = this.basePubInfo.split("[;\\|]");
                for (int i=0;i<pubs.length;i++){
                    Collection<String> pArray = scrub(pubs[i]);
                    for (String p: pArray){
                        String temp = p.replaceAll("\\$","");
                        if (addPublication(temp)) {
                            System.out.println("PublicationScrubber.init(): Adding temp: " + temp);
                        }
                    }

                }
            }
            else {
                Collection<String> newPubArray = scrub(this.basePubInfo);
                for (String newPub: newPubArray){
                    String temp = newPub.replaceAll("\\$","");
                    addPublication(temp);
                }
            }
        }
    }
    
    protected Collection<String> scrub(String publication){
        String result = normalizeWhiteSpace(publication);
        if (result.startsWith("P.Ross.Georg.")){
            result = result.replaceAll("P\\.Ross\\.Georg\\.","P.Ross. Georg.");
        }
        if (result.startsWith("O.Ber.")){
            result = result.replaceAll("O\\.Ber\\.","O.Berenike");
        }
        if (result.startsWith("P.Strasb.")){
            result = result.replaceAll("P\\.Strasb\\.","P.Stras.");
        }
        result = result.replaceAll(" recto"," Recto").replaceAll(" verso", " Verso");
        return PublicationMatcher.findMatches(result);
    }

    private static String translateEntRefs(String string) {

        string = string.replaceAll("\\&lt;", "<");
        string = string.replaceAll("\\&gt;", ">");
        string = string.replaceAll("\\&amp;", "&");

        return string;

    }
    
    public Collection<String> getPublications(){
        return publications;
    }
    
    public static String normalizeWhiteSpace(String original){
        return original.trim().replaceAll("\\s+"," ");
    }
    
    public static PublicationScrubber get(String pubInfo, APISTuple apis) {
        PublicationScrubber result = null;
        String controlName = apis.getControlName();
        if (controlName.indexOf("chicago") != -1){
            result = new ChicagoScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("columbia") != -1){
            result = new ColumbiaScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("michigan") != -1){
            result = new MichiganScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("berkeley") != -1){
            result = new BerkeleyScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("princeton") != -1){
            result = new PrincetonScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("toronto") != -1){
            result = new TorontoScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("nyu") != -1){
            result = new NYUScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("hermitage") != -1){
            result = new HermitageScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("wisconsin") != -1){
            result = new WisconsinScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("yale") != -1){
            result = new YaleScrubber(pubInfo,apis);
        }
        else {
            result = new PublicationScrubber(pubInfo,apis);
        }
        return result;
    }
    
    public static PublicationScrubber get(String pubInfo, CoreMetadataRecord apis){
        PublicationScrubber result = null;
        String controlName = apis.getControlName();
        if (controlName.indexOf("chicago") != -1){
            result = new ChicagoScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("columbia") != -1){
            result = new ColumbiaScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("michigan") != -1){
            result = new MichiganScrubber(pubInfo,apis);   
        }
        else if (controlName.indexOf("berkeley") != -1){
            result = new BerkeleyScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("princeton") != -1){
            result = new PrincetonScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("toronto") != -1){
            result = new TorontoScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("nyu") != -1){
            result = new NYUScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("hermitage") != -1){
            result = new HermitageScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("wisconsin") != -1){
            result = new WisconsinScrubber(pubInfo,apis);
        }
        else if (controlName.indexOf("yale") != -1){
            result = new YaleScrubber(pubInfo,apis);
        }
        else {
            result = new PublicationScrubber(pubInfo,apis);
        }
        return result;
    }
    
    protected void removeParentPubs(String pub){
        String [] publications = new String[0];
        publications = this.publications.toArray(publications);
        for(String old:publications){
            if(pub.startsWith(old)) this.publications.remove(old);
        }
    }
}
