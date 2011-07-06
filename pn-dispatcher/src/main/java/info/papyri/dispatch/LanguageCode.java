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
    
    en("English"),
    fr("French"),
    de("German"),
    la("Latin"),
    grc_Latn("Ancient Greek in Latin script"),
    la_Grek("Latin in Greek script"),
    cop("Coptic"),
    grc("Ancient Greek"),
    egy_Copt("Egyptian\\Coptic"),
    egy_Coptgrc("Greek(?) Egyptian\\Coptic(?)"),
    ar_Arabegy_Copt("(A) Egyptian\\Coptic (B) Arabic; Egyptian\\Coptic (?)");
    
    private String expanded;
    
    LanguageCode(String ex){
        
        expanded = ex;
        
    }
    
    public String expanded(){
        
        return expanded;
        
    }
            
    
}
