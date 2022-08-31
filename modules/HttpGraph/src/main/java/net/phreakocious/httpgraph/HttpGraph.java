package net.phreakocious.httpgraph;

import com.predic8.membrane.core.HttpRouter;
import com.predic8.membrane.core.rules.ProxyRule;
import com.predic8.membrane.core.rules.ProxyRuleKey;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import net.phreakocious.httpgraph.SnarfData.SDEdge;
import net.phreakocious.httpgraph.SnarfData.SDNode;
import net.phreakocious.httpgraph.layout.HGForceAtlas;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.AppendProcessor;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author phreakocious
 */
@ServiceProvider(service = Generator.class)
public class HttpGraph implements Generator {

	public static HttpGraph INSTANCE;
	private static final Logger log;
	private HttpRouter router;
	private HttpGraphServer server;
	private static final int COLORDIV = 32;
	private static ArrayList<Color> colors;
	private static final HashMap<String, Color> colormap;
	private Workspace workspace;
	private ImportController importController;
	protected ProgressTicket progress;
	protected boolean cancel = false;
	protected int proxyport = 8088;
	private static boolean autoLayout;
	private static boolean clientLabelVisible, domainLabelVisible, hostLabelVisible, resourceLabelVisible;

	static {
		INSTANCE = new HttpGraph();
		log = Logger.getLogger(HttpGraph.class.getName());
		colormap = new HashMap<>();
		generateColors(COLORDIV);
	}

	public void graphupdate(SnarfData data) {

		if (workspace == null) {
			workspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
		}

		if (autoLayout) {
			LayoutBuilder lb = Lookup.getDefault().lookup(HGForceAtlas.class);
			LayoutController lc = Lookup.getDefault().lookup(LayoutController.class);
			lc.setLayout(lb.buildLayout());
			lc.executeLayout();
			autoLayout = false;
		}

		Container container = Lookup.getDefault().lookup(Container.Factory.class).newContainer();
		container.setSource("HTTP Graph");
		container.setReport(new Report());

		ContainerLoader cldr = container.getLoader();
//	cldr.setTimeRepresentation(TimeRepresentation.TIMESTAMP);

		NodeDraft nd, nd1, nd2;

		for (SDNode n : data.getNodes()) {

			String domain = n.domain;

			if (!cldr.nodeExists(n.id)) {
				// log.log(Level.INFO, "node doesn''t exist! {0}", n.label);

				nd = cldr.factory().newNodeDraft(n.id);

				if (!colormap.containsKey(domain) && !colors.isEmpty()) {
					colormap.put(domain, colors.remove(0));
				}

				nd.setLabel(n.label);
				nd.setColor(colormap.get(domain));
				nd.addTimestamp(new Date().getTime());

				for (String attrib : n.attributes.keySet()) {
					Object value = n.attributes.get(attrib);
					nd.setValue(attrib, value);
				}

				nd.setLabelVisible(n.labelvisible);
				nd.setSize(n.size);
				log.info(String.format("adding node: %s size: %s", n.id, n.size));
				cldr.addNode(nd);
			}
		}

		for (SDEdge e : data.getEdges()) {
			SDNode n1 = e.src;
			SDNode n2 = e.dst;
			nd1 = cldr.getNode(n1.id);
			nd2 = cldr.getNode(n2.id);

			if (!cldr.edgeExists(n1.id, n2.id)) {
				EdgeDraft ed = cldr.factory().newEdgeDraft();
				ed.setSource(nd1);
				ed.setTarget(nd2);
				ed.setWeight(0.8d);
				ed.addTimestamp(new Date().getTime());
				log.info(String.format("adding edge: %s -> %s", n1.id, n2.id));
				cldr.addEdge(ed);
			}
		}

		importController = Lookup.getDefault().lookup(ImportController.class);
		importController.process(container, new AppendProcessor(), workspace);
	}

	@Override
	public void generate(ContainerLoader c) {
		progress.start();

		c = null;                            // We are going to replace the ContainerLoader provided by the Generator

		//Lookup.getDefault().lookup(VizController.class).getInstance().getTextManager().getModel().setShowNodeLabels(true);

		router = new HttpRouter();

		try {
			router.getRuleManager().addProxyAndOpenPortIfNew(new ProxyRule(new ProxyRuleKey(proxyport)));
			router.addUserFeatureInterceptor(new HttpGraphProxyInterceptor());
			router.init();
			server = new HttpGraphServer();
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}

		log.finest("HttpGraph.generate() finished");
	}

	private static void generateColors(int n) {
		colors = new ArrayList<>(COLORDIV);
		for (int i = 0; i < n; i++) {
			//colors.add(Color.getHSBColor((float) i / (float) n, 0.6f, 0.75f));
			colors.add(Color.getHSBColor((float) i / (float) n, 0.85f, 1f));
			//colors.add(Color.getHSBColor((float) i / (float) n, 0.65f, 0.8f));
		}
	}

// <editor-fold defaultstate="collapsed" desc="comment">
	@Override
	public String getName() {
		return "HTTP Graph";
	}

	@Override
	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(HttpGraphUI.class);
	}

	public int getProxyPort() {
		return proxyport;
	}

	public void setProxyPort(int port) {
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("TCP ports must be between 1 and 65535.  Preferably higher than 1024.");
		}
		this.proxyport = port;
	}

	public boolean getAutoLayout() {
		return autoLayout;
	}

	public void setAutoLayout(boolean dolayout) {
		HttpGraph.autoLayout = dolayout;
	}

	public boolean isClientLabelVisible() {
		return clientLabelVisible;
	}

	public void setClientLabelVisible(boolean clientLabelVisible) {
		HttpGraph.clientLabelVisible = clientLabelVisible;
	}

	public boolean isDomainLabelVisible() {
		return domainLabelVisible;
	}

	public void setDomainLabelVisible(boolean domainLabelVisible) {
		HttpGraph.domainLabelVisible = domainLabelVisible;
	}

	public boolean isHostLabelVisible() {
		return hostLabelVisible;
	}

	public void setHostLabelVisible(boolean hostLabelVisible) {
		HttpGraph.hostLabelVisible = hostLabelVisible;
	}

	public boolean isResourceLabelVisible() {
		return resourceLabelVisible;
	}

	public void setResourceLabelVisible(boolean resourceLabelVisible) {
		HttpGraph.resourceLabelVisible = resourceLabelVisible;
	}

	@Override
	public boolean cancel() {
		cancel = true;
		return true;
	}

	@Override
	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progress = progressTicket;
	}// </editor-fold>
}
