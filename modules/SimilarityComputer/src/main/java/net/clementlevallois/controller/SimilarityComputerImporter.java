package net.clementlevallois.controller;

/*
 Copyright 2008-2013 Clement Levallois
 Authors : Clement Levallois <clementlevallois@gmail.com>
 Website : http://www.clementlevallois.net


 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Clement Levallois. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s): Clement Levallois

 */
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import net.clementlevallois.wizard.Panel1;
import net.clementlevallois.wizard.Panel2;
import net.clementlevallois.wizard.SimilarityComputerWizardUI;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class SimilarityComputerImporter implements WizardImporter, LongTask {

    public ContainerLoader container;
    private Report report;
    private boolean cancel = false;
    private String[] headers;

    private DefaultListModel listModelHeaders = new DefaultListModel();

    @Override
    public boolean execute(ContainerLoader loader) {

        //Get Importer
        ImporterWizardUI importer = null;
        for (ImporterWizardUI wizardBuilder : Lookup.getDefault().lookupAll(SimilarityComputerWizardUI.class)) {
            importer = wizardBuilder;
        }

        WizardDescriptor.Panel[] panels = importer.getPanels();
        Panel1 panel1 = (Panel1) panels[0].getComponent();
        Panel2 panel2 = (Panel2) panels[1].getComponent();

        container = loader;

        Controller controller = new Controller(container, panel1.getSelectedFileAndPath(), panel1.getSelectedFieldDelimiter(), panel1.getSelectedTextDelimiter(), panel1.getSelectedSheet(), panel1.isHeadersPresent(), panel2.isWeightedAttributes());

        try {
            report = controller.run();
        } catch (InvalidFormatException | ExecutionException | IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return !cancel;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public DefaultListModel getListModelHeaders() {
        return listModelHeaders;
    }

    public void setListModelHeaders(DefaultListModel listModelHeaders) {
        this.listModelHeaders = listModelHeaders;
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
    }

}
