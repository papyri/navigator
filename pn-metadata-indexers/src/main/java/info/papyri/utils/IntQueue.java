/**
 * 
 */
package info.papyri.util;

import java.util.Arrays;

public final class IntQueue {
    private int _index = 0;
    private int _lastIndex = 0;
    private int[] _array;
    public IntQueue(){
        this(16);
    }
    public IntQueue(int size){
        _array = new int[size];
    }
    private IntQueue(int[] src, int start, int length){
        _array = new int[length];
        _lastIndex = length;
        System.arraycopy(src,start,_array,0,length);
    }
    public final void add(int i) {
      if (_lastIndex == _array.length)
        growArray();

      _array[_lastIndex++] = i;
    }

    public final int peek(){
        return _array[_index];
    }
    
    public final int next() {
        if(_index > _lastIndex) throw new IndexOutOfBoundsException("Index out of bounds: " + _index);
      return _array[_index++];
    }

    final void sort() {
      Arrays.sort(_array, _index, _lastIndex);
    }

    final void clear() {
      _index = 0;
      _lastIndex = 0;
    }
    
    public final boolean contains(int i){
        for(int j=_index;j<_lastIndex;j++){
            if(i == _array[j]) return true;
        }
        return false;
    }

    public final int size() {
      return (_lastIndex - _index);
    }

    private void growArray() {
      int[] newArray = new int[_array.length + _array.length / 2];
      System.arraycopy(_array, 0, newArray, 0, _array.length);
      _array = newArray;
    }
    public static IntQueue copy(IntQueue original){
        IntQueue copy = new IntQueue(original._array,original._index,original.size());
        return copy;
    }
  }