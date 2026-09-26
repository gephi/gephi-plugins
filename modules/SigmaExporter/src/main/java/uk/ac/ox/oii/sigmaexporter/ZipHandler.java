/*
 Copyright Scott A. Hale, 2016
 */
package uk.ac.ox.oii.sigmaexporter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;

public class ZipHandler {

    public static void extractZip(InputStream input, String dest) throws Exception {
        byte[] buf = new byte[1024];
        ZipInputStream zinstream = new ZipInputStream(input);
        ZipEntry zentry = zinstream.getNextEntry();
        //Logger.getLogger(ZipHandler.class.getName()).log(Level.INFO, "Name of current Zip Entry : " + zentry + "\n");
        while (zentry != null) {
            String entryName = zentry.getName();
            //Logger.getLogger(ZipHandler.class.getName()).log(Level.INFO, "Name of  Zip Entry : " + entryName);
            if (zentry.isDirectory()) {
                File f = new File(dest + "/" + entryName);
                f.mkdirs();
                //Logger.getLogger(ZipHandler.class.getName()).log(Level.INFO, "Create directory " + f.toString());
                zentry = zinstream.getNextEntry();
                continue;
            }
            FileOutputStream outstream = new FileOutputStream(dest + "/" + entryName);
            int n;

            while ((n = zinstream.read(buf, 0, 1024)) > -1) {
                outstream.write(buf, 0, n);

            }
            Logger.getLogger(ZipHandler.class.getName()).log(Level.INFO, "Successfully Extracted File Name : {0}", entryName);
            outstream.close();

            zinstream.closeEntry();
            zentry = zinstream.getNextEntry();
        }
        zinstream.close();
    }
}
