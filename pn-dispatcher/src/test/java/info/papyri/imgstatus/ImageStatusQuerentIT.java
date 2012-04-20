package info.papyri.imgstatus;

import java.net.MalformedURLException;
import java.util.EnumMap;
import junit.framework.TestCase;

/**
 * Test for class info.papyri.imgstatus.ImageStatusQuerent.
 * 
 * @author thill
 */
public class ImageStatusQuerentIT extends TestCase{
    
    ImageStatusQuerent testInstance;
    
    public ImageStatusQuerentIT(String testName) throws MalformedURLException{
        super(testName);
        testInstance = new ImageStatusQuerent();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getImageTypesFromIdentifier method, of class ImageStatusQuerent.
     */
    public void testGetImageTypesFromIdentifier() throws Exception {
                
   /*     EnumMap<ImageStatusQuerent.ImageType, Boolean> test1 = testInstance.getImageTypesFromIdentifier("http://papyri.info/ddbdp/o.berenike;1;105");
        assertTrue(test1.get(ImageStatusQuerent.ImageType.INT));
        assertFalse(test1.get(ImageStatusQuerent.ImageType.EXT));
        assertFalse(test1.get(ImageStatusQuerent.ImageType.PRINT));
        
        EnumMap<ImageStatusQuerent.ImageType, Boolean> test2 = testInstance.getImageTypesFromIdentifier("http://papyri.info/ddbdp/p.col;5;1,v,1b");
        assertTrue(test2.get(ImageStatusQuerent.ImageType.INT));
        assertTrue(test2.get(ImageStatusQuerent.ImageType.EXT));
        assertFalse(test2.get(ImageStatusQuerent.ImageType.PRINT));
        
        EnumMap<ImageStatusQuerent.ImageType, Boolean> test3 = testInstance.getImageTypesFromIdentifier("http://papyri.info/ddbdp/p.worp;;58");
        assertTrue(test3.get(ImageStatusQuerent.ImageType.INT));
        assertTrue(test3.get(ImageStatusQuerent.ImageType.EXT));
        assertTrue(test3.get(ImageStatusQuerent.ImageType.PRINT));        
        
        EnumMap<ImageStatusQuerent.ImageType, Boolean> test4 = testInstance.getImageTypesFromIdentifier("http://papyri.info/ddbdp/sb;18;13210");
        assertFalse(test4.get(ImageStatusQuerent.ImageType.INT));
        assertFalse(test4.get(ImageStatusQuerent.ImageType.EXT));
        assertFalse(test4.get(ImageStatusQuerent.ImageType.PRINT));
        
        EnumMap<ImageStatusQuerent.ImageType, Boolean> test5 = testInstance.getImageTypesFromIdentifier("http://papyri.info/ddbdp/doesntexistactually");
        assertEquals(0, test5.size());
        
      */  
    }
}
