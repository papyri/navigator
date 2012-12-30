package info.papyri.dispatch.browse;

import info.papyri.dispatch.FileUtils;
import info.papyri.dispatch.browse.facet.IdentifierFacet;

/**
     * <code>DocumentCollectionBrowseRecord</code>s are records of all information necessary to identify a <i>collection</i>
     * of documents.
     * 
     * Minimally, this will be collections and series. Maximally this will be collection, series, and volume.
     * 
     * 
     */
    
    public class DocumentCollectionBrowseRecord extends BrowseRecord{
        
        /* (ddbdp | hgv | apis) */
        private String collection;
        private String series;
        private String volume;
        /* True if the immediate children of this record are documents; false if they are other collections */
        private Boolean isDocumentParent;
        private String unicodeLabel;
    
        /**
         * Constructor for cases in which the volume is known
         * 
         * Note that if a volume is known, the collection's children must be documents and not collections;
         * i.e., <code>isDocumentParent</code> is <code>true</code>
         * 
         * @param collection
         * @param series
         * @param volume 
         */
        
        public DocumentCollectionBrowseRecord(String collection, String series, String volume){
            
            this.collection = collection;
            this.series = trimUnderscores(series);
            this.volume = trimUnderscores(volume);
            this.isDocumentParent = true;
            
        }
        
 
        /**
         * Constructor for cases in which the volume is either unknown, or there is no volume in
         * the classification hierarchy
         * 
         * @param collection
         * @param series
         * @param isParent 
         */
        public DocumentCollectionBrowseRecord(String collection, String series, Boolean isDocumentParent){
            
            this.collection = collection;
            this.series = isDocumentParent ? trimUnderscores(series) :  series;
            this.isDocumentParent = isDocumentParent;
            this.volume = null;
            
        }
        
        public DocumentCollectionBrowseRecord(String collection, String series, String volume, String lbl){
            
            this(collection, series, volume);
            unicodeLabel = lbl;
            
        }
        
        public DocumentCollectionBrowseRecord(String collection, String series, Boolean isDocumentParent, String lbl){
            
            this(collection, series, isDocumentParent);
            unicodeLabel = lbl;
            
        }
        
        
        @Override
        public String getHTML(){
            
            String href = assembleLink().replaceAll(" ", "_");
            String seriesRepresentation = (unicodeLabel == null || unicodeLabel.equals("")) ? series : unicodeLabel;
            String displayString = seriesRepresentation + (volume == null ? "" : " " + volume);
            displayString = displayString;
            String html = "<li><a href='" + href + "'>" + displayString + "</li>";
            return html;
            
            
        }
        
        /**
         * Assembles the anchor tag to the next level down in the hierarchy
         * 
         * @return An anchor tag
         */
        

        public String assembleLink(){
                       
            if(!this.isDocumentParent) return assembleLinkToCollection();
            return assembleLinkToFacetedBrowse();
            
        }
        
        private String assembleLinkToCollection(){
            StringBuilder href = new StringBuilder();
            href.append(CollectionBrowser.BROWSE_SERVLET);
            href.append("/");
            href.append(collection);
            href.append("/");
            href.append(FileUtils.stripDiacriticals(series));
            if (volume != null) {
              href.append("/");
              href.append(volume);
            }
            if(this.isDocumentParent) href.append("/documents/page1");
            return href.toString();                      
            
        }
        
        private String assembleLinkToFacetedBrowse(){
            
            String href = CollectionBrowser.FACET_SERVLET;
            href += "?";
            String collParam = IdentifierFacet.IdParam.SERIES.name();
            if(collection.equals("apis")) collParam = IdentifierFacet.IdParam.COLLECTION.name();
            String seriesRepresentation = (unicodeLabel != null && !unicodeLabel.equals("")) ? unicodeLabel : series;
            collParam += "=" + seriesRepresentation;
            href += collParam;
            if(volume != null){
                
                String volParam = IdentifierFacet.IdParam.VOLUME.name();
                volParam += "=" + volume;
                href += "&";
                href += volParam;
                
            }
            
            return href;
            
        }
        
        /**
         * HGV collection identifiers often substitute underscores for whitespace,
         * leading to problems in parsing. This fixes that.
         * 
         * @param scored The potentially problematic identifier
         * @return The identifier, with underscores removed
         */
        
        private String trimUnderscores(String scored){
          return scored.replaceAll("(^_|_$)", ""); 
        }
        
        public String getCollection() { return collection; }
        public String getSeries(){ return series; }
        public String getVolume(){ return (volume == null) ? "" : volume; }

        @Override
        public int compareTo(Object o) {
            
            DocumentCollectionBrowseRecord comparandum = (DocumentCollectionBrowseRecord)o;
            
            if(this.series.compareToIgnoreCase(comparandum.getSeries()) != 0) return this.series.compareToIgnoreCase(comparandum.getSeries());
            
            if(this.getVolume().equals("0")) return 0;
            String thisVolume = this.getVolume() != null ? this.getVolume() : "";
            String thatVolume = comparandum.getVolume() != null ? comparandum.getVolume() : "";

            thisVolume = thisVolume.replaceAll("[^\\d]", "");
            thatVolume = thatVolume.replaceAll("[^\\d]", "");

            if(thisVolume.equals("")) thisVolume = "0";
            if(thatVolume.equals("")) thatVolume = "0";

            Integer thisNo = Integer.parseInt(thisVolume);
            Integer thatNo = Integer.parseInt(thatVolume); 


            return thisNo - thatNo;
            
        }
        
    }