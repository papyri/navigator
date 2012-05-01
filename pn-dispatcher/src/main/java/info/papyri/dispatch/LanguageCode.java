package info.papyri.dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Maps BCP 47 language tags to expanded values for display
 * 
 * 
 * @author thill
 * @see http://people.w3.org/rishida/utils/subtags/
 */
public enum LanguageCode {
    
    aa("Afar"),
    ab("Abkhazian"),
    ae("Avestan"),
    af("Afrikaans"),
    ak("Akan"),
    am("Amharic"),
    an("Aragonese"),
    ar("Arabic"),
    as("Assamese"),
    av("Avaric"),
    ay("Aymara"),
    az("Azerbaijani"),
    ba("Bashkir"),
    be("Belarusian"),
    bg("Bulgarian"),
    bh("Bihari languages"),
    bi("Bislama"),
    bm("Bambara"),
    bn("Bengali"),
    bo("Tibetan"),
    br("Breton"),
    bs("Bosnian"),
    ca("Catalan, Valencian"),
    ce("Chechen"),
    ch("Chamorro"),
    co("Corsican"),
    cr("Cree"),
    cs("Czech"),
    cu("Church Slavic, Church Slavonic, Old Bulgarian, Old Church Slavonic, Old Slavonic"),
    cv("Chuvash"),
    cy("Welsh"),
    da("Danish"),
    de("German"),
    dv("Dhivehi, Divehi, Maldivian"),
    dz("Dzongkha"),
    ee("Ewe"),
    el("Modern Greek"),
    en("English"),
    eo("Esperanto"),
    es("Spanish, Castilian"),
    et("Estonian"),
    eu("Basque"),
    fa("Persian"),
    ff("Fulah"),
    fi("Finnish"),
    fj("Fijian"),
    fo("Faroese"),
    fr("French"),
    fy("Western Frisian"),
    ga("Irish"),
    gd("Scottish Gaelic, Gaelic"),
    gl("Galician"),
    gn("Guarani"),
    gu("Gujarati"),
    gv("Manx"),
    ha("Hausa"),
    he("Hebrew"),
    hi("Hindi"),
    ho("Hiri Motu"),
    hr("Croatian"),
    ht("Haitian, Haitian Creole"),
    hu("Hungarian"),
    hy("Armenian"),
    hz("Herero"),
    ia("Interlingua (International Auxiliary Language"),
    id("Indonesian"),
    ie("Interlingue, Occidental"),
    ig("Igbo"),
    ii("Sichuan Yi, Nuosu"),
    ik("Inupiaq"),
    in("Indonesian"),
    io("Ido"),
    is("Icelandic"),
    it("Italian"),
    iu("Inuktitut"),
    iw("Hebrew"),
    ja("Japanese"),
    ji("Yiddish"),
    jv("Javanese"),
    jw("Javanese"),
    ka("Georgian"),
    kg("Kongo"),
    ki("Kikuyu, Gikuyu"),
    kj("Kuanyama, Kwanyama"),
    kk("Kazakh"),
    kl("Kalaallisut, Greenlandic"),
    km("Central Khmer"),
    kn("Kannada"),
    ko("Korean"),
    kr("Kanuri"),
    ks("Kashmiri"),
    ku("Kurdish"),
    kv("Komi"),
    kw("Cornish"),
    ky("Kirghiz, Kyrgyz"),
    la("Latin"),
    lb("Luxembourgish, Letzeburgesch"),
    lg("Ganda"),
    li("Limburgan, Limburger, Limburgish"),
    ln("Lingala"),
    lo("Lao"),
    lt("Lithuanian"),
    lu("Luba-Katanga"),
    lv("Latvian"),
    mg("Malagasy"),
    mh("Marshallese"),
    mi("Maori"),
    mk("Macedonian"),
    ml("Malayalam"),
    mn("Mongolian"),
    mo("Moldavian, Moldovan"),
    mr("Marathi"),
    ms("Malay (macrolanguage)"),
    mt("Maltese"),
    my("Burmese"),
    na("Nauru"),
    nb("Norwegian Bokmål"),
    nd("North Ndebele"),
    ne("Nepali"),
    ng("Ndonga"),
    nl("Dutch, Flemish"),
    nn("Norwegian Nynorsk"),
    no("Norwegian"),
    nr("South Ndebele"),
    nv("Navajo, Navaho"),
    ny("Nyanja, Chewa, Chichewa"),
    oc("Occitan (post 1500)"),
    oj("Ojibwa"),
    om("Oromo"),
    or("Oriya"),
    os("Ossetian, Ossetic"),
    pa("Panjabi, Punjabi"),
    pi("Pali"),
    pl("Polish"),
    ps("Pushto, Pashto"),
    pt("Portuguese"),
    qu("Quechua"),
    rm("Romansh"),
    rn("Rundi"),
    ro("Romanian, Moldavian, Moldovan"),
    ru("Russian"),
    rw("Kinyarwanda"),
    sa("Sanskrit"),
    sc("Sardinian"),
    sd("Sindhi"),
    se("Northern Sami"),
    sg("Sango"),
    sh("Serbo-Croatian"),
    si("Sinhala, Sinhalese"),
    sk("Slovak"),
    sl("Slovenian"),
    sm("Samoan"),
    sn("Shona"),
    so("Somali"),
    sq("Albanian"),
    sr("Serbian"),
    ss("Swati"),
    st("Southern Sotho"),
    su("Sundanese"),
    sv("Swedish"),
    sw("Swahili (macrolanguage)"),
    ta("Tamil"),
    te("Telugu"),
    tg("Tajik"),
    th("Thai"),
    ti("Tigrinya"),
    tk("Turkmen"),
    tl("Tagalog"),
    tn("Tswana"),
    to("Tonga (Tonga Islands)"),
    tr("Turkish"),
    ts("Tsonga"),
    tt("Tatar"),
    tw("Twi"),
    ty("Tahitian"),
    ug("Uighur, Uyghur"),
    uk("Ukrainian"),
    ur("Urdu"),
    uz("Uzbek"),
    ve("Venda"),
    vi("Vietnamese"),
    vo("Volapük"),
    wa("Walloon"),
    wo("Wolof"),
    xh("Xhosa"),
    yi("Yiddish"),
    yo("Yoruba"),
    za("Zhuang, Chuang"),
    zh("Chinese"),
    zu("Zulu"),
    grc("Ancient Greek"),
    egy_Copt("Egyptian\\Coptic"),
    grc_Latn("Ancient Greek in Latin script"),
    la_Grek("Latin in Greek script"),
    cop("Coptic"),
    egy_Coptgrc("Greek(?) Egyptian\\Coptic(?)"),
    ar_Arabegy_Copt("(A) Egyptian\\Coptic (B) Arabic; Egyptian\\Coptic (?)"),
    egy_Egyd("Egyptian - Demotic script"),
    ar_Arab("Arabic - Arabic script"),
    egy_Egyh("Egyptian - Hieratic script"),
    egy_Egyp("Egyptian - Hieroglyphic script"),
    egy_Egydgrc("Egyptian - Demotic script\\Ancient Greek"),
    und("Undetermined"),
    ar_Arabgrc("Arabic - Arabic script\\Ancient Greek"),
    grcla("Ancient Greek\\Latin"),
    he_Hebr("Hebrew - Hebrew script"),
    egy_Egydegy_Egyh("Egyptian - Demotic script\\Egyptian - Hieratic script"),
    egy_Egyhegy_Egyp("Egyptian - Hieratic script\\Egyptian - Hieroglyphic script"),
    egy_Egydegy_Egyp("Egyptian - Demotic script\\Egyptian - Hieroglyphic script"),
    ar_Arabegy_Coptgrc("Arabic - Arabic script\\Egyptian - Coptic\\Ancient Greek"),
    sem("Semitic language"),
    arc("Aramaic"),
    faspal_Phil("Persian - Pahlavi script"),
    faspal_Phli("Persian - Pahlavi script"),
    grcegy_Egyhegy_Egyp("Ancient Greek\\Egyptian - Hieratic script\\Egyptian - Hieroglyphic script"),
    ar_Arabegy_Egyd("Arabic - Arabic script\\Egyptian - Demotic script"),
    egy("Egypian"),
    egy_Coptegy_Egydegy_Egyh("Egyptian - Coptic\\Egyptian - Demotic script\\Egyptian - Hieratic script"),
    egy_Coptgrcund("Egyptian - Coptic\\Ancient Greek\\Undetermined"),
    egy_Copthe_Hebr("Egyptian - Coptic\\Hebrew - Hebrew script"),
    egy_Egydgrcegy_Egyh("Egyptian - Demotic script\\Ancient Greek\\Egyptian - Hieratic script"),
    fas("Persian"),
    grcegy_Egyh("Ancient Greek\\Egyptian - Hieratic script"),
    xpr_Prti("Parthian");
    
