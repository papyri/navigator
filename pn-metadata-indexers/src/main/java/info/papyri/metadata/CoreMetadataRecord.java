package info.papyri.metadata;

import java.util.*;
import java.util.regex.*;
import java.net.URL;

import info.papyri.data.publication.*;
import info.papyri.data.provenance.*;

/**
 * JavaBean class that holds properties of each APISRecord entry. It is
 * important that this class be public and static, in order for Digester to
 * be able to instantiate it.
 */
public abstract class CoreMetadataRecord {
    public static int DATE_ARBITRATION_CERTAIN = 25;
    public static int DATE_ARBITRATION_UNCERTAIN = 50;
    public static enum ModernLanguage{
        ENGLISH, FRENCH, GERMAN
    }
    private static final String MULTIPLE_VALUES_DELIMITER = " ; ";

    private static final int MULTIPLE_VALUES_DELIMITER_LENGTH = MULTIPLE_VALUES_DELIMITER.length();
    public static final String UNDEFINED_DATE = "none";
    private String controlName = null;
    
    private boolean apisCNFormatted;

    private HashSet<String> associatedName = new HashSet<String>();

    private HashSet<String> author = new HashSet<String>();

    private Set<String> externalResource = new HashSet<String>();

    private String ddbdpAll = null;

    protected String ddbdpFirstOnly = null;

    protected String date1 = null;

    protected Map<String,String> dateIndex = new TreeMap<String,String>();

    protected String date2 = null;
    
    protected String datePreferredIndex = null;

    protected Set<String> notesCorrections  = new HashSet<String>();
    protected Set<String> notesGeneral  = new HashSet<String>();
    protected Set<String> notesIllustrations  = new HashSet<String>();
    protected Set<String> notesTranslations  = new HashSet<String>();

    protected Vector<String> historicalData = new Vector<String>();

    protected Vector<String> image = new Vector<String>();

    protected String inventoryNumber = new String();

    protected HashSet<String> language = new HashSet<String>();

    protected Vector<String> material = new Vector<String>();

    protected String physicalDescription = null;

    protected Vector<String> provenanceNotes = new Vector<String>();

    protected Vector<String> provenance = new Vector<String>();
    
    protected String publicationNote = "";

    protected Set<String> publication = new HashSet<String>();
    protected Set<String> structured_publication = new HashSet<String>();
    
    protected Set<String> indexedSeries = new TreeSet<String>();

    protected Vector<String> publicationsAbout = new Vector<String>();

    protected Vector<String> publicationsAboutMoreInfo = new Vector<String>();

    protected Vector<String> reference = new Vector<String>();

    protected Vector<String> subjectSearchField = new Vector<String>();

    protected Vector<String> subjectDisplayInItemView = new Vector<String>();

    protected Vector<String> summary = new Vector<String>();
    
    protected Set<String> mentionedDates = new HashSet<String>();

    protected String title = null;

    protected Vector<String> translationEN = new Vector<String>();
    protected Vector<String> translationDE = new Vector<String>();
    protected Vector<String> translationFR = new Vector<String>();
    
    protected String ddbdpPub = null;
    
    protected String UTC = null;
    
    protected Vector<String> errors = new Vector<String>();
    private boolean error = false;
    
    public abstract void addIdentifier(String identifier);
    public abstract void addXref(String xref);
    public abstract void addWebImage(String caption,URL uri);
    public abstract Map<URL,String> getWebImages();
    public abstract Collection<String> getXrefs();
    public void addError(String msg){
        error = true;
        errors.add(msg);
    }
    
    public boolean hadError(){
        return error;
    }
    
    public String [] getErrors(){
        return errors.toArray(new String[0]);
    }

    public Vector<String> getAll() {
        Vector<String> all = new Vector<String>();

        all.addAll(this.getAllNoTrans());
        if (this.getTranslation(ModernLanguage.ENGLISH) != null)
            all.addAll(this.getTranslation(ModernLanguage.ENGLISH));
        if (this.getTranslation(ModernLanguage.GERMAN) != null)
            all.addAll(this.getTranslation(ModernLanguage.GERMAN));
        if (this.getTranslation(ModernLanguage.FRENCH) != null)
            all.addAll(this.getTranslation(ModernLanguage.FRENCH));
        return all;
    }

