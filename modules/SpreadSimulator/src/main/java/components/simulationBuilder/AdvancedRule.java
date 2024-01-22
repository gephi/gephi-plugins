package components.simulationBuilder;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AdvancedRule {
    public String rule;
    public Integer coverage;
    public boolean ascending;

    @Override
    public String toString(){
        return  rule + " " +
                coverage.toString() + " "
                + (ascending ? "ascending" : "descending");
    }
}
