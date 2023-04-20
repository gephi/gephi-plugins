/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import java.util.logging.Logger;

/**
 *
 * @author edemairy
 */
public class EmptyAbstractPostProcessor extends AbstractPostProcessor {

    private static final Logger logger = Logger.getLogger(EmptyAbstractPostProcessor.class.getName());

    @Override
    public void run() {
        logger.info("Applying empty post-processing");
    }
}
