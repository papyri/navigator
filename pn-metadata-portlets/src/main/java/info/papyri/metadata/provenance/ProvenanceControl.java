package info.papyri.metadata.provenance;
import java.util.HashMap;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
public class ProvenanceControl {
    
    private static final HashMap<String,String> CANON = getMap();
    
    private static final String [] vals = {
        "Antaiopolis",
        "Antaiopolite",
        "Antinoite",
        "Antinoopolis",
        "Antiochia",
        "Aphrodite",
        "Aphroditopolite",
        "Apollonopolite",
        "Arsinoite",
        "Athribite",
        "Berenike",
        "Bubastos",
        "Bubastite",
        "Busiris",
        "Busirite",
        "Diopolite",
        "Eileithyiopolite",
        "Euhemeria",
        "Fayum", 
        "Gynaikopolite",
        "Hephaestias",
        "Herakleopolite",
        "Hermonthite",
        "Hermopolite",
        "Hermopolis",
        "Italy",
        "Kerkesis",
        "Kerkesucha",
        "Kieratu",
        "Koptite",
        "Krokodilopolite",
        "Kynopolis",
        "Latopolite",
        "Leontopolite",
        "Letopolite",
        "Lykopolis",
        "Lykopolite",
        "Lykia",
        "Mauretania",
        "Memphite",
        "Mesopotamia",
        "Mochite", 
        "Muchis", 
        "Narmuthis", 
        "Naukratis", 
        "Nemera", 
        "Nilopolite", 
        "Nubia", 
        "Onuphite", 
        "Oxyrhynchos", 
        "Oxyrhynchite", 
        "Pamphylia",
        "Pannonia", 
        "Panopolite", 
        "Pathyrite",
        "Pelusion",
        "Pharbaithite",
        "Philadelphia",
        "Phthemphuth",
        "Polydeukia",
        "Prosopite",
        "Rome",
        "Saite",
        "Sakkara",
        "Samaria",
        "Sebennyte",
        "Sebennytos",
        "Sethroite",
        "Syria",
        "Taampemu",
        "Taampeti",
        "Tebetny",
        "Tebtynis",
        "Tentyrite",
        "Thebaid",
        "Thebes",
        "Thinite",
        "Thmuis",        
    };
    public static final char [] [] PROVENANCE = getKeyChars(CANON);
    static {
        for (int i=0;i<vals.length;i++){
            PROVENANCE[i] = vals[i].toLowerCase().toCharArray();
        }
    }
    
    private static char[][] getKeyChars(HashMap map){
        java.util.Set<String> keys = map.keySet();
        String [] keyStrings = keys.toArray(new String[0]);
        char [] [] result = new char[keyStrings.length][];
        for (int i=0;i<keyStrings.length;i++){
            result[i] = keyStrings[i].toLowerCase().toCharArray();
        }
        return result;
    }
    
