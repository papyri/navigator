package info.papyri.numbers.indexer;
import java.util.Set;
import java.util.HashSet;
public class Node {
    private final String id;
    private final String uri;
    private final String href;
    private final Set<Node>  links = new HashSet<Node>();
    public boolean equals(Object obj){
        if(!(obj instanceof Node)) return false;
        Node o = (Node)obj;
        if(!o.id.equals(this.id)) return false;
        if(!o.uri.equals(this.id)) return false;
        if(!o.href.equals(this.href)) return false;
        return true;
    }
    public int hashCode(){
        int result = 1;
        result = result * 31 + id.hashCode();
        result = result * 31 + uri.hashCode();
        return result * 31 + href.hashCode();
    }
}
