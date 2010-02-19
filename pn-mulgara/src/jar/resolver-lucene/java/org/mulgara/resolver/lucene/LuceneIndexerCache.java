/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.lucene;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * A cache of lucene index-readers and index-writers. Opening a lucene index-reader or writer is
 * fairly expensive, so caching them can provide substantial performance gains. No cache-expiry
 * has been implemented, however; the assumption is that there will a limited number of lucene
 * models.
 *
 * <p>This also manages the setting up and removal of the index directory.
 *
 * @created 2008-09-28
 * @author Ronald Tschalär
 * @licence Apache License v2.0
 */
public class LuceneIndexerCache {
  private static final Logger logger = Logger.getLogger(LuceneIndexerCache.class);

  /* our caches */
  private final Stack<ReaderInfo> freeReaders = new Stack<ReaderInfo>();
  private final Stack<WriterInfo> freeWriters = new Stack<WriterInfo>();
  private final Map<IndexReader,ReaderInfo> allocdReaders = new HashMap<IndexReader,ReaderInfo>();
  private final Map<IndexWriter,WriterInfo> allocdWriters = new HashMap<IndexWriter,WriterInfo>();

  /** The Directory for Lucene.  */
  private final Directory luceneIndexDirectory;

  /** Whether this cache has been closed */
  private boolean closed = false;

  /**
   * Create a new cache.
   *
   * @param directory the directory to use for the indexes; it is created if it does not exist.
   * @throws IOException if the directory is not readable, writable, or some other error occurs
   *                     trying to access it
   */
  public LuceneIndexerCache(String directory) throws IOException {
    luceneIndexDirectory = FSDirectory.getDirectory(createOrValidateDirectory(directory));
    clearLocks();

    if (logger.isDebugEnabled()) {
      logger.debug("Fulltext string indexer cache initialized; directory =" + directory);
    }
  }

  private static File createOrValidateDirectory(String directory) throws IOException {
    File indexDirectory = new File(directory);

    // make the directory if it doesn't exist
    if (!indexDirectory.exists()) {
      indexDirectory.mkdirs();
    }

    // ensure the index directory is a directory
    if (!indexDirectory.isDirectory()) {
      logger.fatal("The fulltext string index directory '" + directory + "' is not a directory!");
      throw new IOException("The fulltext string index directory '" + directory +
                            "' is not a directory!");
    }

    // ensure the directory is writeable
    if (!indexDirectory.canWrite()) {
      logger.fatal("The fulltext string index directory '" + directory + "' is not writeable!");
      throw new IOException("The fulltext string index directory '" + directory +
                            "' is not writeable!");
    }

    return indexDirectory;
  }

  private void clearLocks() throws IOException {
    if (IndexWriter.isLocked(luceneIndexDirectory)) {
      /* This shouldn't happen unless mulgara was shut down abruptly since mulgara has a single
       * writer lock.
       */
      logger.warn("Fulltext index directory '" + luceneIndexDirectory + "' is locked; forcibly unlocking");
      IndexWriter.unlock(luceneIndexDirectory);
    }

    if (IndexWriter.isLocked(luceneIndexDirectory)) {
      throw new IOException("Fulltext index directory '" + luceneIndexDirectory + "' is locked; " +
                            "forced unlock failed; giving up");
    }
  }

  /**
   * Get an index-reader. It must be returned via {@link #returnReader}.
   *
   * @return the index-reader
   */
  public synchronized IndexReader getReader() throws IOException {
    if (closed) throw new IllegalStateException("IndexerCache has been closed: " + luceneIndexDirectory);

    ReaderInfo ri = freeReaders.pop();
    if (ri == null) {
      ri = new ReaderInfo(luceneIndexDirectory);

      if (logger.isDebugEnabled()) logger.debug("Creating new index-reader: " + ri.reader);
    } else if (ri.needsRefresh) {
      IndexReader reader = ri.reader.reopen();
      if (reader != ri.reader) {
        ri.reader.close();
        ri = new ReaderInfo(reader);

        if (logger.isDebugEnabled()) logger.debug("Refreshed index-reader: " + ri.reader);
      } else {
        ri.needsRefresh = false;
      }
    } else {
      if (logger.isDebugEnabled()) logger.debug("Reusing index-reader: " + ri.reader);
    }

    allocdReaders.put(ri.reader, ri);
    return ri.reader;
  }

  /**
   * Get an index-writer. It must be returned via {@link #returnWriter}.
   *
   * @return the index-writer
   */
  public synchronized IndexWriter getWriter() throws IOException {
    if (closed) throw new IllegalStateException("IndexerCache has been closed: " + luceneIndexDirectory);

    WriterInfo wi = freeWriters.pop();
    if (wi == null) {
      wi = new WriterInfo(luceneIndexDirectory);

      if (logger.isDebugEnabled()) logger.debug("Created new index-writer: " + wi.writer);
    } else if (wi.needsRefresh) {
      wi.writer.close();
      wi = new WriterInfo(luceneIndexDirectory);

      if (logger.isDebugEnabled()) logger.debug("Refreshed index-writer: " + wi.writer);
    } else {
      if (logger.isDebugEnabled()) logger.debug("Reusing index-writer: " + wi.writer);
    }

    allocdWriters.put(wi.writer, wi);
    return wi.writer;
  }

