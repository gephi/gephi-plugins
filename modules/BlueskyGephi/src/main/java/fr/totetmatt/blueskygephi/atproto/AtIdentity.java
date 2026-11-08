package fr.totetmatt.blueskygephi.atproto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Unauthenticated ATProto identity resolution: given a handle, follow the
 * standard chain <em>handle &rarr; DID &rarr; DID document &rarr; PDS
 * serviceEndpoint</em> to discover the host that actually stores the account's
 * data.
 *
 * <p>This is independent of any session, so the "Check" button can determine
 * the correct host before the user connects.</p>
 *
 * @author totetmatt
 */
public final class AtIdentity {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    // Public AppView: resolves any handle (including custom domains) to a DID.
    private static final String RESOLVER = "https://public.api.bsky.app";
    private static final String PLC_DIRECTORY = "https://plc.directory/";
    private static final String PDS_SERVICE_SUFFIX = "#atproto_pds";
    private static final String PDS_SERVICE_TYPE = "AtprotoPersonalDataServer";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AtIdentity() {
    }

    /**
     * Resolves a handle to the host of its PDS (without scheme), e.g.
     * {@code shaggymane.us-west.host.bsky.network}.
     *
     * @param handle a Bluesky handle, with or without a leading {@code @}.
     * @return the PDS host.
     * @throws AtProtoException if the handle, DID or PDS endpoint can't be resolved.
     */
    public static String resolveHost(String handle) {
        String normalized = normalize(handle);
        if (normalized.isEmpty()) {
            throw new AtProtoException("Enter a handle first, then press Check.");
        }
        HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        String did = resolveHandleToDid(http, normalized);
        JsonNode didDocument = fetchDidDocument(http, did);
        String endpoint = extractPdsEndpoint(didDocument, did);
        return stripScheme(endpoint);
    }

    private static String normalize(String handle) {
        String h = (handle == null) ? "" : handle.trim();
        if (h.startsWith("@")) {
            h = h.substring(1);
        }
        // Tolerate someone pasting a full profile URL or a scheme-prefixed handle.
        if (h.startsWith("https://")) {
            h = h.substring("https://".length());
        } else if (h.startsWith("http://")) {
            h = h.substring("http://".length());
        }
        int slash = h.indexOf('/');
        if (slash >= 0) {
            h = h.substring(0, slash);
        }
        return h;
    }

    private static String resolveHandleToDid(HttpClient http, String handle) {
        String url = RESOLVER + "/xrpc/com.atproto.identity.resolveHandle?handle="
                + URLEncoder.encode(handle, StandardCharsets.UTF_8);
        JsonNode body = getJson(http, url, "com.atproto.identity.resolveHandle",
                "resolve handle \"" + handle + "\"");
        JsonNode did = body.get("did");
        if (did == null || did.asText().isBlank()) {
            throw new AtProtoException("Bluesky did not return a DID for handle \"" + handle + "\".");
        }
        return did.asText();
    }

    private static JsonNode fetchDidDocument(HttpClient http, String did) {
        final String url;
        if (did.startsWith("did:plc:")) {
            url = PLC_DIRECTORY + URLEncoder.encode(did, StandardCharsets.UTF_8);
        } else if (did.startsWith("did:web:")) {
            String domain = did.substring("did:web:".length()).replace("%3A", ":");
            url = "https://" + domain + "/.well-known/did.json";
        } else {
            throw new AtProtoException("Unsupported DID method for \"" + did + "\".");
        }
        return getJson(http, url, "did-document", "fetch the DID document for \"" + did + "\"");
    }

    private static String extractPdsEndpoint(JsonNode didDocument, String did) {
        JsonNode services = didDocument.get("service");
        if (services != null && services.isArray()) {
            for (JsonNode service : services) {
                JsonNode id = service.get("id");
                JsonNode type = service.get("type");
                boolean isPds = (id != null && id.asText().endsWith(PDS_SERVICE_SUFFIX))
                        || (type != null && PDS_SERVICE_TYPE.equals(type.asText()));
                if (isPds) {
                    JsonNode endpoint = service.get("serviceEndpoint");
                    if (endpoint != null && !endpoint.asText().isBlank()) {
                        return endpoint.asText();
                    }
                }
            }
        }
        throw new AtProtoException("No PDS endpoint (" + PDS_SERVICE_SUFFIX + ") found for \"" + did + "\".");
    }

    private static JsonNode getJson(HttpClient http, String url, String lexicon, String what) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AtProtoException(response.statusCode(), lexicon, response.body());
            }
            return MAPPER.readTree(response.body());
        } catch (IOException e) {
            throw new AtProtoException("Network error trying to " + what + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AtProtoException("Interrupted trying to " + what + ".", e);
        }
    }

    private static String stripScheme(String endpoint) {
        String e = endpoint.trim();
        if (e.startsWith("https://")) {
            e = e.substring("https://".length());
        } else if (e.startsWith("http://")) {
            e = e.substring("http://".length());
        }
        int slash = e.indexOf('/');
        if (slash >= 0) {
            e = e.substring(0, slash);
        }
        return e;
    }
}
