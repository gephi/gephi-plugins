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

def addFilter(filter, name = None):
    import org.gephi.filters.api.FilterController as FilterController
    import org.openide.util.Lookup as Lookup

    filterController = Lookup.getDefault().lookup(FilterController)

    if name != None:
        filterController.rename(filter.getUnderlyingQuery(), name)

    filterController.add(filter.getUnderlyingQuery())

def export(filename):
    import org.gephi.io.exporter.api.ExportController as ExportController
    import org.openide.util.Lookup as Lookup
    import java.io.File

    exportController = Lookup.getDefault().lookup(ExportController)
    exportController.exportFile(java.io.File(filename))

def setVisible(subgraph):
    global visible
    visible = subgraph
