/* Copyright 2015 Wouter Spekkink
Authors : Wouter Spekkink <wouterspekkink@gmail.com>
Website : http://www.wouterspekkink.org
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
Copyright 2015 Wouter Spekkink. All rights reserved.
The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License. When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
Contributor(s): Wouter Spekkink

The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0.
Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/
Ref: "Algorithmics Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). 
Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."

 */
package org.wouterspekkink.plugins.metric.mdsstatistics;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 * The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0. Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/ Ref: "Algorithmics
 * Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."
 *
 * For the calculation of shortest paths the plugin uses the algorithm originally used by Gephi as a step in the calculation of centrality metrics.
 *
 * @author wouter
 */
@ServiceProvider(service = StatisticsUI.class)
public class MdsStatisticsUI implements StatisticsUI {

    private MdsStatistics statistic;
    private MdsStatisticsPanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new MdsStatisticsPanel();
        return panel;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.statistic = (MdsStatistics) ststcs;
        if (panel != null) {
            panel.setDissimilarity(statistic.isDissimilarity(), statistic.isSimilarity());
            panel.setDistanceWeight(statistic.getDistanceWeight());
            panel.setNumberDimensions(statistic.getNumberDimensions());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            statistic.setDissimilarity(panel.isDissimilarity());
            statistic.setSimilarity(panel.isSimilarity());
            statistic.setNoWeights(panel.isNoWeights());
            statistic.setDistanceWeight(panel.getDistanceWeight());
            statistic.setNumberDimensions(panel.getNumberDimensions());
        }
        this.panel = null;
        this.statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MdsStatistics.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Multidimensional scaling";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NODE_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 11000;
    }

    @Override
    public String getShortDescription() {
        return null;
    }

}
