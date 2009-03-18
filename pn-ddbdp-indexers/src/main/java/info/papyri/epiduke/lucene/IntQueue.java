/**
 * 
 */
package info.papyri.epiduke.lucene;

import java.util.Arrays;

public final class IntQueue {
    private int _arraySize = 16;
    private int _index = 0;
    private int _lastIndex = 0;
    private int[] _array = new int[_arraySize];
public IntQueue(){
    this._arraySize = 16;
    this._array = new int[16];
}

public IntQueue(int capacity){
    this._arraySize = capacity;
    this._array = new int[capacity];
}

 public final void add(int i) {
      if (_lastIndex == _arraySize)
        growArray();

      _array[_lastIndex++] = i;
    }
    
public  final void add(int[] ints, int start, int len){
        while ( _arraySize - _lastIndex  < len)
            growArray();
        System.arraycopy(ints,start,_array,_lastIndex,len);
        _lastIndex+= len;
    }

 public   final void add(int[] ints){
        while ( _arraySize - _lastIndex  < ints.length)
            growArray();
        
        System.arraycopy(ints,0,_array,_lastIndex,ints.length);
        _lastIndex+= ints.length;
    }

  public  final int next() {
      return _array[_index++];
    }

 public   final void sort() {
      Arrays.sort(_array, _index, _lastIndex);
    }

  public  final void clear() {
      _index = 0;
      _lastIndex = 0;
    }

  public  final int size() {
      return (_lastIndex - _index);
    }

    private void growArray() {
      int[] newArray = new int[_arraySize * 2];
      System.arraycopy(_array, 0, newArray, 0, _arraySize);
      _array = newArray;
      _arraySize = newArray.length;
    }
  }