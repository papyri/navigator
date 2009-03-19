package info.papyri.index;
import java.util.HashMap;
import info.papyri.metadata.*;
public abstract class DisplayFields {
    public static final String [] apisFields = new String[]{
            CoreMetadataFields.TITLE, CoreMetadataFields.BIBL_PUB, CoreMetadataFields.SUMMARY,
            CoreMetadataFields.INV, CoreMetadataFields.PHYS_DESC, CoreMetadataFields.PROVENANCE_NOTE,
            CoreMetadataFields.LANG, CoreMetadataFields.MATERIAL, CoreMetadataFields.BIBL_ILLUSTR, CoreMetadataFields.BIBL_CORR,
            CoreMetadataFields.GEN_NOTES, CoreMetadataFields.SUBJECT_I, CoreMetadataFields.NAME_ASSOC
    };
    public static final String [] apisLabels = new String[]{
            "Title", "Publication", "Summary",
            "Inv. Id", "Physical Desc.", "Provenance",
            "Language", "Material", "Notes (Illustrations)", "Notes (Corrections)", 
            "Notes (General)", "Subject(s)", "Associated Name(s)"
    };
    public static final String [] apisDebugFields = new String[]{
        CoreMetadataFields.DATE1_I,
        CoreMetadataFields.DATE2_I,
            CoreMetadataFields.SORT_HAS_IMG,
            CoreMetadataFields.SORT_HAS_TRANS,
            CoreMetadataFields.INDEXED_SERIES,
            CoreMetadataFields.PROVENANCE,
            CoreMetadataFields.XREFS
    };
    public static final String [] apisDebugLabels = new String[]{
            "Date 1 Index",
            "Date 2 Index",
            "Image Flag",
            "Translation Flag",
            "Indexed Series",
            "Indexed Provenance",
            "XREFS"
    };
    public static final String [] hgvFields = new String[]{
        CoreMetadataFields.TITLE, CoreMetadataFields.BIBL_PUB, CoreMetadataFields.BIBL_ILLUSTR, CoreMetadataFields.SUMMARY,
        CoreMetadataFields.INV, CoreMetadataFields.PHYS_DESC, CoreMetadataFields.PROVENANCE_NOTE,
        CoreMetadataFields.LANG, CoreMetadataFields.MATERIAL,
        CoreMetadataFields.GEN_NOTES, CoreMetadataFields.BIBL_TRANS, CoreMetadataFields.SUBJECT_I, CoreMetadataFields.NAME_ASSOC
    };
    public static final String [] hgvLabels = new String[]{
            "Title", "Publication", "Notes (Illustrations)", "Summary",
            "Inv. Id", "Physical Desc.", "Provenance",
            "Language", "Material", 
            "Notes (General)", "Translations","Subject(s)", "Associated Name(s)"
    };
    public static final String [] hgvDebugFields = new String[]{
        CoreMetadataFields.DATE1_I,
        CoreMetadataFields.DATE2_I,
        CoreMetadataFields.SORT_HAS_IMG,
        CoreMetadataFields.SORT_HAS_TRANS,
        CoreMetadataFields.INDEXED_SERIES,
        CoreMetadataFields.PROVENANCE,
        CoreMetadataFields.XREFS
    };
    public static final String [] hgvDebugLabels = new String[]{
            "Date 1 Index",
            "Date 2 Index",
            "Image Flag",
            "Translation Flag",
            "Indexed Series",
            "Indexed Provenance",
            "XREFS"
    };}