    public Vector<String> getAllNoTrans() {
        Vector<String> allNoTrans = new Vector<String>();

        if (this.getTitle() != null)
            allNoTrans.add(this.getTitle());
        if (this.getSubjectDisplayInItemView() != null)
            allNoTrans.addAll(this.getSubjectDisplayInItemView());
        if (this.getAssociatedNames() != null)
            allNoTrans.addAll(this.getAssociatedNames());
        if (this.getMaterial() != null)
            allNoTrans.addAll(this.getMaterial());
        if (this.getLanguages() != null)
            allNoTrans.addAll(this.getLanguages());
        if (this.getProvenance().size() > 0){
            allNoTrans.addAll(this.provenance);
            allNoTrans.addAll(this.provenanceNotes);
        }
        if(this.summary.size()>0){
            allNoTrans.addAll(this.summary);
        }
        if (this.notesGeneral.size() > 0){
            allNoTrans.add(this.getGeneralNotes());
        }
        if (this.notesCorrections.size() > 0){
            allNoTrans.add(this.getCorrectionNote());
        }
        if (this.notesIllustrations.size() > 0){
            allNoTrans.add(this.getIllustrationNote());
        }
        if (this.notesTranslations.size() > 0){
            allNoTrans.add(this.getTranslationNote());
        }
        if (this.notesTranslations.size() > 0){
            allNoTrans.add(this.getTranslationNote());
        }
        if (this.subjectSearchField.size()> 0){
            allNoTrans.addAll(this.subjectSearchField);
        }
        return allNoTrans;

    }

    public void setAssociatedName(String newAssociatedName) {
        if (newAssociatedName != null
                && (!newAssociatedName.matches("^\\s*$")))
            associatedName.add(newAssociatedName);
    }

    public Collection<String> getAssociatedNames() {
        if (associatedName.size() == 0) {
            return null;
        } else {
            return associatedName;
        }
    }

    public void setAuthor(String newAuthor) {
        if (newAuthor != null && (!newAuthor.matches("^\\s*$")))
            author.add(newAuthor);
    }

    public Collection<String> getAuthor() {
        if (author.size() == 0) {
            return null;
        } else {
            return author;
        }
    }

    public void setControlName(String newControlName) {
        controlName = newControlName;
        if (controlName != null && controlName.startsWith(CoreMetadataFields.APIS_PREFIX)){
            apisCNFormatted = true;
        }
    }

    public String getControlName() {
        return controlName;
    }
    
    public String getInstitution(){
        if (apisCNFormatted){
            String id = controlName.substring(CoreMetadataFields.APIS_PREFIX.length());
            return id.substring(0,id.indexOf(':'));
        }
        else return null;
     }
    
    public String getAPISNumber(){
        if (apisCNFormatted){
            String id = controlName.substring(CoreMetadataFields.APIS_PREFIX.length());
            return id.substring(id.indexOf(':')+1);
        }
        else return null;
    }
    
    private static Pattern YEAR = Pattern.compile("^[-]?\\d+$");
    private static String getDisplayDate(String date){
        if (date == null || date.length() == 0 ) return date;
        Matcher m = YEAR.matcher(date);
        if(!m.matches()) return date;
        if (date.charAt(0) == '-'){
            date = date.substring(1) + " BCE";
        }
        else {
            date += " CE";
        }
        return date;
    }

    public void setDate1(String date) {
        date1 = getDisplayDate(date);
    }
    
    private static String getIndexDate(String date) throws OutOfRangeException, NumberFormatException {
        boolean bce = false;
        if(date==null || "".equals(date)) return null;
        if(date.charAt(0) == '-'){
            bce = true;
            date = date.substring(1);
        }
        String [] parts = date.split("-");
        if(parts.length < 1) return null;
        for(int i=0;i<parts.length;i++){
            while(parts[i].charAt(0) == '0'){
                parts[i] = parts[i].substring(1);
            }
        }
        int year = Integer.parseInt(parts[0]);
        if(bce) year = -1*year;
        int month = (parts.length > 1)?Integer.parseInt(parts[1]):1;
        int day = (parts.length > 2)?Integer.parseInt(parts[2]):1;
        return info.papyri.util.NumberConverter.encodeDate(year, month, day);
    }

