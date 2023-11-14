package net.phreakocious.httpgraph;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/*
TODO: Standardize calls to Logger
      Figure out screen flicker on update
      Keep a window open to allow canceling/stopping thread (maybe with stats from the server service ?
      Set the font to Arial Narrow....
      Make it restartable somehow after canceling?  No clue wtf is up with that.
*/

/**
 *
 * @author phreakocious
 */
public class SnarfData {

	private static final Logger log = Logger.getLogger(SnarfData.class.getName());
	private static final HashMap<String, String> attribtypes = new HashMap<String, String>();
	private String clientaddr, domain, host, url;
	private int bytes;
	private final int MAXLABEL = 32;
	private final HashMap<String, SDNode> nodes = new HashMap<String, SDNode>();
	private final ArrayList<SDEdge> edges = new ArrayList<SDEdge>();

	public SnarfData(String xsrcaddr, URL xurl, String xmethod, String xhost, URL xreferer) {
		this.bytes = 0;
		setClient(xsrcaddr);
		setHost(xhost);
		setUrl(xurl, xmethod);
		addEdge("client", "url");
		addEdge("host", "url");
		addEdge("domain", "host");

		//explodeUri(nodes.get("url"));
		//explodeUri(nodes.get("referer"));
		if (xreferer != null) {
			setReferer(xreferer);
			addEdge("rdomain", "rhost");
			addEdge("rhost", "referer");
			addEdge("referer", "url");
		}
	}

	public class SDNode {

		final String type, id, domain, label;
		final float size;
		boolean labelvisible;
		HashMap<String, Object> attributes;

		public SDNode(String t, String d, String l, String i, boolean lv, float sz) {
			type = t;
			label = l;
			domain = d;
			id = i;
			labelvisible = lv;
			size = sz;
			attributes = new HashMap<String, Object>();
			setAttrib("node_type", type);
			setAttrib("domain", domain);
		}

		public final void setAttrib(String name, Object value) {
			String attribclass = value.getClass().getName();
			String result;

			result = attribtypes.put(name, attribclass);

			if (result != null && !result.equals(attribclass)) {
				log.warning(String.format("Redefined attribute type: %s is now %s !!", result, attribclass));
			}
			attributes.put(name, value);
		}

		public final String getAttribAsString(String name) {
			return attributes.get(name).toString();
		}
	}

	public class SDEdge {

		SDNode src, dst;

		public SDEdge(SDNode source, SDNode destination) {
			src = source;
			dst = destination;
		}
	}

	private void setClient(String rawaddr) {
		clientaddr = rawaddr.replaceFirst("[^\\d]+(\\d+\\.\\d+\\.\\d+\\.\\d+).*", "$1");
		nodes.put("client", new SDNode("client", "localdomain", clientaddr, clientaddr, HttpGraph.INSTANCE.isClientLabelVisible(), 8f));
	}

	private void setUrl(URL xurl, String method) {
		if (xurl == null) {
			log.severe("URL was null! :(");
			return;
		}
		url = xurl.getHost() + xurl.getPath();

		String label = formatLabel(url);
		nodes.put("url", new SDNode("resource", domain, label, url, HttpGraph.INSTANCE.isResourceLabelVisible(), 3f));
		nodes.get("url").setAttrib("method", method);
		nodes.get("url").setAttrib("protocol", xurl.getProtocol());
	}

	//Experimental
	private void explodeUri(SDNode node) {

		String urlpath = node.label;

		ArrayList<String> npath = new ArrayList<String>();
		npath.addAll(Arrays.asList(urlpath.split("/")));

		String nhost = npath.remove(0);
		SDNode prevnode = getNodeByID(nhost);

		if (npath.size() >= 1) {
			npath.remove(npath.size() - 1);
		}

		String path = "/";

		for (String s : npath) {
			path = path + s + "/";
			SDNode newnode = new SDNode("urlpath", domain, path, host + path, true, 1f);
			nodes.put(host + path, newnode);
			log.finer(String.format("edge: %s -> %s", prevnode.id, newnode.id));
			edges.add(new SDEdge(prevnode, newnode));
			prevnode = newnode;
		}

		edges.add(new SDEdge(prevnode, node));
	}

