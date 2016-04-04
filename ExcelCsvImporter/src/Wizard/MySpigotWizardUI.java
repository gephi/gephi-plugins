/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wizard;

import Controller.MyFileImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.lookup.ServiceProvider;

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
@ServiceProvider(service = ImporterWizardUI.class)
public class MySpigotWizardUI implements ImporterWizardUI {

    private Panel[] panels = null;

    @Override
    public String getDisplayName() {
        return "Convert Excel and csv files to networks";
    }

    @Override
    public String getCategory() {
        return "Data importer (co-occurrences)";
    }

    @Override
    public String getDescription() {
        return "This plugin helps you import Excel or csv files into Gephi, based on entities which co-occur line by line.\n Feedback and feature requests are welcome!\n contact: @seinecle on Twitter.";
    }

    @Override
    public Panel[] getPanels() {
        if (panels == null) {
            panels = new Panel[6];
            panels[0] = new Panel1Wizard();
            panels[1] = new Panel2Wizard();
            panels[2] = new Panel3Wizard();
            panels[3] = new Panel4Wizard();
            panels[4] = new Panel5Wizard();
            panels[5] = new Panel6Wizard();
        }
        return panels;
    }

    @Override
    public void setup(Panel panel) {
        //Before opening the wizard
    }

    @Override
    public void unsetup(SpigotImporter importer, Panel panel) {
        //When the wizard has been closed
//        ((Panel1) ((Panel) panels[0]).getComponent()).unsetup((MyFileImporter)importer);
        MyFileImporter.innerLinksIncluded = Panel5.jCheckBoxInnerLinks.isSelected();
        MyFileImporter.removeDuplicates = Panel5.jCheckBoxRemoveDuplicates.isSelected();
        MyFileImporter.removeSelfLoops = Panel5.jCheckBoxSelfLoops.isSelected();

        panels = null;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof MyFileImporter;
    }
}
