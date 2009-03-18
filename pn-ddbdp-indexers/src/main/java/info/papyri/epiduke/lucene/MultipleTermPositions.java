package info.papyri.epiduke.lucene;
/**
 * Batching version of MultipleTermPositions
 * Faster, not threadsafe
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.util.PriorityQueue;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.*;

import org.apache.lucene.index.*;
/**
 * Describe class <code>MultipleTermPositions</code> here.
 * 
 * @author Anders Nielsen
 * @version 1.0
 */
public class MultipleTermPositions implements TermPositions {

  private static final class TermPositionsQueue extends PriorityQueue {
    TermPositionsQueue(int size){
        initialize(size);
    }
    
    TermPositionsQueue(List termPositions) throws IOException {
      initialize(termPositions.size());

      Iterator i = termPositions.iterator();
      while (i.hasNext()) {
        TermPositions tp = (TermPositions) i.next();
        if (tp.next())
          put(tp);
      }
    }

    final TermPositions peek() {
      return (TermPositions) top();
    }

    public final boolean lessThan(Object a, Object b) {
      return ((TermPositions) a).doc() < ((TermPositions) b).doc();
    }
  }

  private int _doc;
  private int _freq;
  private TermPositionsQueue _termPositionsQueue;
  private IntQueue _posList;
  /**
   * Creates a new <code>MultipleTermPositions</code> instance.
   * 
   * @exception IOException
   */
  public MultipleTermPositions(IndexReader indexReader, Term[] terms) throws IOException {

    _termPositionsQueue = new TermPositionsQueue(terms.length);
    TermPositions tp = null;
    for(Term t:terms){
        tp = indexReader.termPositions(t);
        if(tp.next()) _termPositionsQueue.put(tp);
    }
    _posList = new IntQueue(_termPositionsQueue.peek().freq());
  }
  
  public MultipleTermPositions(IndexReader indexReader, String [] terms, String field) throws IOException {
      
      _termPositionsQueue = new TermPositionsQueue(terms.length);
    Term t = new Term(field,"");
    TermPositions tp = null;
    for (String text:terms){
        TermTextSwap.swapText(t, text);
        tp = indexReader.termPositions(t);
        if(tp.next())_termPositionsQueue.put(tp);
    }

    if(_termPositionsQueue.peek() != null){
      _posList = new IntQueue(_termPositionsQueue.peek().freq());
      } else _posList = new IntQueue(16);
    }

  public MultipleTermPositions(IndexReader indexReader, Collection<String> terms, String field) throws IOException {

      _termPositionsQueue = new TermPositionsQueue(terms.size());
      Term t = new Term(field,"");
      TermPositions tp = null;
      for (String text:terms){
          TermTextSwap.swapText(t, text);
          tp = indexReader.termPositions(t);
          if(tp.next())_termPositionsQueue.put(tp);
      }
      _posList = new IntQueue(_termPositionsQueue.peek().freq());
    }

  int [] intBuff = new int[32];
  public final boolean next() throws IOException {
    if (_termPositionsQueue.size() == 0)
      return false;

    _posList.clear();
    _doc = _termPositionsQueue.peek().doc();

    TermPositions tp;
    do {
      tp = _termPositionsQueue.peek();

      int ix = 0;
      for(int i=0;i<tp.freq();i++){
          if(ix==intBuff.length){
              _posList.add(intBuff);
              ix=0;
          }
          intBuff[ix++] = tp.nextPosition();
      }
      _posList.add(intBuff,0,ix);
      if (tp.next())
        _termPositionsQueue.adjustTop();
      else {
        _termPositionsQueue.pop();
        tp.close();
      }
    } while (_termPositionsQueue.size() > 0 && _termPositionsQueue.peek().doc() == _doc);

    _posList.sort();
    _freq = _posList.size();

    return true;
  }

  public final int nextPosition() {
    return _posList.next();
  }

  public final boolean skipTo(int target) throws IOException {
    while (_termPositionsQueue.peek() != null && target > _termPositionsQueue.peek().doc()) {
      TermPositions tp = (TermPositions) _termPositionsQueue.pop();
      if (tp.skipTo(target))
        _termPositionsQueue.put(tp);
      else
        tp.close();
    }
    return next();
  }

  public final int doc() {
    return _doc;
  }

  public final int freq() {
    return _freq;
  }

  public final void close() throws IOException {
    while (_termPositionsQueue.size() > 0)
      ((TermPositions) _termPositionsQueue.pop()).close();
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public void seek(Term arg0) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public void seek(TermEnum termEnum) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public int read(int[] arg0, int[] arg1) throws IOException {
    throw new UnsupportedOperationException();
  }
  
  
  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public int getPayloadLength() {
    throw new UnsupportedOperationException();
  }
   
  /**
   * Not implemented.
   * @throws UnsupportedOperationException
   */
  public byte[] getPayload(byte[] data, int offset) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   *
   * @return false
   */
  // TODO: Remove warning after API has been finalized
  public boolean isPayloadAvailable() {
    return false;
  }
  
  static class Skipper implements Runnable {
      private Exchanger<TermPositions> exchanger;
      private int skipTo;
      public Skipper(Exchanger<TermPositions> exchanger, int skipTo){
          this.exchanger = exchanger;
          this.skipTo = skipTo;
      }
      public void run(){
          TermPositions tp = FIRST;
          try{
          while(tp != null){
              tp = exchanger.exchange(tp);
              if(tp != null){
                  if(!tp.skipTo(skipTo)){
                      tp = FIRST;
                  }
              }
          }
          }
          catch(InterruptedException ie){
              System.err.println(ie);
              return;
          }
          catch(IOException ie){
              System.err.println(ie);
              return;
          }
      }
  }
  static TermPositions FIRST = new TermPositions(){

    public byte[] getPayload(byte[] arg0, int arg1) throws IOException {
        return null;
    }

    public int getPayloadLength() {
        return 0;
    }

    public boolean isPayloadAvailable() {
        return false;
    }

    public int nextPosition() throws IOException {
        return 0;
    }

    public void close() throws IOException {
    }

    public int doc() {
        return -1;
    }

    public int freq() {
        return 0;
    }

    public boolean next() throws IOException {
        return false;
    }

    public int read(int[] arg0, int[] arg1) throws IOException {
        return 0;
    }

    public void seek(Term arg0) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void seek(TermEnum arg0) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public boolean skipTo(int arg0) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }
      
  };
}
