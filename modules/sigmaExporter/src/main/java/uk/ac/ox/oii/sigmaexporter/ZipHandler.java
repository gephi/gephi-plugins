/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ox.oii.sigmaexporter;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.*;

/**
 *
 * @author shale
 */
public class ZipHandler {

    /*public static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    public static final void unzip(InputStream input, String dest) {
        try {
            ZipInputStream zipFile = new ZipInputStream(input);

            ZipEntry entry = zipFile.getNextEntry();
            while (entry != null) {

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    System.err.println("Extracting directory: " + entry.getName());
                    // This is not robust, just for demonstration purposes.
                    (new File(dest + "/" + entry.getName())).mkdir();
                    continue;
                }

                System.err.println("Extracting file: " + entry.getName());
               // copyInputStream(zipFile.getInputStream(entry),
                //        new BufferedOutputStream(new FileOutputStream(dest + "/" + entry.getName())));
            }

            zipFile.close();
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }*/

    public static void extractZip(InputStream input, String dest) throws Exception {
        byte[] buf = new byte[1024];
        ZipInputStream zinstream = new ZipInputStream(input);
        ZipEntry zentry = zinstream.getNextEntry();
        System.out.println("Name of current Zip Entry : " + zentry + "\n");
        while (zentry != null) {
            String entryName = zentry.getName();
            System.out.println("Name of  Zip Entry : " + entryName);
            if (zentry.isDirectory()) {
                File f = new File(dest + "/" + entryName);
                f.mkdirs();
                System.out.println("Create directory " + f.toString());
                zentry = zinstream.getNextEntry();
                continue;
            }
            FileOutputStream outstream = new FileOutputStream(dest + "/" + entryName);
            int n;

            while ((n = zinstream.read(buf, 0, 1024)) > -1) {
                outstream.write(buf, 0, n);

            }
            System.out.println("Successfully Extracted File Name : "
                    + entryName);
            outstream.close();

            zinstream.closeEntry();
            zentry = zinstream.getNextEntry();
        }
        zinstream.close();
    }
    
    
    /*JDK7 / NIO Way
     * //walk the zip file tree and copy files to the destination
                try {
                    //FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env);
                    FileSystem zipFileSystem = FileSystems.newFileSystem(zipPath, null);
                    final Path root = zipFileSystem.getPath("/");

                    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file,
                                BasicFileAttributes attrs) throws IOException {
                            final Path destFile = Paths.get(path.toString(),
                                    file.toString());
                            System.out.printf("Extracting file %s to %s\n", file, destFile);
                            Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir,
                                BasicFileAttributes attrs) throws IOException {
                            final Path dirToCreate = Paths.get(path.toString(),
                                    dir.toString());
                            if (Files.notExists(dirToCreate)) {
                                System.out.printf("Creating directory %s\n", dirToCreate);
                                Files.createDirectory(dirToCreate);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    zipPath.toFile().delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
     */
}
