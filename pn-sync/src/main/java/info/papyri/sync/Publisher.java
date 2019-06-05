package info.papyri.sync;

import info.papyri.indexer;
import info.papyri.map;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
/**
 *
 * @author hcayless
 */
public class Publisher implements Runnable {
  
  private final String base;
  private static final String urlBase = "http://papyri.info/";
  private boolean success = true;
  public static String IDLE = "Currently idle.";
  public static String SYNCING = "Syncing files.";
  public static String MAPPING = "Mapping files.";
  public static String INFERENCING = "Generating inferences.";
  public static String PUBLISHING = "Publishing new files.";
  private static final String SOLR = "http://localhost:8983/";
  private String status = IDLE;
  private Date started;
  private Date lastrun;
  private static final Logger logger = Logger.getLogger("pn-sync");
  
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
    if (success && IDLE.equals(status)) {
      started = new Date();
      lastrun = started;
      try {
        status = SYNCING;
        String head = GitWrapper.getLastSync();
        logger.info("Syncing at " + new Date());
        GitWrapper.executeSync();
        logger.info(head + " = " + GitWrapper.getHead() + "?");
        if (!head.equals(GitWrapper.getHead())) {
          List<String> diffs = GitWrapper.getDiffs(head);
          List<String> files = new ArrayList<String>();
          List<String> urls = new ArrayList<String>();
          for (String diff : diffs) {
            String url = GitWrapper.filenameToUri(base + File.separator + diff, false);
            if (!"".equals(url)) {
              files.add(base + File.separator + diff);
              logger.debug(base + File.separator + diff);
              urls.add(url);
            }
          }
          if (files.size() > 0) {
            status = MAPPING;
            logger.info("Mapping " + files.size() +" files starting at " + new Date());
            map.mapFiles(files);
            status = INFERENCING;
            logger.info("Running inferencing on " + files.size() + " files starting at " + new Date());
            for (String url : urls) {
              logger.info("Run inferencing on '" + url +"'");
              map.insertInferences(url);
            }
            urls.clear();
            // Reload the url list, this time resolving files that should be aggregated.
            for (String diff : diffs) {
              String url = GitWrapper.filenameToUri(base + File.separator + diff, true);
              if (!"".equals(url)) {
                logger.info("Queued " + url);
                urls.add(url);
              }
            }
            status = PUBLISHING;
            logger.info("Generating pages starting at " + new Date());
            indexer.generatePages(urls);
            logger.info("Indexing files starting at " + new Date());
            indexer.index();
            logger.info("Loading bibliography starting at " + new Date());
            indexer.loadBiblio();
          } else {
            logger.info("No files to map.");
          }
        }
        status = IDLE;
        started = null;
      } catch (Exception e) {
        logger.error(e.getLocalizedMessage(), e);
        success = false;
      }
    }
  }
}
