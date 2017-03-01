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

 */
package org.wouterspekkink.plugins.metric.lineage;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wouter
 */
@ServiceProvider(service = StatisticsUI.class)
public class LineageUI implements StatisticsUI {

    private Lineage statistic;
    private LineagePanel panel;

    @Override
    public JPanel getSettingsPanel() {
        panel = new LineagePanel();
        return panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.statistic = (Lineage) statistics;
        if (panel != null) {
            panel.setOrigin(statistic.getOrigin());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null) {
            statistic.setOrigin(panel.originNode());
        }
        panel = null;
        statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Lineage.class;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "Lineage";
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
