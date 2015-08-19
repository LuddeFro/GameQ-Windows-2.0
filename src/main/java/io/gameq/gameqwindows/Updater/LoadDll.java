package io.gameq.gameqwindows.Updater;

import io.gameq.gameqwindows.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fabianwikstrom on 8/19/2015.
 */
public class LoadDll {
    //        private static final String LIB_BIN = "//";
//        private final static Log logger = LogFactory.getLog(ACWrapper.class);
    private final static String JNETPCAP = "jnetpcap";

    static {
//            logger.info("Loading DLL");
        try {
            System.loadLibrary(JNETPCAP);
            System.out.println("Tried");
//                logger.info("DLL is loaded from memory");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("catchhhh");
                loadLib("libpcap" + Long.toString(System.currentTimeMillis()), JNETPCAP);
        }
    }

    /**
     * Puts library to temp dir and loads to memory
     */
    private static void loadLib(String path, String name) {
        name = name + ".dll";
        try {
            System.out.println("loadlib called");
            // have to use a stream
            System.out.println(Main.class.getResource("/"+name).getPath());
            InputStream in = Main.class.getResourceAsStream("/jnetpcap.dll");
            // always write to different location
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/jnetpcap.dll");
            System.out.println(fileOut.getPath());
//                logger.info("Writing dll to: " + fileOut.getAbsolutePath());
            OutputStream out = FileUtils.openOutputStream(fileOut);
            IOUtils.copy(in, out);
            in.close();
            out.close();
            System.load(fileOut.toString());
        } catch (Exception e) {
            System.out.println("bajs dll gar inte att ladda");
            e.printStackTrace();
        }
    }
}
