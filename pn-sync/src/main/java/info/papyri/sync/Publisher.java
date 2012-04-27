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
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.methods.PostMethod;

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
  private static String SOLR = "http://localhost:8083/";
  private String status = IDLE;
  private Date started;
  private Date lastrun;
  private static Logger logger = Logger.getLogger("pn-sync");
  
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
        String head = GitWrapper.getLastSync();
        logger.info("Syncing at " + new Date());
        GitWrapper.executeSync();
        logger.info(head + " = " + GitWrapper.getHead() + "?");
        if (!head.equals(GitWrapper.getHead())) {
          List<String> diffs = GitWrapper.getDiffs(head);
          List<String> files = new ArrayList<String>();
          for (String diff : diffs) {
            files.add(base + File.separator + diff);
            logger.debug(base + File.separator + diff);
          }
          if (files.size() > 0) {
            status = MAPPING;
            logger.info("Mapping files starting at " + new Date());
            map.mapFiles(files);
            status = INFERENCING;
            for (String file : files) {
              map.insertInferences(file);
            }
            status = PUBLISHING;
            logger.info("Publishing files starting at " + new Date());
            List<String> urls = new ArrayList<String>();
            for (String diff : diffs) {
              urls.add(GitWrapper.filenameToUri(diff));
            }
            indexer.index(urls);
            success = callSolrMethod("commit");
            success = callSolrMethod("optimize");
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
  
  private boolean callSolrMethod(String action) {
    boolean result = false;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      StringBuilder uri = new StringBuilder();
      uri.append(SOLR);
      if ("commit".equals(action) || "optimize".equals(action)) {
        uri.append("solr/pn-search/update");
        PostMethod post = new PostMethod(uri.toString());
        post.setRequestBody("<" + action + " />");
        Document doc = db.parse(post.getResponseBodyAsStream());
        NodeList nl = doc.getElementsByTagName("int");
        for (int i = 0 ; i < nl.getLength(); i++) {
          Element elt = (Element)nl.item(i);
          if ("status".equals(elt.getAttribute("name"))) {
            if ("0".equals(elt.getTextContent())) {
              result = true;
              break;
            }
          }
        }
      }
    } catch (ParserConfigurationException pce) {
      logger.error("Your parser setup is wrong.", pce);
    } catch (org.xml.sax.SAXException saxe) {
      logger.error("Got back a non-XML response from Solr.", saxe);
    } catch (IOException ioe) {
      logger.error(ioe.getMessage(), ioe);
    }
    return result;
  }
  
}
