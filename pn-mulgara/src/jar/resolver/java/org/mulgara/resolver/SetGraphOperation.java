/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.io.*;
import java.net.URI;

import javax.activation.MimeType;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.content.ContentLoader;
import org.mulgara.content.NotModifiedException;
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;

/**
 * An {@link Operation} that implements the
 * {@link org.mulgara.server.Session#setModel(URI, GraphExpression)} and
 * {@link org.mulgara.server.Session#setModel(InputStream, URI, GraphExpression, MimeType)} methods.
 *
 * @created 2004-11-04
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/02 20:07:56 $ by $Author: raboczi $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class SetGraphOperation implements Operation {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(SetGraphOperation.class.getName());

  private final URI         srcModelURI;
  private final URI         destModelURI;
  private final InputStream inputStream;
  private final MimeType    contentType;

  private Content               content;
  private final ContentHandlerManager contentHandlers;
  private final DatabaseSession databaseSession;

  /**
   * Number of statements loaded, or -1 if the {@link #execute} method hasn't yet been
   * invoked.
   */
  private long statementCount = -1;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param srcModelURI the {@link URI} of the model to be redefined
   * @param destModelURI the {@link URI} of the model to be redefined
   * @param inputStream  a stream containing the content at the <var>srcModelURI</var>
   *   or <code>null</code> if the content must be fetched
   */
  SetGraphOperation(URI         srcModelURI,
                    URI         destModelURI,
                    InputStream inputStream,
                    MimeType    contentType,
                    ContentHandlerManager contentHandlers,
                    DatabaseSession databaseSession)
  {
    this.srcModelURI      = srcModelURI;
    this.destModelURI     = destModelURI;
    this.inputStream      = inputStream;
    this.contentType      = contentType;
    this.contentHandlers  = contentHandlers;
    this.databaseSession  = databaseSession;
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception
  {
    // A prior call to execute will have set statementCount >= 0.
    if (statementCount != -1) {
      throw new IllegalStateException("SetGraphOperation already executed.  Cannot reexecute.");
    }

    long destinationModel = systemResolver.localize(new URIReferenceImpl(destModelURI));
    try {
      long sourceModel = systemResolver.localize(new URIReferenceImpl(srcModelURI, false));
      if (destinationModel == sourceModel) throw new QueryException("Identical source and destination: " + destModelURI);
    } catch (Exception e) {
      // source and destinations cannot be equal, so ignore
    }

    // update the destination to the canonical form
    destinationModel = operationContext.getCanonicalModel(destinationModel);

    // Obtain a resolver for the destination model type
    Resolver destinationResolver = operationContext.obtainResolver(
      operationContext.findModelResolverFactory(destinationModel));
    assert destinationResolver != null;

    ContentHandler contentHandler = contentHandlers.getContentHandler(content);

    Statements statements = contentHandler.parse(content,
        new PersistentResolverSession(systemResolver));

    if (logger.isDebugEnabled()) {
      logger.debug("Modifying " + destModelURI + " using " + destinationResolver);
    }

    destinationResolver.modifyModel(destinationModel, statements, true);
    if (logger.isDebugEnabled()) {
      logger.debug("Modified " + destModelURI);
    }

    statementCount = statements.getRowCount();
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation() {
    return true;
  }


  //
  // Operation result accessor
  //

  /**
   * @return the number of statements loaded
   * @throws IllegalStateException  if {@link #execute} hasn't yet been called
   */
  long getStatementCount()
  {
    if (statementCount == -1) {
      throw new IllegalStateException("Statement count not available before execution.");
    }

    return statementCount;
  }


  /**
   * This method is a refactoring hack, purely to get all the code which
   * depends on content handler and resolver components out of the core
   * <code>resolver</code> component.
   *
   * @return whether {@link #execute} should be called next
   */
  boolean preExecute() throws QueryException
  {
    // create a StreamContent if an inputStream has been supplied
    if (inputStream != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Detected inputstream associated with " + srcModelURI );
      }
      if (srcModelURI == null) {
        content = new StreamContent(inputStream, contentType);
      } else {
        content = new StreamContent(inputStream, srcModelURI);
      }
    } else {
      try {
        content = ContentFactory.getContent(srcModelURI);
      } catch (ContentHandlerException ec) {
        throw new QueryException("Failed to find Content for uri", ec);
      }
    }

    ContentLoader loader;
    try {
      loader = contentHandlers.getContentLoader(content);
    } catch (NotModifiedException e) {
      throw new QueryException("Content is already cached", e);
    } catch (ContentHandlerException ec) {
      throw new QueryException("Failure finding contentLoader");
    }

    if (loader != null) {
      try {
        statementCount = loader.load(content, new GraphResource(destModelURI), databaseSession);

        return false; // notifies caller that a call to execute will fail.
      } catch (ContentHandlerException handlerException) {
        throw new QueryException("Failed to setModel.", handlerException);
      }
    }

    return true;
  }
}
