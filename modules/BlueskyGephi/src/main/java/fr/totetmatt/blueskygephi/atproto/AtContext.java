package fr.totetmatt.blueskygephi.atproto;

/**
 *
 * @author totetmatt
 */
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AtContext {

    private final String host;

    public AtContext(String host) {
        this.host = host;
    }

    private String formatUrl(String host, String lexicon) {
        return "https://" + host + "/xrpc/" + lexicon;
    }

    public URI getURIForLexicon(String lexicon) {

        return URI.create(formatUrl(host, lexicon));
    }

    public URI getURIForLexicon(String lexicon, HashMap<String, String> parameters) {

        String url_parameters = parameters.entrySet().stream().map(x
                -> URLEncoder.encode(x.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(x.getValue(), StandardCharsets.UTF_8)
        ).collect(Collectors.joining("&"));

        return URI.create(formatUrl(host, lexicon) + "?" + url_parameters);
    }

    public URI getURIForLexicon(String lexicon, String parameters) {

        return URI.create(formatUrl(host, lexicon) + "?" + parameters);
    }

}
