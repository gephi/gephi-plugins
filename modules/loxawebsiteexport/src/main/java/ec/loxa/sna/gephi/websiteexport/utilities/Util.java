package ec.loxa.sna.gephi.websiteexport.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author jorgaf
 * 
 */
public class Util {

    
    private File directoryToExtract;
    public static final String PREFIX = "FilesWebSite/";
    
    
    public void unZip(String zipFilePath){        
        ZipUtil.unpack(new File(zipFilePath), getDirectoryToExtract(), new NameMapper() {
            @Override
            public String map(String name) {
                return name.startsWith(PREFIX) ? name.substring(PREFIX.length()) : name;
            }
        });        
    }


    public void copyFromJar(String jar, String source) throws Exception {
        InputStream is = getClass().getResourceAsStream(jar + source);

        File file = new File(getDirectoryToExtract() + (getDirectoryToExtract().getPath().endsWith(File.separator) ? "" : File.separator) + source);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
    }

    /**
     * @return the directoryToExtract
     */
    public File getDirectoryToExtract() {
        return directoryToExtract;
    }

    /**
     * @param directoryToExtract the directoryToExtract to set
     */
    public void setDirectoryToExtract(File directoryToExtract) {
        this.directoryToExtract = directoryToExtract;
    }
}
