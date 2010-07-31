/**
 * 
 */
package org.gephi.streaming.server.impl;

import org.gephi.graph.api.Graph;
import org.gephi.streaming.server.ServerController;
import org.gephi.streaming.server.ServerControllerFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = ServerControllerFactory.class)
public class ServerControllerFactoryImpl implements ServerControllerFactory {

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.ServerControllerFactory#createServerController(org.gephi.graph.api.Graph)
     */
    @Override
    public ServerController createServerController(Graph graph) {
        return new ServerControllerImpl(graph);
    }

}
