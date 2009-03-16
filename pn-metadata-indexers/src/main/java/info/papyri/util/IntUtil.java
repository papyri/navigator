package info.papyri.util;

public abstract class IntUtil {
    private static final int MASK = 0xff;
    public static byte[] toBytes(int i){
        byte [] b = new byte[4];
        b[0] =(byte) (MASK & (i >> 24));
        b[1] =(byte) (MASK & (i >> 16));
        b[2] =(byte) (MASK & (i >> 8));
        b[3] =(byte) (MASK & i);
        return b;
    }
    
    public static int fromBytes(byte[] b){
        int i = (((b[0] & MASK) << 24) | ((b[1] & MASK) << 16) | ((b[2] & MASK) << 8) | (b[3] & MASK));
        return i;
    }
}