	private void setReferer(URL xreferer) {
		String referer, rdomain, rhost;
		referer = xreferer.getHost() + xreferer.getPath();
		rhost = xreferer.getHost();
		rdomain = parseDomain(rhost);

		nodes.put("referer", new SDNode("resource", rdomain, referer, referer, HttpGraph.INSTANCE.isResourceLabelVisible(), 3f));
		nodes.put("rhost", new SDNode("host", rdomain, rhost, rhost, HttpGraph.INSTANCE.isHostLabelVisible(), 4f));
		nodes.put("rdomain", new SDNode("domain", rdomain, rdomain, rdomain, HttpGraph.INSTANCE.isDomainLabelVisible(), 6f));

	}

	private String formatLabel(String url) {
		String label = url.replaceFirst("[^/]*/", "/");

		if (label.length() > MAXLABEL) {
			label = label.substring(label.length() - MAXLABEL);
			label = "... ".concat(label);
		}

		return label;
	}

	private void setHost(String rawhost) {
		host = rawhost;
		host = host.replaceFirst(":.*$", "");
		domain = parseDomain(host);

		nodes.put("host", new SDNode("host", domain, host, host, HttpGraph.INSTANCE.isHostLabelVisible(), 4f));
		nodes.put("domain", new SDNode("domain", domain, domain, domain, HttpGraph.INSTANCE.isDomainLabelVisible(), 6f));
	}

	private String parseDomain(String domain) {
		domain = domain.replaceFirst("/.*", "");

		if (domain.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
			//
		} else if (domain.matches(".*\\.[^.]{2}\\.[^.]{2}$")) {
			domain = domain.replaceFirst(".*\\.([^.]+\\.[^.]{2}\\.[^.]{2})$", "$1");

		} else {
			domain = domain.replaceFirst(".*\\.([^.]+\\.[^.]+)$", "$1");
		}

		// Eliminates naming conflicts between host and
		// domain for those with no high level qualifier ;)
		domain = domain.concat(".");

		if (domain.equals(".")) {
			domain = "localdomain";
		}
		return domain;
	}

	public void setBytes(int thebytes) {
		bytes = thebytes;
	}

	public SDNode getNode(String nodetype) {
		return (nodes.get(nodetype));
	}

	public SDNode getNodeByID(String id) {
		for (SDNode n : nodes.values()) {
			if (n.id.equals(id)) {
				return n;
			}
		}
		return null;
	}

	public SDNode[] getNodes() {
		return nodes.values().toArray(new SDNode[nodes.size()]);
	}

	public HashMap<String, Object> getAttributes(SDNode node) {
		return node.attributes;
	}

	public HashMap<String, String> getNodeAttributeList() {
		return attribtypes;
	}

	public SDNode[] getEdgeNodes(SDEdge e) {
		SDEdge edge = e;
		return new SDNode[]{edge.src, edge.dst};
	}

	private boolean addEdge(String n1, String n2) {
		SDNode src = nodes.get(n1);
		SDNode dst = nodes.get(n2);

		if (src.label == null || dst.label == null) {
			log.warning(String.format("something is null!  src.label = %s  dst.label = %s", src.label, dst.label));
			return false;
		}
		if (src.label.equals(dst.label)) {
			// log.info(String.format("labels are equal!  label = %s  src.type = %s  dst.type = %s", src.label, src.type, dst.type));
			return false;
		}
		edges.add(new SDEdge(src, dst));
		return true;
	}

	public SDEdge[] getEdges() {
		return edges.toArray(new SDEdge[edges.size()]);
	}

	public void nullCheck() {
		for (SDNode n : getNodes()) {
			for (String attrib : n.attributes.keySet()) {
				String value = n.attributes.get(attrib).toString();
				if (value == null) {
					log.warning(String.format("node %s attrib %s is null!", n.label, attrib));
				}
			}
		}
	}

	public void graphUpdate() {
		//   dump();
		try {
			HttpGraph.queue.put(this);
		} catch (InterruptedException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	private void dump() {
		log.info(String.format("vars:  srcaddr %s", clientaddr));
		log.info(String.format("vars:  bytes %d", bytes));
		log.info(String.format("vars:  url %s", nodes.get("url").label));
		log.info(String.format("vars:  host %s", nodes.get("host").label));
		log.info(String.format("vars:  domain %s", nodes.get("domain").label));
		log.info(String.format("vars:  referer %s", nodes.get("referer").label));
		log.info(String.format("vars:  rhost %s", nodes.get("rhost").label));
		log.info(String.format("vars:  rdomain %s", nodes.get("rdomain").label));
	}
}
