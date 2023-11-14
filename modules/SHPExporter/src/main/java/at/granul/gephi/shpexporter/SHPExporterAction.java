/*
 * Copyright (c) 2017 by Roman Seidl - romanAeTgranul.at
 * 
 *  This Program uses code copyright (c) 2012 by David Shepard
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.granul.gephi.shpexporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "at.granul.gephi.shpexporter.SHPExporterAction")
@ActionRegistration(
        displayName = "#CTL_SHPExporterAction")
@ActionReference(path = "Menu/Plugins", position = 3333)
@Messages("CTL_SHPExporterAction=Export to SHP...")
public final class SHPExporterAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new SHPExporter().execute();
    }
}
