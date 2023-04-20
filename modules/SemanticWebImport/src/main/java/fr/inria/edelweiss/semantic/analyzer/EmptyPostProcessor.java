/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import fr.inria.edelweiss.semantic.analyzer.PostProcessor;
import java.util.logging.Logger;

/**
 *
 * @author edemairy
 */
public class EmptyPostProcessor extends PostProcessor {

    private static final Logger logger = Logger.getLogger(EmptyPostProcessor.class.getName());

    @Override
    public void run() {
        logger.info("Applying empty post-processing");
    }
}
