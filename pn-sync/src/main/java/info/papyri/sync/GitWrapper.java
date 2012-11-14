/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


/**
 *
 * @author hcayless
 */
public class GitWrapper {
  
  private static GitWrapper git;
  private static String graph = "http://papyri.info/graph";
  private static String path = "/pi/query";
  private static String sparqlserver = "http://localhost:8090";
  private static Logger logger = Logger.getLogger("pn-sync");
  
  public static GitWrapper init (String gitDir, String dbUser, String dbPass) {
    git = new GitWrapper();
    git.gitDir = new File(gitDir);
    git.dbUser = dbUser;
    git.dbPass = dbPass;
    try {
        git.head = getLastSync();
      } catch (Exception e) {
        git.success = false;
      }
    return git;
  }
  
  public static GitWrapper get() {
    if (git != null) {
      return git;
    } else {
      throw new RuntimeException("Uninitialized GitWrapper");
    }
  }

  private File gitDir;
  /*
   * success==false means that the repos could not be synchronized for some
   * reason, perhaps a conflict, and that human intervention is called for.
   */
  private boolean success = true;
  private String head = "";
  private String dbUser;
  private String dbPass;
  private Date gitSync;
  private Date canonicalSync;
  
  

  public static void executeSync() {
    try {
      git.pull("canonical");
      git.pull("github");
      git.push("canonical");
      git.push("github");
    } catch (Exception e) {
      if (git.success) {
        git.success = false;
      }
      logger.error("Sync Failed", e);
    }

    // get current HEAD's SHA : git rev-parse HEAD
    // run local pull from canonical, then git pull from github‚ record time stamps, success :: RESTful: return 200 for success, 500 for failure/conflict
    // on failure, git reset to previous SHA
    // get list of files affected by pull: git diff --name-only SHA1 SHA2
    // execute indexing on file list
  }
  
  public static String getPreviousSync() throws Exception {
    String result = null;
    Connection connect = null;
    Class.forName("com.mysql.jdbc.Driver");
    try {
      connect = DriverManager.getConnection(
              "jdbc:mysql://localhost/pn?"
              + "user=" + git.dbUser + "&password=" + git.dbPass);
      Statement st = connect.createStatement();
      ResultSet rs = st.executeQuery("SELECT hash FROM sync_history ORDER BY date DESC LIMIT 2");
      if (rs.next()) {
        if (!rs.isAfterLast() || !rs.next()) {
          result = getHead();
        } else {
          result = rs.getString("hash");
        }
      }
    } finally {
      connect.close();
    }
    return result;
  }

  public static String getLastSync() throws Exception {
    String result = null;
    Connection connect = null;
    Class.forName("com.mysql.jdbc.Driver");
    try {
      connect = DriverManager.getConnection(
              "jdbc:mysql://localhost/pn?"
              + "user=" + git.dbUser + "&password=" + git.dbPass);
      Statement st = connect.createStatement();
      ResultSet rs = st.executeQuery("SELECT hash FROM sync_history WHERE date = (SELECT MAX(date) FROM sync_history)");
      if (rs.next()) {
        result = rs.getString("hash");
      }
    } finally {
      connect.close();
    }
    return result;
  }

  private void storeHead() throws Exception {
    Connection connect = null;
    Class.forName("com.mysql.jdbc.Driver");
    try {
      connect = DriverManager.getConnection(
              "jdbc:mysql://localhost/pn?"
              + "user=" + git.dbUser + "&password=" + git.dbPass);
      Statement st = connect.createStatement();
      st.executeUpdate("INSERT INTO sync_history (hash, date) VALUES ('" + git.head + "', NOW())");
    } finally {
      connect.close();
    }
  }

