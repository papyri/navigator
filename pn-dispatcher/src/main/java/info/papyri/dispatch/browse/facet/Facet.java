package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;
import info.papyri.dispatch.browse.facet.customexceptions.CustomApplicationException;
import info.papyri.dispatch.ServletUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Handles all the necessary backend (Solr) and frontend (HTML) interactions
 * needed for selection of constraint values within a single facet.
 *
 *
 * @author thill
 */
abstract public class Facet {

  /**
   * A
   * <code>List</code> of values to which the Solr responses must (possibly
   * after processing by this class) conform
   */
  ArrayList<String> facetConstraints = new ArrayList<>();
  /**
   * A
   * <code>List</code> of all values fond in the faceted field, along with the
   * number of each.

   * Note the peculiarity of Solr terminology here: a
   * <code>Count</code> object is actually a member of a
   * <code>FacetField</code>, and holds information both on the
   * <code>String</code> representation of the value, and the number associated
   * with it (i.e., the "count" in the normal sense of the word)
   */
  List<Count> valuesAndCounts;
  /**
   * The relevant Solr field
   */
  SolrField field;
  /**
   * The value used for the
   * <code>name</code> attribute in the
   * <code>Facet</code>'s HTML control.
   *
   * @see FacetParam
   */
  FacetParam formName;
  /**
   * The label displayed to the user
   */
  String displayName;
  /**
   * Default value to be displayed if no value set

   * Note that this value is only applicable for drop-down selectors; some
   * subclasses may require a different default to be specified
   */
  static String defaultValue = "--- All values ---";
  private static final Logger logger = Logger.getLogger("pn-dispatch");

  /**
   * Constructor
   *
   * @param sf The relevant Solr field
   * @param formName The value used for the <code>name</code> attribute in the <code>Facet</code>'s HTML control
   * @param displayName The label displayed to the user
   */
  public Facet(SolrField sf, FacetParam formName, String displayName) {

    this.field = sf;
    this.formName = formName;
    this.displayName = displayName;
    valuesAndCounts = new ArrayList<>();
  }

  /**
   * Modifies the passed
   * <code>SolrQuery</code> to reflect the constraints set upon, and faceting
   * information required by, the
   * <code>Facet</code>
   *
   * @param solrQuery The Solr query to be modified
   * @return The passed solrQuery, modified
   */
  public SolrQuery buildQueryContribution(SolrQuery solrQuery) {

    solrQuery.addFacetField(field.name());
    solrQuery.setFacetLimit(-1);                // = no limit

    for (String fq : facetConstraints) {

      // slash-escape madness: java, solr, and java.regex all use backslash
      // as an escape character
      fq = fq.replaceAll("\\\\", "\\\\\\\\");
      if (fq.contains(" ")) {
        fq = "\"" + fq + "\"";
      }
      fq = field.name() + ":" + fq;
      solrQuery.addFilterQuery(fq);


    }

    return solrQuery;

  }

