package info.papyri.ddbdp.xslt;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
public class DelegatingResolver implements URIResolver {
    private final URIResolver delegate;
    public DelegatingResolver(URIResolver delegate){
        this.delegate = delegate;
    }
    public Source resolve(String href, String base) throws TransformerException {
        try{
            String path = "/info/papyri/ddbdp/xslt/" + href;
            return new StreamSource(DelegatingResolver.class.getResourceAsStream(path));
        }
        catch(Throwable t){
            return this.delegate.resolve(href, base);
        }
    }

}
