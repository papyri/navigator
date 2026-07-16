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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "Sync", urlPatterns = {"/sync"})
public class SyncServlet extends HttpServlet {

  private Publisher publisher;
  private static final ScheduledExecutorService scheduler =
          Executors.newScheduledThreadPool(1);
  private static final Logger logger = Logger.getLogger("pn-sync");

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    
    // Read from environment variables first, then fallback to context params
    String postgresHost = System.getenv("POSTGRES_HOST") != null ? 
      System.getenv("POSTGRES_HOST") : config.getInitParameter("postgresHost");
    String dbUser = System.getenv("PN_DB_USER") != null ? 
      System.getenv("PN_DB_USER") : config.getServletContext().getInitParameter("dbUser");
    String dbPass = System.getenv("PN_DB_PASSWORD") != null ? 
      System.getenv("PN_DB_PASSWORD") : config.getServletContext().getInitParameter("dbPass");
    String gitDir = System.getenv("PN_GIT_DIR") != null ? 
      System.getenv("PN_GIT_DIR") : config.getInitParameter("gitDir");
    String pnMdDir = System.getenv("PN_MD_DIR") != null ? 
      System.getenv("PN_MD_DIR") : config.getInitParameter("mdDir");
    String ipdDataBranch = System.getenv("PN_IDP_DATA_GIT_BRANCH") != null ? 
      System.getenv("PN_IDP_DATA_GIT_BRANCH") : config.getInitParameter("idpDataGitBranch");
    String siteDocsGitBranch = System.getenv("PN_SITE_DOCS_GIT_BRANCH") != null ? 
      System.getenv("PN_SITE_DOCS_GIT_BRANCH") : config.getInitParameter("siteDocsGitBranch");
        
    logger.info("Initializing SyncServlet with the following configuration:");
    logger.info("Using postgresHost: " + postgresHost);
    logger.info("Using dbUser: " + dbUser);
    logger.info("Using gitDir: " + gitDir);
    logger.info("Using mdDir: " + pnMdDir);
    logger.info("Using ipdDataBranch: " + ipdDataBranch);
    logger.info("Using siteDocsGitBranch: " + siteDocsGitBranch);
    
    GitWrapper.init(
      postgresHost,
      gitDir,
      ipdDataBranch,
      dbUser,
      dbPass
    );
    publisher = new Publisher(gitDir);
    // Run at 5 minutes past the hour, and every half hour thereafter.
    Calendar cal = Calendar.getInstance();
    int start = cal.get(Calendar.MINUTE);
    if (start > 5) {
      start = (60 - start) + 5;
    } else {
      start = 5 - start;
    }
    // check for updates to idp.data repo and sync them across Github and Canonical
    scheduler.scheduleWithFixedDelay(publisher, start, 30, MINUTES);
    final File mdDir = new File(pnMdDir);
    // pull any changes to site-docs so they get published
    scheduler.scheduleWithFixedDelay(() -> {
      ProcessBuilder pb = new ProcessBuilder("git", "pull", "origin", siteDocsGitBranch);
      pb.directory(mdDir);
      try {
        pb.start().waitFor();
        logger.info("MarkDown directory synced at " + new Date());
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Failed to sync MarkDown directory " + mdDir, e);
      }
    }, start + 15, 15, MINUTES);
    logger.fine("Syncing scheduled.");
  }

  /** 
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    
    PrintWriter out = response.getWriter();
    String action = request.getParameter("action");
    try {
      if ("status".equals(action)) {
        response.setContentType("application/json;charset=UTF-8");
        out.println("{");
        out.println("  \"success\": " + publisher.getSuccess() + ",");
        out.println("  \"status\": \"" + publisher.status() + "\",");
        out.println("  \"started\": \"" + publisher.getTimestamp() + "\",");
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
            out.println(result);
          } catch (Exception e) {
            out.println("{ \"error\": \"" + e.getMessage() + "\"}");
            logger.log(Level.SEVERE, "Update failed.", e);
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
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
    processRequest(request, response);
  }

  /** 
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
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