    private String expanded;
    public static ArrayList<String> modernLanguages = new ArrayList<String>(Arrays.asList("English", "German", "French"));
    public static ArrayList<String> modernLanguageCodes = new ArrayList<String>(Arrays.asList("en", "de", "fr"));
    
    LanguageCode(String ex){
        
        expanded = ex;
        
    }
    
    public String expanded(){
        
        return expanded;
        
    }
    
    public static String filterModernLanguages(String rawString){
        
        
        String[] passedLanguages = rawString.split(",");
        ArrayList<String> filteredLanguages = new ArrayList<String>();
        String filtered = "";
        
        for(int i = 0; i < passedLanguages.length; i++){
            
            String passedLanguage = passedLanguages[i].trim();
            if(!modernLanguages.contains(passedLanguage)) filteredLanguages.add(passedLanguage);           
            
        }
        
        Iterator<String> flit = filteredLanguages.iterator();
        while(flit.hasNext()){
            
            filtered += flit.next();
            if(flit.hasNext()) filtered += ", ";
                
        }
        
        return filtered;
        
    }
    
    public static String filterModernLanguageCodes(String rawString){
        
        String[] passedLanguages = rawString.split(",");
        ArrayList<String> filteredLanguages = new ArrayList<String>();
        String filtered = "";
        
        for(int i = 0; i < passedLanguages.length; i++){
            
            String passedLanguage = passedLanguages[i].trim();
            if(!modernLanguageCodes.contains(passedLanguage)) filteredLanguages.add(passedLanguage);           
            
        }
        
        Iterator<String> flit = filteredLanguages.iterator();
        while(flit.hasNext()){
            
            filtered += flit.next();
            if(flit.hasNext()) filtered += ", ";
                
        }
        
        return filtered;        
       
        
    }
            
    
}