  /**
   * Return an index-reader to the cache. It must have been previously retrieved through {@link
   * #getReader}.
   *
   * @param reader the reader to return
   * @param close if true the reader is closed and not returned to the pool
   */
  public synchronized void returnReader(IndexReader reader, boolean close) {
    ReaderInfo ri = allocdReaders.remove(reader);

    if (close || closed) {
      try {
        reader.close();
        if (logger.isDebugEnabled()) logger.debug("Closed index-reader: " + reader);
      } catch (IOException ioe) {
        logger.warn("Error closing index-reader: " + reader);
      }
    } else {
      freeReaders.push(ri);
      if (logger.isDebugEnabled()) logger.debug("Returned index-reader: " + reader);
    }
  }

  /**
   * Return an index-writer to the cache. It must have been previously retrieved through {@link
   * #getWriter}.
   *
   * @param writer the writer to return
   * @param close if true the writer is closed and not returned to the pool
   */
  public synchronized void returnWriter(IndexWriter writer, boolean close) {
    WriterInfo wi = allocdWriters.remove(writer);

    if (close || closed) {
      try {
        writer.close();
        if (logger.isDebugEnabled()) logger.debug("Closed index-writer: " + writer);
      } catch (IOException ioe) {
        logger.warn("Error closing index-writer: " + writer);
      }
    } else {
      freeWriters.push(wi);
      if (logger.isDebugEnabled()) logger.debug("Returned index-writer: " + writer);
    }
  }

  /**
   * Notify the cache that the index has been modified. All newly returned indexers will be
   * appropriately refreshed.
   *
   * @param writer the writer that made the modification
   */
  public synchronized void indexModified(IndexWriter writer) {
    for (RefreshableObject ro : freeReaders) ro.needsRefresh = true;
    for (RefreshableObject ro : freeWriters) ro.needsRefresh = true;
    for (RefreshableObject ro : allocdReaders.values()) ro.needsRefresh = true;
    for (WriterInfo wi : allocdWriters.values()) {
      if (wi.writer != writer) wi.needsRefresh = true;
    }

    if (logger.isDebugEnabled()) logger.debug("All indexers marked for refresh");
  }

  /**
   * @return the directory being used for the indexes
   */
  public String getDirectory() {
    return luceneIndexDirectory.toString();
  }

  /**
   * Remove all index files from the current initialised directory. WARNING : All
   * files are removed in the specified directory. This is probably only useful for
   * testing. See {@link FullTextStringIndex#removeAll} for an alternate solution.
   *
   * @return return true if successful at removing all index files
   * @throws IOException if an exception occurs while attempting to delete the files
   */
  public synchronized boolean removeAllIndexes() throws IOException {
    if (allocdWriters.size() > 0 || allocdReaders.size() > 0) {
      logger.warn("Attempting to remove all indexes while readers or writers are still active");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Removing all indexes from " + luceneIndexDirectory);
    }

    for (String file : luceneIndexDirectory.list()) {
      luceneIndexDirectory.deleteFile(file);
    }

    return ((FSDirectory)luceneIndexDirectory).getFile().delete();
  }

  /**
   * Close this cache. All pooled index readers/writers are closed; readers/writers that are still
   * in use will be closed upon being returned.
   */
  public synchronized void close() {
    if (allocdWriters.size() > 0 || allocdReaders.size() > 0) {
      logger.warn("Attempting to close indexer-cache while readers or writers are still active");
    }

    closed = true;

    for (ReaderInfo ri : freeReaders) {
      try {
        ri.reader.close();
      } catch (IOException ioe) {
        logger.error("Error closing index-reader: " + ri.reader, ioe);
      }
    }

    for (WriterInfo wi : freeWriters) {
      try {
        wi.writer.close();
      } catch (IOException ioe) {
        logger.error("Error closing index-writer: " + wi.writer, ioe);
      }
    }

    if (logger.isDebugEnabled()) logger.debug("IndexerCacher closed: " + luceneIndexDirectory);
  }

  private static abstract class RefreshableObject {
    public boolean needsRefresh = false;
  }

  private static class ReaderInfo extends RefreshableObject {
    public final IndexReader reader;

    public ReaderInfo(Directory directory) throws IOException {
      reader = IndexReader.open(directory, true);
    }

    public ReaderInfo(IndexReader reader) {
      this.reader = reader;
    }
  }

  private static class WriterInfo extends RefreshableObject {
    public final IndexWriter writer;

    public WriterInfo(Directory directory) throws IOException {
      writer = new IndexWriter(directory, new StandardAnalyzer(), IndexWriter.MaxFieldLength.LIMITED);
    }
  }

  private static class Stack<T> extends ArrayList<T> {
    /** Serialization ID */
    private static final long serialVersionUID = -8597253123267228667L;

    public void push(T obj) {
      add(obj);
    }

    public T pop() {
      return size() > 0 ? remove(size() - 1) : null;
    }
  }
}
