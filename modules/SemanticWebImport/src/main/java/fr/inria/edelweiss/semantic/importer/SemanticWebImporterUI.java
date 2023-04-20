package fr.inria.edelweiss.semantic.importer;

import fr.inria.edelweiss.sparql.corese.CoreseDriver;

import javax.swing.JPanel;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author edemairy
 */
@ServiceProvider(service = ImporterUI.class)
public class SemanticWebImporterUI implements ImporterUI {

    private SemanticWebImporterPanel panel;
    private SemanticWebImporter importer;
    private CoreseDriver driver;

    @Override
    public void setup(Importer[] importers) {
        this.importer = (SemanticWebImporter) importers[0];
    }

    @Override
    public JPanel getPanel() {
        driver = new CoreseDriver();
        panel =  new SemanticWebImporterPanel();
        panel.setDriver(driver);
        panel.addResource(SemanticWebImportBuilder.getLastFileName());
        panel.setSparqlRequest("construct \n{?x ?r ?y } \nwhere\n{?x ?r ?y}");
        return panel;
    }

    /*
     *  Called when the user press ok.
     */
    @Override
    public void unsetup(boolean bln) {
        importer.setResources(panel.getResourceList());
        importer.setSparqlRequest(panel.getSparqlRequest());
    }

    @Override
    public String getDisplayName() {
        return "SemanticWebImporter";
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof SemanticWebImporter;
    }
}
