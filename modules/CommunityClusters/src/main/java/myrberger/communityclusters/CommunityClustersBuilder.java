package myrberger.communityclusters;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class CommunityClustersBuilder implements LayoutBuilder {

    private final UI ui = new UI();

    @Override
    public String getName() {
        return NbBundle.getMessage(CommunityClustersBuilder.class, "CommunityClustersBuilder.name");
    }

    @Override
    public Layout buildLayout() {
        return new CommunityClusters(this);
    }

    @Override
    public LayoutUI getUI() {
        return ui;
    }

    private static class UI implements LayoutUI {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(CommunityClustersBuilder.class, "CommunityClustersBuilder.description");
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
    }
}
