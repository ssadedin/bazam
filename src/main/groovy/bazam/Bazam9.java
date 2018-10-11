package bazam;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class Bazam9 {
    
    /**
     * Invokes the real Bazam main after attempting the workaround to suppress the 
     * annoying Java 9 module warnings about groovy code.
     * 
     * @param args
     */
    public static void main(String [] args) {
        disableWarning();
        Bazam.main(args);
    }
    
    /**
     * See https://stackoverflow.com/questions/46454995/how-to-hide-warning-illegal-reflective-access-in-java-9-without-jvm-argument
     */
    public static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }
}
