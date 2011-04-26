/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;

/**
 *
 * @author hcayless
 */
public class GitWrapper {
  
  private static GitWrapper git;
  
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
    }

    // get current HEAD's SHA : git rev-parse HEAD
    // run local pull from canonical, then git pull from github ârecord time stamps, success :: RESTful: return 200 for success, 500 for failure/conflict
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
    return diffs;
  }
}
