/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competencerank;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laura
 */
@ServiceProvider(service = StatisticsUI.class)
public class CompetenceUI implements StatisticsUI{

    private CompetencePanel panel;
     
    //return panel
    @Override
    public JPanel getSettingsPanel() {
        panel = new CompetencePanel();
        return panel;
    }

    //empty beacause algorithm does not use parameters
    @Override
    public void setup(Statistics statistics) {
        if(panel!=null){          
        }
    }

    //empty beacause algorithm does not use parameters
    @Override
    public void unsetup() {
       if(panel!=null){           
       }
       panel=null;
    }

    //return statistics' class the UI belongs to
    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CompetenceRank.class;
    }

    //null because algorithm does not have single value result
    @Override
    public String getValue() {
        return null;
    }

    //name which is shown in Statistics
    @Override
    public String getDisplayName() {
        return "CompetenceRank"; 
    }

    //fill short description
    @Override
    public String getShortDescription() {
        return "Calculates the CompetenceRank."; 
    }

    //algorithm should be placed in category 'Network'
    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    //position of the statistic in the UI
    @Override
    public int getPosition() {
        return 800; 
    }
    
}
