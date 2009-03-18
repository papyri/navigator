package info.papyri.epiduke.lucene.analysis;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
public class LemmaFilter extends TokenFilter {
    public static final String checkLEMMA = "SELECT LEMMA FROM APP.LEMMA WHERE MORPH = ?";
    private final static WeakHashMap<String,WeakReference<Collection<String>>> cache = new WeakHashMap<String,WeakReference<Collection<String>>>();
    private final Connection db;
    private final PreparedStatement stmt;
    private Iterator<String> lemmas;
    private Token morphToken;
    public LemmaFilter(TokenStream tokens, Connection db) throws SQLException {
        super(tokens);
        this.db = db;
        this.stmt = getPreparedStatement(db);
    }
    
    public static PreparedStatement getPreparedStatement(Connection db) throws SQLException {
        return db.prepareStatement(checkLEMMA);
    }
    
    public static Collection<String> getLemmas(String morph, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, morph);
        ResultSet result = stmt.executeQuery();
        TreeSet<String> lemmas = new TreeSet<String>();
        int col = result.findColumn("LEMMA");
        while(result.next()){
            String lem = result.getString(col);
            lemmas.add(lem);
        }
        if(lemmas.size()==0){
            lemmas.add(morph);
        }
        return lemmas;
    }
    
    @Override
    public Token next() throws IOException {
        if(lemmas != null && lemmas.hasNext()){
            String next = lemmas.next();
            morphToken.setTermText(next);
            morphToken.setPositionIncrement(0);
            return morphToken;
        }
        morphToken = this.input.next();
        if(morphToken==null)return null;
        String text = new String(morphToken.termBuffer(),0,morphToken.termLength());
        if(text.length()==0) return morphToken;
        if(text.charAt(0)=='~') text = text.substring(1);
        try{
        Iterator<String> lemmas;
        if(!LemmaFilter.cache.containsKey(text)){
            LemmaFilter.cache.put(text, new WeakReference<Collection<String>>(getLemmas(text,this.stmt)));
            lemmas = LemmaFilter.cache.get(text).get().iterator();
        }
        else{
            lemmas = LemmaFilter.cache.get(text).get().iterator();
        }
        
        if(lemmas.hasNext()){
            String next = lemmas.next();
            this.lemmas = lemmas;
            morphToken.setTermText(next);
            return morphToken;
        }
        return null;
        }
        catch(SQLException e){
            throw new IOException(e.toString());
        }
    }
}
