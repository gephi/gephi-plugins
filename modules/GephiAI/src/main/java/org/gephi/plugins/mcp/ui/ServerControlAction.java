/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.mcp.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * Tools menu entry that opens the Gephi AI server control dialog. The
 * registration annotations generate the layer entries at compile time.
 */
@ActionID(category = "Tools", id = "org.gephi.plugins.mcp.ui.ServerControlAction")
@ActionRegistration(displayName = "#CTL_ServerControlAction")
@ActionReference(path = "Menu/Tools", position = 1550)
public final class ServerControlAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        ServerControlPanel.showDialog();
    }
}
