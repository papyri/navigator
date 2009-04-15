package info.papyri.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hcayless
 */
public class JetspeedUrlRewriter {
    //TODO: this class knows about Jetspeed's URL scheme. There may be nothing that can be done about this, but we should try...

    /**
     * Rewrites Jetspeed URLs to enable "nice" URLs.
     */
    public JetspeedUrlRewriter(){}

    private Pattern p = Pattern.compile("oai:papyri.info:identifiers:(apis|ddbdp|hgv):([^:]+):(.+)");


    /**
     * Checks whether this is a rewritable URL, and rewrites it if so,
     * otherwise returns the input URL.  If an invalid URL is passed in, the
     * input String is returned.
     *
     * @param Object in
     * @return String the rewritten URL, or the URL that was passed in
     */
    public String rewriteUrl(Object in) {
        //TODO: Add support for rewriting min/max links
        StringBuffer result = new StringBuffer();
        try {
            URL url = new URL(in.toString());
                 //           "http://localhost:80/navigator/portal/apisfull.psml?controlName=oai:papyri.info:identifiers:hgv:P.Oxy.:4:744"
                //URL url = new URL("http://localhost/navigator/hgv/P.Oxy./4_744");
            String path = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
            String port = "";
            if (80 != url.getPort()) {
                port = ":" + url.getPort();
            }
            result.append(url.getProtocol()+"://"+url.getHost()+port+"/navigator/");
            String query = url.getQuery();
            if (query != null) {
                Map<String,String> params = new HashMap<String,String>();
                for (String param:query.split("&")){
                    String[] parts = param.split("=");
                    params.put(parts[0], parts[1]);
                }
                if ("apisfull.psml".equals(path)) {
                    result.append("full/");
                    result.append(rewriteId(params.get("controlName")));
                } else if ("apismetadata.psml".equals(path)) {
                    result.append("metadata/");
                    result.append(rewriteId(params.get("controlName")));
                } else if ("text.psml".equals(path)) {
                    result.append("text/");
                    result.append(rewriteId(params.get("controlName")));
                } else {
                    return in.toString();
                }
            } else if ("default-page.psml".equals(path)) {
                    result.append("search");
            } else if ("ddbdp-search.psml".equals(path)) {
                result.append("ddbdpsearch");
            } else if ("numbers.psml".equals(path)) {
                result.append("numbers");
            } else {
                return in.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return in.toString();
        }
        return result.toString();
    }

    /**
     * Takes an input PN id, of the form oai:papyri.info:identifiers:hgv:P.Oxy.:4:744
     * and rewrites it to a URL form: hgv_P.Oxy._4:744.
     * @param Object in
     * @return String the rewritten id
     */
    public String rewriteId(Object in) {
        Matcher m = p.matcher(in.toString());
        if (m.matches()) {
            return m.group(1)+"_"+m.group(2)+"_"+m.group(3);
        }
        return in.toString();
    }

}
