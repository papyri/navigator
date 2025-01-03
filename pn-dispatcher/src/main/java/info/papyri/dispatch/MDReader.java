package info.papyri.dispatch;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import info.papyri.dispatch.markdown.PNLinkExtension;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcayless
 */
@WebServlet(name = "MDReader", urlPatterns = {"/docs"})
public class MDReader extends HttpServlet {

  private static String DOCSHOME;
  private static String TEMPLATE;
  private static final DataHolder OPTIONS = new MutableDataSet().set(Parser.EXTENSIONS,
          Collections.singletonList(PNLinkExtension.create()));
  private static final Parser PARSER = Parser.builder(OPTIONS).build();
  private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();
  private static final Logger LOGGER = Logger.getLogger("pn-dispatch");

  @Override
  public void init(ServletConfig config) {
    DOCSHOME = config.getInitParameter("docs");
    TEMPLATE = config.getInitParameter("template");
  }

  /**
   * Reads a MarkDown file from the directory specified in the "docs" web.xml
   * param, and converts it into HTML, interpolating it into the HTML file
   * specified in the "template" param. The HTML output is cached and the
   * cached file is used for subsequent requests until the MarkDown file is
   * updated.
   * 
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
    response.setContentType("text/html;charset=UTF-8");
    StringBuilder requestPath = new StringBuilder(DOCSHOME);
    requestPath.append("/").append(request.getParameter("f")).append(".md");
    File f = new File(requestPath.toString());
    File cf = new File(requestPath.toString().replaceAll("\\.md$", ".html"));
    File cfTmp = null;
    PrintWriter cacheOut = null;
    if (f.exists()) {
      if (f.lastModified() > cf.lastModified()) {
        PrintWriter out = response.getWriter();
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
          cfTmp = File.createTempFile(cf.getName(), null, f.getParentFile());
          cacheOut = new PrintWriter(new OutputStreamWriter (new FileOutputStream(cfTmp), StandardCharsets.UTF_8));
          StringBuilder mdf = new StringBuilder();
          char[] ch = new char[1024];
          int c;
          while ((c = reader.read(ch)) > 0) {
            mdf.append(ch, 0, c);
          }
          reader = new BufferedReader(new FileReader(new File(TEMPLATE)));
          String line;
          while ((line = reader.readLine()) != null) {
            out.println(line);
            cacheOut.println(line);
            if (line.contains("<div class=\"markdown\">")) {
              Node md = PARSER.parse(mdf.toString());
              String result = RENDERER.render(md);
              out.write(result);
              cacheOut.write(result);
              reader.readLine(); // assume template has a throwaway line inside the content div
            }
          }
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Unable to process MarkDown.", e);
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
          out.close();
          if (cacheOut != null) {
            cacheOut.close();
          }
          if (cfTmp != null) {
            cfTmp.renameTo(cf);
          }
        }
      } else {
        ServletUtils.send(response, cf);
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
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
