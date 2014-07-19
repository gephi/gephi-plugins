/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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
package org.gephi.neo4j.plugin.impl;

import java.util.Collection;
import java.util.Iterator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service = LifecycleManager.class, position = 1)
public class CustomNetBeansLifecycleManager extends LifecycleManager {

    @Override
    public void saveAll() {
    }

    @Override
    public void exit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (GraphDatabaseService graphDB : GraphModelImportConverter.getAllGraphDBs())
                    graphDB.shutdown();
            }
        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                Collection c = Lookup.getDefault().lookup(new Lookup.Template(LifecycleManager.class)).allInstances();
                for (Iterator i = c.iterator(); i.hasNext();) {
                    LifecycleManager lm = (LifecycleManager) i.next();
                    if (lm != CustomNetBeansLifecycleManager.this) {
                        lm.exit();
                    }
                }
            }
        }).start();
    }
}
