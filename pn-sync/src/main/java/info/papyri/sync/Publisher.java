/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.util.ArrayList;
import java.util.List;
import info.papyri.map;
import info.papyri.indexer;
import java.io.File;

/**
 *
 * @author hcayless
 */
public class Publisher implements Runnable {
  
  String base;
  boolean success = true;
  
  public Publisher (String base) {
    this.base = base;
  }
  
  public boolean getSuccess() {
    return this.success;
  }

  @Override
  public void run() {
    try {
      GitWrapper.executeSync();
      List<String> diffs = GitWrapper.getDiffs(GitWrapper.getLastSync());
      List<String> files = new ArrayList<String>();
      for (String diff : diffs) {
        files.add(base + File.separator + diff);
      }
      map.mapFiles(files);
      indexer.index(files);
    } catch (Exception e) {
      success = false;
    }
  }
  
}
