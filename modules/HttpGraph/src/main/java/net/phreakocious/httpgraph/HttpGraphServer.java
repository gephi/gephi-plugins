package net.phreakocious.httpgraph;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.Exceptions;

//TODO: Make port configurable
public class HttpGraphServer {

	private static final Logger log = Logger.getLogger(HttpGraphServer.class.getName());
	private final byte[] response = "OK\r\n".getBytes();
	private final HttpServer server;

	public HttpGraphServer() throws IOException {
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName("localhost"), HttpGraph.INSTANCE.getRestPort());
		server = HttpServer.create(socketAddress, 0);
		server.createContext("/add_record", new AddRecordHandler());
		server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());  // This will grow as needed based on request volume
		server.start();
		log.info(String.format("HTTP Graph REST server now running on port %d", HttpGraph.INSTANCE.getRestPort()));
	}

	public void stop() {
		server.stop(1);
	}

	class AddRecordHandler implements HttpHandler {

		@SuppressWarnings("empty-statement")
		@Override
		public void handle(HttpExchange ex) throws IOException {
			String POSTBody = IOUtils.toString(ex.getRequestBody(), "UTF-8");
			ex.sendResponseHeaders(200, response.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(response);
			}
			//log.info(POSTBody);
			String srchost = ex.getRemoteAddress().getHostString();
			Stream<String> POSTlines = POSTBody.lines();
			POSTlines.forEach((String line) -> {
				JSONObject details;
				try {
					details = new JSONObject(line);
				} catch (JSONException ex1) {
					log.warning(String.format("Bad JSON: %s", line));
					return;
				}
				URL url;
				try {
					url = new URL(details.getString("url"));
				} catch (Exception ex1) {
					log.warning("Bad URL");
					return;
				}
				URL referer;
				try {
					referer = new URL(details.optString("referer"));
				} catch (Exception e) {
					referer = null;
				}
				String method = details.optString("method");
				String type = details.optString("type");
				String contentType = details.optString("content_type");
				int statuscode = details.optInt("status");
				int bytes = details.optInt("bytes");

				SnarfData sd = new SnarfData(srchost, url, method, url.getHost(), referer);

				SnarfData.SDNode urlnode = sd.getNode("url");
				urlnode.setAttrib("bytes", bytes);
				urlnode.setAttrib("type", type);
				urlnode.setAttrib("content-type", contentType);
				urlnode.setAttrib("status_code", statuscode);

				sd.nullCheck();
				sd.graphUpdate();
			});

			// log.info(String.format("%s %s %s %s %s", urlnode.id, contentType, type, urlnode.getAttribAsString("status_code"), urlnode.getAttribAsString("bytes")));
		}
	}
}
