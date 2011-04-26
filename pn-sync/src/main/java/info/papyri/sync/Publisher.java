/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package info.papyri.sync;

import java.util.List;
import info.papyri.map;

/**
 *
 * @author hcayless
 */
public class Publisher implements Runnable {
  
  String base;
  
  public Publisher (String base) {
    this.base = base;
  }

  @Override
  public void run() {
    try {
      GitWrapper.executeSync();
      List<String> diffs = GitWrapper.getDiffs(GitWrapper.getLastSync());
      for (String file : diffs) {
        
      }
    } catch (Exception e) {
      
    }
  }
  
}
