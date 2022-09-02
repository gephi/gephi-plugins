/*
 * author: Clément Levallois
 */
package net.clementlevallois.web.publish.plugin.controller;

/*
 *
 * @author LEVALLOIS
 */
import java.util.ResourceBundle;
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/*

 * @author Clement Levallois
 */
@ServiceProvider(service = ExporterClassUI.class)
public final class GephiPluginDesktopLogic implements ExporterClassUI {

    private static final ResourceBundle bundle = NbBundle.getBundle(GephiPluginDesktopLogic.class);

    @Override
    public String getName() {
        return bundle.getString("general.message.plugin.name");
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        String initialValue = bundle.getString("general.verb.close_window");
        String[] options = new String[]{initialValue};
        JPanelWebExport jPanelWebExport = new JPanelWebExport();
        NotifyDescriptor dd = new NotifyDescriptor(jPanelWebExport,
                bundle.getString("general.message.plugin.name"),
                NotifyDescriptor.INFORMATION_MESSAGE,
                NotifyDescriptor.PLAIN_MESSAGE,
                options,
                initialValue);
        DialogDisplayer.getDefault().notify(dd);
    }
}
