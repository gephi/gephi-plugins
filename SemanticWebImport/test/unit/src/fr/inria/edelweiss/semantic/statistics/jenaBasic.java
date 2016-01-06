/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class jenaBasic {

    public jenaBasic() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testBasic() throws IOException {
        Model model = ModelFactory.createDefaultModel();
        File tempRdf = File.createTempFile("temp", ".rdf");
        tempRdf.deleteOnExit();
        FileOutputStream fo = new FileOutputStream(tempRdf);

        System.err.println(model.toString());
        model.write(System.out);
        fo.close();
    }
}
