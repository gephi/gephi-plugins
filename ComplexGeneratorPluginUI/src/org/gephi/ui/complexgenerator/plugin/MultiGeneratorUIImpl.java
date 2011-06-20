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
import org.gephi.io.complexgenerator.plugin.MultiGenerator;
import org.gephi.io.complexgenerator.plugin.MultiGeneratorUI;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = MultiGeneratorUI.class)
public class MultiGeneratorUIImpl implements MultiGeneratorUI {
	private MultiGeneratorPanel panel;
	private MultiGenerator multiGenerator;

	@Override
	public JPanel getPanel() {
		if (panel == null)
			panel = new MultiGeneratorPanel();
		return panel;
	}

	@Override
	public void setup(Generator generator) {
		this.multiGenerator = (MultiGenerator)generator;

		if (panel == null)
			panel = new MultiGeneratorPanel();

		panel.setChosenGenerators(multiGenerator.getChosenGenerators());
	}

	@Override
	public void unsetup() {
		multiGenerator.setChosenGenerators(panel.getChosenGenerators());
		panel = null;
	}
}
