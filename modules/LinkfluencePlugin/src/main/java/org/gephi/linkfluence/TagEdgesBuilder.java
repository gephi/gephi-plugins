/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.linkfluence;

import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.datalab.spi.edges.EdgesManipulatorBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builder for TagEdges edges manipulator.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service=EdgesManipulatorBuilder.class)
public class TagEdgesBuilder implements EdgesManipulatorBuilder{

    public EdgesManipulator getEdgesManipulator() {
        return new TagEdges();
    }
}
