/*
 * Copyright 2008 Fedora Commons, Inc.
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


package org.mulgara.content.rlog;

// Java 2 enterprise packages
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ModifiedException;
import org.mulgara.content.NotModifiedException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints in models defined by RLog documents.
 *
 * @created Feb 24, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RlogContentHandler implements ContentHandler {
  /** Logger. */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(RlogContentHandler.class);

  /** The MIME type of RLog. */
  private static final MimeType TEXT_RLOG;

  static {
    try {
      TEXT_RLOG    = new MimeType("text", "rlog");
    } catch (MimeTypeParseException e) {
      throw new ExceptionInInitializerError(e);
    }
  }


  public Statements parse(Content content, ResolverSession resolverSession) throws ContentHandlerException {
    return new RlogStatements(content, resolverSession);
  }


  /**
   * Tests if this content handler can handle the provided content.
   * @return <code>true</code> if the file part of the URI has an
   *         <code>.rl</code>, or <code>.rlog</code> extension
   * @throws NotModifiedException If the content has been accessed before
   *         and not modified in the meantime.
   */
  public boolean canParse(Content content) throws NotModifiedException {
    // We definitely can parse anything of MIME type text/rlog
    MimeType contentType = content.getContentType();
    if (contentType != null && (TEXT_RLOG.match(contentType))) return true;

    // Don't know about this MIME type, so sniff at the pathname extension
    // We're breaking the WWW architectural principle of URI Opacity by doing
    // so (see http://www.w3.org/TR/webarch/#uri-opacity).

    if (content.getURI() == null) return false;

    // Obtain the path part of the URI
    String path = content.getURI().getPath();
    if (path == null) return false;

    // We recognize a fixed set of extensions
    return  path.endsWith(".dl") || path.endsWith(".rl") || path.endsWith(".rlog");
  }


  /**
   * Writes out the statements in basic RLog format.
   */
  public void serialize(Statements statements, Content content, ResolverSession resolverSession)
          throws ContentHandlerException, ModifiedException {
    try {
      Writer out = new BufferedWriter(new OutputStreamWriter(content.newOutputStream(), "utf-8"));
      RlogStructure struct = new RlogStructure(resolverSession);
      struct.load(statements);
      struct.write(out);
      out.close();
    } catch (Exception e) {
      throw new ContentHandlerException("Failed to serialize RLog to " + content.getURIString(), e);
    }
  }
}
