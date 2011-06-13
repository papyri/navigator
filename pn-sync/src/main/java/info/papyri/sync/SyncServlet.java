/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.List;
import static java.util.concurrent.TimeUnit.*;

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
@WebServlet(name = "Sync", urlPatterns = {"/sync"})
public class SyncServlet extends HttpServlet {

  private GitWrapper git;
  private Publisher publisher;
  private static final ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    git = GitWrapper.init(config.getInitParameter("gitDir"), config.getInitParameter("dbUser"), config.getInitParameter("dbPass"));
    publisher = new Publisher(config.getInitParameter("gitDir"));
    scheduler.scheduleWithFixedDelay(publisher, 10, 60, MINUTES);
    System.out.println("Syncing scheduled.");
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
    
    PrintWriter out = response.getWriter();
    String action = request.getParameter("action");
    try {
      if ("status".equals(action)) {
        response.setContentType("application/json;charset=UTF-8");
        out.println("{");
        out.println("  \"success\": " + publisher.getSuccess() + ",");
        out.println("  \"status\": \"" + publisher.status() + "\"");
        out.println("  \"started\": \"" + publisher.getTimestamp() + "\"");
        out.println("  \"last\": \"" + publisher.getLastRun() + "\"");
        out.println("}");
      }
      if ("check".equals(action)) {
        response.setContentType("text/plain;charset=UTF-8");
        out.println(publisher.getSuccess());
      }
      if ("updates".equals(action)) {
        response.setContentType("application/json;charset=UTF-8");
        if (request.getParameterMap().containsKey("since")) {
          try {
            StringBuilder result = new StringBuilder();
            result.append("{ \"updates\":[");
            List<String> diffs = git.getDiffsSince(request.getParameter("since"));
            for (int i = 0; i < diffs.size(); i++) {
              result.append("\"");
              result.append(git.filenameToUri(diffs.get(i)));
              result.append("\"");
              if (i < (diffs.size() - 1)) result.append(",");
            }
            result.append("]}");
            out.println(result.toString());
          } catch (Exception e) {
            out.println("{ \"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
          }
        }
      }
    } finally {
      out.close();
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
