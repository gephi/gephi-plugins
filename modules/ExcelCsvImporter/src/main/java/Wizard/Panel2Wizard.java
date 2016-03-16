/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wizard;

import java.awt.Component;
import java.io.FileNotFoundException;
import javax.swing.event.ChangeListener;
import Controller.MyFileImporter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

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
public class Panel2Wizard implements WizardDescriptor.ValidatingPanel {

    private Component component;

    @Override
    public Component getComponent() {
        if (component == null) {
            Panel2 panel2 = new Panel2();
            component = panel2;

        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void readSettings(WizardDescriptor data) {

    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    @Override
    public void validate() throws WizardValidationException {
        if (Panel2.firstConnector == null || Panel2.secondConnector == null) {
            throw new WizardValidationException(null, "Please select 2 kinds of agents to connect (can be the same ones)", null);
        }
    }

    @Override
    public void readSettings(Object data) {
        if (MyFileImporter.getFilePath().endsWith("xlsx")) {
            try {
                MyFileImporter.parseExcel();
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidFormatException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            MyFileImporter.parseCsv();
        }
        ((Panel2) getComponent()).getjList1().setModel(MyFileImporter.getListModelHeaders());
        ((Panel2) getComponent()).getjList2().setModel(MyFileImporter.getListModelHeaders());
    }

    @Override
    public void storeSettings(Object data) {
    }
}
