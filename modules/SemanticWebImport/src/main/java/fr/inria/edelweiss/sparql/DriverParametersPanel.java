/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import java.util.Observer;
import javax.swing.JPanel;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public abstract class DriverParametersPanel<P extends SparqlDriverParameters> extends JPanel implements Observer {
    private P parameters;
    public P getParameters() {
        return parameters;
    }

    public void setParameters(final P parameters) {
        this.parameters = parameters;
    }
}
