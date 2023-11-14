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
# Setup Lookup and Controllers
#

import org.openide.util.Lookup as Lookup

import org.gephi.io.exporter.api.ExportController
ExportController = Lookup.getDefault().lookup(org.gephi.io.exporter.api.ExportController)

import org.gephi.filters.api.FilterController
FilterController = Lookup.getDefault().lookup(org.gephi.filters.api.FilterController)

import org.gephi.io.importer.api.ImportController
ImportController = Lookup.getDefault().lookup(org.gephi.io.importer.api.ImportController)

import org.gephi.layout.api.LayoutController
LayoutController = Lookup.getDefault().lookup(org.gephi.layout.api.LayoutController)

import org.gephi.project.api.ProjectController
ProjectController = Lookup.getDefault().lookup(org.gephi.project.api.ProjectController)

import org.gephi.visualization.VizController
VizController = Lookup.getDefault().lookup(org.gephi.visualization.VizController)


#
# Filters API support
#

def addFilter(filter, name = None):
    if name != None:
        FilterController.rename(filter.getUnderlyingQuery(), name)

    FilterController.add(filter.getUnderlyingQuery())

def removeFilter(filter):
    FilterController.remove(filter.getUnderlyingQuery())

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

def exportGraph(filename):
    import java.io.File
    ExportController.exportFile(java.io.File(filename))


#
# Import API support
#

def importGraph(filename):
    import org.gephi.io.processor.plugin.DefaultProcessor as DefaultProcessor
    import java.io.File

    workspace = ProjectController.getCurrentWorkspace()
    container = ImportController.importFile(java.io.File(filename))
    ImportController.process(container, DefaultProcessor(), workspace)


#
# Layouts API support
#

try:
    import org.gephi.layout.plugin.force.yifanHu.YifanHu as YifanHu
    import org.gephi.layout.plugin.force.yifanHu.YifanHuProportional as YifanHuProportional
    import org.gephi.layout.plugin.forceAtlas.ForceAtlas as ForceAtlas
    import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder as ForceAtlas2
    import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder as FruchtermanReingold
    import org.gephi.layout.plugin.labelAdjust.LabelAdjustBuilder as LabelAdjust
    import org.gephi.layout.plugin.random.Random as RandomLayout
    import org.gephi.layout.plugin.rotate.RotateLayout as Rotate
    import org.gephi.layout.plugin.scale.Contract as Contract
    import org.gephi.layout.plugin.scale.Expand as Expand
except ImportError:
    print "Failed to import some default layout modules " + ImportError


def getLayoutBuilders():
    import org.gephi.layout.spi.LayoutBuilder as LayoutBuilder
    return Lookup.getDefault().lookupAll(LayoutBuilder)

def runLayout(layoutBuilder, iters = None):
    layout = layoutBuilder().buildLayout()
    layout.resetPropertiesValues()
    LayoutController.setLayout(layout)

    if iters == None:
        LayoutController.executeLayout()
    else:
        LayoutController.executeLayout(iters)

def stopLayout():
    LayoutController.stopLayout()


#
# Visualization API support
#

def center(node):
    VizController.selectionManager.centerOnNode(node.getNode())

def selectSubGraph(subgraph):
    selectNodes(subgraph.nodes)
    selectEdges(subgraph.edges)

def selectNodes(nodes):
    VizController.selectionManager.selectNodes([v.getNode() for v in nodes])

def selectEdges(edges):
    VizController.selectionManager.selectEdges([v.getEdge() for v in edges])

def resetSelection():
    VizController.resetSelection()

#
# Miscelaneous functions
#

def setVisible(subgraph):
    global visible
    visible = subgraph

def execurl(url):
    '''
    Allow to load an external Python file from an URL and `exec` it as if were local
    :param url: URL for the file to load
    '''
    import urllib
    exec urllib.urlopen(url).read() in globals()

#
# Handy aliases
#

add_filter = addFilter
remove_filter = removeFilter
get_node_attributes = getNodeAttributes
get_edge_attributes = getEdgeAttributes
get_layout_builders = getLayoutBuilders
run_layout = runLayout
stop_layout = stopLayout
selectSubgraph = selectSubGraph
select_sub_graph = selectSubGraph
select_subgraph = selectSubGraph
select_nodes = selectNodes
select_edges = selectEdges
set_visible = setVisible
graph = g
