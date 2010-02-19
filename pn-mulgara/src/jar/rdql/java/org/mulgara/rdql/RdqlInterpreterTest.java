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

package org.mulgara.rdql;

// third party packages
import junit.framework.*;

// Java 2 standard packages
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

// automagically generated classes
import org.mulgara.rdql.lexer.LexerException;
import org.mulgara.rdql.parser.ParserException;

/**
* Unit test for {@link RdqlInterpreter}.
*
* @created 2004-02-04
*
* @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
*
* @version $Revision: 1.8 $
*
* @modified $Date: 2005/01/05 04:58:21 $ by $Author: newmana $
*
* @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
*
* @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
*      Software Pty Ltd</a>
*
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class RdqlInterpreterTest extends TestCase
{
  /**
  * Logger.
  */
  private static Logger logger =
      Logger.getLogger(RdqlInterpreterTest.class.getName());

  /**
  * Test instance.
  */
  private static RdqlInterpreter interpreter = null;

  //
  // Public API
  //

  /**
  * Constructs a new RdqlInterpreter unit test.
  *
  * @param name the name of the test
  */
  public RdqlInterpreterTest(String name)
  {
    // delegate to super class constructor
    super(name);

    // load the logging configuration
    try {
      DOMConfigurator.configure(
        System.getProperty("cvs.root") + "/log4j-conf.xml"
      );
    }
    catch (FactoryConfigurationError fce) {
      logger.error(
        "Unable to configure logging service from XML configuration file"
      );
    }
  }

  /**
  * Returns a test suite containing the tests to be run.
  *
  * @return the test suite
  */
  public static TestSuite suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new RdqlInterpreterTest("testParseQuery"));
    return suite;
  }

  /**
  * Initialise members.
  *
  * @throws Exception if something goes wrong
  */
  protected void setUp() throws Exception
  {
    interpreter = new RdqlInterpreter(null);
  }

  /**
  * Default text runner.
  *
  * @param args the command line arguments
  */
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //
  // Test cases
  //

  /**
  * Test for {@link RdqlInterpreter#parseQuery}.
  *
  * @throws Exception EXCEPTION TO DO
  */
  public void testParseQuery() throws Exception
  {
    File dir = new File(System.getProperty("cvs.root"));
    dir = new File(dir, "test");
    dir = new File(dir, "RDQL");
    File testFiles[] = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("test-");
      }
    });
    for (int i = 0; i<testFiles.length; i++) {
      System.err.println("Testing "+testFiles[i]);
      if (testFiles[i].getName().equals("test-8-04") ||
          testFiles[i].getName().equals("test-8-05"))
      {
        continue;
      }
      
      // Read the file into a string and parse it
      RandomAccessFile file = new RandomAccessFile(testFiles[i], "r");
      byte[] bytes = new byte[(int) file.length()];
      file.read(bytes);
      //interpreter.parseQuery((new String(bytes))+";");
    }
  }
}
