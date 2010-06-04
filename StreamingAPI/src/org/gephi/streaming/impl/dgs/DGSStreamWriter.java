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

import java.io.OutputStream;
import java.io.PrintStream;

import org.gephi.streaming.api.StreamWriter;

/**
 * @author panisson
 *
 */
public class DGSStreamWriter extends BaseDGSStreamWriter implements StreamWriter {

    private OutputStream outputStream;
    
    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void startStream() {
        super.outputHeader();
    }

    @Override
    public void endStream() {
        super.outputEndOfFile();
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.out = new PrintStream(outputStream);
    }

    @Override
    public void edgeAttributeAdded(String edgeId,
            String attributeName, Object value) {
        this.edgeAttributeAdded(edgeId, attributeName, value);
    }

    @Override
    public void edgeAttributeChanged(String edgeId,
            String attributeName, Object newValue) {
        super.edgeAttributeChanged(edgeId, attributeName, null, newValue);
    }

    @Override
    public void edgeAttributeRemoved(String edgeId,
            String attributeName) {
        super.edgeAttributeRemoved(edgeId, attributeName);
    }

    @Override
    public void graphAttributeAdded(String attributeName,
            Object value) {
        super.graphAttributeAdded(attributeName, value);
    }

    @Override
    public void graphAttributeChanged(String attributeName,
            Object newValue) {
        super.graphAttributeChanged(attributeName, null, newValue);
    }

    @Override
    public void graphAttributeRemoved(String attributeName) {
        super.graphAttributeRemoved(attributeName);
    }

    @Override
    public void nodeAttributeAdded(String nodeId,
            String attributeName, Object value) {
        super.nodeAttributeAdded(nodeId, attributeName, value);
    }

    @Override
    public void nodeAttributeChanged(String nodeId,
            String attributeName, Object newValue) {
        super.nodeAttributeChanged(nodeId, attributeName, null, newValue);
    }

    @Override
    public void nodeAttributeRemoved(String nodeId,
            String attributeName) {
        super.nodeAttributeRemoved(nodeId, attributeName);
    }

}
