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
        
        /**
         * Returns the relative path to be associated with the Record
         * 
         * @return the relative path as a String
         */
        abstract public String assembleLink();
        
        /**
         * Returns a tag or tags opening the appropriate html element(s) that wraps the list of <code>Record</code>s 
         * 
         * @return An html opening tag or tags
         */

        
    }