/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.complexgenerator.plugin;

import javax.swing.JPanel;
import org.gephi.io.complexgenerator.plugin.MultiBarabasiAlbert;
import org.gephi.io.complexgenerator.plugin.MultiBarabasiAlbertUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = MultiBarabasiAlbertUI.class)
public class MultiBarabasiAlbertUIImpl implements MultiBarabasiAlbertUI {
	private MultiBarabasiAlbertPanel panel;
	private MultiBarabasiAlbert multiBarabasiAlbert;

	@Override
	public JPanel getPanel() {
		if (panel == null)
			panel = new MultiBarabasiAlbertPanel();
		return MultiBarabasiAlbertPanel.createValidationPanel(panel);
	}

	@Override
	public void setup(Generator generator) {
		this.multiBarabasiAlbert = (MultiBarabasiAlbert)generator;

		if (panel == null)
			panel = new MultiBarabasiAlbertPanel();

		panel.NField.setText(String.valueOf(multiBarabasiAlbert.getN()));
		panel.m0Field.setText(String.valueOf(multiBarabasiAlbert.getm0()));
		panel.setMmap(multiBarabasiAlbert.getMmap());
	}

	@Override
	public void unsetup() {
		multiBarabasiAlbert.setN(Integer.parseInt(panel.NField.getText()));
		multiBarabasiAlbert.setm0(Integer.parseInt(panel.m0Field.getText()));
		multiBarabasiAlbert.setMmap(panel.getMmap());
		panel = null;
	}
}
