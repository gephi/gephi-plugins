package fr.totetmatt.blueskygephi.atproto;

/**
 *
 * @author totetmatt
 */
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyActorGetProfile;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollowers;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollows;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetList;
import fr.totetmatt.blueskygephi.atproto.response.ComAtprotoServerCreateSession;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openide.util.Exceptions;

public class AtClient {

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final AtContext context;

    private final HttpClient client = HttpClient.newHttpClient();
    private ComAtprotoServerCreateSession session = null;
ExecutorService executorService = Executors.newFixedThreadPool(2);
    public AtClient(String host) {
        context = new AtContext(host);
    }

    public boolean comAtprotoServerCreateSession(String identifier, String password) {
        try {
            HttpRequest request = HttpRequest.newBuilder(context.getURIForLexicon("com.atproto.server.createSession"))
                    .POST(HttpRequest.BodyPublishers.ofString("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}"))
                    .header("Content-Type", "application/json")
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            this.session = objectMapper.readValue(response.body(), ComAtprotoServerCreateSession.class);
            return true;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest getRequest(String xrpcMethod, HashMap<String, String> params) {
        return HttpRequest
                .newBuilder(context.getURIForLexicon(xrpcMethod, params))
                .GET()
                .header("Authorization", "Bearer " + session.getAccessJwt())
                .build();
    }

    // Yeah, it should be generalized, and async, but it works right now so it's ok.
    public List<AppBskyGraphGetFollowers> appBskyGraphGetFollowers(String actor, Optional<Integer> limitCrawl) {
        List<AppBskyGraphGetFollowers> pagedResponse = new ArrayList<>();
        try {
            var params = new HashMap<String, String>();
            params.put("actor", actor);
            params.put("limit", "100");
            int currentCrawlLoop=0;
            while (limitCrawl.isEmpty() ||currentCrawlLoop < limitCrawl.get()) {
                var request = getRequest("app.bsky.graph.getFollowers", params);
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                AppBskyGraphGetFollowers objectResponse = objectMapper.readValue(response.body(), AppBskyGraphGetFollowers.class);
                pagedResponse.add(objectResponse);
                if (objectResponse.getCursor() == null) {
                    break;
                }
                params.put("cursor", objectResponse.getCursor());
                currentCrawlLoop++;
            }
            return pagedResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AppBskyGraphGetFollows> appBskyGraphGetFollows(String actor, Optional<Integer> limitCrawl) {
        List<AppBskyGraphGetFollows> pagedResponse = new ArrayList<>();
        try {
            var params = new HashMap<String, String>();
            params.put("actor", actor);
            params.put("limit", "100");
            int currentCrawlLoop=0;
            while (limitCrawl.isEmpty() || currentCrawlLoop < limitCrawl.get()) {
                var request = getRequest("app.bsky.graph.getFollows", params);
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                var objectResponse = objectMapper.readValue(response.body(), AppBskyGraphGetFollows.class);
                pagedResponse.add(objectResponse);
                if (objectResponse.getCursor() == null) {
                    break;
                }
                params.put("cursor", objectResponse.getCursor());
                currentCrawlLoop++;
            }
            return pagedResponse;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public AppBskyActorGetProfile appBskyActorGetProfile(String actor) {
        try {
            var params = new HashMap<String, String>();
            params.put("actor", actor);
            var request = getRequest("app.bsky.actor.getProfile", params);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return objectMapper.readValue(response.body(), AppBskyActorGetProfile.class);
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
     public List<AppBskyGraphGetList> appBskyGraphGetList(String list) {
         List<AppBskyGraphGetList> lists = new ArrayList<>();
        try {
            var params = new HashMap<String, String>();
            params.put("list", list);
            params.put("limit", "100");
            while (true) {
            var request = getRequest("app.bsky.graph.getList", params);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
              var objectResponse =  objectMapper.readValue(response.body(), AppBskyGraphGetList.class);
               lists.add(objectResponse);
                         System.out.println(response.body());
                if (objectResponse.getCursor() == null) {
                        break;
                }
               
                params.put("cursor", objectResponse.getCursor());
            }
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return lists;
    }

    public AppBskyActorGetProfile appBskyActorGetProfiles(String actors) {
        try {
            var request = HttpRequest
                    .newBuilder(context.getURIForLexicon("app.bsky.actor.getProfiles", actors))
                    .GET()
                    .header("Authorization", "Bearer " + session.getAccessJwt())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), AppBskyActorGetProfile.class);
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
