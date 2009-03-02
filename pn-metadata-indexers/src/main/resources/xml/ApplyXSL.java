package xml;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class ApplyXSL {
    public static void main(String [] args){
        transform("p0094_004");
    }
    public static void transform(String name){
        
        try{
            InputStream xsl = ApplyXSL.class.getResourceAsStream("modsToProps.xsl");
            InputStream xml = ApplyXSL.class.getResourceAsStream("/info/papyri/data/publication/checklist/checklist.xml");
            StreamSource source = new StreamSource(xsl);
            StreamResult result = new StreamResult(System.out);
            FileOutputStream fileS = new FileOutputStream(new File("ddbdp.properties"));
            StreamResult file = new StreamResult(fileS);
            Transformer trans = TransformerFactory.newInstance().newTransformer(source);    
            trans.transform(new StreamSource(xml), result);
            xml.close();
            xml = ApplyXSL.class.getResourceAsStream("/info/papyri/data/publication/checklist/checklist.xml");
            trans.transform(new StreamSource(xml), file);
            fileS.flush();
            xml.close();
        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }
}