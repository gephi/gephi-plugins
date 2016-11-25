/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wizard;

import java.awt.Font;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
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
public class Panel1Wizard implements WizardDescriptor.ValidatingPanel {

    private List<ChangeListener> listeners; //these allow you to tell Gephi when UI changes are made
    private Panel1 component;
    public static boolean isValid = true;

    @Override
    public Panel1 getComponent() {
        if (component == null) {
            component = new Panel1();

        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public void readSettings(Object data) {
    }

    @Override
    public void storeSettings(Object data) {
    }

    protected final void fireChangeEvent(Object source, boolean oldState, boolean newState) {
        if (oldState != newState) {
            ChangeEvent ev = new ChangeEvent(source);
            for (ChangeListener listener : listeners) {
                listener.stateChanged(ev);
            }
        }
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    @Override
    public void validate() throws WizardValidationException {
        if (Panel1.fileSelectedName == null) {
            throw new WizardValidationException(null, "Please select an Excel or csv  file", null);
        }
        else if (Panel1.fileSelectedName.endsWith("xls")) {
            throw new WizardValidationException(null, "Please convert your excel file ending with .xls to the new format: ending in .xlsx", null);
        }
        else if (!Panel1.fileSelectedName.endsWith("xlsx") & Panel1.selectedFileDelimiter == null) {
            Font font = new Font("Tahoma", Font.BOLD, 11);
            Panel1.jLabelFieldDelimiter.setFont(font);
            throw new WizardValidationException(null, "Please select a field delimiter", null);
        }
    }
}
