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

package org.mulgara.protocol.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mulgara.itql.TqlInterpreter;
import org.mulgara.protocol.StreamedAnswer;
import org.mulgara.protocol.StreamedSparqlJSONAnswer;
import org.mulgara.protocol.StreamedSparqlJSONObject;
import org.mulgara.protocol.StreamedSparqlXMLObject;
import org.mulgara.protocol.StreamedTqlXMLAnswer;
import org.mulgara.query.Answer;
import org.mulgara.server.SessionFactoryProvider;

/**
 * A query gateway for TQL.
 *
 * @created Sep 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TqlServlet extends ProtocolServlet {

  /** Serialization ID */
  private static final long serialVersionUID = -72714067636720775L;


  /**
   * Creates the servlet for communicating with the given server.
   * @param server The server that provides access to the database.
   */
  public TqlServlet(SessionFactoryProvider server) throws IOException {
    super(server);
  }


  /**
   * Creates the servlet in a default application server environment.
   */
  public TqlServlet() throws IOException {
  }


  /** @see org.mulgara.protocol.http.ProtocolServlet#initializeBuilders() */
  protected void initializeBuilders() {
    // TODO: create a JSON answer and a XML object for TQL.
    AnswerStreamConstructor jsonBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedSparqlJSONAnswer(ans, s); }
    };
    AnswerStreamConstructor xmlBuilder = new AnswerStreamConstructor() {
      public StreamedAnswer fn(Answer ans, OutputStream s) { return new StreamedTqlXMLAnswer(ans, s); }
    };
    streamBuilders.put(Output.JSON, jsonBuilder);
    streamBuilders.put(Output.XML, xmlBuilder);
    streamBuilders.put(Output.RDFXML, xmlBuilder);  // TODO: create an RDF/XML builder
    streamBuilders.put(Output.N3, xmlBuilder);      // TODO: create an N3 builder

    ObjectStreamConstructor jsonObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlJSONObject(o, s); }
    };
    ObjectStreamConstructor xmlObjBuilder = new ObjectStreamConstructor() {
      public StreamedAnswer fn(Object o, OutputStream s) { return new StreamedSparqlXMLObject(o, s); }
    };
    objectStreamBuilders.put(Output.JSON, jsonObjBuilder);
    objectStreamBuilders.put(Output.XML, xmlObjBuilder);
    objectStreamBuilders.put(Output.RDFXML, xmlObjBuilder);  // TODO: create an RDF/XML object builder
    objectStreamBuilders.put(Output.N3, xmlObjBuilder);      // TODO: create an N3 object builder
  }


  /**
   * Provide a description for the servlet.
   * @see javax.servlet.GenericServlet#getServletInfo()
   */
  public String getServletInfo() {
    return "Mulgara TQL Query Endpoint";
  }


  /**
   * Gets the TQL interpreter for the current session,
   * creating it if it doesn't exist yet.
   * @param req The current request environment.
   * @return A connection that is tied to this HTTP session.
   */
  protected TqlInterpreter getInterpreter(HttpServletRequest req) throws BadRequestException {
    HttpSession httpSession = req.getSession();
    TqlInterpreter interpreter = (TqlInterpreter)httpSession.getAttribute(INTERPRETER);
    if (interpreter == null) {
      interpreter = new TqlInterpreter();
      httpSession.setAttribute(INTERPRETER, interpreter);
    }
    return interpreter;
  }

}
