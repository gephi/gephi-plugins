/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.semantictweet;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author edemairy
 */
public class SemanticTweetAccess {

    public SemanticTweetAccess() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void simpleConnect() throws MalformedURLException, IOException {
        final String url = "http://semantictweet.com/fabien_gandon";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "*/*");

        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        System.out.println(String.format("Server response: code = %d; message = %s", responseCode, responseMessage));

        InputStream response = connection.getInputStream();
        BufferedReader bf = new BufferedReader(new InputStreamReader(response));
        String currentLine = null;
        while ((currentLine = bf.readLine()) != null) {
            System.out.println(currentLine + '\n');
        }
    }
}
