/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch.browse.facet;

import info.papyri.dispatch.browse.SolrField;

/**
 *
 * @author hac13
 */
public class AuthorFacet extends Facet {

    public AuthorFacet() {
        super(SolrField.author_str, FacetParam.AUTHOR, "Author");
    }

    @Override
    String getToolTipText() {
        return "Select documents by author/text group.";
    }
    
}
