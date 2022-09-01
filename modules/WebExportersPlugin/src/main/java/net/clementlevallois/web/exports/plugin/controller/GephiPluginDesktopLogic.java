/*
 * author: Clément Levallois
 */
package net.clementlevallois.web.exports.plugin.controller;

/*
 *
 * @author LEVALLOIS
 */
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.ServiceProvider;

/*

 * @author Clement Levallois
 */
@ServiceProvider(service = ExporterClassUI.class)
public final class GephiPluginDesktopLogic implements ExporterClassUI {


    @Override
    public String getName() {
        return "Publish to the web";
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {
        String initialValue = "Close";
        String[] options = new String[]{initialValue};
        JPanelWebExport jPanelWebExport = new JPanelWebExport();
        NotifyDescriptor dd = new NotifyDescriptor(jPanelWebExport,
                "Publish to the web",
                NotifyDescriptor.INFORMATION_MESSAGE,
                NotifyDescriptor.PLAIN_MESSAGE,
                options,
                initialValue);
        DialogDisplayer.getDefault().notify(dd);
    }
}
