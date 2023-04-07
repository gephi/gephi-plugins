/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.utils;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class FilesUtils {

    static final private Logger logger = Logger.getLogger(FilesUtils.class.getName());

    public static Properties readProperties(String fileName) {
        InputStream is = readFile(fileName);

        Properties result = new Properties();
        try {
            result.loadFromXML(is);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "exception occurred when loading the file: {0}", ex.getMessage());
            throw new IllegalArgumentException(ex);
        }
        return result;
    }

    public static InputStream readFile(String fileName) {
        InputStream result = null;
        try {
            result = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (result == null) {
            throw new IllegalArgumentException("Impossible to open the file.");
        }
        return result;
    }

    /**
     * Function to open a JFileChooser and set it properly.
     *
     * @return a File object if one has been chosen. Null otherwise.
     */
    public static File selectFile(final int openMode, final String title, final String buttonText, final String lastDirectoryUsed, final Component parent, final String suffix, final String description) {
        JFileChooser chooser = new JFileChooser(lastDirectoryUsed);
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                return pathname.getName().endsWith(suffix);
            }

            @Override
            public String getDescription() {
                return description;
            }
        });

        int returnVal = chooser.showDialog(parent, buttonText);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile();
    }

    public static InputStream getResourceOrFile(String fileName) throws IllegalArgumentException {
        InputStream result = FilesUtils.class.getResourceAsStream(fileName);
        if (result == null) {
            try {
                result = new FileInputStream(fileName);
            } catch (FileNotFoundException ex) {
            }
        }
        if (result == null) {
            throw new IllegalArgumentException(fileName + " not found.");
        }
        return result;
    }
}
