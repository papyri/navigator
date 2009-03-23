package info.papyri.ddbdp.util;

public class ArrayIterator<T> implements java.util.Iterator<T> {
    final T[] items;
    int ix;
    boolean next;
     public ArrayIterator(T[] a){
        this.items = a;
        this.ix = 0;
        next = items.length != 0;
   }
    public boolean hasNext() {
        return next ;
    }

    public T next() {
        if(!next){
            throw new java.util.NoSuchElementException("No  more elements");
        }
        T result = items[ix];
        next &= (++ix < items.length);
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("java.util.Iterator.remove() not implemented");
    }

}
