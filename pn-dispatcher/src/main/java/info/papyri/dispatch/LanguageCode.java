/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

/**
 *
 * @author thill
 */
public enum LanguageCode {
    
    grc("Ancient Greek"),
    egy_Copt("Egyptian\\Coptic"),
    la("Latin"),
    en("English"),
    fr("French"),
    de("German"),
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
    
    LanguageCode(String ex){
        
        expanded = ex;
        
    }
    
    public String expanded(){
        
        return expanded;
        
    }
            
    
}