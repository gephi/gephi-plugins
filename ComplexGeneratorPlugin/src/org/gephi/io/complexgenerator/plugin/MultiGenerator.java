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
package org.gephi.io.complexgenerator.plugin;

import java.util.ArrayList;
import java.util.List;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates a graph using the specified list of generators.
 *
 * @author Cezary Bartosiak
 */
// @ServiceProvider(service = Generator.class)
public class MultiGenerator implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private List<Generator> chosenGenerators = new ArrayList<Generator>();

	@Override
	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, chosenGenerators.size());
		container.setEdgeDefault(EdgeDefault.UNDIRECTED);

		for (Generator generator : chosenGenerators)
			if (!cancel)
				generator.generate(container);
			else break;

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	public List<Generator> getChosenGenerators() {
		return chosenGenerators;
	}

	public void setChosenGenerators(List<Generator> chosenGenerators) {
		this.chosenGenerators = chosenGenerators;
	}

	@Override
	public String getName() {
		return "Multi Generator";
	}

	@Override
	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(MultiGeneratorUI.class);
	}

	@Override
	public boolean cancel() {
		cancel = true;
		return true;
	}

	@Override
	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
