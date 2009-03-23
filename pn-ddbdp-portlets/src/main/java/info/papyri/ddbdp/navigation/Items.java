package info.papyri.ddbdp.navigation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.io.*;
public class Items extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(Items.class); 
    private static final String XML_HDR = "<?xml version=\"1.0\"?>";
    private File docRoot = null;
    @Override
    public void init() throws ServletException {
        super.init();
        String docRootPath = getServletContext().getInitParameter("docroot");
        if (docRootPath!= null){
                this.docRoot = new File(docRootPath);
                
                if (!this.docRoot.canRead() || !this.docRoot.isDirectory()){
                    this.docRoot = null;
                    LOG.error("bad docroot path at " + docRootPath);
                }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mode = getParm("mode",request);
        String series = getParm("series",request);
        String volume = getParm("volume",request);
        series = series.trim();
        series = series.replaceAll("\\s", "");
        if (series.charAt(series.length() - 1) == '.') series = series.substring(0, series.length() - 1);

        
        String [] items = new String[0];
        if ("series".equals(mode)){
            items = getSeries(this.docRoot);
        }
        else if ("volume".equals(mode)){
            items = getVolumes(this.docRoot,series);
        }
        else if ("document".equals(mode)){
            items = getDocuments(this.docRoot,series, volume);
        }
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        out.println(XML_HDR);
        out.print("<items mode=\"");
        out.print(mode);
        out.println("\" >");
        if (items != null){
            out.print("<msg>");
            out.print("");
            out.println("</msg>");
        for (String item: items){
            out.print("<item>");
            out.print(item);
            out.println("</item>");
        }
        }
        else{
            out.print("<msg>");
            out.print("No " + mode + " items found for ");
            if (series != null && !series.equals("error")){
                out.print("series = \"");
                out.print(series);
                out.print("\" ");
            }
            if (volume != null && !volume.equals("error")){
                out.print("volume = \"");
                out.print(volume);
                out.print("\" ");
            }
            out.println("</msg>");
        }
        out.println("</items>");
        out.flush();
    }
    
    private static String getParm(String name, HttpServletRequest request){
        String mode = request.getParameter(name);
        if (mode == null) mode = "error";
        mode = mode.toLowerCase();
        return mode;
    }
    private static String [] getSeries(File docRoot){
        return docRoot.list(dirFilter(docRoot));
    }
    private static String [] getVolumes(File docRoot,String series){
        File r = new File(docRoot,series);
        try{
            LOG.debug("Trying to get volumes in series root " + r.getCanonicalPath());
        }
        catch (IOException ioe){
            LOG.error(ioe.toString(),ioe);
        }
        String [] result = r.list(dirFilter(r));
        if (result == null) return result;
        if (result.length == 0) return new String[] {"-"};
        return result;
    }
    private static String[] getDocuments(File docRoot,String series, String volume){
        File r = ("-".equals(volume))?new File(docRoot,series):new File(new File(docRoot,series),volume);
        try{
            LOG.debug("Trying to get documents in series root " + r.getCanonicalPath());
        }
        catch (IOException ioe){
            LOG.error(ioe.toString(),ioe);
        }
        return r.list(xmlFilter(r));
    }
    
    private static FilenameFilter dirFilter(final File parent){
        return new FilenameFilter(){
            public boolean accept(File file){
                boolean result = file.getParentFile().equals(parent);
                return result && file.isDirectory() && !(file.getPath().endsWith(".svn"));
            }
            public boolean accept(File file,String path){
                boolean result = file.equals(parent);
                return result && new File(file,path).isDirectory()&& !(path.endsWith(".svn"));
            }
        };
    }
    private static FilenameFilter xmlFilter(final File parent){
        return new FilenameFilter(){
            public boolean accept(File file){
                boolean result = file.getParentFile().equals(parent)
                                 && file.isFile()
                                 && file.getName().endsWith(".xml");
                return result;
            }
            public boolean accept(File file,String path){
                File child = new File(file,path);
                boolean result = file.equals(parent)
                                 && child.isFile()
                                 && child.getName().endsWith(".xml");
                try{
                    if (!result) LOG.debug("Rejecting " + file.getCanonicalPath());
                }
                catch (IOException ioe){}
                return result;
            }
        };
    }

}
