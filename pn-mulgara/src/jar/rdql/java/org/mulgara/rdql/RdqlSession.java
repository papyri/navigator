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

// Java 2 standard packages
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

// Third party packages
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

// Automatically generated packages (SableCC)
import org.mulgara.rdql.analysis.DepthFirstAdapter;
import org.mulgara.rdql.lexer.Lexer;
import org.mulgara.rdql.lexer.LexerException;
import org.mulgara.rdql.node.*;
import org.mulgara.rdql.parser.Parser;
import org.mulgara.rdql.parser.ParserException;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.server.Session;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;

import javax.swing.*;

/**
 * Interactive TQL session command line shell.
 *
 * @created 2001-Aug-17
 *
 * @author Simon Raboczi
 * @author Tom Adams
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:22 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RdqlSession {

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * The prompt.
   */
  public final static String PROMPT = "RDQL> ";

  /**
   * The secondary prompt, indicating an incomplete command.
   */
  public final static String PROMPT2 = "      ";

  /**
   * The CLI version.
   */
  public final static String VERSION = "0.1";

  //
  // Constants
  //

  /**
   * the log4j configuration file path (withing the JAR file)
   */
  private final static String LOG4J_CONFIG_PATH = "log4j-rdql.xml";

  /**
   * the logging category to log to
   */
  private final static Logger log = Logger.getLogger(RdqlSession.class);

  /**
   * the default path to the pre-loading script
   */
  private final static String PRE_LOADING_SCRIPT_PATH = "default-pre.rdql";

  /**
   * the default path to the post-loading script
   */
  private final static String POST_LOADING_SCRIPT_PATH = "default-post.rdql";

  //
  // Private state
  //

  /**
   * The graphical UI.
   */
  private static RdqlSessionUI ui;

  /**
   * The message from the previous query.
   */
  private StringBuffer message = new StringBuffer();

  //
  // Members
  //

  /**
   * the RDQL interpreter associated with this session
   */
  private RdqlInterpreterBean rdqlBean = null;

  /**
   * the URL of the post-loading script
   */
  private URL postLoadingScriptURL = null;

  /**
   * the URL of the pre-loading script
   */
  private URL preLoadingScriptURL = null;

  /**
   * the multiplexer session connecting us to various databases
   */
  private Session session;

  //
  // Constructors
  //

  /**
   * Creates a new RDQL session.
   *
   * @throws IOException if the logging configuration can't be read
   * @throws QueryException if a session to the server can't be opened
   * @throws SessionFactoryFinderException if a server can't be located
   */
  public RdqlSession()
    throws IOException, QueryException, SessionFactoryFinderException {

    // load the default logging configuration
    this.loadLoggingConfig();

    // set the session
    session = SessionFactoryFinder.newSessionFactory(null).newSession();

    // set the interpreter
    this.setInterpreter(new RdqlInterpreterBean(session));
  }

  /**
   * Initiates a session using the given <code>session</code>
   *
   * @param session the interactive session to issue commands to
   * @param in the stream to read commands from
   * @param out the stream to print responses to
   */
  public static void session(RdqlSession session, InputStream in,
      PrintStream out) {

    ui.print("@@build.label@@" + eol + "Command Line Interface" + eol +
        "Copyright (C) 2001-2003 Plugged In Software Pty Ltd" + eol);

    // print a prompt
    ui.print(PROMPT);
  }

  /**
   * Start an interactive TQL session.
   *
   * @param args command line parameters
   * @throws IOException EXCEPTION TO DO
   * @throws QueryException EXCEPTION TO DO
   * @throws SessionFactoryFinderException EXCEPTION TO DO
   */
  public static void main(String[] args)
    throws IOException, QueryException, SessionFactoryFinderException {

    // create a new session to work with
    RdqlSession session = new RdqlSession();

    // Create the UI.
    JFrame app = new JFrame("RDQL Text UI");
    app.setSize(640, 480);
    app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    ui = new RdqlSessionUI(session);
    app.getContentPane().add(ui);
    app.setVisible(true);

    try {

      // set the default pre- and post-loading scripts
      session.retrieveDefaultLoadingScripts();

      // create a parser to parse the command line options
      RdqlOptionParser optsParser = new RdqlOptionParser(args);

      // parse the
      optsParser.parse();

      // process the options
      boolean startSession = session.processOptions(optsParser);

      // log that we've processed the options
      log.debug("Processed command line options");

      // execute the pre-loading script - we need to do this after we get the
      // command line options as we can override the defaults
      session.executeLoadingScript(session.getPreLoadingScriptURL());

      // log that we've executed the pre-loading script
      log.debug("Executed pre-loading script");

      // if we can, execute this session using std in and std out
      if (startSession) {

        log.info("Starting RDQL interpreter");
        RdqlSession.session(session, System.in, System.out);
        log.info("Stopping RDQL interpreter");
      }

      // end if
    }
    catch (RdqlOptionParser.UnknownOptionException uoe) {

      // log that the option we received was invalid
      log.warn("Invalid command line option specified: " + uoe.getOptionName());

      // let the user know about the invalid option
      System.err.println("Invalid option: " + uoe.getOptionName());

      // print the usage instructions
      session.printUsage();
    }
    catch (RdqlOptionParser.IllegalOptionValueException iove) {

      // format the incorrect option string
      String optionMsg =
          "-" + iove.getOption().shortForm() + ", --" +
          iove.getOption().longForm() + " = " + iove.getValue();

      // log that the option value we received was invalid
      log.warn("Invalid command line option value specified: " + optionMsg);

      // let the user know about the invalid option
      System.err.println("Invalid option value: " + optionMsg);

      // print the usage instructions
      session.printUsage();
    }

    // try-catch
    // execute the post-loading script
    session.executeLoadingScript(session.getPostLoadingScriptURL());

    // log that we've executed the pre-loading script
    log.debug("Executed post-loading script");
  }

  // RdqlSession()
  //
  // Methods overriding the AbstractSession superclass
  //

  /**
   * Returns the message from the execution of the last command.
   *
   * @return the message from the execution of the last command.
   */
  public String getLastMessage() {

    return message.toString();
  }

  /**
   * Executes the given command and returns its result.
   *
   * @param command the command to execute
   * @return the results of the command in human-readable format
   */
  public Answer executeCommand(String command) {

    try {

      // log the command we're executing
      log.debug("Starting execution of command \"" + command + "\"");

      // execute the command
      Answer answer = getInterpreter().executeQuery(command);

      // close the session if requested
      if (this.getInterpreter().isQuitRequested()) {

        this.close();
      }

      message = new StringBuffer(getInterpreter().getLastMessage());

      // log that we've executed the command
      log.debug("Completed execution of commmand \"" + command + "\"");
      return answer;
    }
    catch (UnsupportedOperationException uoe) {

      message.append(uoe.getMessage());
      log.warn("Couldn't execute command", uoe);
    }
    catch (Exception e) {

      message.append(e.getMessage());
      log.error("Couldn't execute command", e);
    }
    return null;
  }

  // loadLoggingconfig()

  /**
   * Sets the RDQL interpreter associated with this session.
   *
   * @param newRdqlBean the RDQL interpreter associated with this session
   */
  private void setInterpreter(RdqlInterpreterBean newRdqlBean) {

    rdqlBean = newRdqlBean;
  }

  // getInterpreter()

  /**
   * Sets the URL of the pre-loading script.
   *
   * @param preLoadingScriptURL the URL of the pre-loading script
   */
  private void setPreLoadingScriptURL(URL preLoadingScriptURL) {

    this.preLoadingScriptURL = preLoadingScriptURL;
  }

  // getPreLoadingScriptURL()

  /**
   * Sets the URL of the post-loading script.
   *
   * @param postLoadingScriptURL the URL of the post-loading script
   */
  private void setPostLoadingScriptURL(URL postLoadingScriptURL) {

    this.postLoadingScriptURL = postLoadingScriptURL;
  }

  // setInterpreter()

  /**
   * Returns the RDQL interpreter associated with this session.
   *
   * @return the RDQL interpreter associated with this session
   */
  private RdqlInterpreterBean getInterpreter() {

    return rdqlBean;
  }

  // setPreLoadingScriptURL()

  /**
   * Returns the URL of the pre-loading script.
   *
   * @return the URL of the pre-loading script
   */
  private URL getPreLoadingScriptURL() {

    return this.preLoadingScriptURL;
  }

  // setPostLoadingScriptURL()

  /**
   * Returns the URL of the post-loading script.
   *
   * @return the URL of the post-loading script
   */
  private URL getPostLoadingScriptURL() {

    return this.postLoadingScriptURL;
  }

  // command()

  /**
   * Closes the session associated with this interpreter. Subclasses that
   * override this method <strong>must</strong> call <code>super.close()</code>.
   */
  private void close() {

    // shut down the multiplexing proxy to various servers
    try {
      session.close();
    }
    catch (QueryException e) {
      log.warn("Unable to close session", e);
    }

    // Exit program
    System.exit(0);
  }

  // main()
  //
  // Internal methods
  //

  /**
   * Retrieves the default loading scripts.
   */
  private void retrieveDefaultLoadingScripts() {

    // locate the pre-loading script
    URL preScriptURL = this.locateScript(PRE_LOADING_SCRIPT_PATH);

    if (preScriptURL != null) {

      this.setPreLoadingScriptURL(preScriptURL);
    }

    // end if
    // locate the post-loading script
    URL postScriptURL = this.locateScript(POST_LOADING_SCRIPT_PATH);

    if (postScriptURL != null) {

      this.setPostLoadingScriptURL(postScriptURL);
    }

    // end if
  }

  // retrieveDefaultLoadingScripts()

  /**
   * Locates the loading script with the given path. <p>
   *
   * This locates scripts in the following order:</p>
   * <ol>
   *   <li> Current working directory;</li>
   *   <li> System classpath (if embedded in a JAR).</li>
   * </ol>
   * <p>
   *
   * Note. These could be overwritten by the command-line options <code>-o</code>
   * and <code>-p</code>. </p>
   *
   * @param scriptPath the path to the script to locate
   * @return a URL to the script, null if the script could not be found
   */
  private URL locateScript(String scriptPath) {

    URL scriptURL = null;

    // find the current directory
    String currentDirectory = System.getProperty("user.dir");

    // append a "/" if we need to
    if (!currentDirectory.endsWith("/")) {

      currentDirectory += File.separator;
    }

    // end if
    // log that we're looking for scripts
    log.debug("Looking for script " + scriptPath + " in " + currentDirectory);

    // try to find the script
    File loadingScript = new File(currentDirectory + scriptPath);

    if (loadingScript.exists() && loadingScript.isFile()) {

      // log that we've found the file
      log.debug("Found loading script - " + loadingScript);

      // return the URL!!!
      try {

        scriptURL = loadingScript.toURL();
      }
      catch (MalformedURLException mue) {

        // log the error
        log.warn("Unable to convert loading script filename to URL - " +
            mue.getMessage());
        System.err.println("Unable to convert loading script filename " +
            "to URL - " + loadingScript);
      }

      // try-catch
    }
    else {

      // log that we're now looking in the classpath
      log.debug("Looking for loading script " + scriptPath + " in classpath");

      // try to obtain from the classpath
      URL loadingScriptURL = ClassLoader.getSystemResource(scriptPath);

      // set it
      if (loadingScriptURL != null) {

        log.debug("Found loading script at - " + loadingScriptURL);
        scriptURL = loadingScriptURL;
      }

      // end if
    }

    // end if
    // return the URL
    return scriptURL;
  }

  // locateScript()

  /**
   * Executes the pre-loading script.
   *
   * @param loadingScriptURL the URL of the loading (pre/post) script to execute
   */
  private void executeLoadingScript(URL loadingScriptURL) {

    // execute it
    if (loadingScriptURL != null) {

      // log that we're executing the script
      log.debug("Executing loading script " + loadingScriptURL);

      // execute the script
      this.executeScript(loadingScriptURL);
    }

    // end if
  }

  // executeLoadingScript()

  /**
   * Processes the command line options passed to the interpreter.
   *
   * @param parser the command line option parser to use to parse the command
   *      line options
   * @return RETURNED VALUE TO DO
   */
  private boolean processOptions(RdqlOptionParser parser) {

    // log that we're processing command line options
    log.debug("Processing command line options");

    // flag to indicate whether we can start the interpreter
    boolean startInterpreter = true;

    try {

      // find out if the user wants help
      if (parser.getOptionValue(RdqlOptionParser.HELP) != null) {

        // print the help
        this.printUsage();

        // don't start the interpreter
        startInterpreter = false;
      }
      else {

        // dump the interpreter configuration
        Object dumpConfig = parser.getOptionValue(RdqlOptionParser.DUMP_CONFIG);

        if (dumpConfig != null) {

          this.dumpConfig();
        }

        // end if
        // load an external interpreter configuration file
        Object rdqlConf = parser.getOptionValue(RdqlOptionParser.RDQL_CONFIG);

        if (rdqlConf != null) {

          this.loadRdqlConfig(new URL( (String) rdqlConf));
        }

        // end if
        // load an external logging configuration file
        Object logConf = parser.getOptionValue(RdqlOptionParser.LOG_CONFIG);

        if (logConf != null) {

          this.loadLoggingConfig(new URL( (String) logConf));
        }

        // end if
        // find out whether to execute pre-and post loading scripts
        Object defaultLoadingScripts =
            parser.getOptionValue(RdqlOptionParser.NO_LOAD);

        if (defaultLoadingScripts == null) {

          // override the default pre-loading script
          Object preScript = parser.getOptionValue(RdqlOptionParser.PRE_SCRIPT);

          if (preScript != null) {

            this.setPreLoadingScriptURL(new URL( (String) preScript));
          }

          // end if
          // override the default post-loading script
          Object postScript =
              parser.getOptionValue(RdqlOptionParser.POST_SCRIPT);

          if (postScript != null) {

            this.setPostLoadingScriptURL(new URL( (String) preScript));
          }

          // end if
        }
        else {

          // log that we've turned off pre- and post-loading scripts
          log.debug("Pre- and post-loading scripts disabled");

          // unset default pre- and post-loading scripts
          this.setPreLoadingScriptURL(null);
          this.setPostLoadingScriptURL(null);
        }

        // end if
        // execute an RDQL script and quit
        Object script = parser.getOptionValue(RdqlOptionParser.SCRIPT);

        if (script != null) {

          this.executeScript(new URL( (String) script));
          startInterpreter = false;
        }

        // end if
      }

      // end if
    }
    catch (IOException ioe) {

      // log the error
      log.warn("Invalid URL on command line - " + ioe.getMessage());

      // print the usage
      System.err.println("Invalid URL - " + ioe.getMessage());
      this.printUsage();

      // don't start the interpreter
      startInterpreter = false;
    }
    catch (Exception e) {

      // log the error
      log.warn("Could not start interpreter - " + e.getMessage());

      // let the user know
      System.err.println("Error - " + e.getMessage());

      // don't start the interpreter
      startInterpreter = false;
    }

    // try-catch
    // return the continue flag
    return startInterpreter;
  }

  // processOptions()

  /**
   * Prints the usage instructions for the interpreter.
   */
  private void printUsage() {

    // build the usage message
    StringBuffer usage = new StringBuffer();
    usage.append("Usage: java -jar <jarfile> ");

    //usage.append("[-d|");
    //usage.append("-g|");
    //usage.append("-h] ");
    usage.append("[-h|");
    usage.append("-n] ");

    //usage.append("[-i <url>] ");
    usage.append("[-l <url>] ");
    usage.append("[-o <url>] ");
    usage.append("[-p <url>] ");
    usage.append("[-s <url>]");
    usage.append(eol);
    usage.append(eol);

    //usage.append("-d, --dumpconfig    dump the interpreter configuration to " +
    //    "the current directory\n");
    //usage.append("-g, --gui           display the RDQL GUI  ");
    usage.append("-h, --help          display this help screen" + eol);
    usage.append("-n, --noload        do not execute pre- and post-loading " +
        "scripts (useful with -s)" + eol);

    //usage.append("-i, --rdqlconfig    use an external configuration file\n");
    usage.append("-l, --logconfig     use an external logging configuration " +
        "file" + eol);
    usage.append("-o, --postload      execute an RDQL script after " +
        "interpreter stops," + eol);
    usage.append("                    overriding default post-loading script" +
        eol);
    usage.append("-p, --preload       execute an RDQL script before " +
        "interpreter starts," + eol);
    usage.append("                    overriding default pre-loading script" +
        eol);
    usage.append("-s, --script        execute an RDQL script and quit" + eol);
    usage.append(eol);
    usage.append("The intepreter executes default pre- and post-loading " +
        "scripts. These can be" + eol);
    usage.append("used to load aliases etc. into the interpreter to simplify " +
        "commands. The" + eol);
    usage.append("default scripts are contained within the JAR file, " +
        "however you can overide" + eol);
    usage.append("these by placing files named default-pre.rdql and " +
        "default-post.rdql in" + eol);
    usage.append("the directory from which you run the interpreter, or by " +
        "using the -p and" + eol);
    usage.append("-o options." + eol);

    // print the usage
    System.out.println(usage.toString());
  }

  // printUsage()

  /**
   * Dunps the current interpreter configuration to the current directory. This
   * will dump the entire interpreter configuration including the logging and
   * application logging.
   *
   */
  private void dumpConfig() {

    // we don't support this feature yet
    throw new UnsupportedOperationException();
  }

  // dumpConfig()

  /**
   * Loads an external RDQL interpreter configuration file. This will use the
   * configuration in the file located at <code>rdqlConfURL</code>, instead of
   * the configuration contained within the distribution JAR file.
   *
   * @param rdqlConfURL the URL of the external RDQL interpreter configuration
   *      file
   * @return RETURNED VALUE TO DO
   */
  private boolean loadRdqlConfig(URL rdqlConfURL) {

    // we don't support this feature yet
    throw new UnsupportedOperationException();
  }

  // loadRdqlConfig()

  /**
   * Executes a script.
   *
   * @param scriptURL the URL of the script to load
   */
  private void executeScript(URL scriptURL) {

    // log that we're executing the script
    log.debug("Executing script from " + scriptURL);

    // keep a record of the line number
    int line = 0;

    try {

      // create a reader to read the contents of the script
      BufferedReader scriptIn =
          new BufferedReader(new InputStreamReader(scriptURL.openStream()));

      // execute the script!
      String command = scriptIn.readLine();

      while (command != null) {

        // increment the line number
        line++;

        if (!command.equals("")) {

          // execute the command
          Answer answer = getInterpreter().executeQuery(command);

          // print the results
          if (answer != null) {

            while (answer.next()) {

              for (int index = 0; index < answer.getNumberOfVariables();
                  index++) {

                ui.print(String.valueOf(answer.getObject(index)));
              }
              ui.println();
            }
            answer.close();
          }

          String lastMessage = getInterpreter().getLastMessage();
          if ((lastMessage != null) && (lastMessage != "")) {

            ui.println(lastMessage);
          }
        }

        // end if
        // get the next command
        command = scriptIn.readLine();
      }

      // end if
    }
    catch (RdqlInterpreterException pe) {

      // let the user know the problem
      System.err.println("Syntax error (line " + line + "): " +
          pe.getMessage());
      log.warn("Unable to execute script - " + scriptURL + " - " + pe);
    }
    catch (TuplesException te) {

      // let the user know the problem
      System.err.println("Syntax error (line " + line + "): " +
          te.getMessage());
      log.warn("Unable to execute script - " + scriptURL + " - " + te);
    }
    catch (IOException ioe) {

      // let the user know the problem
      System.err.println("Could not execute script - " + ioe);
      log.warn("Unable to execute script - " + scriptURL + " - " + ioe);
    }

    // try-catch
  }

  // executeScript()

  /**
   * Loads an external XML log4j configuration file. This will use the
   * configuration in the file located at <code>logConfURL</code>, instead of
   * the configuration contained within the distribution JAR file.
   *
   * @param logConfURL the URL of the external XML log4j configuration file
   * @throws Exception if unable to complete the method sucessfully
   */
  private void loadLoggingConfig(URL logConfURL) throws Exception {

    // configure the logging service
    DOMConfigurator.configure(logConfURL);
    log.info("Using new logging configuration from " + logConfURL);
  }

  // loadLoggingConfig()

  /**
   * Loads the embedded logging configuration (from the JAR file).
   *
   */
  private void loadLoggingConfig() {

    // get a URL from the classloader for the logging configuration
    URL log4jConfigURL = ClassLoader.getSystemResource(LOG4J_CONFIG_PATH);

    // if we didn't get a URL, tell the user that something went wrong
    if (log4jConfigURL == null) {

      System.err.println("Unable to find logging configuration file in JAR " +
          "with " + LOG4J_CONFIG_PATH + ", reverting to default configuration.");
      BasicConfigurator.configure();
    }
    else {

      try {

        // configure the logging service
        DOMConfigurator.configure(log4jConfigURL);
        log.info("Using logging configuration from " + log4jConfigURL);
      }
      catch (FactoryConfigurationError e) {

        System.err.println("Unable to configure logging service");
      }

      // try-catch
    }

    // end if
  }

  // getPostLoadingScriptURL()
}
