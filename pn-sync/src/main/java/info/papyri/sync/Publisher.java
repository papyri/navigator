/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import info.papyri.map;
import info.papyri.indexer;
import java.io.File;

/**
 *
 * @author hcayless
 */
public class Publisher implements Runnable {
  
  private String base;
  private static String urlBase = "http://papyri.info/";
  private boolean success = true;
  public static String IDLE = "Currently idle.";
  public static String MAPPING = "Mapping files.";
  public static String INFERENCING = "Generating inferences.";
  public static String PUBLISHING = "Publishing new files.";
  private String status = IDLE;
  private Date started;
  
  public Publisher (String base) {
    this.base = base;
  }
  
  public boolean getSuccess() {
    return this.success;
  }
  
  public String status() {
    return this.status;
  }
  
  public Date getTimestamp() {
    return this.started;
  }

  @Override
  public void run() {
    if (success && status == IDLE) {
      started = new Date();
      try {
        GitWrapper.executeSync();
        List<String> diffs = GitWrapper.getDiffs(GitWrapper.getLastSync());
        List<String> files = new ArrayList<String>();
        for (String diff : diffs) {
          files.add(base + File.separator + diff);
          System.out.println(base + File.separator + diff);
        }
        if (files.size() > 0) {
          status = MAPPING;
          map.mapFiles(files);
          status = INFERENCING;
          for (String file : files) {
            map.insertInferences(file);
          }
          status = PUBLISHING;
          indexer.index(files);
        }
        status = IDLE;
        started = null;
      } catch (Exception e) {
        success = false;
      }
    }
  }
  

  
}
