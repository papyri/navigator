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

// Java APIs
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

// Third party packages
import org.apache.log4j.*;
import org.apache.soap.*;
import org.apache.soap.rpc.*;
import org.apache.soap.util.xml.*;
import org.jrdf.graph.URIReference;

// Mulgara packages
import org.mulgara.rdql.lexer.LexerException;
import org.mulgara.rdql.parser.ParserException;
import org.mulgara.query.Answer;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.server.Session;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;

// Required xml packages
import org.w3c.dom.*;

/**
 * RDQL Interpreter Bean. <p>
 *
 * This class provides a simple interface for the execution of RDQL queries.
 * </p>
 *
 * @created 2001-Aug-30
 *
 * @author <a href="http://staff.PIsoftware.com/tate/">Tate Jones</a>
 * @author <a href="http://staff.PIsoftware.com/tate/">Ben Warren</a>
 * @author <a href="http://staff.PIsoftware.com/tom/">Tom Adams</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:21 $ by $Author: newmana $
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RdqlInterpreterBean {

  /**
   * the logging category to log to
   */
  private final static Logger log =
      Logger.getLogger(RdqlInterpreterBean.class.getName());

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * Description of the Field
   */
  private final static String TQL_NS = "http://mulgara.org/tql#";

  /**
   * The RDQL interpreter Bean.
   */
  private RdqlInterpreter interpreter = null;

  /**
   * Create the RDQL interpreter.
   */
  public RdqlInterpreterBean() {

    if (interpreter == null) {

      try {

        interpreter = new RdqlInterpreter(
          SessionFactoryFinder.newSessionFactory(null).newSession()
        );
      }
      catch (IOException e) {

        interpreter.setLastError(e);
        interpreter.setLastMessage("Couldn't create an interpreter:" +
            e.getMessage());
      }
      catch (QueryException e) {

        interpreter.setLastError(e);
        interpreter.setLastMessage("Couldn't create an interpreter:" +
            e.getMessage());
      }
      catch (SessionFactoryFinderException e) {

        interpreter.setLastError(e);
        interpreter.setLastMessage("Couldn't create an interpreter:" +
            e.getMessage());
      }
    }
  }

  /**
   * Create the RDQL interpreter using the given <code>session</code>.
   *
   * @param session the session to use to communicate with the Mulgara server
   */
  public RdqlInterpreterBean(Session session) {

    if (interpreter == null) {

      try {

        this.interpreter = new RdqlInterpreter(session);
      }
      catch (IOException e) {

        interpreter.setLastError(e);
        interpreter.setLastMessage("Couldn't create an interpreter:" +
            e.getMessage());
      }
    }
  }

  // executeQueryToMap()

  /**
   * Splits a query containing multiple queries into an array of single queries.
   *
   * @param multiQuery PARAMETER TO DO
   * @return An array of query strings which include the ending ';' charater.
   */
  public static String[] splitQuery(String multiQuery) {

    List singleQueryList = new ArrayList();

    // Inside a URI?
    boolean INSIDE_URI = false;

    // Inside a text literal?
    boolean INSIDE_TEXT = false;

    // Start index for next single query
    int startIndex = 0;

    if (log.isDebugEnabled()) {

      log.debug("About to break up query: " + multiQuery);
    }

    multiQuery = multiQuery.trim();

    // Iterate along the multi query and strip out the single queries.
    for (int lineIndex = 0; lineIndex < multiQuery.length(); lineIndex++) {

      char currentChar = multiQuery.charAt(lineIndex);

      switch (currentChar) {

        // Quote - end or start of a literal if not in a URI
        // (OK so maybe it won't appear in a legal URI but let things further
        // down handle this)
        case '\'':

          if (!INSIDE_URI) {

            if (INSIDE_TEXT) {

              // Check for an \' inside a literal
              if ( (lineIndex > 1) && (multiQuery.charAt(lineIndex - 1) != '\\')) {

                INSIDE_TEXT = false;
              }
            }
            else {

              INSIDE_TEXT = true;
            }
          }

          break;

          // URI start - if not in a literal
        case '<':

          if (!INSIDE_TEXT) {

            INSIDE_URI = true;
          }

          break;

          // URI end - if not in a literal
        case '>':

          if (!INSIDE_TEXT) {

            INSIDE_URI = false;
          }

          break;

        case ';':

          if (!INSIDE_TEXT && !INSIDE_URI) {

            String singleQuery =
                multiQuery.substring(startIndex, lineIndex + 1).trim();
            startIndex = lineIndex + 1;
            singleQueryList.add(singleQuery);

            if (log.isDebugEnabled()) {

              log.debug("Found single query: " + singleQuery);
            }
          }

          break;

        default:
      }
    }

    // Lasy query is not terminated with a ';'
    if (startIndex < multiQuery.length()) {

      singleQueryList.add(multiQuery.substring(startIndex, multiQuery.length()));
    }

    return (String[]) singleQueryList.toArray(new String[singleQueryList.size()]);
  }

  /**
   * Returns the session to use to communicate with the Mulgara server.
   *
   * @return the session to use to communicate with the Mulgara server
   */
  public Session getSession() {

    return this.interpreter.getSession();
  }

  //
  // Public API
  //

  /**
   * Begin a new transaction by setting the autocommit off
   *
   * @param name the name of the transaction ( debug purposes only )
   * @throws QueryException EXCEPTION TO DO
   */
  public void beginTransaction(String name) throws QueryException {

    if (log.isDebugEnabled()) {

      log.debug("Begin transaction for :" + name);
    }

    this.getSession().setAutoCommit(false);
  }

  /**
   * Commit a new transaction by setting the autocommit on
   *
   * @param name the name of the transaction ( debug purposes only ) *
   * @throws QueryException EXCEPTION TO DO
   */
  public void commit(String name) throws QueryException {

    if (log.isDebugEnabled()) {

      log.debug("Commit transaction for :" + name);
    }

    // this is the same as a commit call
    this.getSession().setAutoCommit(true);
  }

  /**
   * Rollback an existing transaction
   *
   * @param name the name of the transaction ( debug purposes only ) *
   * @throws QueryException EXCEPTION TO DO
   */
  public void rollback(String name) throws QueryException {

    log.warn("Rollback transaction for :" + name);

    this.getSession().rollback();
    this.getSession().setAutoCommit(true);
  }

  /**
   * Answer TQL queries.
   *
   * @param queryString PARAMETER TO DO
   * @return the answer DOM to the TQL query
   * @throws Exception EXCEPTION TO DO
   */
  public Element execute(String queryString) throws Exception {

    try {

      DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
      Document doc = xdb.newDocument();

      Element answerDom = doc.createElementNS(TQL_NS, "answer");
      answerDom.setAttribute("xmlns", TQL_NS);

      if (log.isDebugEnabled()) {

        log.debug("Received a TQL query : " + queryString);
      }

      Element QUERY = doc.createElementNS(TQL_NS, "query");
      Element VARIABLES = doc.createElementNS(TQL_NS, "variables");

      String[] queries = splitQuery(queryString);

      for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {

        String singleQuery = queries[queryIndex];

        // Attach the answer
        Element query = (Element) answerDom.appendChild(QUERY);

        // Resolve the query
        if (log.isDebugEnabled()) {

          log.debug("Executing query : " + singleQuery);
        }

        interpreter.executeCommand(singleQuery);

        Answer answer = this.interpreter.getLastAnswer();

        if ( (answer == null) || answer.isUnconstrained()) {

          if (this.interpreter.getLastError() == null) {

            //Not an error, but a message does exist
            Element message =
                (Element) query.appendChild(doc.createElementNS(TQL_NS,
                "message"));
            message.appendChild(doc.createTextNode(interpreter.getLastMessage()));

            if (log.isDebugEnabled()) {

              log.debug("Attached last message: " + interpreter.getLastMessage());
            }
          }
          else {

            //Error has occurred at the interpreter
            //Generate a SOAP fault
            log.warn("Execute query failed", this.interpreter.getLastError());

            StringWriter stringWriter = new StringWriter();
            this.interpreter.getLastError().printStackTrace(new PrintWriter(
                stringWriter));

            SOAPException se =
                new SOAPException(Constants.FAULT_CODE_SERVER,
                "RdqlInterpreter error - " +
                interpreter.getCause(interpreter.getLastError()));
            throw se;
          }
        }
        else {

          if (log.isDebugEnabled()) {

            log.debug("Building XML result set");
          }

          appendSolution(answer, query);

          if (log.isDebugEnabled()) {

            log.debug("Attached answer text");
          }
        }
        if (answer != null) answer.close();
      }

      // Send the answer back to the caller
      return answerDom;
    }
    catch (Exception e) {

      log.error("Failed to execute query", e);
      throw e;
    }
  }

  /**
   * Answer TQL queries.
   *
   * @param queryString PARAMETER TO DO
   * @return the answer String to the TQL query
   * @throws Exception EXCEPTION TO DO
   */
  public String executeQueryToString(String queryString) throws Exception {

    String result = DOM2Writer.nodeToString(this.execute(queryString));

    if (log.isDebugEnabled()) {

      log.debug("Sending result to caller : " + result);
    }

    return result;
  }

  /**
   * Executes a semicolon delimited string of queries. <p>
   *
   * This method allows you to execute mulitple queries at once by specifying a
   * string of the following form: </p> <pre>
   * String queryString = "select $s $p $o from <rmi://machine/server1#model> " +
   *     "where $s $p $o;";
   * queryString += "select $s $p $o from <rmi://machine2/server1#model> " +
   *     "where $s $p $o;";
   * </pre> <p>
   *
   * The ordering of the result list will correspond to the ordering of the
   * queries in the input string. </p> <p>
   *
   * Note. Two different return types will be contained in the returned list. An
   * {@link org.mulgara.query.Answer} or a {@link java.lang.String} (error)
   * message. </p>
   *
   * @param queryString semi-colon delimited string containing the queries to be
   *      executed
   * @return a list of answers, messages and errors, answers are of type {@link
   *      org.mulgara.query.Answer}, the messages are {@link
   *      java.lang.String}s
   */
  public List executeQueryToList(String queryString) {

    return executeQueryToList(queryString, false);
  }

  /**
   * Executes a semicolon delimited string of queries. <p>
   *
   * This method allows you to execute mulitple queries at once by specifying a
   * string of the following form: </p> <pre>
   * String queryString = "select $s $p $o from <rmi://machine/server1#model> " +
   *     "where $s $p $o;";
   * queryString += "select $s $p $o from <rmi://machine2/server1#model> " +
   *     "where $s $p $o;";
   * </pre> <p>
   *
   * The ordering of the result list will correspond to the ordering of the
   * queries in the input string. </p> <p>
   *
   * Note. Two different return types will be contained in the returned list. An
   * {@link org.mulgara.query.Answer} or a {@link java.lang.String} (error)
   * message. </p>
   *
   * @param queryString semi-colon delimited string containing the queries to be
   *      executed
   * @param keepExceptions return exceptions, don't convert them to a string.
   * @return a list of answers, messages and errors, answers are of type {@link
   *      org.mulgara.query.Answer}, the messages are {@link
   *      java.lang.String}s
   */
  public List executeQueryToList(String queryString, boolean keepExceptions) {

    List answers = new ArrayList();

    if (log.isDebugEnabled()) {

      log.debug("Received a TQL query : " + queryString);
    }

    String[] queries = splitQuery(queryString);

    for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {

      String singleQuery = queries[queryIndex];

      // Resolve the query
      if (log.isDebugEnabled()) {

        log.debug("Executing query : " + singleQuery);
      }

      // end if
      // execute it
      answers.add(this.executeQueryToNiceResult(singleQuery, keepExceptions));
    }

    // end for
    // Send the answers back to the caller
    return answers;
  }

  /**
   * Executes a {@link java.util.Map} of queries, returning the results of those
   * queries in a map keyed with the same keys as the input map. <p>
   *
   * The <var>queries</var> is a map of keys ({@link java.lang.Object}s) to
   * queries ({@link java.lang.String}s). Each query will be excuted (in the
   * order in which <var>queries</var> 's map implementation {@link
   * java.util.Map#keySet()}'s {@link java.util.Set#iterator()} returns keys)
   * and the results added to the returned map, keyed on the same key as the
   * original query. </p> <p>
   *
   * For example: </p> <pre>
   * // create the queries
   * URI title = new URI("http://www.foo.com/title");
   * String titleQuery = "select $s $p $o from <rmi://machine/server1#model> " +
   *     "where $s $p $o;";
   * URI date = new URI("http://www.foo.com/date");
   * String dateQuery = "select $s $p $o from <rmi://machine2/server1#model> " +
   *     "where $s $p $o;";
   *
   * // add them to the map
   * HashMap queries = new HashMap();
   * queries.put(title, titleQuery);
   * queries.put(date, dateQuery);
   *
   * // execute them
   * RdqlInterpreterBean itb = new RdqlInterpreterBean();
   * HashMap answers = itb.executeQueryToMap(queries);
   *
   * // get the answers
   * Answer titleAnswer = answers.get(title);
   * Answer dateAnswer = answers.get(date);
   * </pre> <p>
   *
   * Note. Each answer will be either a {@link org.mulgara.query.Answer} or a
   * {@link java.lang.String} (error) message. </p>
   *
   * @param queries a map of keys to queries to be executed
   * @return a map of answers and error messages
   */
  public Map executeQueryToMap(Map queries) {

    // create the answer map
    HashMap answers = new HashMap();

    // iterate over the queries
    for (Iterator keyIter = queries.keySet().iterator(); keyIter.hasNext(); ) {

      // get the key and the query
      Object key = keyIter.next();
      String query = (String) queries.get(key);

      // log the query we're executing
      if (log.isDebugEnabled()) {

        log.debug("Executing " + key + " -> " + query);
      }

      // end if
      // execute the query
      answers.put(key, this.executeQueryToNiceResult(query, false));
    }

    // end for
    // return the answers
    return answers;
  }

  // getSession()

  /**
   * Builds a {@link org.mulgara.query.Query} from the given <var>query</var>.
   *
   * @param query PARAMETER TO DO
   * @return a {@link org.mulgara.query.Query} constructed from the given
   *      <var>query</var>
   * @throws IOException if the <var>query</var> can't be buffered
   * @throws LexerException if <var>query</var> can't be tokenized
   * @throws ParserException if the <var>query</var> is not syntactically
   *      correct
   */
  public Query buildQuery(String query) throws IOException, LexerException,
      ParserException {

    // defer to the interpreter
    return this.interpreter.parseQuery(query);
  }

  // executeQueryToNiceResult()

  /**
   * Execute an RDQL update statement and return a result message.
   *
   * @param rdql The statement to execute.
   * @return A message regarding the statement.
   * @throws RdqlInterpreterException if the statement fails.
   */
  public void executeUpdate(String rdql) throws RdqlInterpreterException {

    try {

      interpreter.executeCommand(rdql);
    }
    catch (Exception e) {

      throw new RdqlInterpreterException(e);
    }

    RdqlInterpreterException exception = interpreter.getLastError();
    Answer answer = interpreter.getLastAnswer();

    if (answer != null) {

      try {
        answer.close();
      }
      catch (TuplesException qe) {
        // fall into the following exception throw
      }
      throw new IllegalStateException("The execute update method should not " +
          "return an Answer object!");
    }

    if (exception != null) {

      throw exception;
    }
  }


  /**
   * Returns true if a quit command has been entered, simply calls the
   * interpreter.isQuitRequested.
   *
   * @return true if a quit command has been entered
   */
  public boolean isQuitRequested() {

    return interpreter.isQuitRequested();
  }

  /**
   * Returns the results of the last command execution. Methods overriding
   * {@link org.mulgara.rdql.analysis.DepthFirstAdapter} are expected to set
   * a results message, even if that message is null.
   *
   * @return the results of the last command execution, null if the command did
   *      not set any message
   * @see RdqlInterpreter#setLastMessage(String)
   */
  public String getLastMessage() {

    return interpreter.getLastMessage();
  }


  /**
   * Execute an RDQL query and return an answer.
   *
   * @param rdql The query to execute.
   * @return The answer to the query.
   * @throws RdqlInterpreterException if the query fails.
   */
  public Answer executeQuery(String rdql) throws RdqlInterpreterException {

    try {
      interpreter.executeCommand(rdql);
    }
    catch (Exception e) {

      throw new RdqlInterpreterException(e);
    }

    RdqlInterpreterException exception = interpreter.getLastError();

    if (exception != null) {

      throw exception;
    }

    Answer answer = interpreter.getLastAnswer();

    return answer;
  }

  /**
   * @param answer the answer to convert into XML
   * @param parent the XML element to add the query solutions to
   * @throws QueryException if the solutions can't be added
   */
  private void appendSolution(Answer answer, Element parent)
    throws QueryException
  {
    try {
      Document doc = parent.getOwnerDocument();

      Element VARIABLES = doc.createElementNS(TQL_NS, "variables");

      // Add the variable list
      Element variables = (Element) parent.appendChild(VARIABLES);

      for (int column = 0; column < answer.getVariables().length; column++) {

        Element variable =
            (Element) variables.appendChild(doc.createElement(
            answer.getVariables()[column].getName()));
      }

      // Add any solutions
      answer.beforeFirst();

      while (answer.next()) {

        Element solution = doc.createElementNS(TQL_NS, "solution");

        for (int column = 0; column < answer.getVariables().length; column++) {

          Object object = answer.getObject(column);

          // If the node is null, don't add a tag at all
          if (object == null) {

            continue;
          }

          // Otherwise, add a tag for the node
          Element variable =
              (Element) solution.appendChild(doc.createElementNS(TQL_NS,
              answer.getVariables()[column].getName()));

          if (object instanceof Answer) {

            appendSolution( ( (Answer) object), variable);

            continue;
          }
          else if (object instanceof LiteralImpl) {

            variable.appendChild(doc.createTextNode(
                ( (LiteralImpl) object).getLexicalForm()));
          }
          else if (object instanceof URIReference) {

            variable.setAttribute("resource",
                ((URIReference) object).getURI().toString());
          }
          else {

            throw new AssertionError("Unknown RDFNode type: " +
                object.getClass());
          }
        }

        parent.appendChild(solution);
      }
    }
    catch (TuplesException e) {
      throw new QueryException("Failed to append solution", e);
    }
  }

  // buildQuery()
  //
  // Internal methods
  //

  /**
   * Executes the <var>query</var> , returning a &quot;nice&quot; result. <p>
   *
   * The result is either a {@link java.lang.String} or a {@link
   * org.mulgara.query.Answer}. Any exceptions are logged, gobbled and return
   * as a {@link java.lang.String}. </p>
   *
   * @param query the query to execute
   * @param keepExceptions keep exceptions, don't convert to a message.
   * @return the result of the query in a &quot;nice&quot; format
   */
  private Object executeQueryToNiceResult(String query, boolean keepExceptions) {

    // create the result
    Object result = null;

    try {

      // get the answer to the query
      interpreter.executeCommand(query);
      Answer answer = this.interpreter.getLastAnswer();

      // log the query response
      if (log.isDebugEnabled()) {

        log.debug("Query response message = " + interpreter.getLastMessage());
      }

      // end if
      // turn the answer into a form we can handle
      if (answer != null) {

        // set this as the answer
        result = answer;
      }
      else {

        // get the error in an appropriate form
        if (this.interpreter.getLastError() != null) {

          // error has occurred at the interpreter
          if (log.isDebugEnabled()) {

            log.debug("Adding query error to map - " +
                this.interpreter.getLastError());
          }

          // end if
          // set this as the answer
          if (keepExceptions) {

            result = this.interpreter.getLastError();
          }
          else {

            result = this.interpreter.getLastError().getMessage();
          }
        }
        else {

          // log that we're adding the response message
          if (log.isDebugEnabled()) {

            log.debug("Adding response message to map - " +
                interpreter.getLastMessage());
          }

          // end if
          // set this as the answer
          result = interpreter.getLastMessage();
        }

        // end if
      }

      // end if
    }
    catch (Exception e) {

      if (keepExceptions) {

        result = e;
      }
      else {

        // get root cause exception
        Throwable cause = e.getCause();
        Throwable lastCause = e;

        while (cause != null) {

          lastCause = cause;
          cause = cause.getCause();
        }

        // end while
        // format the exception message
        String exceptionMessage = lastCause.getMessage();

        if (exceptionMessage == null) {

          exceptionMessage = lastCause.toString();
        }

        // end if
        // turn it into a pretty string
        exceptionMessage = "Query Error: " + exceptionMessage;

        // log the message
        if (log.isDebugEnabled()) {

          log.debug(exceptionMessage);
        }

        // end if
        // add the exception message to the answers
        result = exceptionMessage;

        // log full stack trace
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();

        log.error("Error while processing query: '" + query + "' " + eol +
            stringWriter.getBuffer().toString());
      }
    }

    // try-catch
    // return the result
    return result;
  }
}
