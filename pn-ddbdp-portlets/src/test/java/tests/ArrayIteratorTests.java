package tests;

import junit.framework.TestCase;
import info.papyri.ddbdp.util.*;
public class ArrayIteratorTests extends TestCase {
    public void testIterator(){
        String [] strings = new String[]{"a","b","c"};
        ArrayIterator<String> iter = new ArrayIterator<String>(strings);
        String next;
        assertTrue(iter.hasNext());
        next = iter.next();
        assertEquals("a",next);
        next = iter.next();
        assertEquals("b",next);
        next = iter.next();
        assertEquals("c",next);
        assertTrue(!iter.hasNext());
        assertTrue(!iter.hasNext()); // just making sure there's no inadvertent flip
    }
    public void testEmpty(){
        ArrayIterator<String> iter = new ArrayIterator<String>(new String[0]);
        assertTrue(!iter.hasNext());
        assertTrue(!iter.hasNext()); // just making sure there's no inadvertent flip
        boolean exception = false;
        try{
            iter.next();
        }
        catch(java.util.NoSuchElementException e){
            exception = true;
        }
        assertTrue(exception);
    }
}
