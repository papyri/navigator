/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.papyri.dispatch;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hcayless
 */
@WebServlet(name="Reader", urlPatterns={"/reader"})
public class Reader extends HttpServlet {
  
  private String xmlPath = "";
  private String htmlPath = "";
  private FileUtils util;

  @Override
    public void init(ServletConfig config) throws ServletException {
      super.init(config);
      xmlPath = config.getInitParameter("xmlPath");
      htmlPath = config.getInitParameter("htmlPath");
      util = new FileUtils(xmlPath, htmlPath);
    }
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String page = request.getParameter("p");
        if (page != null) {
          String[] parts = page.split("/");
          String collection = parts[0];
          String item = "";
          if (parts.length > 1) {
            item = parts[1];
          }
          if (page.endsWith("/source")) {
            response.setContentType("application/xml;charset=UTF-8");
            send(response, util.getXmlFile(collection, item));
          } else if (page.endsWith("text")) {
            response.setContentType("text/plain;charset=UTF-8");
            send(response, util.getTextFile(collection, item));
          } else {
            response.setContentType("text/html;charset=UTF-8");
            send(response, util.getHtmlFile(collection, item.replaceAll(",", "-").replaceAll("/", "_")));
          }
        } else {
          response.sendError(response.SC_NOT_FOUND);
        }
    }
    
    private void send(HttpServletResponse response, File f)
    throws ServletException, IOException {
      FileInputStream reader = null;
      System.out.println(f.getAbsolutePath());
      OutputStream out = response.getOutputStream();
      if (f != null && f.exists()) {
        try {
          reader = new FileInputStream(f);
          byte[] buffer = new byte[4096];
          int size = reader.read(buffer);
          while (size > 0) {
            out.write(buffer, 0, size);
            size = reader.read(buffer);
          }
        } catch (IOException e) {
            response.sendError(response.SC_NOT_FOUND);
            System.out.println("Failed to send " + f);
        } finally {
            reader.close();
            out.close();
        }
      } else {
        response.sendError(response.SC_NOT_FOUND);
      }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
