/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competencerank;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laura
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class CompetenceBuilder implements StatisticsBuilder{

    @Override
    public String getName() {
        return "CompetenceRank"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Statistics getStatistics() {
        return new CompetenceRank(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return CompetenceRank.class; //To change body of generated methods, choose Tools | Templates.
    }
    
}
