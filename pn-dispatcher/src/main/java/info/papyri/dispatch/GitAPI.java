/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.dispatch;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import javax.servlet.ServletConfig;
import org.apache.log4j.Logger;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "GitAPI", urlPatterns = {"/GitAPI"})
public class GitAPI extends HttpServlet {
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.repo = config.getInitParameter("repository");
  }
  
  private String repo;
  private static Logger logger = Logger.getLogger("pn-dispatch");

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    PrintWriter out = response.getWriter();
    Git git = new Git(repo);
    FileUtils utils = new FileUtils("");
    String path = utils.getXmlFilePathFromId(request.getParameter("id"));
    try {
      out.println("{");
      out.print("\"sha\": \"");
      out.print(git.getLastCommitSHA(path));
      out.println("\"");
      out.println("}");
    } catch (Exception e) {
      logger.error(e);
    } finally {      
      out.close();
    }
  }
  
  private class Git {
    
    public Git(String repo) {
      try {
        this.repo = new FileRepository(repo);
        this.git = new org.eclipse.jgit.api.Git(this.repo);
      } catch (IOException e) {
        logger.error(e);
      }
    }
    
    public RevCommit getLastCommit(String file)
            throws AmbiguousObjectException, MissingObjectException,
            IncorrectObjectTypeException, IOException, GitAPIException {
      Iterable<RevCommit> commits = git.log().add(repo.resolve(Constants.HEAD)).addPath(file).setMaxCount(1).call();
      return commits.iterator().next();
    }
    
    public String getLastCommitSHA(String file)
            throws AmbiguousObjectException, MissingObjectException,
            IncorrectObjectTypeException, IOException, GitAPIException {
      return getLastCommit(file).name();
    }
    
    private Repository repo;
    private org.eclipse.jgit.api.Git git;
    
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP
   * <code>GET</code> method.
   *
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
   * Handles the HTTP
   * <code>POST</code> method.
   *
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
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>
}
