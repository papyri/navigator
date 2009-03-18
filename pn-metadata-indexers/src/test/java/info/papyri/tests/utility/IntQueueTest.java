package info.papyri.tests.utility;

import info.papyri.util.IntQueue;
import junit.framework.TestCase;
public class IntQueueTest extends TestCase {
    public void testCopy(){
        IntQueue q1 = new IntQueue(5);
        for(int i=1;i<6;i++){
           q1.add(i);   
        }
        IntQueue q2 = IntQueue.copy(q1);
        assertEquals(q1.size(),q2.size());
        while(q1.size() > 0){
            assertEquals(q1.next(),q2.next());
        }
    }
}
