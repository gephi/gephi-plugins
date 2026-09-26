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


@ServiceProvider(service = ExporterClassUI.class, position = Integer.MAX_VALUE)
public final class WebPublishExporterUI implements ExporterClassUI {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

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
        WebExportJPanel jPanelWebExport = new WebExportJPanel();
        NotifyDescriptor dd = new NotifyDescriptor(jPanelWebExport,
                bundle.getString("general.message.plugin.name"),
                NotifyDescriptor.INFORMATION_MESSAGE,
                NotifyDescriptor.PLAIN_MESSAGE,
                options,
                initialValue);
        DialogDisplayer.getDefault().notify(dd);
    }
}
