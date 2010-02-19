/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query.operation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimeType;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.GraphResource;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.server.Session;

/**
 * Represents a command to load data into a model.
 *
 * @created Aug 19, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Load extends DataInputTx<GraphResource> {

  /** The logger */
  static final Logger logger = Logger.getLogger(Load.class.getName());

  /** Graph resource form of the source URI */
  private final GraphResource srcRsc;

  /** The type of data that may be in a stream. */
  private MimeType contentType;

  /**
   * Build a load operation, loading data from one URI into a graph specified by another URI.
   * @param source The URI of the source of the RDF data.
   * @param graphURI The URI of the graph to receive the data.
   * @param local Set to <code>true</code> to indicate that the source is on the client system.
   */
  public Load(URI source, URI graphURI, boolean local) {
    super(source, graphURI, graphURI, local);
    
    // Validate arguments.
    if (graphURI == null) throw new IllegalArgumentException("Need a valid destination graph URI");
    
    srcRsc = source == null ? null : new GraphResource(source);
    contentType = null;
  }


  /**
   * Alternate constructor for creating a load operation whose source will be a local input stream.
   * @param graphURI The URI of the graph to receive the data.
   * @param stream The local input stream that is the source of data to load.
   * @param contentType the content type for the stream.
   */
  public Load(URI graphURI, InputStream stream, MimeType contentType) {
    this(null, graphURI, true);
    setOverrideInputStream(stream);
    this.contentType = contentType;
  }

  /**
   * Alternate constructor for creating a load operation whose source will be a local input stream,
   * and a filename has been provided. The filename is for informative purposes only.
   * @param graphURI The URI of the graph to receive the data.
   * @param stream The local input stream that is the source of data to load.
   * @param contentType the content type for the stream.
   * @param file A string form of the uri of the file to load.
   */
  public Load(URI graphURI, InputStream stream, MimeType contentType, String file) {
    this(toUri(file), graphURI, true);
    setOverrideInputStream(stream);
    this.contentType = contentType;
  }

  /**
   * Load the data into the destination graph through the given connection.
   * @param conn The connection to load the data over.
   * @return The number of statements that were inserted.
   */
  public Object execute(Connection conn) throws QueryException {
    URI src = getSource();
    URI dest = getDestination();

    if (isLocal() && !conn.isRemote() && overrideInputStream == null) {
      logger.error("Used a LOCAL modifier when loading <" + src + "> to <" + dest + "> on a non-remote server.");
      throw new QueryException("LOCAL modifier is not valid for LOAD command when not using a client-server connection.");
    }

    try {
      long stmtCount = isLocal() ? sendMarshalledData(conn, true) : doTx(conn, srcRsc);
      if (logger.isDebugEnabled()) logger.debug("Loaded " + stmtCount + " statements from " + src + " into " + dest);
  
      if (stmtCount > 0L) setResultMessage("Successfully loaded " + stmtCount + " statements from " + 
          (src != null ? src : "input stream") + " into " + dest);
      else setResultMessage("WARNING: No valid RDF statements found in " + (src != null ? src : "input stream"));
      
      return stmtCount;
      
    } catch (IOException ex) {
      logger.error("Error attempting to load : " + src, ex);
      throw new QueryException("Error attempting to load : " + src, ex);
    }
  }


  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.io.InputStream)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final InputStream inputStream) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        return session.setModel(inputStream, getDestination(), srcRsc, contentType);
      }
    };
  }


  /* (non-Javadoc)
   * @see org.mulgara.query.operation.DataInputTx#getExecutable(java.net.URI)
   */
  @Override
  protected SessionOp<Long,QueryException> getExecutable(final GraphResource src) {
    return new SessionOp<Long,QueryException>() {
      public Long fn(Session session) throws QueryException {
        return session.setModel(getDestination(), src);
      }
    };
  }


  /**
   * Get the text of the command, or generate a virtual command if no text was parsed.
   * @return The query that created this command, or a generated query if no query exists.
   */
  public String getText() {
    String text = super.getText();
    if (text == null || text.length() == 0) text = "load <" + getSource() + "> into <" + getDestination() + ">";
    return text;
  }


  /**
   * Attempt to turn a filename into a URI. If unsuccessful return null.
   * @param filename The path for a file.
   * @return The URI for the file, or null if the filename could not be converted.
   */
  private static URI toUri(String filename) {
    if (filename == null) return null;
    try {
      return new URI(Mulgara.VIRTUAL_NS + filename);
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
