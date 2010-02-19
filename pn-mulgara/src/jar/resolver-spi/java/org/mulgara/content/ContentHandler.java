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

package org.mulgara.content;

// Local packages
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Represents a parser for a given Content type.  Conceptually each ContentHandler
 * consists of a function 'parse' of type Content -&gt; (Statements || CorruptContentException); 
 * and a predicate 'canParse' of type Content -&gt; boolean.  If canParse returns true, parse should
 * return one of Statements or CorruptContentException.  If canParse returns false then parse
 * may throw a generic ContentHandlerException.
 *
 * @created 2004-09-10
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface ContentHandler {
  /**
   * Parse {@link Content} into {@link Statements}.
   *
   * Note that just because this method successfully returns a
   * {@link Statements} instance does not require that the underlying
   * content be fully parsed.  Hence it is possible for subsequent use of the 
   * Statements to fail with a parse error.
   *
   * @param content  the document to parse, never <code>null</code>
   * @param resolverSession  the context within which to localize RDF resources
   *   contained within the <var>content</var>
   * @return the statements encoded in the <var>content</var>, expressed in
   *   the <var>resolverSession</var>'s local nodes
   * @throws CorruptContentException if the <var>content</var> is definitely
   *   of the correct format (as indicated by file magic, for instance) but
   *   not valid for that format
   * @throws ContentHandlerException if statements couldn't be obtained for any
   *   other reason
   * @throws NotModifiedException if parsing was aborted because a valid
   *   cached version of it is available
   */
  public Statements parse(Content content, ResolverSession resolverSession)
    throws ContentHandlerException, NotModifiedException;

  /**
   * Estimate whether the given content can be parsed by this handler.
   *
   * Typically, this is used to check for a known file extension in the
   * <var>content</var> URI, even through doing so violates the WWW
   * architectural principle of
   * <a href="http://www.w3.org/TR/webarch/#uri-opacity">URI opacity</a>.
   *
   * @param content  a candidate for the <var>content</var> of the
   *   {@link #parse} method
   * @return whether the <var>content</var> is likely to be successfully parsed
   * @throws NotModifiedException if we don't need to parse the
   *   <var>content</var>, because it's already cached
   */
  public boolean canParse(Content content) throws NotModifiedException;

  /**
   * Serialize {@link Statements} into {@link Content}.
   *
   * This method blocks until the serialization is complete.  It overwrites the
   * previous contents with only the {@link Statements} specified; it does not
   * append/merge them.
   *
   * @param statements  the new statements to put into the <var>content</var>
   * @param content  the document to populate
   * @param resolverSession  the context in which to globalize local node
   *   numbers contained within the <var>statements</var>
   * @throws ContentHandlerException  if the serialization failed; whether or
   *   not this can corrupt the {@link Content} depends on whether this
   *   particular handler is transactional
   * @throws ModifiedException if serialization was aborted because the
   *   <var>content</var> was detected to have been modified by a third party
   */
  public void serialize(Statements      statements,
                        Content         content,
                        ResolverSession resolverSession)
    throws ContentHandlerException, ModifiedException;
}
