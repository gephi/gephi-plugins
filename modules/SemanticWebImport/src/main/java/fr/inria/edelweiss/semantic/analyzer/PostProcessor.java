package fr.inria.edelweiss.semantic.analyzer;

import org.gephi.graph.api.GraphModel;

/**
 * Define the interfaces for the classes implementing a behaviour to be applied after building a graph from a Sparql request.
 *
 * @author edemairy
 */
public abstract class PostProcessor implements Runnable {

    private GraphModel model;

    @Override
    abstract public void run();

    public GraphModel getModel() {
        return model;
    }

    public void setModel(GraphModel model) {
        this.model = model;
    }
}
