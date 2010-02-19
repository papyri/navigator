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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;

import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.relational.d2rq.ClassMapElem;
import org.mulgara.resolver.relational.d2rq.DatatypePropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.ObjectPropertyBridgeElem;
import org.mulgara.resolver.relational.d2rq.TranslationTableElem;

public class ColumnDesc extends VariableDesc {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(ColumnDesc.class);

  private String column;
  private String table;
  private int index;
  private TranslationTableElem ttable;

  private Class<? extends Node> resourceType;

  public ColumnDesc(ClassMapElem classMap) {
    super(classMap);
    column = classMap.uriColumn;
    table = RelationalResolver.parseTableFromColumn(column);
    resourceType = URIReference.class;
    ttable = classMap.translateWith;
  }

  public ColumnDesc(ObjectPropertyBridgeElem bridge) {
    super(bridge);
    this.column = bridge.column;
    this.table = RelationalResolver.parseTableFromColumn(this.column);
    this.resourceType = URIReference.class;
    ttable = bridge.translateWith;
  }

  public ColumnDesc(DatatypePropertyBridgeElem bridge) {
    super(bridge);
    this.column = bridge.column;
    this.table = RelationalResolver.parseTableFromColumn(this.column);
    this.resourceType = Literal.class;
    ttable = bridge.translateWith;
  }

  public Node getNode(ResultSet result) throws SQLException, TuplesException {
    // Do translation.
    String value = result.getString(index + 1);
    if (ttable != null) {
      String tvalue = (String)ttable.db2rdf.get(value);
      value = tvalue != null ? tvalue : value;
    }

    if (resourceType == URIReference.class) {
      try {
        return new URIReferenceImpl(new URI(value));
      } catch (URISyntaxException eu) {
        return new LiteralImpl(value);
      } catch (IllegalArgumentException ei) {
        return new LiteralImpl(value);
      }
    } else if (resourceType == Literal.class) {
      return new LiteralImpl(value);
    } else {
      throw new TuplesException("Unknown type");
    }
  }

  public String restrict(String rdfValue) {
    String value = rdfValue;
    if (ttable != null) {
      String tvalue = (String)ttable.rdf2db.get(rdfValue);
      if (tvalue != null) {
        value = tvalue;
      }
    }

    // Handle multiple datatypes here
    return getColumn() + " = " + encode(getColumn(), value);
  }

  public Set<String> getTables() {
    return Collections.singleton(table);
  }

  public Set<String> getColumns() {
    return Collections.singleton(column);
  }

  public String getTable() {
    return table;
  }

  public String getColumn() {
    return column;
  }

  public void assignColumnIndex(String column, int index) {
    if (!this.column.equals(column)) {
      throw new IllegalArgumentException("Column in index assignment does not match column in description: " + column + " - " + this.column);
    }
    
    this.index = index;
  }


  public String toString() {
    return "ColumnDesc:(" +
        "column=" + column + ", " +
        "table=" + table + ", " +
        "index=" + index + ")";
  }
}