    private static HashMap<String,String> getMap(){
        HashMap<String,String> result = new HashMap<String,String>();
        result.put("antaiopolis", "antaiopolis");
        result.put("antaeopolis", "antaiopolis");

        result.put("antaiopolite", "antaiopolite");
        result.put("antaiopolites", "antaiopolite");
        result.put("antaeopolites", "antaiopolite");

        result.put("antinoite", "antinoite");
        result.put("antinoites", "antinoite");

        result.put("antinoopolis", "antinoopolis");
        result.put("antinoe", "antinoopolis");
        result.put("antinooupolis", "antinoopolis");
        
        result.put("antiochia","antiochia");
        result.put("antioch","antiochia");
        result.put("antiocheia","antiochia");
        
        result.put("aphrodite","aphrodite");
        result.put("aphrodito","aphrodite");
        
        result.put("aphroditopolite","aphroditopolite");
        result.put("aphroditopolites","aphroditopolite");
        
        result.put("apollonopolite","apollonopolite");
        result.put("apollonopolites","apollonopolite");
        
        result.put("arsinoite","arsinoite");
        result.put("arsinoites","arsinoite");
        
        result.put("athribite","athribite");
        result.put("athribites","athribite");
        
        result.put("berenike","berenike");
        result.put("berenikes","berenike");
        result.put("berenikis","berenike");
        
        result.put("bubastos","bubastos");
        result.put("boubastos","bubastos");
        result.put("bubastis","bubastos");
        
        result.put("bubastite","bubastite");
        result.put("bubastites","bubastite");
        
        result.put("busiris","busiris");
        result.put("bousiris","busiris");
        
        result.put("busirite","busirite");
        result.put("busirites","busirite");
        
        result.put("diopolite","diopolite");
        result.put("diopolites","diopolite");
        
        result.put("eileithyiopolite", "eileithyiopolite");
        result.put("eileithyiopolites", "eileithyiopolite");
        
        result.put("euhemeria", "euhemeria");
        result.put("euhemereia", "euhemeria");
        result.put("euhemeris", "euhemeria");
        
        result.put("fayum", "fayum");
        result.put("fayoum", "fayum");
        result.put("faiyum", "fayum");
        result.put("fayuum", "fayum");
        result.put("fayyum", "fayum");
        
        result.put("gynaikopolite", "gynaikopolite");
        result.put("gynaikopolites", "gynaikopolite");
        
        result.put("hephaestias", "hephaestias");
        result.put("hephaistias", "hephaestias");
        
        result.put("herakleopolite", "herakleopolite");
        result.put("herakleopolites", "herakleopolite");
        result.put("herakleopolitenome", "herakleopolite");
        
        result.put("hermonthite", "hermonthite");
        result.put("hermonthites", "hermonthite");
        
        result.put("hermopolite", "hermopolite");
        result.put("hermopolites", "hermopolite");
        
        result.put("hermopolis", "hermopolis");
        result.put("hermoupolis", "hermopolis");
        result.put("hermupolis", "hermopolis");
        
        result.put("italy", "italy");
        result.put("italien", "italy");
        
        result.put("kerkesis", "kerkesis");
        result.put("kerkeesis", "kerkesis");
        
        result.put("kerkesucha", "kerkesucha");
        result.put("kerkesoucha", "kerkesucha");
        
        result.put("kieratu", "kieratu");
        result.put("kieratou", "kieratu");
        
        result.put("koptite", "koptite");
        result.put("koptites", "koptite");
        
        result.put("krokodilopolite", "krokodilopolite");
        result.put("krokodilopolites", "krokodilopolite");
        
        result.put("kynopolis", "kynopolis");
        result.put("cynopolis", "kynopolis");
        
        result.put("latopolite", "latopolite");
        result.put("latopolites", "latopolite");
        
        result.put("leontopolite", "leontopolite");
        result.put("leontopolites", "leontopolite");
        
        result.put("letopolite", "letopolite");
        result.put("letopolites", "letopolite");
        
        result.put("lykopolis", "lykopolis");
        result.put("lycopolis", "lykopolis");
        
        result.put("lykopolite", "lykopolite");
        result.put("lykopolites", "lykopolite");
        
        result.put("lykia","lykia");
        result.put("lykien","lykia");
        
        result.put("mauretania","mauretania");
        result.put("mauretanien","mauretania");
        
        result.put("memphite", "memphite");
        result.put("memphites", "memphite");
        
        result.put("mesopotamia","mesopotamia");
        result.put("mesopotamien","mesopotamia");
        
        result.put("mochite", "mochite");
        result.put("mochites", "mochite");
        
        result.put("muchis","muchis");
        result.put("mouchis", "muchis");
        
        result.put("narmuthis","narmuthis");
        result.put("narmouthis", "narmuthis");
        
        result.put("naukratis", "naukratis");
        result.put("naucratis", "naukratis");
        
        result.put("nemera", "nemera");
        result.put("nemerai", "nemera");
        
        result.put("nilopolite","nilopolite");
        result.put("nilopolites","nilopolite");
        
        result.put("nubia","nubia");
        result.put("nubien","nubia");
        
        result.put("onuphite","onuphite");
        result.put("onuphites","onuphite");
        
        result.put("oxhyrhynchos","oxyrhynchos");
        result.put("oxhyrhynchus","oxyrhynchos");
        result.put("oxyrhynchus","oxyrhynchos");
        result.put("oxyrhinchos","oxyrhynchos");
        result.put("oxyrhinchus","oxyrhynchos");
        result.put("oxyrhunchos","oxyrhynchos");
        result.put("oxyrhunchus","oxyrhynchos");
        result.put("oxyrhynchos","oxyrhynchos");
        result.put("oxyrhyncos","oxyrhynchos");
        
        result.put("oxyrhynchite", "oxyrhynchite");
        result.put("oxyrhynchites", "oxyrhynchite");
        
        result.put("pamphilia","pamphilia");
        result.put("pamphilien","pamphilia");
        
        result.put("pannonia","pannonia");
        result.put("pannonien","pannonia");
        
        result.put("panopolite", "panopolite");
        result.put("panopolites", "panopolite");
        
        result.put("pathyrite", "pathyrite");
        result.put("pathyrites", "pathyrite");
        
        result.put("pelusion", "pelusion");
        result.put("pelousion", "pelusion");
        
        result.put("pharbaithite", "pharbaithite");
        result.put("pharbaithites", "pharbaithite");
        
        result.put("philadelphia","philadelphia");
        result.put("philadelpheia","philadelphia");
        
        result.put("phthemphuth","phthemphuth");
        result.put("phthemphouth","phthemphuth");
        
        result.put("polydeukia","polydeukia");
        result.put("polydeukeia","polydeukia");
        
        result.put("prosopite", "prosopite");
        result.put("prosopites", "prosopite");
        
        result.put("rome","rome");
        result.put("rom","rome");
        
        result.put("saite","saite");
        result.put("saites","saite");
        result.put("saitic","saite");
        
        result.put("sakkara","sakkara");
        result.put("saqqara","sakkara");
        
        result.put("samaria","samaria");
        result.put("samareia","samaria");
        
        result.put("sebennyte","sebennyte");
        result.put("sebennytes","sebennyte");
        
        result.put("sebennytos","sebennytos");
        result.put("sebennutos","sebennytos");
        
        result.put("sethroite","sethroite");
        result.put("sethroites","sethroite");
        
        result.put("syria","syria");
        result.put("syrien","syria");
        
        result.put("taampemu","taampemu");
        result.put("taampemou","taampemu");
        result.put("tampemu","taampemu");
        
        result.put("taampeti","taampeti");
        result.put("tampeti","taampeti");
        
        result.put("tebetny","tebetny");
        result.put("tebetnoi","tebetny");
        
        result.put("tebtynis","tebtynis");
        result.put("tebtunis","tebtynis");

        result.put("tentyrite","tentyrite");
        result.put("tentyrites","tentyrite");
        
        result.put("thebaid","thebaid");
        result.put("thebais","thebaid");
        
        result.put("thebes","thebes");        
        result.put("theben","thebes");
        
        result.put("thinite","thinite");
        result.put("thinites","thinite");
        
        result.put("thmuis","thmuis");
        result.put("thmouis","thmuis");
        return result;
    }
    
    public static String match(String provenance){
        if (provenance == null) return null;
        provenance = provenance.toLowerCase().trim();
        String match = null;
        match = CANON.get(provenance);
        if (match != null) return match;
        
        int min = Math.min(4, provenance.length()-4);

        int result = -1;
        int distance = 99;
        char [] source = provenance.toLowerCase().trim().toCharArray();
        for (int i=0;i< PROVENANCE.length; i++){
            int d = util.EditDistance.distance(source, PROVENANCE[i]);
            if (d == 0) return CANON.get(new String(PROVENANCE[i]));
            if (d < distance && d <= min){
                result = i;
                distance = d;
            }
        }
        if (result == -1) return null;
        String key = new String(PROVENANCE[result]);
        return CANON.get(key);
    }
}
