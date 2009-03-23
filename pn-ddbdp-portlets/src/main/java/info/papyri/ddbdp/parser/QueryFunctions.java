package info.papyri.ddbdp.parser;

public interface QueryFunctions {
    static final String FUNC_DOCS = "docs";
    static final String FUNC_TERM = "term";
    static final String FUNC_SUBSTRING = "sub";
    static final String FUNC_NEAR = "near";
    static final String FUNC_THEN = "then";
    static final String FUNC_NOTNEAR = "notnear";
    static final String FUNC_AND = "and";
    static final String FUNC_OR = "or";
    static final String FUNC_NOT = "not";
    static final String FUNC_BETA = "beta";
    static final String CONST_IGNORE_ALL = "IA";
    static final String CONST_IGNORE_MARKS = "IM";
    static final String CONST_IGNORE_CAPS = "IC";
    static final String CONST_LEMMAS = "LEMMAS";
    static final int MODE_NONE = 0;
    static final int MODE_FILTER_CAPITALS = 1;
    static final int MODE_FILTER_DIACRITIC = 2;
    static final int MODE_FILTER_CAPITALS_AND_DIACRITICS = MODE_FILTER_CAPITALS + MODE_FILTER_DIACRITIC;
    static final int MODE_BETA = 4;
    static final int MODE_BETA_FILTER_CAPITALS = MODE_BETA + MODE_FILTER_CAPITALS;
    static final int MODE_BETA_FILTER_DIACRITICS = MODE_BETA + MODE_FILTER_DIACRITIC; 
    static final int MODE_BETA_FILTER_ALL = MODE_BETA + MODE_FILTER_CAPITALS + MODE_FILTER_DIACRITIC;
    static final int MODE_LEMMAS = 8;
}
