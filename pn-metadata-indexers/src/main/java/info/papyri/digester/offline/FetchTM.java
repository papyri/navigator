package info.papyri.digester.offline;
import java.net.*;
import java.io.*;

public class FetchTM {
    public static void main(String [] args) throws IOException {
        String urlTemplate = "http://www.trismegistos.org/tm/list_all.php?p=";
        File dir = new File("/C:/staging/data/tm/");
        dir.mkdirs();
        byte [] buf = new byte [1024];
        int len = -1;
        for(int p = 1;p<2108;p++){
            String urlString = urlTemplate + p;
            URL url = new URL(urlString);
            InputStream xmlStream = url.openStream();
            File file = new File(dir,"p" + p + ".html");
            FileOutputStream out = new FileOutputStream(file);
            while((len = xmlStream.read(buf)) != -1){
                out.write(buf,0,len);
            }
            out.flush();
            out.close();
        }
        
    }

}
