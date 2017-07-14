/**
 * Created by pmurray on 6/13/2017.
 */
package org.gephi.plugin.CirclePack;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class CirclePackLayoutBuilder implements LayoutBuilder {

    @Override
    public String getName() {
        return "Circle Pack Layout";
    }

    @Override
    public Layout buildLayout() {
        return new CirclePackLayout(this);
    }

    @Override
    public LayoutUI getUI() {
        return new LayoutUI() {

            @Override
            public String getDescription() {
                return "Packs circles using Mike Bostock's circle packing algorithm";
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                return null;
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }
}
