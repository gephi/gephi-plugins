/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import Controller.MyFileImporter;
import org.openide.WizardDescriptor;
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
public class Panel6Wizard implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    private List<ChangeListener> listeners; //these allow you to tell Gephi when UI changes are made
    private Component component;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            Panel6 panel5 = new Panel6();
            component = panel5;
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;             //if you implement the change listeners properly, this should contain actual logic
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        if (listeners == null) {
            listeners = new ArrayList();
        }

        listeners.add(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        listeners.remove(cl);
    }

    @Override
    public void readSettings(WizardDescriptor data) {
        ((Panel6) getComponent()).jLabel3.setText(MyFileImporter.getFirstConnectedAgent());
        ((Panel6) getComponent()).jLabel5.setText(MyFileImporter.getSecondConnectedAgent());
        MyFileImporter.innerLinksIncluded = Panel5.jCheckBoxInnerLinks.isSelected();

    }

    @Override
    public void storeSettings(WizardDescriptor data) {
    }
}
