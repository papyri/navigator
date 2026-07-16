package info.papyri.dispatch.browse;

    
   /**
     * <code>BrowseRecord</code>s store the salient characteristics of  documents retrieved from solr.
     * 
     * Which characteristics are salient will depend upon whether the user is viewing at the document
     * or document-collection level
     * 
     * @author: thill
     * 
     */
    
    public abstract class BrowseRecord implements Comparable<BrowseRecord> {
        
        /**
         * Returns an html representation of the Record.
         * 
         * This method is required so that large groups of records can be iterated over 
         * and displayed automatically.
         * 
         * @return a string of html
         */
        abstract public String getHTML();
        
        /**
         * Returns a string representation of the record, for use in debugging and testing.
         *
         * @return a string representation of the record
         */
        @Override
        abstract public String toString();
        
    }