  /**
   * Generates the HTML form element used for input.
   *
   * @return A string representation of the requisite HTML
   * java.lang.StringBuilder, java.util.Map
   */
  public String generateWidget() {

    StringBuilder html = new StringBuilder("<div class=\"facet-widget\" title=\"");
    html.append(getToolTipText());
    html.append("\" id=\"");
    html.append(formName.name().toLowerCase());
    html.append("-selector");
    html.append("\">");
    html.append(generateHiddenFields());
    html.append("<p>");
    // if only one value possible, then gray out control
    boolean onlyOneValue = valuesAndCounts.size() == 1;
    boolean allSelected = facetConstraints.size() == valuesAndCounts.size();
    String disabled = (onlyOneValue || allSelected) ? " disabled=\"true\"" : "";
    String defaultSelected = (onlyOneValue || allSelected) ? "" : "selected=\"true\"";

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
    html.append("\" class=\"form-select\"");
    html.append(" placeholder=\"");
    html.append(Facet.defaultValue);
    html.append("\">");
    html.append("<option ");
    html.append(defaultSelected);
    html.append(" value=\"\">");
    html.append(Facet.defaultValue);
    html.append("</option>");

    boolean oneConstraintSet = facetConstraints.size() == 1;

    for (Count valueAndCount : valuesAndCounts) {

      String value = valueAndCount.getName();
      String displayValue = getDisplayValue(value);
      // truncate if too long; otherwise control potentially takes up the whole screen
      if (displayValue.length() > 35) {
        displayValue = displayValue.substring(0, 35);
      }
      String count = String.valueOf(valueAndCount.getCount());
      String selected = onlyOneValue || (oneConstraintSet && value.equals(facetConstraints.get(0))) ? " selected=\"true\"" : "";
      html.append("<option");
      html.append(selected);
      html.append(" value=\"");
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

    return html.toString();

  }

  /**
   * Returns the
   * <code>Facet</code>'s constraints as a query string.

   * Required for pagination links to maintain state across pages.
   *
   * @return A querystring representing a <code>Facet</code>s constraints.
   */
  public String getAsQueryString() {

    StringBuilder queryString = new StringBuilder();

    Iterator<String> cit = facetConstraints.iterator();
    while (cit.hasNext()) {

      String value = cit.next();
      queryString.append(formName.name()).append("=").append(value);
      if (cit.hasNext()) {
        queryString.append("&");
      }


    }

    return queryString.toString();

  }

  /**
   * Returns the
   * <code>Facet</code>'s constraints as a query string, minus the value passed
   * to the method,

   * Required for the anchor links that (from the user's perspective) 'remove'
   * constraints from the faceted display.
   *
   * @return A querystring representing the <code>Facet</code>'s constraints,
   * excluding the value passed as a <code>String</code>.
   * java.lang.StringBuilder, java.util.Map
   */
  public String getAsFilteredQueryString(String filterParam, String filterValue) {

    StringBuilder queryString = new StringBuilder();

    for (String value : facetConstraints) {

      if (!value.equals(filterValue)) {

        queryString.append(formName.name()).append("=").append(value);
        queryString.append("&");

      }

    }

    if (!queryString.toString().isEmpty() && queryString.substring(queryString.length() - 1).equals("&")) {
      queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1));
    }
    return queryString.toString();

  }

  /**
   * Sets the values to be displayed by the
   * <code>Facet</code>'s HTML form control.
   *
   *
   * @param queryResponse The Solr response from which the values and counts are to be extracted
   * java.lang.StringBuilder, java.util.Map
   */
  public void setWidgetValues(QueryResponse queryResponse) {

    FacetField facetField = queryResponse.getFacetField(field.name());
    valuesAndCounts = new ArrayList<>();
    List<Count> unfiltered = facetField.getValues();
    for (Count count : unfiltered) {

      if (count.getName() != null && !count.getName().isEmpty() && count.getCount() > 0 && !count.getName().equals("null")) {
        valuesAndCounts.add(count);
      }

    }

  }

  /**
   * Generates hidden fields for previously-selected constraints on the
   * <code>Facet</code>.
   *
   *
   * @return The HTML for the hidden fields, as a <code>String</code>.
   */
  String generateHiddenFields() {

    StringBuilder html = new StringBuilder();

    for (String facetConstraint : facetConstraints) {

      String name = formName.name();
      html.append("<input type=\"hidden\" name=\"").append(name).append("\" value=\"").append(facetConstraint).append("\"/>");

    }

    return html.toString();

  }

  /**
   * Parses the parameters submitted in the
   * <code>HttpServletRequest</code> and stores those relevant to the
   * <code>Facet</code> in question
   *
   * @param params The parameters submitted in the <code>HttpServletRequest</code>, as a
   * @return A <code>Boolean</code> indicating whether a constraint
   * exists on the current <code>Facet</code>.
   */
  public Boolean addConstraints(Map<String, String[]> params) {

    boolean hasConstraint = false;

    if (params.containsKey(this.formName.name())) {

      String[] values = params.get(formName.name());

      for (String value : values) {

        String param = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);

        if (param != null && !param.equals("default") && !param.isEmpty()
            && !param.contains("<") && !param.contains(">") && !param.contains(";")) {
          addConstraint(param);
          hasConstraint = true;
        }


      }

    }

    return hasConstraint;

  }

  /**
   * Parses each value submitted to the
   * <code>Facet</code>.

   * The chief purpose of the method as defined here (i.e., in the superclass)
   * is to weed out default values and prevent them being used as constraints.
   * Subclasses with idiosyncratic values may have considerably more complex
   * behavior.
   *
   * @param newValue The value to be parsed and added as a constraint, if appropriate
   * java.lang.StringBuilder, java.util.Map
   */
  void addConstraint(String newValue) {

    if (newValue.equals(Facet.defaultValue)) {
      return;
    }
    if (!facetConstraints.contains(newValue)) {
      facetConstraints.add(newValue.trim());
    }


  }

  /**
   * Returns the value(s) to be used for the name attribute on HTML form
   * controls.

   * In most cases only one value is required, and will be that of the
   * <code>formName</code> member. However, some facets have more than one HTML
   * control - hence the need for this method to return an array of Strings,
   * rather than a String.
   *
   * @return An array of Strings representing the value(s) to be used for the name
   */
  public String[] getFormNames() {

    return new String[]{formName.name()};

  }

  /**
   * Returns the
   * <code>formName</code> member in lower case, to be used as an id value for
   * the HTML form control
   *
   * @return A String representing the value to be used for the id attribute of the HTML form control
   * java.lang.StringBuilder, java.util.Map
   */
  public String getCSSSelector() {

    return "id-" + this.formName.name().toLowerCase();

  }

  /**
   * Takes a raw facet value and formats it appropriately for display in the
   * facet's HTML form control.

   * Under most circumstances, the passed value itself will be appropriate for
   * display; some subclasses, however, may need to override this method to cope
   * with particular values requiring special treatment.
   *
   * @param value The raw facet value, as returned by Solr
   * @return A String representing the value to be displayed in the HTML form control
    * java.lang.StringBuilder, java.util.Map
   */
  public String getDisplayValue(String value) {

    return ServletUtils.scrub(value);

  }

  /* getters and setters below */
  public SolrField getFacetField() {

    return field;

  }

  public ArrayList<String> getFacetConstraints(String facetParam) {

    return facetConstraints;

  }

  public ArrayList<CustomApplicationException> getExceptionLog() {

    return new ArrayList<>();

  }

  public String getDisplayName(String facetParam, java.lang.String facetValue) {

    return displayName;

  }

  abstract String getToolTipText();
}
