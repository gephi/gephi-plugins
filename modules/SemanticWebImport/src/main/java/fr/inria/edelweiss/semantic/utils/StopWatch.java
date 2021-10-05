/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.utils;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class StopWatch {

    private long start;

    public StopWatch() {
        start = java.lang.System.currentTimeMillis();
    }

    public long elapsedMillis() {
        long end = java.lang.System.currentTimeMillis();
        return end-start;
    }
}
