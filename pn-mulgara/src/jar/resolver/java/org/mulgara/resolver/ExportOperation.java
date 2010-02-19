/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.resolver;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Map;

import org.mulgara.content.rdfxml.writer.RDFXMLWriter;
import org.mulgara.content.rlog.RlogStructure;
import org.mulgara.content.n3.N3Writer;
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.resolver.spi.TuplesWrapperStatements;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.store.tuples.Tuples;

/**
 * An {@link Operation} that serializes the contents of an RDF graph to either
 * an output stream or a destination file.
 *
 * @created Jun 25, 2008
 * @author Alex Hall
 * @copyright &copy; 2008 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class ExportOperation extends OutputOperation {

  private final URI graphURI;
  private final Map<String,URI> prefixes;

  /**
   * Create an {@link Operation} which exports the contents of the specified RDF graph
   * to a URI or to an output stream.
   *
   * The database is not changed by this method.
   * If an {@link OutputStream} is supplied then the destinationURI is ignored.
   *
   * @param outputStream An output stream to receive the contents, may be
   *   <code>null</code> if a <var>destinationURI</var> is specified
   * @param graphURI The URI of the graph to export, never <code>null</code>.
   * @param destinationURI The URI of the file to export into, may be
   *   <code>null</code> if an <var>outputStream</var> is specified
   * @param initialPrefixes An optional set of user-supplied namespace prefix mappings;
   *   may be <code>null</code> to use the generated namespace prefixes.
   */
  public ExportOperation(OutputStream outputStream, URI graphURI, URI destinationURI,
      Map<String,URI> initialPrefixes) {
    super(outputStream, destinationURI);

    if (graphURI == null) {
      throw new IllegalArgumentException("Graph URI may not be null.");
    }
    this.graphURI = graphURI;
    this.prefixes = initialPrefixes;
  }

  /* (non-Javadoc)
   * @see org.mulgara.resolver.OutputOperation#execute(org.mulgara.resolver.OperationContext, org.mulgara.resolver.spi.SystemResolver, org.mulgara.resolver.spi.DatabaseMetadata)
   */
  @Override
  public void execute(OperationContext operationContext, SystemResolver systemResolver,
                      DatabaseMetadata metadata) throws Exception {
    // Verify that the graph is of a type that supports exports.
    long graph = systemResolver.localize(new URIReferenceImpl(graphURI));
    ResolverFactory resolverFactory = operationContext.findModelResolverFactory(graph);

    if (resolverFactory.supportsExport()) {
      OutputStream os = getOutputStream();
      assert os != null;
      OutputStreamWriter writer = null;

      try {
        writer = new OutputStreamWriter(os, "UTF-8");

        // create a constraint to get all statements
        Variable[] vars = new Variable[] {
            StatementStore.VARIABLES[0],
            StatementStore.VARIABLES[1],
            StatementStore.VARIABLES[2]
        };
        Constraint constraint = new ConstraintImpl(vars[0], vars[1], vars[2], new LocalNode(graph));

        // Get all statements from the graph.  Delegate to the operation context to do the security check.
        Tuples resolution = operationContext.resolve(constraint);
        Statements graphStatements = new TuplesWrapperStatements(resolution, vars[0], vars[1], vars[2]);

        // Do the writing.
        try {
          String path = (destinationURI != null) ? destinationURI.getPath() : null;
          if (path != null && (path.endsWith(".n3") || path.endsWith(".nt") || path.endsWith(".ttl"))) {
            N3Writer n3Writer = new N3Writer();
            n3Writer.write(graphStatements, systemResolver, writer);
          } else if (path != null && (path.endsWith(".rl") || path.endsWith(".dl") || path.endsWith(".rlog"))) {
            RlogStructure struct = new RlogStructure(systemResolver);
            struct.load(graphStatements);
            struct.write(writer);
          } else {
            RDFXMLWriter rdfWriter = new RDFXMLWriter();
            rdfWriter.write(graphStatements, systemResolver, writer, prefixes);
          }
        } finally {
          // This will close the wrapped resolution as well.
          graphStatements.close();
        }
      } finally {
        // Clean up.
        if (writer != null) {
          // Close the writer if it exists.  This will also close the wrapped
          // OutputStream.
          writer.close();
        } else if (os != null) {
          // Close the os if it exists.
          os.close();
        }
      }
    } else {
      throw new QueryException("Graph " + graphURI + " does not support export.");
    }
  }
}
