/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.query;

import java.io.Serializable;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;

/**
 * An Answer that represents a graph.
 *
 * @created Jun 30, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class GraphAnswer implements Answer, Serializable {

  /** The serialization ID. */
  private static final long serialVersionUID = -5499236950928116988L;

  /** The variable name for the first column. */
  private static final String CONSTANT_VAR_SUBJECT = "subject";

  /** The variable name for the second column. */
  private static final String CONSTANT_VAR_PREDICATE = "predicate";

  /** The variable name for the third column. */
  private static final String CONSTANT_VAR_OBJECT = "object";

  /** The first column variable. */
  private static final Variable SUBJECT_VAR = new Variable(CONSTANT_VAR_SUBJECT);

  /** The second column variable. */
  private static final Variable PREDICATE_VAR = new Variable(CONSTANT_VAR_PREDICATE);

  /** The third column variable. */
  private static final Variable OBJECT_VAR = new Variable(CONSTANT_VAR_OBJECT);

  /** An array containing the variable. */
  private static final Variable[] CONSTANT_VAR_ARR = new Variable[] { SUBJECT_VAR, PREDICATE_VAR, OBJECT_VAR };

  /** The raw answer to wrap. */
  private Answer rawAnswer;

  /** The column counter for emulating rows. */
  private int colOffset = 0;

  /** The number of rows per column. */
  private final int rowsPerCol;

  /**
   * Constructs a new BooleanAnswer.
   * @param rawAnswer The result this answer represents.
   */
  public GraphAnswer(Answer rawAnswer) {
    int cols = rawAnswer.getNumberOfVariables();
    if (cols % 3 != 0) throw new IllegalArgumentException("Cannot construct a graph with " + cols + " columns.");
    rowsPerCol = cols / 3;
    this.rawAnswer = rawAnswer;
  }

  /**
   * @see org.mulgara.query.Answer#getObject(int)
   */
  public Object getObject(int column) throws TuplesException {
    return rawAnswer.getObject(column + colOffset);
  }

  /**
   * @see org.mulgara.query.Answer#getObject(java.lang.String)
   */
  public Object getObject(String columnName) throws TuplesException {
    // use an unrolled loop
    if (CONSTANT_VAR_SUBJECT.equals(columnName)) return rawAnswer.getObject(colOffset);
    if (CONSTANT_VAR_PREDICATE.equals(columnName)) return rawAnswer.getObject(1 + colOffset);
    if (CONSTANT_VAR_OBJECT.equals(columnName)) return rawAnswer.getObject(2 + colOffset);
    throw new TuplesException("Unknown variable: " + columnName);
  }

  /** @see org.mulgara.query.Cursor#beforeFirst() */
  public void beforeFirst() throws TuplesException {
    rawAnswer.beforeFirst();
    colOffset = (rowsPerCol - 1) * 3;
  }

  /** @see org.mulgara.query.Cursor#close() */
  public void close() throws TuplesException {
    rawAnswer.close();
  }

  /**
   * @see org.mulgara.query.Cursor#getColumnIndex(org.mulgara.query.Variable)
   */
  public int getColumnIndex(Variable column) throws TuplesException {
    // use an unrolled loop
    if (SUBJECT_VAR.equals(column)) return 0;
    if (PREDICATE_VAR.equals(column)) return 1;
    if (OBJECT_VAR.equals(column)) return 2;
    throw new TuplesException("Unknown variable: " + column);
  }

  /**
   * @see org.mulgara.query.Cursor#getNumberOfVariables()
   */
  public int getNumberOfVariables() {
    return 3;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCardinality()
   */
  public int getRowCardinality() throws TuplesException {
    int rawCardinality = rawAnswer.getRowCardinality() * rowsPerCol;
    if (rawCardinality == 0) return 0;
    // get a copy to work with
    GraphAnswer answerCopy = (GraphAnswer)clone();
    try {
      answerCopy.beforeFirst();
      // test if one row
      if (!answerCopy.next()) return 0;
      // test if we know it can't be more than 1, or if there is no second row
      if (rawCardinality == 1) return 1;
      if (!answerCopy.next()) return rowsPerCol;
      // Return the raw cardinality
      return rawCardinality;
    } finally {
      answerCopy.close();
    }
  }

  /**
   * @see org.mulgara.query.Cursor#getRowCount()
   */
  public long getRowCount() throws TuplesException {
    // Urk. Doing this the hard way...
    // get a copy to work with
    GraphAnswer answerCopy = (GraphAnswer)clone();
    try {
      answerCopy.beforeFirst();
      long result = 0;
      while (answerCopy.next()) result++;
      return result * rowsPerCol;
    } finally {
      answerCopy.close();
    }
  }

  /**
   * @see org.mulgara.query.Cursor#getRowUpperBound()
   */
  public long getRowUpperBound() throws TuplesException {
    return rawAnswer.getRowUpperBound() * rowsPerCol;
  }

  /**
   * @see org.mulgara.query.Cursor#getRowExpectedCount()
   */
  public long getRowExpectedCount() throws TuplesException {
    return rawAnswer.getRowExpectedCount() * rowsPerCol;
  }

  /**
   * @see org.mulgara.query.Cursor#getVariables()
   */
  public Variable[] getVariables() {
    return CONSTANT_VAR_ARR;
  }

  /**
   * Since the returned variables are static, provide them statically as well.
   */
  public static Variable[] getGraphVariables() {
    return CONSTANT_VAR_ARR;
  }

  /**
   * @see org.mulgara.query.Cursor#isUnconstrained()
   */
  public boolean isUnconstrained() throws TuplesException {
    return false;
  }


  /**
   * @see org.mulgara.query.Cursor#next()
   */
  public boolean next() throws TuplesException {
    boolean nextAvailable;
    do {
      nextAvailable = internalNext();
    } while (nextAvailable && !graphable());
    return nextAvailable;
  }


  /** @see java.lang.Object#clone() */
  public Object clone() {
    return new GraphAnswer((Answer)rawAnswer.clone());
  }


  /**
   * An internal method for moving on to the next row, without testing validity.
   * @return <code>true</code> if this call has not exhausted the rows.
   * @throws TuplesException Due to an error in the underlying rawAnswer.
   */
  private boolean internalNext() throws TuplesException {
    if ((colOffset += 3) < (rowsPerCol * 3)) return true;
    colOffset = 0;
    return rawAnswer.next();
  }


  /**
   * Test if the current row is expressible as a graph row.
   * @return <code>true</code> if the subject-predicate-object have valid node types.
   * @throws TuplesException The row could not be accessed.
   */
  private boolean graphable() throws TuplesException {
    if (rawAnswer.getObject(colOffset) instanceof Literal) return false;
    Object predicate = rawAnswer.getObject(1 + colOffset);
    return !(predicate instanceof Literal || predicate instanceof BlankNode);
  }
}
