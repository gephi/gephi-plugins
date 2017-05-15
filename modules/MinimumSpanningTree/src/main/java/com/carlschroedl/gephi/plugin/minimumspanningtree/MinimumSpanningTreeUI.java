/*
 * Your license here
 */

package com.carlschroedl.gephi.plugin.minimumspanningtree;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * See http://wiki.gephi.org/index.php/HowTo_write_a_metric#Create_StatisticsUI
 *
 * @author Carl Schroedl <carlschroedl@gmail.com>
 */
@ServiceProvider(service = StatisticsUI.class)
public class MinimumSpanningTreeUI implements StatisticsUI {

    private MinimumSpanningTreePanel panel;
/*    
    public MinimumSpanningTreeUI(){
        this.myMetric = new MinimumSpanningTree();
    }
  */  
    
    @Override
    public JPanel getSettingsPanel() {
        panel = new MinimumSpanningTreePanel(this);
        return panel; //null if no panel exists
    }

    @Override
    public void setup(Statistics statistics) {
    }

    @Override
    public void unsetup() {
        panel = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return MinimumSpanningTree.class;
    }

    @Override
    public String getValue() {
        //Returns the result value on the front-end. 
        //If your metric doesn't have a single result value, return null.
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Minimum Spanning Tree";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        //The position control the order the metric front-end are displayed. 
        //Returns a value between 1 and 1000, that indicates the position. 
        //Less means upper.
        return 1;
    }

    @Override
    public String getShortDescription() {
        return "Calculate a minimum spanning tree for a graph";
    }
}
