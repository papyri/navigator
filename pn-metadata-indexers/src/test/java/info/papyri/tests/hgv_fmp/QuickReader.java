package info.papyri.tests.hgv_fmp;
import java.io.*;
import java.util.*;
public class QuickReader {
  public static void main(String[] args){
      File hgv = new File("C:\\staging\\data\\hgv_dso.xml");
      FileReader in = null;
      try{
          in = new FileReader(hgv);
          char [] buf = new char[1024];
          int read = -1;
          while((read = in.read(buf)) != -1){
              String r = new String(buf,0,read);
              int q = r.indexOf('?');
              if(q != -1){
                  int q2 = r.indexOf("(?)");
                  int start = Math.max(q - 50, 0);
                  int end = Math.min(start + 100, read);
                  if( q2 != -1){
                      //System.out.println(r.substring(start,end));    
                  }
                  else {
                      System.err.println(r.substring(start,end));    
                  }
              }
          }
      }
      catch (Throwable t){
          System.out.println(t.toString());
      }
      finally{
          try{
              if (in != null) in.close();    
          }
          catch (Throwable t){}
      }
  }
}
