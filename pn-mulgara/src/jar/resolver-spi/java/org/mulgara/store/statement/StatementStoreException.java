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

package org.mulgara.store.statement;


// Java 2 standard packages
import java.io.*;

import org.mulgara.store.StoreException;

/**
 * Exception thrown by {@link StatementStore} operations.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StatementStoreException extends StoreException {

  /** Version ID for serialization. */
  private static final long serialVersionUID = -2415826145039347120L;

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * CONSTRUCTOR GraphException TO DO
   *
   * @param message PARAMETER TO DO
   */
  public StatementStoreException(String message) {
    super(message);
  }

  /**
   * CONSTRUCTOR GraphException TO DO
   *
   * @param message PARAMETER TO DO
   * @param throwable PARAMETER TO DO
   */
  public StatementStoreException(String message, Throwable throwable) {
    super(message + eol + stackTrace(throwable), throwable);
  }

  /**
   * CONSTRUCTOR GraphException TO DO
   *
   * @param throwable PARAMETER TO DO
   */
  public StatementStoreException(Throwable throwable) {
    super(stackTrace(throwable), throwable);
  }

  /**
   * METHOD TO DO
   *
   * @param throwable PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  private static String stackTrace(Throwable throwable) {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.print(throwable.getMessage());
    pw.println(':');
    throwable.printStackTrace(pw);

    return sw.toString();
  }
}
