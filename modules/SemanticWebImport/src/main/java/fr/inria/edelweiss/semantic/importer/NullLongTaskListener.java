/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.semantic.importer;

import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
class NullLongTaskListener implements LongTaskListener {

    public NullLongTaskListener() {
    }

    @Override
    public void taskFinished(LongTask lt) {
    }

}
