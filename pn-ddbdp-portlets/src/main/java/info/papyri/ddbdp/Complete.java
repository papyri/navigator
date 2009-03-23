package info.papyri.ddbdp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;

public class Complete extends HttpServlet {
private HashMap<String,String> series = null;
    @Override
    public void init() throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        HashMap<String,String> series = new HashMap<String,String>();
        series.put("col.", "P.Col.");
        series.put("cair.", "P.Cair.");
        series.put("p.col.", "P.Col.");
        series.put("p.cair.", "P.Cair.");
        series.put("psi", "PSI");
        this.series = series;
    }

    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        String query = arg0.getParameter("qu");
        boolean json = "true".equals(arg0.getParameter("js"));
        String type = json?"text/javascript":"text/xml";
        if (query == null) query = "a";
        else query = query.toLowerCase();
        arg1.setContentType(type);
        PrintWriter out = arg1.getWriter();
        if (json) {
            doJSON(query,out);
        }
        else{
            doXML(query,out);
        }
        out.flush();
    }
    
    private void doXML(String query, PrintWriter out){
        out.println("<?xml version=\"1.0\"?>");
        out.println("<topLevel>");
        out.println("<CompleteSuggestion>");
        Iterator<String> keys = this.series.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            if (key.startsWith(query)){
                out.println("<suggestion data=\"" + this.series.get(key) + "\" />");
                out.println("<num_queries int=\"1\" />");
            }
        }

        out.println("<num_queries int=\"1\" />");
        out.println("</CompleteSuggestion>");
        out.print("</topLevel>");
    }
    
    private void doJSON(String query, PrintWriter out){
        boolean matched = false;
        Iterator<String> keys = this.series.keySet().iterator();
        out.print("sendRPCDone(frameElement,\"" + query + "\", new Array(");
        while(keys.hasNext()){
            String key = keys.next();
            if (key.startsWith(query)){
                if (!matched){
                    matched = true;
                }
                else {
                    out.print(" , ");
                }
                out.print('"');
                out.print(this.series.get(key));
                out.print('"');
            }
        }

        out.print("), new Array(\"\"), new Array(\"\"));");
    }

}
