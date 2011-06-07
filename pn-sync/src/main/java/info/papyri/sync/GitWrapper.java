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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.text.DateFormat;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;

/**
 *
 * @author hcayless
 */
public class GitWrapper {
  
  private static GitWrapper git;
  private static String graph = "rmi://localhost/papyri.info#pi";
  private static String path = "/sparql/";
  private static String mulgara = "http://papyri.info/mulgara";
  
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
    } catch (Exception e) {
      if (git.success) {
        git.success = false;
      }
      e.printStackTrace();
    }

    // get current HEAD's SHA : git rev-parse HEAD
    // run local pull from canonical, then git pull from github‚ record time stamps, success :: RESTful: return 200 for success, 500 for failure/conflict
    // on failure, git reset to previous SHA
    // get list of files affected by pull: git diff --name-only SHA1 SHA2
    // execute indexing on file list
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
      if (!rs.next()) {
        result = getHead();
      } else {
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
      st.executeUpdate("INSERT INTO sync_history (hash) VALUES (" + git.head + ")");
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
    try {
      ProcessBuilder pb = new ProcessBuilder("git", "pull", repo);
      pb.directory(git.gitDir);
      pb.start().waitFor();
      git.head = getHead();
      storeHead();
    } catch (Exception e) {
      git.success = false;
      git.reset(git.head);
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
      result.add(filenameToUri(diff));
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
      st.setDate(0, Date.valueOf(date));
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
      String sparql = "prefix dc: <http://purl.org/dc/terms/> "
                  + "select ?id "
                  + "from <rmi://localhost/papyri.info#pi> "
                  + "where { ?id dc:identifier \"" + file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")) + "\" }";
      try {
        URL m = new URL(mulgara + path + "?query=" + URLEncoder.encode(sparql, "UTF-8") + "&format=json");
        JsonNode root = getDDbDPJson(m);
        String uri = root.path("results").path("bindings").path(0).path("id").path("value").getValueAsText();
        result.append(uri.substring(0, uri.lastIndexOf("/")));
      } catch (Exception e) {

      }
    } else {
      result.append("http://papyri.info/");
      if (file.contains("HGV_meta")) {
        result.append("hgv/");
        result.append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")));
      }
      if (file.contains("APIS")) {
        result.append("apis/");
        result.append(file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(".")));
      }
    }
    return result.toString();
  }
  
  private static JsonNode getDDbDPJson(URL q) throws java.io.IOException {
      HttpURLConnection http = (HttpURLConnection)q.openConnection();
      http.setConnectTimeout(2000);
      ObjectMapper o = new ObjectMapper();
      JsonNode result = o.readValue(http.getInputStream(), JsonNode.class);
      return result;
  }
}
