/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "Sync", urlPatterns = {"/sync"})
public class SyncServlet extends HttpServlet {

  private Publisher publisher;
  private static final ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);
  private static Logger logger = Logger.getLogger("pn-sync");

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    String log4j = config.getInitParameter("log4j-properties-location");
    ServletContext sc = config.getServletContext();
    if (log4j == null) {
      BasicConfigurator.configure();
    } else {
      try {
        PropertyConfigurator.configure(config.getServletContext().getRealPath("/") + log4j);
        System.out.println("LOG4J INFO:");
        System.out.println(config.getServletContext().getRealPath("/") + log4j);
      } catch (Exception e) {
        System.out.println("Unable to load log4j properties from " + log4j);
        BasicConfigurator.configure();
      }
    }
    GitWrapper.init(config.getInitParameter("gitDir"), config.getInitParameter("dbUser"), config.getInitParameter("dbPass"));
    publisher = new Publisher(config.getInitParameter("gitDir"));
    // Run at 5 minutes past the hour, and every hour thereafter.
    Calendar cal = Calendar.getInstance();
    int start = cal.get(Calendar.MINUTE);
    if (start > 5) {
      start = (60 - start) + 5;
    } else {
      start = 5 - start;
    }
    // check for updates to idp.data repo and sync them across Github and Canonical
    scheduler.scheduleWithFixedDelay(publisher, start, 60, MINUTES);
    final File mdDir = new File(config.getInitParameter("mdDir"));
    // pull any changes to site-docs so they get published
    scheduler.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        ProcessBuilder pb = new ProcessBuilder("git", "pull", "origin", "master");
        pb.directory(mdDir);
        try {
          pb.start().waitFor();
          logger.info("MarkDown directory synced at " + new Date());
        } catch (Exception e) {
          logger.error("Failed to sync MarkDown directory " + mdDir, e);
        } 
      }
    }, start + 15, 15, MINUTES);
    logger.debug("Syncing scheduled.");
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
            List<String> diffs = GitWrapper.getDiffsSince(request.getParameter("since"));
            for (int i = 0; i < diffs.size(); i++) {
              result.append("\"");
              result.append(GitWrapper.filenameToUri(diffs.get(i)));
              result.append("\"");
              if (i < (diffs.size() - 1)) {
                result.append(",");
              }
            }
            result.append("]}");
            out.println(result.toString());
          } catch (Exception e) {
            out.println("{ \"error\": \"" + e.getMessage() + "\"}");
            logger.error("Update failed.", e);
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
