package info.papyri.dispatch.atom;

/**
 * Optional change-type filters
 * 
 * @author thill
 */
    
public enum SearchType{
        
        appchange   (SolrField.app_edit_date, SolrField.app_editor),
        datechange  (SolrField.date_edit_date, SolrField.date_editor),
        placechange (SolrField.place_edit_date, SolrField.place_editor), 
        other       (SolrField.edit_date, SolrField.last_editor);
        
        private final SolrField dateField;
        private final SolrField editorField;
        
        SearchType(SolrField df, SolrField ef){
            
            dateField = df;
            editorField = ef;
            
        }
        
        public SolrField getDateField(){ return dateField; }
        public SolrField getEditorField() { return editorField; }
        
}
