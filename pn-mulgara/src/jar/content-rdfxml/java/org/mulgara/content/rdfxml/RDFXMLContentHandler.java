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

package org.mulgara.content.rdfxml;

// Java 2 standard packages
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.ContentHandler;
import org.mulgara.content.ContentHandlerException;
import org.mulgara.content.ModifiedException;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.rdfxml.writer.RDFXMLWriter;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-09-10
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:03 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RDFXMLContentHandler implements ContentHandler
{
  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static Logger logger =
    Logger.getLogger(RDFXMLContentHandler.class.getName());

  /**
   * The MIME type of RDF/XML.
   */
  private static final MimeType APPLICATION_RDF_XML;

  static {
    try {
      APPLICATION_RDF_XML = new MimeType("application", "rdf+xml");
    }
    catch (MimeTypeParseException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  //
  // Methods implementing ContentHandler
  //

  public Statements parse(Content content, ResolverSession resolverSession)
                          throws ContentHandlerException
  {
    return new RDFXMLStatements(content, resolverSession);
  }

  /**
   * @return <code>true</code> if the file part of the URI has an
   *   <code>.n3</code>, <code>.nt</code> or <code>.rdf</code> extension
   */
  public boolean canParse(Content content) throws NotModifiedException
  {
    // We definitely can parse anything of MIME type application/rdf+xml
    MimeType contentType = content.getContentType();
    if (contentType != null && APPLICATION_RDF_XML.match(contentType)) {
      return true;
    }

    // Don't know about this MIME type, so sniff at the pathname extension
    // We're breaking the WWW architectural principle of URI Opacity by doing
    // so (see http://www.w3.org/TR/webarch/#uri-opacity).

    if (content.getURI() == null) {
      return false;
    }

    // Obtain the path part of the URI
    String path = content.getURI().getPath();
    if (path == null) {
      return false;
    }
    assert path != null;
    
    // We recognize a fixed set of extensions
    return path.endsWith(".owl") ||
           path.endsWith(".rdf") ||
           path.endsWith(".rdfs");
  }

  /**
   * @throws ContentHandlerException  {@inheritDoc}; this particular
   *   implementation doesn't implement this method and will always throw the
   *   exception
   */
  public void serialize(Statements      statements,
                        Content         content,
                        ResolverSession resolverSession)
      throws ContentHandlerException, ModifiedException {

    RDFXMLWriter rdfXmlWriter = new RDFXMLWriter();
    try {
      rdfXmlWriter.write(
        statements,
        resolverSession,
        new OutputStreamWriter(content.newOutputStream(), "utf-8")
      );
    } catch (QueryException e) {
      throw new ContentHandlerException("Failed to serialize RDF/XML to " + content.getURIString(), e);
    } catch (IOException e) {
      throw new ContentHandlerException("Failed to serialize RDF/XML to " + content.getURIString(), e);
    }
  }
}
