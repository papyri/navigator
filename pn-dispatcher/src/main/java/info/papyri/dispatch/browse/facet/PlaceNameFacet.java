package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;


/**
 * <code>Facet</code> for selection by ancient place name.
 *
 *
 * @author thill
 */
public class PlaceNameFacet extends Facet {

    public PlaceNameFacet(){

        super(SolrField.placename, FacetParam.PLACENAME, "Ancient Place Name");

    }


    @Override
    String getToolTipText() {

        return "Indicates the ancient name of the place associated with the text's provenance.";

    }



}
