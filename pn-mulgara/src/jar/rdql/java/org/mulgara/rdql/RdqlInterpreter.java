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

// Third party packages
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.server.Session;

// Automatically generated packages (SableCC)
import org.mulgara.rdql.analysis.*;
import org.mulgara.rdql.lexer.*;
import org.mulgara.rdql.node.*;
import org.mulgara.rdql.parser.*;

/**
 * RDF Data Query Language (RDQL) command interpreter.
 *
 * This class parses streams into {@link Query} objects.  See the
 * <a href="http://www.w3.org/Submission/2004/SUBM-RDQL-20040109/">RDQL
 * specfication</a> for the detailed grammar.
 *
 * @created 2001-08-21
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:21 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RdqlInterpreter extends DepthFirstAdapter {
  /**
   * Logger.
   */
  private final static Logger logger =
      Logger.getLogger(RdqlInterpreter.class.getName());

  static {
    // force initialization of static, unsynchronized variables inside these classes
    new Parser(new RdqlLexer());
  }

  //
  // Members
  //

  /**
   * Accumulated <code>SELECT</code> clause.
   */
  //private List selectList = null;

  //private ModelExpression modelExpression = null;

  //private ConstraintExpression constraintExpression = null;

  //private Constraint constraint = null;

  private Answer lastAnswer = null;

  private RdqlInterpreterException lastError = null;

  private String lastMessage = "";

  private Query query = null;

  private boolean quitRequested = false;

  private Session session = null;

  private Map prefixesMap = Collections.EMPTY_MAP;

  private RdqlLexer lexer = new RdqlLexer();

  //
  // Constructors
  //

  /**
   * Creates a new RDQL command interpreter.
   */
  public RdqlInterpreter(Session session) throws IOException {
    /*
         if (session == null) {
      throw new IllegalArgumentException("Null \"session\" parameter");
         }
     */

    this.session = session;
  }

  //
  // Public API
  //

  String getCause(Throwable e) {
    return e.toString();
  }

  Answer getLastAnswer() {
    return lastAnswer;
  }

  void setLastAnswer(Answer answer) {
    lastAnswer = answer;
  }

  RdqlInterpreterException getLastError() {
    return lastError;
  }

  void setLastError(Exception exception) {
    lastError = (exception == null) ? null
        : new RdqlInterpreterException(exception);
  }

  String getLastMessage() {
    return lastMessage;
  }

  void setLastMessage(String string) {
    lastMessage = string;
  }

  Session getSession() {
    return session;
  }

  boolean isQuitRequested() {
    return quitRequested;
  }

  void executeCommand(String string) throws IOException, LexerException,
      ParserException {
    // Validate "string" parameter
    if (string == null) {
      throw new IllegalArgumentException("Null \"string\" parameter");
    }

    try {
      lexer.add(string);
      while (lexer.nextCommand()) {
        Parser parser = new Parser(lexer);
        parser.parse().apply(this);
      }
    }
    catch (Exception e) {
      setLastError(e);

      // Use the exception stack trace as the last message
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      setLastMessage(sw.toString());

      lexer.flush();
    }
  }

  /**
   * Exercise the parser.
   *
   * @param string  RDQL-formatted character stream
   * @throws IllegalArgumentException  if <var>reader</var> is <code>null</code>
   */
  public Query parseQuery(String string) throws IOException, LexerException,
      ParserException {
    // Validate "string" parameter
    if (string == null) {
      throw new IllegalArgumentException("Null \"string\" parameter");
    }

    lexer.add(string);
    if (lexer.nextCommand()) {
      Parser parser = new Parser(lexer);
      parser.parse().apply(this);
      if (query == null) {
        lexer.flush();
        throw new ParserException(null,
            "Parameter was not a query: \"" + string + "\""
            );
      }
      return query;
    }
    else {
      lexer.flush();
      throw new ParserException(null,
          "Parameter was incomplete command: \"" + string + "\""
          );
    }
  }

  //
  // Methods overridden from DepthFirstAdapter
  //

  public void outAQueryCommand(AQueryCommand node) {
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("Processing query command " + node);
      }

      Query query = toQuery(node.getQuery());
      if (session == null) {
        throw new QueryException("No session available");
      }
      Answer answer = session.query(query);

      this.setLastAnswer(answer);
      this.setLastError(null);
      this.setLastMessage("Parsed query: " + query);
    }
    catch (ParserException e) {
      this.setLastAnswer(null);
      this.setLastError(e);
      this.setLastMessage("Couldn't parse query: " + query + " because of " + e);
    }
    catch (QueryException e) {
      this.setLastAnswer(null);
      this.setLastError(e);
      this.setLastMessage("Couldn't resolve query: " + query + " because of " +
          e);
    }
  }

  public void outAQuitCommand(AQuitCommand node) {
    // log the command
    if (logger.isDebugEnabled()) {
      logger.debug("Processing quit command " + node);
    }

    this.setLastAnswer(null);
    this.setLastError(null);
    this.setLastMessage("Quitting RDQL session");

    // indicate that a quit command was received
    quitRequested = true;

    // let the user know that we're closing down
    this.setLastMessage("Quitting RDQL session");
  }

  //
  // Syntax tree type conversion methods
  //

  /**
   * Adds a mapping between a namespace prefix and a URI.
   *
   * @param map  the map to modify
   * @param p    the RDQL syntax tree node from which to obtain the mapping
   */
  private static void addMapEntry(Map map,
      PPrefixDecl p) throws ParserException {
    APrefixDecl prefixDecl = (APrefixDecl) p;
    String prefix = toString(prefixDecl.getIdentifier());
    String value = ((AQuotedUri) prefixDecl.getQuotedUri()).getUnquotedUri()
        .toString().trim();
    if (map.containsKey(prefix)) {
      if (map.get(prefix).equals(value)) {
        logger.warn("Redundant redeclaration of " + prefix + " as " + value);
        return;
      }
      throw new ParserException(null,
          "Tried to redeclare Prefix " + prefix + " from " + map.get(prefix) +
          " to " + value);
    }
    map.put(prefix, value);
  }

  private ConstraintExpression toConstraintExpression(PTriplePatternClause p)
      throws ParserException {
    ATriplePatternClause triplePatternClause =
        (ATriplePatternClause) p;
    ConstraintExpression constraintExpression =
        toConstraint(triplePatternClause.getTriplePattern());

    Iterator i = triplePatternClause.getTripleTail().iterator();
    while (i.hasNext()) {
      constraintExpression = new ConstraintConjunction(
          constraintExpression,
          toConstraint(((ATripleTail) i.next()).getTriplePattern())
      );
    }

    return constraintExpression;
  }

  private ConstraintExpression toConstraint(PTriplePattern node)
      throws ParserException {
    return new ConstraintImpl(
        toConstraintElement(((ATriplePattern) node).getSubject()),
        toConstraintElement(((ATriplePattern) node).getPredicate()),
        toConstraintElement(((ATriplePattern) node).getObject())
    );
  }

  private ConstraintElement toConstraintElement(PVarOrConst varOrConst) throws
      ParserException {
    if (varOrConst instanceof AVariableVarOrConst) {
      return toVariable(((AVariableVarOrConst) varOrConst).getVar());
    }
    else if (varOrConst instanceof AConstantVarOrConst) {
      return toLiteralImpl(((AConstantVarOrConst) varOrConst).getConst());
    }
    else {
      throw new Error("Unhandled PVarOrConst: " + varOrConst.getClass());
    }
  }

  /**
   * @throws ParserException if the element is a resource whose URI is a qname
   *                         with an undefined prefix
   */
  private ConstraintElement toConstraintElement(PVarOrUri varOrUri) throws
      ParserException {
    if (varOrUri instanceof AVariableVarOrUri) {
      return toVariable(((AVariableVarOrUri) varOrUri).getVar());
    }
    else if (varOrUri instanceof AUriVarOrUri) {
      return new URIReferenceImpl(toURI(((AUriVarOrUri) varOrUri).getUri()));
    }
    else {
      throw new Error("Unhandled PVarOrUri: " + varOrUri.getClass());
    }
  }

  private List toList(PSelectClause p) throws ParserException {
    if (p instanceof AAllSelectClause) {
      return null;
    }
    else if (p instanceof ASomeSelectClause) {
      ASomeSelectClause selectClause = (ASomeSelectClause) p;
      List list = new LinkedList();
      list.add(toVariable(selectClause.getVar()));
      for (Iterator i = selectClause.getSelectTail().iterator(); i.hasNext(); ) {
        list.add(toVariable(((ASelectTail) i.next()).getVar()));
      }
      return list;
    }
    else {
      throw new Error("Unhandled PSelectClause subtype " + p.getClass());
    }
  }

  private LiteralImpl toLiteralImpl(PConst constant) throws ParserException {
    if (constant instanceof AUriConst) {
      return new LiteralImpl(constant.toString().trim());
    }
    else if (constant instanceof ANumberConst) {
      return new LiteralImpl(constant.toString().trim());
    }
    else if (constant instanceof ATextConst) {
      ATextLiteral textLiteral =
          (ATextLiteral) ((ATextConst) constant).getTextLiteral();

      String language = "";
      if (textLiteral.getAtClause() != null) {
        language = textLiteral.getAtClause().toString().trim();
      }

      URI datatype = null;
      if (textLiteral.getDatatypeClause() != null) {
        datatype =
            toURI(((ADatatypeClause) textLiteral.getDatatypeClause()).getUri());
      }

      return new LiteralImpl(
          textLiteral.getLexicalFormClause().toString().trim(),
          language,
          datatype
          );
    }
    else if (constant instanceof ABooleanConst) {
      return new LiteralImpl(constant.toString().trim());
    }
    else if (constant instanceof ANullConst) {
      return new LiteralImpl(constant.toString().trim());
    }
    else {
      throw new Error("Unhandled PConst subclass " + constant.getClass());
    }
  }

  private Map toMap(PPrefixesClause p) throws ParserException {
    Map map = new HashMap();

    // Start with only the preloaded XML Schema prefix mapping to "xsd"
    map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    map.put("xsd", "http://www.w3.org/2001/XMLSchema#");

    // Add any prefix mappings specified by the USING clause
    if (p != null) {
      APrefixesClause prefixesClause = (APrefixesClause) p;
      addMapEntry(map, prefixesClause.getPrefixDecl());
      Iterator i = prefixesClause.getPrefixesClauseTail().iterator();
      while (i.hasNext()) {
        addMapEntry(map, ((APrefixesClauseTail) i.next()).getPrefixDecl());
      }
    }

    return map;
  }

  private Model toModel(PSourceSelector p) throws ParserException {
    return new ModelResource(toURI(((ASourceSelector) p).getUri()));
  }

  private ModelExpression toModelExpression(PSourceClause p) throws
      ParserException {
    ASourceClause sourceClause = (ASourceClause) p;
    ModelExpression modelExpression =
        toModel(sourceClause.getSourceSelector());

    Iterator i = sourceClause.getSourceTail().iterator();
    while (i.hasNext()) {
      modelExpression = new ModelUnion(
          modelExpression,
          toModel(((ASourceTail) i.next()).getSourceSelector())
          );
    }

    return modelExpression;
  }

  private Query toQuery(PQuery p) throws ParserException {
    AQuery query = (AQuery) p;
    prefixesMap = toMap(query.getPrefixesClause());
    return new Query(
        toList(query.getSelectClause()),                         // SELECT
        toModelExpression(query.getSourceClause()),              // FROM
        toConstraintExpression(query.getTriplePatternClause()),  // WHERE
        null,                                                    // HAVING
        Collections.EMPTY_LIST,                                  // ORDER BY
        null,                                                    // LIMIT
        0,                                                       // OFFSET
        new UnconstrainedAnswer()                                // GIVEN
        );
  }

  private static String toString(PIdentifier identifier) {
    if (identifier instanceof ANcnameIdentifier) {
      return ((ANcnameIdentifier) identifier).getNcname().toString().trim();
    }
    else {
      return identifier.toString().trim();
    }
  }

  /**
   * Extract a {@link URI} from the abstract syntax tree.
   *
   * This method depends on the global {@link #prefixesMap} to dereference
   * QNames.
   *
   * @throws ParserException if the URI is a qname with an undefined prefix
   */
  private URI toURI(PUri uri) throws ParserException {
    if (uri instanceof AUriUri) {
      try {
        return new URI(
            ((AQuotedUri) ((AUriUri) uri).getQuotedUri()).getUnquotedUri()
            .toString().trim()
            );
      }
      catch (URISyntaxException e) {
        throw new Error("Parser passed bad URI", e);
      }
    }
    else if (uri instanceof AQnameUri) {
      AQname qname = (AQname) ((AQnameUri) uri).getQname();
      String nsPrefix = qname.getNsPrefix().toString().trim();
      if (!prefixesMap.containsKey(nsPrefix)) {
        throw new ParserException(qname.getNsPrefix(),
            "Undefined Qname prefix: " + nsPrefix);
      }
      StringBuffer buffer =
          new StringBuffer((String) prefixesMap.get(nsPrefix));
      if (qname.getLocalPart() != null) {
        buffer.append(qname.getLocalPart().toString().trim());
      }
      try {
        return new URI(buffer.toString());
      }
      catch (URISyntaxException e) {
        throw new Error("Parser generated bad URI", e);
      }
    }
    else {
      throw new Error("Unhandled PUri: " + uri.getClass());
    }
  }

  private Variable toVariable(PVar p) {
    return new Variable(toString(((AVar) p).getIdentifier()));
  }
}