    public void setDateIndexPair(String date1, String date2, boolean encode) {
        if(date1 == null) return;
        if(encode){
        try{
             String date1Index = getIndexDate(date1);
             if(this.datePreferredIndex == null) this.datePreferredIndex = date1Index;
             String date2Index  = (date2 != null)?getIndexDate(date2):date1Index;
             this.dateIndex.put(date1Index, date2Index);
        }
        catch (OutOfRangeException e){
            String msg = (getClass().getName() + ".setDate1Index() : " + e.toString());
            addError(msg);
        }
        catch (NumberFormatException nfe){
            String msg = (getClass().getName() + ".setDate1Index() : " + nfe.toString());
            addError(msg);
        }
        }
        else{
            this.dateIndex.put(date1, date2);
            if(this.datePreferredIndex == null) this.datePreferredIndex = date1;
        }
    }
    
    public void setSortDate(String date){
        this.datePreferredIndex = date;
    }

    public Map<String,String> getDateIndexes() {
        return dateIndex;
    }

    public String getDate1() {
        return date1;
    }

    public void setDate2(String date) {
        date2 = getDisplayDate(date);
    }

    public String getDate2() {
        return date2;
    }
    
    public String getSortDate(){
        return this.datePreferredIndex;
    }

    public void setDCMIDate(String dcmiDate){
        if(dcmiDate == null) return;
        String [] parts = dcmiDate.split(";");
        String start = null;
        String end = null;
        for(String part:parts){
            if(part.startsWith("start=")){
                start = part.substring("start=".length());
            }
            if(part.startsWith("end=")){
                end = part.substring("end=".length());
            }
        }
        if(start == null ) start = dcmiDate;
        if(end==null) end = start;
        setDate1(start);
        setDate2(end);
        setDateIndexPair(start,end, true);
    }
    
    public void addMentionedDate(String date){
        this.mentionedDates.add(date);
    }
    
    public Collection<String> mentionedDates(){
        return this.mentionedDates;
    }

    public void addGeneralNotes(String note) {
        if(note == null || (note = note.trim()).equals("")) return;
        note = translateEntRefs(note);
        if (note.length() == 0) return;
        notesGeneral.add(note);
    }