  public static String getHead() throws Exception {
    ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "HEAD");
    pb.directory(git.gitDir);
    String newhead = null;
    try {
      Process p = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        newhead = line;
      }
    } catch (Exception e) {
      git.success = false;
      throw e;
    }
    return newhead;
  }

  private void pull(String repo) throws Exception {
    logger.info("Starting pull on " + repo + ".");
    try {
      ProcessBuilder pb = new ProcessBuilder("git", "pull", repo, "master");
      pb.directory(git.gitDir);
      pb.start().waitFor();
      git.head = getHead();
      if (!git.head.equals(getLastSync())) storeHead();
    } catch (Exception e) {
      git.success = false;
      git.reset(git.head);
      logger.error("Pull failed", e);
      throw e;
    }
  }
  
  private void push(String repo) throws Exception {
    logger.info("Starting push to " + repo + ".");
    try {
      ProcessBuilder pb = new ProcessBuilder("git", "push", repo);
      pb.directory(git.gitDir);
      pb.start().waitFor();
    } catch (Exception e) {
      git.success = false;
      git.reset(git.head);
      logger.error("Push failed", e);
      throw e;
    }
  }

  private void reset(String commit) throws Exception {
    ProcessBuilder pb = new ProcessBuilder("git", "reset", commit);
    pb.directory(gitDir);
    pb.start().waitFor();
  }

  public static List<String> getDiffs(String commit) throws Exception {
    ProcessBuilder pb = new ProcessBuilder("git", "diff", "--name-only", commit, git.head);
    pb.directory(git.gitDir);
    List<String> diffs = new ArrayList<String>();
    try {
      Process p = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        diffs.add(line);
      }
    } catch (Exception e) {
      git.success = false;
      throw e;
    }
    List<String> result = new ArrayList<String>();
    for (String diff : diffs) {
      result.add(diff);
    }
    return result;
  }
  
  public static List<String> getDiffsSince(String date) throws Exception {
    Connection connect = null;
    Class.forName("com.mysql.jdbc.Driver");
    try {
      connect = DriverManager.getConnection(
              "jdbc:mysql://localhost/pn?"
              + "user=" + git.dbUser + "&password=" + git.dbPass);
      PreparedStatement st = connect.prepareStatement("SELECT hash FROM sync_history WHERE date > ? ORDER BY date LIMIT 1");
      st.setDate(1, Date.valueOf(date));
      ResultSet rs = st.executeQuery();
      if (!rs.next()) {
        return getDiffs(getHead());
      } else {
        return getDiffs(rs.getString("hash"));
      }
    } finally {
      connect.close();
    }
  }
  
  public static String filenameToUri(String file) {
    StringBuilder result = new StringBuilder();
    if (file.contains("DDB")) {
      StringBuilder sparql = new StringBuilder();
      sparql.append("prefix dc: <http://purl.org/dc/terms/> ")
            .append("select ?id ")
            .append("from <http://papyri.info/graph> ")
            .append("where { ?id dc:identifier \"")
            .append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")))
            .append("\" }");
      try {
        // If the numbers server already knows the id for the filename, use that
        // because it will be 100% accurate. 
        URL m = new URL(sparqlserver + path + "?query=" + URLEncoder.encode(sparql.toString(), "UTF-8") + "&output=json");
        JsonNode root = getJson(m);
        if (root.path("results").path("bindings").size() > 0) {
          result.append(root.path("results").path("bindings").path(0).path("id").path("value").asText());
        // Otherwise, attempt to infer the identifier from the filename. 
        } else {
          result.append("http://papyri.info/ddbdp/");
          List<String> collections = GitWrapper.loadDDbCollections();
          Iterator<String> i = collections.iterator();
          while (i.hasNext()) {
            String collection = i.next().substring("http://papyri.info/ddbdp/".length());
            if (file.substring(file.indexOf("/") + 1).startsWith(collection)) {
              result.append(collection).append(";");
              // name should be of the form bgu.1.2, so lose the "bgu."
              String rest = file.substring(file.indexOf("/") + 1 + collection.length() + 2); 
              if (rest.contains(".")) {
                result.append(rest.substring(0, rest.lastIndexOf(".")));
                result.append(";");
                result.append(rest.lastIndexOf(".") + 1);
              } else {
                result.append(";;");
                result.append(rest);
              }
              result.append("/source");
              if (result.toString().matches("http://papyri\\.info/ddbdp/(\\w|\\d)+;(\\w|\\d)*;(\\w|\\d)/source")) {
                return result.toString(); // Early return
              } else {
                throw new Exception("Malformed file name: " + file);
              }
            }
          }
          // If we made it through the collection list without a match,
          // something is wrong.
          throw new Exception("Unknown collection in file: " + file);
        }
      } catch (Exception e) {
        logger.error("Failed to resolve URI.", e);
        result.delete(0, result.length());
      }
    } else {
      result.append("http://papyri.info/");
      if (file.contains("HGV_meta")) {
        result.append("hgv/");
        result.append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")));
        result.append("/source");
      }
      if (file.contains("APIS")) {
        result.append("apis/");
        result.append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")));
        result.append("/source");
      }
      if (file.contains("Biblio")) {
        result.append("biblio/");
        result.append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")));
        result.append("/ref");
      }
      
    }
    logger.debug(result);
    return result.toString();
  }
  
  public static String lookupDDbDPID(String id) {
    StringBuilder sparql = new StringBuilder();
    sparql.append("prefix dc: <http://purl.org/dc/terms/> ")
          .append("select ?id ")
          .append("from <http://papyri.info/graph> ")
          .append("where { ?id dc:relation <")
          .append(id)
          .append("> ")
          .append("filter regex(str(?id), \"^http://papyri.info/ddbdp/.*\") ")
          .append("filter not exists {?id dc:isReplacedBy ?b} }");
    try {
      URL m = new URL(sparqlserver + path + "?query=" + URLEncoder.encode(sparql.toString(), "UTF-8") + "&output=json");
      JsonNode root = getJson(m);
      if (root.path("results").path("bindings").size() > 0) {
        return root.path("results").path("bindings").path(0).path("id").path("value").asText();
      } else {
        if (id.contains("/apis/")) {
          sparql = new StringBuilder();
          sparql.append("prefix dc: <http://purl.org/dc/terms/> ")
                .append("select ?id ")
                .append("from <http://papyri.info/graph> ")
                .append("where { ?id dc:relation <")
                .append(id)
                .append("> ")
                .append("filter regex(str(?id), \"^http://papyri.info/hgv/.*\") }");
          m = new URL(sparqlserver + path + "?query=" + URLEncoder.encode(sparql.toString(), "UTF-8") + "&output=json");
          root = getJson(m);
          if (root.path("results").path("bindings").size() > 0) {
            return root.path("results").path("bindings").path(0).path("id").path("value").asText();
          }
        }
      }
    } catch (Exception e) {
      logger.error("Failed to look up query: \n" + sparql.toString());
    }
    return null;
  }
  
  private static JsonNode getJson(URL q) throws java.io.IOException {
      HttpURLConnection http = (HttpURLConnection)q.openConnection();
      http.setConnectTimeout(2000);
      ObjectMapper o = new ObjectMapper();
      JsonNode result = o.readValue(http.getInputStream(), JsonNode.class);
      return result;
  }
  
  private static List<String> loadDDbCollections() {
    List<String> result = new ArrayList<String>();
    String sparql = "prefix dc: <http://purl.org/dc/terms/> "
            + "select ?id "
            + "from <http://papyri.info/graph> "
            + "where { <http://papyri.info/ddbdp> dc:hasPart ?id } "
            + "order by desc(?id)";
    try {
        URL m = new URL(sparqlserver + path + "?query=" + URLEncoder.encode(sparql.toString(), "UTF-8") + "&output=json");
        JsonNode root = getJson(m);
        Iterator<JsonNode> i = root.path("results").path("bindings").getElements();
        while (i.hasNext()) {
          result.add(i.next().path("id").path("value").asText());
        }
      } catch (Exception e) {
        logger.error("Failed to resolve URI.", e);
      }
    return result;
  }
}
