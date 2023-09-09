package fr.totetmatt.blueskygephi;

import fr.totetmatt.blueskygephi.atproto.AtClient;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollowers;
import fr.totetmatt.blueskygephi.atproto.response.AppBskyGraphGetFollows;
import fr.totetmatt.blueskygephi.atproto.response.common.Identity;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author totetmatt
 */
@ServiceProvider(service = BlueskyGephi.class)
public class BlueskyGephi {

    protected static final Logger logger = Logger.getLogger(BlueskyGephi.class.getName());
    private final static String NBPREF_BSKY_HANDLE = "bsky.handle";
    private final static String NBPREF_BSKY_PASSWORD = "bsky.password";
    private final static String NBPREF_QUERY = "query";
    private final static String NBPREF_QUERY_ISFOLLOWERSACTIVE = "query.isFollowersActive";
    private final static String NBPREF_QUERY_ISFOLLOWSACTIVE = "query.isFollowsActive";
    private final static String NBPREF_QUERY_ISDEEPSEARCH = "query.isDeepSearch";

    private final Preferences nbPref = NbPreferences.forModule(BlueskyGephi.class);
    // If ATProto get released and decentralized, this will change to adapt to other instances
    final private AtClient client = new AtClient("bsky.social");
    final private GraphModel graphModel;

    public BlueskyGephi() {
        initProjectAndWorkspace();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    private void initProjectAndWorkspace() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project currentProject = projectController.getCurrentProject();
        if (currentProject == null) {
            projectController.newProject();
        }
    }

    public boolean connect(String handle, String password) {
        nbPref.put(NBPREF_BSKY_HANDLE, handle);
        nbPref.put(NBPREF_BSKY_PASSWORD, password);

        return client.comAtprotoServerCreateSession(handle, password);

    }

    public String getHandle() {
        return nbPref.get(NBPREF_BSKY_HANDLE, "");
    }

    public String getPassword() {
        return nbPref.get(NBPREF_BSKY_PASSWORD, "");
    }

    public void setQuery(String query) {
        nbPref.put(NBPREF_QUERY, query);
    }

    public String getQuery() {
        return nbPref.get(NBPREF_QUERY, "");
    }

    public void setIsFollowersActive(boolean isFollowersActive) {
        nbPref.putBoolean(NBPREF_QUERY_ISFOLLOWERSACTIVE, isFollowersActive);
    }

    public boolean getIsFollowersActive() {
        return nbPref.getBoolean(NBPREF_QUERY_ISFOLLOWERSACTIVE, true);
    }

    public void setIsFollowsActive(boolean isFollowsActive) {
        nbPref.putBoolean(NBPREF_QUERY_ISFOLLOWSACTIVE, isFollowsActive);
    }

    public boolean getIsFollowsActive() {
        return nbPref.getBoolean(NBPREF_QUERY_ISFOLLOWSACTIVE, true);
    }

    public void setIsDeepSearch(boolean setIsDeepSearch) {
        nbPref.putBoolean(NBPREF_QUERY_ISDEEPSEARCH, setIsDeepSearch);
    }

    public boolean getIsDeepSearch() {
        return nbPref.getBoolean(NBPREF_QUERY_ISDEEPSEARCH, true);
    }

    private Node createNode(Identity i) {

        Node node = graphModel.getGraph().getNode(i.getDid());
        if (node == null) {
            node = graphModel.factory().newNode(i.getDid());
            node.setLabel(i.getHandle());
            node.setSize(10);
            node.setColor(Color.GRAY);
            node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
            graphModel.getGraph().addNode(node);
        }

        return node;
    }

    private Edge createEdge(Node source, Node target) {

        Edge edge = graphModel.getGraph().getEdge(source, target);
        if (edge == null) {
            edge = graphModel.factory().newEdge(source, target, true);
            edge.setWeight(1.0);
            edge.setColor(Color.GRAY);
            graphModel.getGraph().addEdge(edge);
        }

        return edge;
    }

    private void fetchFollowerFollowsFromActor(String actor, boolean isFollowsActive, boolean isFollowersActive, boolean isDeepSearch) {
        // To avoid locking Gephi UI
        Thread t = new Thread() {
            private ProgressTicket progressTicket;
            Set<String> foaf = new HashSet<>();

            private void process(String actor, boolean isDeepSearch) {
                if (isFollowsActive) {
                    List<AppBskyGraphGetFollows> responses = client.appBskyGraphGetFollows(actor);

                    graphModel.getGraph().writeLock();
                    for (var response : responses) {
                        Identity subject = response.getSubject();
                        Node source = createNode(subject);
                        source.setColor(Color.GREEN);
                        for (var follow : response.getFollows()) {
                            if (isDeepSearch) {
                                foaf.add(follow.getDid());
                            }
                            Node target = createNode(follow);
                            createEdge(source, target);
                        }

                    }
                    graphModel.getGraph().writeUnlock();
                }

                if (isFollowersActive) {
                    List<AppBskyGraphGetFollowers> responses = client.appBskyGraphGetFollowers(actor);

                    graphModel.getGraph().writeLock();
                    for (var response : responses) {
                        Identity subject = response.getSubject();
                        Node target = createNode(subject);
                        target.setColor(Color.GREEN);
                        for (var follower : response.getFollowers()) {
                            if (isDeepSearch) {
                                foaf.add(follower.getDid());
                            }
                            Node source = createNode(follower);
                            createEdge(source, target);
                        }
                    }
                    graphModel.getGraph().writeUnlock();
                }
            }

            @Override
            public void run() {
                this.setName("Bluesky Gephi Fetching Data for " + actor);
                progressTicket = Lookup.getDefault()
                        .lookup(ProgressTicketProvider.class)
                        .createTicket(this.getName(), () -> {
                            interrupt();
                            Progress.finish(progressTicket);
                            return true;
                        });

                Progress.start(progressTicket);
                Progress.switchToIndeterminate(progressTicket);

                process(actor, isDeepSearch);
                if (isDeepSearch) {
                    for (var foafActor : foaf) {
                        process(foafActor, false);
                    }
                }
                Progress.finish(progressTicket);
            }
        };
        t.start();

    }

    public void fetchFollowerFollowsFromActors(List<String> actors, boolean isFollowsActive, boolean isFollowersActive, boolean isBlocksActive) {
        actors.stream().forEach(actor -> fetchFollowerFollowsFromActor(actor, isFollowsActive, isFollowersActive, getIsDeepSearch()));
    }

    public void fetchFollowerFollowsFromActors(List<String> actors) {
        actors.stream().forEach(actor -> fetchFollowerFollowsFromActor(actor, getIsFollowsActive(), getIsFollowersActive(), getIsDeepSearch()));
    }
}
