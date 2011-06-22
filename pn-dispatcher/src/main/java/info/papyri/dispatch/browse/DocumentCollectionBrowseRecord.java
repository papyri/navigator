package info.papyri.dispatch.browse; 

/**
     * <code>DocumentCollectionBrowseRecord</code>s are records of all information necessary to identify a <i>collection</i>
     * of documents.
     * 
     * Minimally, this will be collections and series. Maximally this will be collection, series, and volume.
     * 
     * 
     */
    
    public class DocumentCollectionBrowseRecord extends BrowseRecord{
        
        private String collection;
        private String series;
        private String volume;
        private Boolean isDocumentParent;
    
        public DocumentCollectionBrowseRecord(String collection, String series, String volume){
            
            this.collection = collection;
            this.series = series;
            this.volume = volume;
            this.isDocumentParent = true;
            
        }
        
        public DocumentCollectionBrowseRecord(String collection, String series, Boolean isParent){
            
            this.collection = collection;
            this.series = series;
            this.isDocumentParent = isParent;
            this.volume = null;
            
        }
        
        
        @Override
        public String getHTML(){
            
            String href = assembleLink();
            String displayString = series + (volume == null ? "" : " " + volume);
            if(displayString.equals("0")) return "";    // TODO: Work out why zero-records result and fix bodge if necessary
            String html = "<li><a href='" + href + "'>" + series + " " + (volume == null ? "" : volume) + "</li>";
            return html;
            
            
        }
        
        @Override
        public String assembleLink(){
            
            String href = CollectionBrowser.BROWSE_SERVLET + "/" + collection;
            
            String seriesIdent = "/" + series.replaceAll("_", "");
            String volumeIdent = volume == null ? "" : "/" + volume;
            href += seriesIdent + volumeIdent;
            href = href.replaceAll("\\s", "");
            if(this.isDocumentParent) href += "/documents/page1";
            return href;
            
        }
        
        
        public String getCollection() { return collection; }
        public String getSeries(){ return series; }
        public String getVolume(){ return (volume == null) ? "" : volume; }

        @Override
        public int compareTo(Object o) {
            
            DocumentCollectionBrowseRecord comparandum = (DocumentCollectionBrowseRecord)o;
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