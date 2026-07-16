package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Extends the <code>Facet</code> class to deal specifically with Boolean fields.
 *
 * This class reworks its superclass only very slightly. Because only two values are
 * possible, some code can be simplified. In addition, however, the 'false' value may
 * correspond to a number of values in relation to Solr - in particular, to null-
 * and additional checks have been added to cope appropriately with these.
 *
 *
 * @author thill
 */
abstract public class BooleanFacet extends Facet{

    public BooleanFacet(SolrField sf, FacetParam formName, String displayName){

        super(sf, formName, displayName);

    }

    @Override
    public void setWidgetValues(QueryResponse queryResponse){

        FacetField facetField = queryResponse.getFacetField(field.name());
        valuesAndCounts = new ArrayList<Count>();
        List<Count> unfiltered = facetField.getValues();
        Iterator<Count> cit = unfiltered.iterator();
        while(cit.hasNext()){

            Count count = cit.next();

            if(count.getCount() > 0) valuesAndCounts.add(count);

        }

    }

    @Override
    public SolrQuery buildQueryContribution(SolrQuery solrQuery){

        solrQuery.addFacetField(field.name());

        Iterator<String> cit = facetConstraints.iterator();

        while(cit.hasNext()){

            String constraint = cit.next();

            String queryField = field.name();
            // note the Solr syntax here, negating the condition as a whole,
            // rather than specifying it to be false
            if(!"true".equals(constraint)) queryField = "-" + queryField;
            constraint = queryField + ":true";

            solrQuery.addFilterQuery(constraint);

        }
        return solrQuery;

    }

    String generateHiddenFields(){

        String html = "";

        for(int i = 0; i < facetConstraints.size(); i++){

            String name = formName.name();
            String value = facetConstraints.get(i);
            if(value == null) value = "false";
            html += "<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>";

        }

        return html;

    }

    @Override
    public String generateWidget() {

        StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
        html.append(getToolTipText());
        html.append("\">");
        html.append("<p>");
        Boolean onlyOneValue = valuesAndCounts.size() == 1;
        String disabled = onlyOneValue ? " disabled=\"true\"" : "";
        String defaultSelected = onlyOneValue ? "" : "selected=\"true\"";

        html.append("<label class=\"form-label\" for=\"");
        html.append(this.getCSSSelector());
        html.append("\">");
        html.append(getDisplayName(null, null));
        html.append("</label>");

        // tooltip
        String tooltipText = getToolTipText();
        if (tooltipText != null && !tooltipText.isEmpty()) {
            html.append("<a href=\"#\" class=\"info\" data-bs-toggle=\"tooltip\" data-bs-title=\"");
            html.append(tooltipText);
            html.append("\">");
            html.append("<span class=\"visually-hidden\">More Information</span>");
            html.append("<span class=\"ms-1 bi bi-info-circle\"></span>");
            html.append("</a>");
        }

        html.append("<select");
        html.append(disabled);
        html.append(" name=\"");
        html.append(formName.name());
        html.append("\" id=\"");
        html.append(this.getCSSSelector());
        html.append("\" class=\"form-select\">");
        html.append("<option ");
        html.append(defaultSelected);
        html.append(" value=\"\">");
        html.append(Facet.defaultValue);
        html.append("</option>");
        Iterator<Count> vcit = valuesAndCounts.iterator();

        while(vcit.hasNext()){

            Count valueAndCount = vcit.next();
            String value = valueAndCount.getName();
            if(value == null) value = "false";
            String displayValue = getDisplayValue(value);
            String count = String.valueOf(valueAndCount.getCount());
            String selected = onlyOneValue ? " selected=\"true\"" : "";
            html.append("<option");
            html.append(selected);
            html.append(" value =\"");
            html.append(value);
            html.append("\">");
            html.append(displayValue);
            html.append(" (");
            html.append(count);
            html.append(")</option>");
        }

        html.append("</select>");
        html.append("</p>");
        html.append("</div><!-- closing .facet-widget -->");
        html.append(generateHiddenFields());
        return html.toString();


    }

}
