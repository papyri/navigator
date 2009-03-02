package xml;
import java.io.*;
import java.util.*;

public class HexReporter {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try{
        InputStream file = HexReporter.class.getResource("p0197_34.xml").openStream();
        HexReporter main = new HexReporter(file,new PrintWriter(System.out),"UTF-8");
        main.report();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    
    private InputStream src;
    
    private PrintWriter out;
    
    private String charSet;
    
    public HexReporter (InputStream xml, PrintWriter out, String charSet){
        this.src = xml;
        this.out = out;
        this.charSet = charSet;
    }
    
    public void report() throws IOException {
        char [] buffer = new char[16];
        String [] hex = null;
        int read = -1;
        Reader reader = new InputStreamReader(src,charSet);
        while ((read = reader.read(buffer)) != -1){
            hex = getHex(buffer,0,read);
            for (int i=read;i<buffer.length;i++){
                buffer[i] = '\u0000';
            }
            out.print(new String(buffer,0,4));
            out.print(' ');
            out.print(new String(buffer,4,4));
            out.print(' ');
            out.print(new String(buffer,8,4));
            out.print(' ');
            out.print(new String(buffer,12,4));
            out.print("\t\t");
            for (int i = 0 ; i < hex.length; i++){
                out.print(hex[i]);
                out.print(" ");
                if (((i+1) % 4) == 0)out.print("   ");
            }
            out.println();            
        }
        out.flush();
    }
    
    private String [] getHex (char [] buffer, int start, int length){
        if (start + length > buffer.length) throw new IllegalArgumentException();
        
        String [] result = new String[buffer.length];
        for (int i =start;i<length;i++){
            result[i] = Integer.toHexString(buffer[i]);
        }
        
        for (int i = start + length; i < buffer.length; i++){
            result[i] = "  ";
        }
        return result;
    }

}
