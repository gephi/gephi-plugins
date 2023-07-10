package AverageGlobalConnectionEfficiency;

import GenericParamForm.GenericPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AverageGlobalConnectionEfficiencyPanel  extends GenericPanel<AverageGlobalConnectionEfficiencyParam> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new AverageGlobalConnectionEfficiencyParam());
    }

    @Override
    protected void CreateCustomFieldOption(GridBagConstraints constraints, AtomicInteger gridYIterator) {
        String[] data = {"Random", "Random Random"};
        JList<String> list = new JList<>(data);
        constraints.gridy = gridYIterator.getAndIncrement();

        constraints.gridx = 2;
        constraints.anchor = GridBagConstraints.EAST;
        add(new JLabel("Removal Strategy"), constraints);

        constraints.gridx = 3;
        constraints.anchor = GridBagConstraints.EAST;

        list.setVisibleRowCount(2);

        add(new JScrollPane(list), constraints);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                var params = (AverageGlobalConnectionEfficiencyParam)getTParams();
                params.msType = list.getSelectedValue() == "Random" ? MsType.Random : MsType.RandomRandom;
            }
        });
    }
}
