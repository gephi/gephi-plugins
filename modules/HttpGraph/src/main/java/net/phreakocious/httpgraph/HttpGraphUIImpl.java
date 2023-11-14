package net.phreakocious.httpgraph;

/**
 *
 * @author phreakocious
 */
import javax.swing.JPanel;
import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = HttpGraphUI.class)
public class HttpGraphUIImpl implements HttpGraphUI {

	private HttpGraphPanel panel;
	private HttpGraph httpGraph;

	public HttpGraphUIImpl() {
	}

	@Override
	public JPanel getPanel() {
		if (panel == null) {
			panel = new HttpGraphPanel();
		}
		return HttpGraphPanel.createValidationPanel(panel);
	}

	@Override
	public void setup(Generator generator) {
		this.httpGraph = (HttpGraph) generator;

		//Set UI
		if (panel == null) {
			panel = new HttpGraphPanel();
		}
		panel.portField.setText(String.valueOf(httpGraph.getProxyPort()));
		panel.restField.setText(String.valueOf(httpGraph.getRestPort()));
	}

	@Override
	public void unsetup() {
		//Set params
		httpGraph.setProxyPort(Integer.parseInt(panel.portField.getText()));
		httpGraph.setRestPort(Integer.parseInt(panel.restField.getText()));
		httpGraph.setAutoLayout(panel.startLayout.isSelected());
		httpGraph.setClientLabelVisible(panel.clientLabels.isSelected());
		httpGraph.setDomainLabelVisible(panel.domainLabels.isSelected());
		httpGraph.setHostLabelVisible(panel.hostLabels.isSelected());
		httpGraph.setResourceLabelVisible(panel.resourceLabels.isSelected());

		panel = null;
	}
}
