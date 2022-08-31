package net.phreakocious.httpgraph;

import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.interceptor.AbstractInterceptor;
import com.predic8.membrane.core.interceptor.Outcome;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 *
 * @author phreakocious
 */
public class HttpGraphProxyInterceptor extends AbstractInterceptor {

	private static final Logger log = Logger.getLogger(HttpGraphProxyInterceptor.class.getName());

	@Override
	public Outcome handleResponse(Exchange exchange) throws Exception {
		String srchost = exchange.getRemoteAddrIp();
		URL url = new URL(exchange.getOriginalRequestUri());
		String method = exchange.getRequest().getMethod();
		String host = exchange.getServer();
		String type = exchange.getResponseContentType();
		URL referer;
		try {
			referer = new URL(exchange.getRequest().getHeader().getFirstValue("Referer"));
		} catch (MalformedURLException e) {
			referer = null;
		}
		int statuscode = exchange.getResponse().getStatusCode();
		long bytes = exchange.getResponseContentLength();

		if (!exchange.getResponse().isBodyEmpty()) {
			bytes = exchange.getResponse().getBody().getLength();
		}

		SnarfData sd = new SnarfData(srchost, url, method, host, referer);

		SnarfData.SDNode urinode = sd.getNode("url");
		urinode.setAttrib("bytes", bytes);
		urinode.setAttrib("content-type", type);
		urinode.setAttrib("status code", statuscode);

		sd.nullCheck();
		sd.graphUpdate();

		log.info(String.format("%s %s %d %d", urinode.id, type, statuscode, bytes));
		return Outcome.CONTINUE;
	}
}
