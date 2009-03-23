package info.papyri.ddbdp.util;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
public class Collection {
    private final File docRoot;
    private final String collection;
    private final HashMap<String,Integer> volumes = new HashMap<String,Integer>();
    private int docCtr = 0;
    
    public Collection(File docRoot, String collection){
        this.docRoot = docRoot;
        this.collection = collection;
        File collRoot = new File(docRoot,collection);
        if(!collRoot.exists() || !collRoot.isDirectory()){
            if (!docRoot.exists() || !docRoot.isDirectory()) throw new IllegalArgumentException("Invalid docRoot: " + docRoot.getPath());
            throw new IllegalArgumentException("invlaid collection name: " + collection);
        }
        String [] children = collRoot.list();
        int docs = 0;
        for(String child:children){
            if(child.endsWith(".xml")){
                docs++;
                continue;
            }
            if(child.endsWith(".svn"))continue;
            File cDir = new File(collRoot,child);
            int cCtr = countXML(cDir);
            volumes.put(child,Integer.valueOf(cCtr));
            docs+=cCtr;
        }
        this.docCtr = docs;
    }
    
    public String getName(){
        return this.collection;
    }
    
    public Iterator<String> volumes(){
        return volumes.keySet().iterator();
    }
    
    public int numDocs(String volume){
        if(volume == null) return this.docCtr;
        Integer num = volumes.get(volume);
        if (num == null) return 0;
        return num.intValue();
    }
    
    public int numDocs(){
        return this.docCtr;
    }

    private int countXML(File docs){
        int ctr = 0;
        if(!docs.isDirectory()) throw new IllegalArgumentException(docs.getPath() + " is not a directory");
        String [] children = docs.list();
        for(String child:children){
            if(child.endsWith(".svn"))continue;
            if(child.endsWith(".xml")){
                ctr++;
                continue;
            }
            File cDir = new File(docs,child);
            if(cDir.isDirectory()){
                ctr += countXML(cDir);
            }
        }
        return ctr;
    }
}
