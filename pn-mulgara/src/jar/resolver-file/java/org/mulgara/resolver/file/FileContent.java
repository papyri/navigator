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

package org.mulgara.resolver.file;

// Java 2 standard packages
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.zip.*;

// Java 2 enterprise packages
import javax.activation.MimeType;

// Local packages
import org.jrdf.graph.BlankNode;
import org.mulgara.content.Content;

/**
 * Wrapper around a {@link File} to make it satisfy the {@link Content}
 * interface.
 *
 * @created 2004-09-15
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:26 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology</a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileContent implements Content {

  /**
   * A map containing any format-specific blank node mappings from previous
   * parses of this file.
   */
  private final Map<Object,BlankNode> blankNodeMap = new HashMap<Object,BlankNode>();

  /**
   * The wrapped file.
   */
  private File file;

  //
  // Constructor
  //

  public FileContent(URI uri) {
    this(new File(uri));
  }


  /**
   * Wrap a {@link File} as {@link Content}.
   */
  public FileContent(File file) {
    // Validate "file" parameter
    if (file == null) throw new IllegalArgumentException("Null \"file\" parameter");

    // Initialize fields
    this.file = file;
  }

  //
  // Methods implementing Content
  //

  public Map<Object,BlankNode> getBlankNodeMap() {
    return blankNodeMap;
  }

  /**
   * @return {@inheritDoc}; always returns <code>null</code> because Java
   *   {@link File}s don't have any inherent MIME type
   */
  public MimeType getContentType() {
    return null;
  }

  public URI getURI() {
    return file.toURI();
  }

  public InputStream newInputStream() throws IOException {
    InputStream inputStream = new FileInputStream(file);

    // Guess at transfer encoding (compression scheme) based on file extension
    if (file.getName().toLowerCase().endsWith(".gz")) {
      // The file name ends with ".gz", so assume it's a gzip'ed file
      inputStream = new GZIPInputStream(inputStream);
    } else if (file.getName().toLowerCase().endsWith(".zip")) {
      // The file name ends with ".zip", so assume it's a zip'ed file
      inputStream = new ZipInputStream(inputStream);

      // Assume first entry in the zip file is the one we want.
      ((ZipInputStream) inputStream).getNextEntry();
      
    }
    assert inputStream != null;

    return inputStream;
  }

  public OutputStream newOutputStream() throws IOException {
    OutputStream outputStream = new FileOutputStream(file);

    // Guess at transfer encoding (compression scheme) based on file extension
    if (file.getName().toLowerCase().endsWith(".gz")) {
      // The file name ends with ".gz", so assume it's a gzip'ed file
      outputStream = new GZIPOutputStream(outputStream);
    } else if (file.getName().toLowerCase().endsWith(".zip")) {
      // The file name ends with ".zip", so assume it's a zip'ed file
      outputStream = new ZipOutputStream(outputStream);

      // Assume first entry in the zip file is the one we want.
      ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(
        "dummy-specified-by-" + getClass()
      ));
    }
    assert outputStream != null;

    return outputStream;
  }

  /** @see org.mulgara.content.Content#getURIString() */
  public String getURIString() {
    return file.toURI().toString();
  }

}
