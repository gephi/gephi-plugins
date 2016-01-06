/*
 *  Copyright (C) 2011 edemairy
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package fr.inria.edelweiss.semantic.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class IsicilRead {
    private static final Logger LOGGER = Logger.getLogger(IsicilRead.class.getName());
    private static final Map<String, String> urls = new HashMap<String, String>() {

        {
            put("isicilTags", "http://vigilante.inria.fr/sprean-server/api/tags/");
            put("isicilPersons", "http://vigilante.inria.fr/sprean-server/api/persons/");
            put("inria", "http://www.inria.fr/");
            put("dbpedia_beatles", "http://dbpedia.org/data/The_Beatles.rdf");
        }
    };

    public void readURL(String url) throws IOException {


        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);
        BufferedReader br = null;
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw new IOException("An error occurred");
            } else {
                LOGGER.info("Reading the result sent by isicil : begin");
                InputStream input = method.getResponseBodyAsStream();
                assert(input != null);
                br = new BufferedReader(new InputStreamReader(input));
                String readLine;
                while (((readLine = br.readLine()) != null)) {
                    LOGGER.info(readLine);
                }
                LOGGER.info("Reading the result sent by isicil : end");
            }

        } finally {
            method.releaseConnection();
            if (br != null) {
                try {
                    br.close();
                } catch (Exception fe) {
                    LOGGER.severe(fe.getMessage());
                }
            }
        }
    }

    @Test
    public void testInria() throws IOException {
        readURL(urls.get("inria"));
    }

    @Test
    public void testIsicil() throws IOException {
        readURL(urls.get("isicilTags"));
        readURL(urls.get("isicilPersons"));
    }

    @Test
    public void testDbPedia() throws IOException {
        readURL(urls.get("dbpedia_beatles"));
    }
}
