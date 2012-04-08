# Copyright 2008-2011 Gephi
# Authors : Luiz Ribeiro <luizribeiro@gmail.com>
# Website : http://www.gephi.org
#
# This file is part of Gephi.
#
# Gephi is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Gephi is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with Gephi.  If not, see <http://www.gnu.org/licenses/>.

# This file defines wrapper methods that can be used as shortcuts in the
# interpreter from the scripting console.

import java.awt.Color as color
from pawt.colors import *


#
# Filters API support
#

def addFilter(filter, name = None):
    import org.gephi.filters.api.FilterController as FilterController
    import org.openide.util.Lookup as Lookup

    filterController = Lookup.getDefault().lookup(FilterController)

    if name != None:
        filterController.rename(filter.getUnderlyingQuery(), name)

    filterController.add(filter.getUnderlyingQuery())


#
# Attributes API support
#

def getNodeAttributes():
    return g.getNodeAttributes()

def getEdgeAttributes():
    return g.getEdgeAttributes()


#
# Export API support
#

def export(filename):
    import org.gephi.io.exporter.api.ExportController as ExportController
    import org.openide.util.Lookup as Lookup
    import java.io.File

    exportController = Lookup.getDefault().lookup(ExportController)
    exportController.exportFile(java.io.File(filename))


#
# Layouts API support
#

import org.gephi.layout.plugin.force.yifanHu.YifanHu as YifanHu
import org.gephi.layout.plugin.force.yifanHu.YifanHuProportional as YifanHuProportional
import org.gephi.layout.plugin.forceAtlas.ForceAtlas as ForceAtlas
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder as ForceAtlas2
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder as FruchtermanReingold
import org.gephi.layout.plugin.labelAdjust.LabelAdjustBuilder as LabelAdjust
import org.gephi.layout.plugin.multilevel.YifanHuMultiLevel as YifanHuMultiLevel
import org.gephi.layout.plugin.random.Random as RandomLayout
import org.gephi.layout.plugin.rotate.ClockwiseRotate as ClockwiseRotate
import org.gephi.layout.plugin.rotate.CounterClockwiseRotate as CounterClockwiseRotate
import org.gephi.layout.plugin.scale.Contract as Contract
import org.gephi.layout.plugin.scale.Expand as Expand

def getLayoutBuilders():
    import org.gephi.layout.spi.LayoutBuilder as LayoutBuilder
    import org.openide.util.Lookup as Lookup

    return Lookup.getDefault().lookupAll(LayoutBuilder)

def runLayout(layoutBuilder, iters = None):
    import org.gephi.layout.api.LayoutController as LayoutController
    import org.openide.util.Lookup as Lookup

    layoutController = Lookup.getDefault().lookup(LayoutController)

    layout = layoutBuilder().buildLayout()
    layout.resetPropertiesValues()
    layoutController.setLayout(layout)

    if iters == None:
        layoutController.executeLayout()
    else:
        layoutController.executeLayout(iters)

def stopLayout():
    import org.gephi.layout.api.LayoutController as LayoutController
    import org.openide.util.Lookup as Lookup

    layoutController = Lookup.getDefault().lookup(LayoutController)
    layoutController.stopLayout()


#
# Miscelaneous functions
#

def setVisible(subgraph):
    global visible
    visible = subgraph


#
# Handy aliases
#

add_filter = addFilter
get_node_attributes = getNodeAttributes
get_edge_attributes = getEdgeAttributes
get_layout_builders = getLayoutBuilders
run_layout = runLayout
stop_layout = stopLayout
graph = g
