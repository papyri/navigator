package info.papyri.dispatch.browse;

    /* Names of fields indexed in Solr. Note that some may have the collectionPrefix prepended to 
     * them in actual use.
     * 
     * @author: thill 
     */
public enum SolrField {
   
        all,
        collection,
        series,
        identifier,
        volume,
        item,
        display_place,
        display_date,
        has_translation,
        translation_language,
        language,
        hgv_identifier,
        images,
        images_int,
        images_ext,
        image_path,
        illustrations,
        invnum,
        facet_language,
        full_identifier,
        item_letter,
        has_transcription,
        translations,
        date_category,
        unknown_date_flag,
        transcription_ngram_ia,
        transcription,
        transcription_ia,
        metadata,
        translation,
        transcription_ic,
        transcription_id,
        id,
        apis_inventory,
        apis_publication_id,
        ddbdp_series,
        hgv_series,
        apis_series,
        ddbdp_volume,
        hgv_volume,
        ddbdp_full_identifier,
        hgv_full_identifier,
        apis_full_identifier,
        series_led_path,
        volume_led_path,
        idno_led_path,
        apis_title
        
}
