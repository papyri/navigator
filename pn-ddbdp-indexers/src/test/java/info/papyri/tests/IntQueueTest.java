package info.papyri.tests;

import junit.framework.TestCase;
import info.papyri.epiduke.lucene.*;
public class IntQueueTest extends TestCase {
    public void testIntQueueBatchAdd(){
        IntQueue control = new IntQueue();
        IntQueue test = new IntQueue();
        int [] buf = new int[50];
        for(int i = 0; i< 50; i++){
            buf[i] = i + 1;
            control.add(buf[i]);
        }
        test.add(buf);
        assertEquals(control.size(),test.size());
        while(control.size() > 0){
            assertEquals(control.next(),test.next());
        }
        assertEquals(control.size(),test.size());
    }
}
