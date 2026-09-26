
package org.fernunihagen.fapra.girvannewman;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Andrej Sibirski
 */
@ServiceProvider(service = StatisticsUI.class)
public class GirvanNewmanUI implements StatisticsUI {
    private GirvanNewmanPanel girvanNewmanPanel; 
    

    @Override
    public JPanel getSettingsPanel() {
        girvanNewmanPanel = new GirvanNewmanPanel(); 
        return girvanNewmanPanel; 
    }

    @Override
    public void setup(Statistics ststcs) {
        GirvanNewman girvanNewman = (GirvanNewman) ststcs;
        girvanNewman.setRespETypeSPB(girvanNewmanPanel.isRespETypeSPB());
        girvanNewman.setRespMultiESPB(girvanNewmanPanel.isRespMultiESPB());
        girvanNewman.setRespETypeModul(girvanNewmanPanel.isRespETypeModularity());
        girvanNewman.setRespMultiEModul(girvanNewmanPanel.isRespMultiEModularity());
        
    }

    @Override
    public void unsetup() {
      
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return GirvanNewman.class; 
    }

    @Override
    public String getValue() {
        return null; 
    }

    @Override
    public String getDisplayName() {
        return "Girvan-Newman Clustering";
    }

    @Override
    public String getShortDescription() {
        return null; 
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 2000;
    }
    
}
