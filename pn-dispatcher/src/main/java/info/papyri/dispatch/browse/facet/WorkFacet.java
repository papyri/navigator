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
public class WorkFacet extends Facet {

    public WorkFacet() {
        super(SolrField.work_str, FacetParam.WORK, "Work");
    }

    @Override
    String getToolTipText() {
        return "Select documents by work.";
    }
    
}