    public String getGeneralNotes() {
        if (notesGeneral.size() == 0) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> notes = notesGeneral.iterator();
            while(notes.hasNext()){
                buffer.append(notes.next());
                if(notes.hasNext()) buffer.append(" ; ");
            }
            return buffer.toString();
        }
    }

    public String getHasImagesSortField() {

        if (image.size() > 0) {
            return CoreMetadataFields.SORTABLE_YES_VALUE;
            //return "A--has images, float to top";
        } else {
            return CoreMetadataFields.SORTABLE_NO_VALUE;
            //return "B--no images";
        }

    }
    
    public String getHasTranslationSortField(){
        if (translationEN.size() > 0 || translationDE.size() > 0 || translationFR.size() > 0 || notesTranslations.size() > 0){
            return CoreMetadataFields.SORTABLE_YES_VALUE;
        }
        else{
            return CoreMetadataFields.SORTABLE_NO_VALUE;
        }
    }

    public void setHistoricalData(String newHistoricalData) {
        if (newHistoricalData != null
                && (!newHistoricalData.matches("^\\s*$"))
                && !historicalData.contains(newHistoricalData)) {
            newHistoricalData = translateEntRefs(newHistoricalData);
            historicalData.add(newHistoricalData);
        }
    }

    public Vector<String> getHistoricalData() {
        if (historicalData.size() == 0) {
            return null;
        } else {
            return historicalData;
        }
    }
    
    public void addIllustrationNote(String note){
        if(note == null || (note = note.trim()).equals("")) return;
        this.notesIllustrations.add(note);
    }
    
    public String getIllustrationNote(){
        if (notesIllustrations.size() == 0) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> notes = notesIllustrations.iterator();
            while(notes.hasNext()){
                buffer.append(notes.next());
                if(notes.hasNext()) buffer.append(" ; ");
            }
            return buffer.toString();
        }
    }
    public void addCorrectionNote(String note){
        if(note == null || note.trim().equals("")) return;
        this.notesCorrections.add(note);
    }
    
    public String getCorrectionNote(){
        if (notesCorrections.size() == 0) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> notes = notesCorrections.iterator();
            while(notes.hasNext()){
                buffer.append(notes.next());
                if(notes.hasNext()) buffer.append(" ; ");
            }
            return buffer.toString();
        }
    }
    public void addTranslationNote(String note){
        if(note == null || note.trim().equals("")) return;
        this.notesTranslations.add(note);
    }
    
    public String getTranslationNote(){
        if (notesTranslations.size() == 0) {
            return null;
        } else {
            StringBuffer buffer = new StringBuffer();
            Iterator<String> notes = notesTranslations.iterator();
            while(notes.hasNext()){
                buffer.append(notes.next());
                if(notes.hasNext()) buffer.append(" ; ");
            }
            return buffer.toString();
        }
    }
    public void setInventoryNumber(String newInventoryNumber) {
        inventoryNumber = newInventoryNumber;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public void addLanguage(String newLanguage) {

        /*
         * APIS: cu546
         * HGV: div@type="edition"@lang
         */
        
        if(newLanguage.indexOf(';') != -1){
            for(String lang:newLanguage.split(";")){
                language.add(lang);
            }
        }
        else language.add(newLanguage);
    }

    public Collection<String> getLanguages() {
        return language;
    }

    public void setMaterial(String newMaterial) {
        if (newMaterial != null && (!newMaterial.matches("^\\s+$")) && !material.contains(newMaterial))
            material.add(newMaterial);
    }

    public Vector<String> getMaterial() {
        if (material.size() == 0) {
            return null;
        } else {
            return material;
        }
    }

    public void setPhysicalDescription(String newPhysicalDescription) {
        physicalDescription = newPhysicalDescription;
    }

    public String getPhysicalDescription() {
        return physicalDescription;
    }

    public void addProvenance(String newProvenance) {
        if (!provenanceNotes.contains(newProvenance)) provenanceNotes.add(newProvenance);
        if (newProvenance != null && (!newProvenance.matches("^\\s*$"))){
            String [] parts = newProvenance.split("([\\|\\(\\)\\?,]|(((([Pp]robably)|([Pp]erhaps)|([Aa]cquired)|([Pp]urchased))\\s)?[Ff]rom)|(\\s[Nn]ome))");
            for (int i=0;i<parts.length;i++){
                String part = parts[i].trim();
                if (part.matches("^[A-Z][a-z]{2,}$")){
                    part = ProvenanceControl.matchShingled(part);
                    if(part != null && !provenance.contains(part)) provenance.add(part);
                }
            }
        }
    }
    
    public void addProvenanceIndex(String index){
        if(index==null)return;
        index = index.toLowerCase();
        if (!provenanceNotes.contains(index)) provenanceNotes.add(index);
        if(!provenance.contains(index)) provenance.add(index);
    }

    public Vector<String> getProvenance() {
            return provenanceNotes;
    }
    
    public Vector<String> getProvenanceIndices(){
        return provenance;
    }

    public void addPublication(String newPublication) {
        if(newPublication == null) return;
        newPublication = newPublication.trim();
        if ("".equals(newPublication) || publication.contains(newPublication)){
            return;
        }

        Collection<String> matches = PublicationMatcher.findMatches(newPublication);
        Iterator<String> mIter = matches.iterator();
        while (mIter.hasNext()){
            String next = mIter.next();
            if (next.matches("(^[^\\d]*)[\\d]+(.*$)")){
                String p = next.replaceAll("\\$", "");
                p = p.replaceAll("\\s+", " ");
                if (!publication.contains(p)){
                    publication.add(p);
                    Set<String> struct = StructuredPublication.getStructuredPub(p);
                    if(struct.size() > 0){
                        for(String sp:struct) addStructuredPublication(sp);
                    }
                }
                if (next.indexOf('$') != -1){
                    String series = next.substring(0,next.indexOf('$'));
                    if (!indexedSeries.contains(series))indexedSeries.add(series);
                }
            }
        }
        if (this.publicationNote.indexOf(newPublication) == -1){
            this.publicationNote += (" ; " + newPublication);
        }
    }
    
    public void addStructuredPublication(String struct){
        structured_publication.add(struct);
    }

    public Collection<String> getPublication() {
            return publication;
    }
    
    public Collection<String> getStructuredPublication() {
        return structured_publication;
}

    public Collection<String> getIndexedSeries(){
        return indexedSeries;
    }
    
    public String getFreeformPublication(){
        return this.publicationNote;
    }

    public void setPublicationsAbout(String newPublicationsAbout) {
        if (newPublicationsAbout != null
                && (!newPublicationsAbout.matches("^\\s*$"))) {
            newPublicationsAbout = translateEntRefs(newPublicationsAbout);
            publicationsAbout.add(newPublicationsAbout);
        }
    }

    public Vector<String> getPublicationsAbout() {
        if (publicationsAbout.size() == 0) {
            return null;
        } else {
            return publicationsAbout;
        }
    }

    public void setPublicationsAboutMoreInfo(
            String newPublicationsAboutMoreInfo) {
        if (newPublicationsAboutMoreInfo != null
                && (!newPublicationsAboutMoreInfo.matches("^\\s*$"))) {
            newPublicationsAboutMoreInfo = translateEntRefs(newPublicationsAboutMoreInfo);
            publicationsAboutMoreInfo.add(newPublicationsAboutMoreInfo);
        }
    }

    public Vector<String> getPublicationsAboutMoreInfo() {
        if (publicationsAboutMoreInfo.size() == 0) {
            return null;
        } else {
            return publicationsAboutMoreInfo;
        }
    }

    public void setReference(String newReference) {
        if (newReference != null && (!newReference.matches("^\\s*$")))
            reference.add(newReference);
    }

    public Vector<String> getReference() {
        if (reference.size() == 0) {
            return null;
        } else {
            return reference;
        }
    }

    public void addSubjectSearchField(String newSubjectSearchField) {
        if (newSubjectSearchField != null
                && (!newSubjectSearchField.matches("^\\s*$")))
            subjectSearchField.add(newSubjectSearchField);
    }

    public Vector<String> getSubjectSearchField() {
        if (subjectSearchField.size() == 0) {
            return null;
        } else {
            return subjectSearchField;
        }
    }

    public void setSubjectDisplayInItemView(
            String newSubjectDisplayInItemView) {
        if (newSubjectDisplayInItemView != null
                && (!newSubjectDisplayInItemView.matches("^\\s*$")))
            subjectDisplayInItemView.add(newSubjectDisplayInItemView);
    }

    public Vector<String> getSubjectDisplayInItemView() {
        if (subjectDisplayInItemView.size() == 0) {
            return null;
        } else {
            return subjectDisplayInItemView;
        }
    }

    public void setSummary(String newSummary) {
        if (newSummary != null && (!newSummary.matches("^\\s*$"))) {
            newSummary = translateEntRefs(newSummary);
            summary.add(newSummary);
        }
    }

    public Vector<String> getSummary() {
        if (summary.size() == 0) {
            return null;
        } else {
            return summary;
        }
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTranslation(String newTranslation) {
        setTranslation(newTranslation,ModernLanguage.ENGLISH);
    }
    public void setTranslation(String newTranslation, ModernLanguage lang) {
        if (newTranslation != null ) {
            newTranslation = translateEntRefs(newTranslation).trim();
            if (newTranslation.length() == 0) return;
            
            switch(lang){
            case GERMAN:
                translationDE.add(newTranslation);
                break;
            case FRENCH:
                translationFR.add(newTranslation);
                break;
            default:
                translationEN.add(newTranslation);
            }
        }
    }
    
    public Vector<String> getTranslation() {
        return getTranslation(ModernLanguage.ENGLISH);
    }
    public Vector<String> getTranslation(ModernLanguage lang) {
        Vector<String> result = null;
        switch(lang){
        case GERMAN:
            result = this.translationDE;
            break;
        case FRENCH:
            result = this.translationFR;
            break;
        case ENGLISH:
            result = this.translationEN;
            break;
        default:
            return null;
        }
        if (result.size() == 0) {
            return null;
        } else {
            return result;
        }
    }
    
    public void setUTC(String utcTimestamp){
        this.UTC = utcTimestamp;
    }
    
    public String getUTC(){
        return this.UTC;
    }
    
    public void addExternalResource(String rsc) {
        if (!externalResource.contains(rsc)){
            externalResource.add(rsc);
        }
    }
    
    public Collection<String> getExternalResource() {
        if (externalResource.size() > 0){
            return externalResource;
        }
        else return null;
    }
    
    

    public static String getMultipleValuesAsString(Collection vector) {

        if (vector == null || vector.size() == 0) {
            return "";
        } else {
            StringBuffer vectorString = new StringBuffer();
            Iterator strings = vector.iterator();
            while (strings.hasNext()) {
                String next = strings.next().toString();
                vectorString.append(next);
                if (strings.hasNext()) vectorString.append(MULTIPLE_VALUES_DELIMITER);
            }

            return vectorString.toString();
        }

    }

    private static String translateEntRefs(String string) {

        string = string.replaceAll("\\&lt;", "<");
        string = string.replaceAll("\\&gt;", ">");
        string = string.replaceAll("\\&amp;", "&");

        return string;

    }
    
  
}