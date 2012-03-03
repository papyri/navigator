package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;


/**
 * <code>Facet</code> for selection by provenance.
 * 
 * 
 * @author thill
 */
public class PlaceFacet extends Facet {
    
    public PlaceFacet(){
        
        super(SolrField.display_place, FacetParam.PLACE, "Provenance");
        
    }
    
 /*    @Override
     public String generateWidget() {
        
        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" id=\"place-search-widget\" title=\"");
        html.append(getToolTipText());                                          
        html.append("\">");
        html.append(generateHiddenFields()); 
        html.append("<p>");
        // if only one value possible, then gray out control
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        Boolean allSelected = facetConstraints.size() == valuesAndCounts.size();
        Boolean disabled = (onlyOneValue || allSelected);             
        Boolean defaultSelected = !(onlyOneValue || allSelected);
        html.append("<label for=\"");
        html.append(formName.name());
        html.append("\">");
        html.append(this.getDisplayName(null, null));
        html.append("</label>");
        html.append("<input type=\"text\" length=\"35\" maxlength=\"100\" name=\"");
        html.append(formName.name());
        html.append("\" id=\"");
        html.append("id-");
        html.append(formName.name().toLowerCase());
        html.append("\"/>");
        html.append("<div class=\"autocomplete-values\" id=\"place-autocomplete\">");
        
        Boolean oneConstraintSet = facetConstraints.size() == 1;
                
        Iterator<Count> vcit = valuesAndCounts.iterator();
        
        while(vcit.hasNext()){
            
            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            String displayValue = getDisplayValue(value);
            html.append(displayValue);
            html.append("¤");
            
        }
        
        html.append("</div><!-- closing .autocomplete-values -->");
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");

        return html.toString();
        
    }*/
    
    @Override
    String getToolTipText() {
        
        return "Indicates the place where the text was produced, as far as can be determined. Often this will correspond to the findspot of the document.";
        
    }


    
}
