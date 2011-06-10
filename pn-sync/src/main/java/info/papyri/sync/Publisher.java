/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
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
  private Date lastrun;
  
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
  
  public Date getLastRun() {
    return this.lastrun;
  }

  @Override
  public void run() {
    if (success && status == IDLE) {
      started = new Date();
      lastrun = started;
      try {
        String head = GitWrapper.getHead();
        System.out.println("Syncing at " + new Date());
        GitWrapper.executeSync();
        System.out.println(head + " = " + GitWrapper.getHead() + "?");
        if (!head.equals(GitWrapper.getHead())) {
          List<String> diffs = GitWrapper.getDiffs(head);
          List<String> files = new ArrayList<String>();
          for (String diff : diffs) {
            files.add(base + File.separator + diff);
            System.out.println(base + File.separator + diff);
          }
          if (files.size() > 0) {
            status = MAPPING;
            System.out.println("Mapping files starting at " + new Date());
            map.mapFiles(files);
            status = INFERENCING;
            for (String file : files) {
              map.insertInferences(file);
            }
            status = PUBLISHING;
            System.out.println("Publishing files starting at " + new Date());
            indexer.index(files);
          }
        }
        status = IDLE;
        started = null;
      } catch (Exception e) {
        success = false;
      }
    }
  }
  

  
}
