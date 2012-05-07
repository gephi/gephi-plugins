# Copyright 2008-2012 Gephi
# Authors : Luiz Ribeiro <luizribeiro@gmail.com>
# Website : http://www.gephi.org
#
# This file is part of Gephi.
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2012 Gephi Consortium. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 3 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://gephi.org/about/legal/license-notice/
# or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License files at
# /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 3, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 3] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 3 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 3 code and therefore, elected the GPL
# Version 3 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
# Contributor(s):
#
# Portions Copyrighted 2011 Gephi Consortium.

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
