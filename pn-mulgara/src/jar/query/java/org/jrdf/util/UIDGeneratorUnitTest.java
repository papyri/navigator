/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.util;

// Third party packages
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for testing the Unique Identifier Generator (UIDGenerator).
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner </a>
 */
public class UIDGeneratorUnitTest extends TestCase {

  /** Set of UIDs that have been generated. */
  private Set<String> uids;

  /** Number of UIDs generated. */
  private static final int NUM_UIDS = 10000;

  /** Number of Threads concurrently generating UIDs. */
  private static final int NUM_THREADS = 10;

  /** Number of ClassLoaders using UIDGenerator classes to genreateUIDs .*/
  private static final int NUM_CLASSLOADERS = 10;

  /** Short name of the UIDGenerator class. */
  private static final String CLASS_NAME = "UIDGenerator";

  /** Fully qualified name of the UIDGenerator class. */
  private static final String FULL_CLASS_NAME = "org.jrdf.util." + CLASS_NAME;

  /**
   * Constructs a new test with the given name.
   *
   * @param name
   *            the name of the test
   */
  public UIDGeneratorUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new UIDGeneratorUnitTest("testUID"));
    suite.addTest(new UIDGeneratorUnitTest("testConcurrency"));
//    suite.addTest(new UIDGeneratorUnitTest("testMultiClassLoader"));
    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    TestRunner.run(suite());
  }

  /**
   * Tests that UID are unique.
   *
   * @throws Exception
   */
  public void testUID() throws Exception {

    String currentUID = "";

    for (int i = 0; NUM_UIDS > i; i++) {

      currentUID = UIDGenerator.generateUID();

      //is it unique??
      if (uids.contains(currentUID)) {

        fail("UID set already contains UID [" + i + "]: " + currentUID);
      }

      uids.add(currentUID);
    }
  }

  /**
   * Tests that UID are unique when generated from differnet Threads.
   *
   * @throws Exception
   */
  public void testConcurrency() throws Exception {

    //threads that have to complete before the test finished
    List<Thread> threadList = new ArrayList<Thread>();
    Thread currentThread = null;

    //start threads that generate UIDs
    for (int i = 0; NUM_THREADS > i; i++) {
      //start a new thread that inserts and checks UIDS
      currentThread = new Thread(new Runnable() {

        /** run test */
        public void run() {
          try {
            String currentUID = "";
            int numUIDS = NUM_UIDS / NUM_THREADS;
            for (int i = 0; i < numUIDS; i++) {
              currentUID = UIDGenerator.generateUID();
              synchronized (UIDGeneratorUnitTest.class) {
                //is it unique??
                if (uids.contains(currentUID)) {
                  fail("UID set already contains UID [" + i
                      + "]: " + currentUID);
                }
                uids.add(currentUID);
              }
            }
          } catch (Exception exception) {
            throw new RuntimeException(
                "Error occurred while testing concurrency.",
                exception);
          }
        }
      });
      //end thread

      //keep reference
      threadList.add(currentThread);

      //start it
      currentThread.start();
    }

    //must wait for all threads to finish
    for (int i = 0; NUM_THREADS > i; i++) {
      threadList.get(i).join();
    }
  }

  /**
   * Tests the corner case of having multiple applications loaded by different
   * ClassLoaders.
   *
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void testMultiClassLoader() throws Exception {

    //load UIDGenerator from multiple classes
    URLClassLoader currentLoader = null;
    Class<UIDGenerator> currentUIDGeneratorClass = null;

    //get an URL to the UIDGenerator class file
    URL[] uidClass = new URL[] {
        ClassLoader.getSystemClassLoader().getResource(CLASS_NAME)};

    for (int i = 0; NUM_CLASSLOADERS > i; i++) {
      currentLoader = new URLClassLoader(uidClass);
      currentUIDGeneratorClass = (Class<UIDGenerator>)currentLoader.loadClass(FULL_CLASS_NAME);
      testUIDGeneratorClass(currentUIDGeneratorClass);
    }

  }

  /**
   * Uses the supplied UIDGenerator class to generate UIDs.
   *
   * @param uidGenerator
   * @throws Exception
   */
  private void testUIDGeneratorClass(Class<UIDGenerator> uidGenerator) throws Exception {
    //get the UIDGenerator's generateUID method
    Method generateUID = uidGenerator.getMethod("generateUID", (Class[])null);
    String currentUID = "";
    for (int i = 0; NUM_UIDS > i; i++) {
      currentUID = (String) generateUID.invoke(null, (Object[])null);
      //is it unique??
      if (uids.contains(currentUID)) {
        fail("UID set already contains UID [" + i + "]: " + currentUID);
      }
      uids.add(currentUID);
    }
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {

    try {

      uids = new HashSet<String>();

      super.setUp();
    } catch (Exception exception) {

      //try to tear down first
      tearDown();

      //then throw
      throw exception;
    }
  }

  /**
   * The teardown method for JUnit.
   *
   * @throws Exception
   */
  public void tearDown() throws Exception {

    uids.clear();

    //allow super to close down
    super.tearDown();
  }
}
