/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.papyri.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

/**
 *
 * @author hcayless
 */
@WebServlet(name="Sync", urlPatterns={"/sync"})
public class Sync extends HttpServlet {

    private File gitDir;
    private boolean success = true;
    private String head = "";
    private Date gitSync;
    private Date canonicalSync;

    @Override
    public void init(ServletConfig config) throws ServletException {
      super.init(config);
      gitDir = new File(config.getInitParameter("gitDir"));
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Sync</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Sync at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
            */
        } finally { 
            out.close();
        }
    }

    private void executeSync() {
      try {
        head = getHead();
        String oldHead = head;
        pull("canonical");
        pull("github");
        List diffs = getDiffs(oldHead);
      } catch (Exception e) {
        if (success) success = false;
      }

      // get current HEAD's SHA : git rev-parse HEAD
      // run local pull from canonical, then git pull from github —record time stamps, success :: RESTful: return 200 for success, 500 for failure/conflict
      // on failure, git reset to previous SHA
      // get list of files affected by pull: git diff --name-only SHA1 SHA2
      // execute indexing on file list
    }

    private String getHead() throws Exception {
      ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
      pb.directory(gitDir);
      String newhead = null;
      try {
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          newhead = line;
        }
      } catch (Exception e) {
        success = false;
        throw e;
      }
      return newhead;
    }

    private void pull(String repo) throws Exception {
      try {
        ProcessBuilder pb = new ProcessBuilder("git", "pull", repo);
        pb.directory(gitDir);
        pb.start().waitFor();
        head = getHead();
      } catch (Exception e) {
        success = false;
        reset(head);
        throw e;
      }
    }

    private void reset(String commit) throws Exception {
      ProcessBuilder pb = new ProcessBuilder("git", "reset", commit);
      pb.directory(gitDir);
      pb.start().waitFor();
    }

    private List getDiffs(String commit) throws Exception {
      ProcessBuilder pb = new ProcessBuilder("git", "diff", "--name-only", commit, head);
      pb.directory(gitDir);
      List<String> diffs = new ArrayList<String>();
      try {
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          diffs.add(line);
        }
      } catch (Exception e) {
        success = false;
        reset(head);
        throw e;
      }
      return diffs;
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
