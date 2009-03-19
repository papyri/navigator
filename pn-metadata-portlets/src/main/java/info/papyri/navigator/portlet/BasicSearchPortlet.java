package info.papyri.navigator.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;

import info.papyri.metadata.*;

public class BasicSearchPortlet extends GenericPortlet {
    private static final String ALL_PUB = "*:*:*";
    private static final String ALL_INV = "* *";

    protected void doView(RenderRequest arg0, RenderResponse arg1) throws PortletException, IOException {
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher("/WEB-INF/basicSearch.jsp");
        rd.include(arg0, arg1);
        if (true) return;
        BooleanQuery query = new BooleanQuery();
        addPublicationClause(query,arg0);
        addInventoryClause(query,arg0);
        addKeywordClause(query, arg0);
        if (query.getClauses().length > 0){
            //@TODO run search
            rd = getPortletContext().getRequestDispatcher("/WEB-INF/basicResult.jsp");
            rd.include(arg0, arg1);
        }
    }
    
    private static void addPublicationClause(BooleanQuery query, RenderRequest req){
        String series = req.getParameter("pub:series");
        String volume = req.getParameter("pub:volume");
        String number = req.getParameter("pub:number");
        if (series == null || series.equals("")) series = "*";
        if (volume == null || volume.equals("")) volume = "*";
        if (number == null || number.equals("")) number = "*";
        String publication = series.trim() + ":" + volume.trim() + ":" + number.trim();
        if (!ALL_PUB.equals(publication)){
            Query sub = (publication.indexOf('*') == -1 && publication.indexOf('?') == -1)?(new TermQuery(new Term(CoreMetadataFields.BIBL_PUB,publication))):(new WildcardQuery(new Term(CoreMetadataFields.BIBL_PUB,publication)));
            BooleanClause clause = new BooleanClause(sub,BooleanClause.Occur.MUST);
            query.add(clause);
        }
    }
    
    private static void addInventoryClause(BooleanQuery query, RenderRequest req){
        String coll = req.getParameter("inv:collection");
        String number = req.getParameter("inv:number");
        if (coll == null || coll.equals("")) coll = "*";
        if (number == null || number.equals("")) number = "*";
        String inv = coll.trim() + " " + number.trim();
        if (!ALL_INV.equals(inv)){
            Query sub = (inv.indexOf('*') == -1 && inv.indexOf('?') == -1)?(new TermQuery(new Term(CoreMetadataFields.INV,inv))):(new WildcardQuery(new Term(CoreMetadataFields.INV,inv)));
            BooleanClause clause = new BooleanClause(sub,BooleanClause.Occur.MUST);
            query.add(clause);
        }
    }
    
    private static void addKeywordClause(BooleanQuery query, RenderRequest req){
        String keyParm = req.getParameter("keywords").trim();
        if (keyParm == null || "".equals(keyParm)) return;
        String [] keys = keyParm.split("\\s+");
        if (keys.length == 0) return;
        for (int i=0;i<keys.length;i++){
            //@TODO decide whether is an inclusion or exclusion
        }
        
        
    }

}
