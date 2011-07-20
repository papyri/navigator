package info.papyri.dispatch.browse;

    /* Names of fields indexed in Solr. Note that some may have the collectionPrefix prepended to 
     * them in actual use.
     * 
     * @author: thill 
     */
public enum SolrField {
   
        collection,
        series,
        identifier,
        volume,
        item,
        display_place,
        display_date,
        has_translation,
        language,
        hgv_identifier,
        images,
        invnum,
        facet_language,
        full_identifier,
        item_letter,
        transcription,
        translations,
        date_category,
        unknown_date_flag,
        transcription_ngram_ia
    
}
