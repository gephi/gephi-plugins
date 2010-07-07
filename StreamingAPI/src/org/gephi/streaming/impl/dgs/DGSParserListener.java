/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.impl.dgs;

import java.util.Map;

/**
 * A parser listener for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public interface DGSParserListener {
    
    void onNodeAdded( String sourceId, String nodeId, Map<String, Object> attributes );
//    void onNodeAdded( String sourceId, long timeId, String nodeId );
    void onNodeRemoved( String sourceId, String nodeId );
//    void onNodeRemoved( String sourceId, long timeId, String nodeId );
    void onEdgeAdded( String sourceId, String edgeId, String fromNodeId, String toNodeId, boolean directed );
//    void onEdgeAdded( String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed );
    void onEdgeRemoved( String sourceId, String edgeId );
//    void onEdgeRemoved( String sourceId, long timeId, String edgeId );
    void onEdgeAttributeAdded( String sourceId, String edgeId, String attribute, Object value );
//    void onEdgeAttributeAdded( String sourceId, long timeId, String edgeId, String attribute, Object value );
    void onEdgeAttributeChanged( String sourceId, String edgeId, String attribute,
            Object oldValue, Object newValue );
//    void onEdgeAttributeChanged( String sourceId, long timeId, String edgeId, String attribute,
//            Object oldValue, Object newValue );
    void onEdgeAttributeRemoved( String sourceId, String edgeId, String attribute );
//    void onEdgeAttributeRemoved( String sourceId, long timeId, String edgeId, String attribute );
    void onGraphAttributeAdded( String sourceId, String attribute, Object value );
//    void onGraphAttributeAdded( String sourceId, long timeId, String attribute, Object value )
    void onGraphAttributeChanged( String sourceId, String attribute, Object oldValue,
            Object newValue );
//    void onGraphAttributeChanged( String sourceId, long timeId, String attribute, Object oldValue,
//            Object newValue );
    void onGraphAttributeRemoved( String sourceId, String attribute );
//    void onGraphAttributeRemoved( String sourceId, long timeId, String attribute );
    void onNodeAttributeAdded( String sourceId, String nodeId, String attribute, Object value );
//    void onNodeAttributeAdded( String sourceId, long timeId, String nodeId, String attribute, Object value );
    void onNodeAttributeChanged( String sourceId, String nodeId, String attribute,
            Object oldValue, Object newValue );
//    void onNodeAttributeChanged( String sourceId, long timeId, String nodeId, String attribute,
//            Object oldValue, Object newValue );
    void onNodeAttributeRemoved( String sourceId, String nodeId, String attribute );
//    void onNodeAttributeRemoved( String sourceId, long timeId, String nodeId, String attribute );
    
    void onStepBegins(String graphName, double time);

}
