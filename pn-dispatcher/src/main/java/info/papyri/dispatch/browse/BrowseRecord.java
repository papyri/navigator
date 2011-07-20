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
    
    public abstract class BrowseRecord implements Comparable{
        
        /**
         * Returns an html representation of the Record 
         * 
         * @return a string of html
         */
        abstract public String getHTML();
        

        
    }