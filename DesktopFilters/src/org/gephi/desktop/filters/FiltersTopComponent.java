/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

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

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import java.util.logging.Logger;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.filters//Filters//EN",
autostore = false)
public final class FiltersTopComponent extends TopComponent {

    private static FiltersTopComponent instance;
    static final String ICON_PATH = "org/gephi/desktop/filters/resources/small.png";
    private static final String PREFERRED_ID = "FiltersTopComponent";
    //Panel
    private FiltersPanel panel;
    //Models
    private FilterModel filterModel;
    private FilterUIModel uiModel;

    public FiltersTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(FiltersTopComponent.class, "CTL_FiltersTopComponent"));
//        setToolTipText(NbBundle.getMessage(FiltersTopComponent.class, "HINT_FiltersTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        panel = new FiltersPanel();
        add(panel, BorderLayout.CENTER);

        //Model management
        FilterController controller = Lookup.getDefault().lookup(FilterController.class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new FilterUIModel());
            }

            public void select(Workspace workspace) {
                filterModel = workspace.getLookup().lookup(FilterModel.class);
                uiModel = workspace.getLookup().lookup(FilterUIModel.class);
                if (uiModel == null) {
                    uiModel = new FilterUIModel();
                    workspace.add(uiModel);
                }
                refreshModel();
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                filterModel = null;
                uiModel = null;
                refreshModel();
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            Workspace workspace = pc.getCurrentWorkspace();
            filterModel = workspace.getLookup().lookup(FilterModel.class);
            uiModel = workspace.getLookup().lookup(FilterUIModel.class);
            if (uiModel == null) {
                uiModel = new FilterUIModel();
                workspace.add(uiModel);
            }
        }
        refreshModel();
    }

    private void refreshModel() {
        panel.refreshModel(filterModel, uiModel);
    }

    public FilterUIModel getUiModel() {
        return uiModel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized FiltersTopComponent getDefault() {
        if (instance == null) {
            instance = new FiltersTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the FiltersTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized FiltersTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(FiltersTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof FiltersTopComponent) {
            return (FiltersTopComponent) win;
        }
        Logger.getLogger(FiltersTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
