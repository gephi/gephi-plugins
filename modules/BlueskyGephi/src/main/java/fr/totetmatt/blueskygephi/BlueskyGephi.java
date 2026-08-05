package fr.totetmatt.blueskygephi;

import fr.totetmatt.blueskygephi.atproto.AtClient;
import fr.totetmatt.blueskygephi.atproto.AtIdentity;
import fr.totetmatt.blueskygephi.atproto.AtProtoException;
import fr.totetmatt.blueskygephi.atproto.response.common.Identity;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.openide.awt.NotificationDisplayer;
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
    private final static String NBPREF_ATPROTO_HOST ="bsky.social";
    private final static String NBPREF_QUERY = "query";
    private final static String NBPREF_QUERY_ISFOLLOWERSACTIVE = "query.isFollowersActive";
    private final static String NBPREF_QUERY_ISFOLLOWSACTIVE = "query.isFollowsActive";
    private final static String NBPREF_QUERY_ISDEEPSEARCH = "query.isDeepSearch";
    private final static String NBPREF_QUERY_ISLIMITCRAWLACTIVE = "query.isLimitCrawlActive";
    private final static String NBPREF_QUERY_LIMITCRAWL = "query.limitCrawl";

    private final Preferences nbPref = NbPreferences.forModule(BlueskyGephi.class);

    private AtClient client;
    private GraphModel graphModel;

    // Bounded pool so pasting many handles can't spawn an unbounded number of
    // threads all contending on the graph write-lock and hammering the API.
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("[Bsky] worker");
        return t;
    });

    public BlueskyGephi() {
    }

    private void ensureWorkspace() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        // A workspace can only live inside a project.
        if (projectController.getCurrentProject() == null) {
            projectController.newProject();
        }
        // A project can exist with no open workspace (e.g. all were closed), and
        // the graph model can't be resolved until one is created and opened.
        if (projectController.getCurrentWorkspace() == null) {
            Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
            projectController.openWorkspace(workspace);
        }
    }

    public boolean connect(String host,String handle, String password) {
        nbPref.put(NBPREF_BSKY_HANDLE, handle);
        nbPref.put(NBPREF_BSKY_PASSWORD, password);
        nbPref.put(NBPREF_ATPROTO_HOST,host);

        client = new AtClient(host);
        return client.comAtprotoServerCreateSession(handle, password);

    }
    public String getHost(){
        return nbPref.get(NBPREF_ATPROTO_HOST,"bsky.social");
    }

    /**
     * Determines the account's PDS host from its handle (unauthenticated), so
     * the user can auto-fill the host field. This is a network call and must be
     * run off the EDT.
     *
     * @return the resolved PDS host, e.g. {@code shaggymane.us-west.host.bsky.network}.
     * @throws AtProtoException if the handle can't be resolved to a PDS.
     */
    public String resolveHostFromHandle(String handle) {
        return AtIdentity.resolveHost(handle);
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

    public void setIsLimitCrawlActive(boolean isLimitCrawlActive) {
        nbPref.putBoolean(NBPREF_QUERY_ISLIMITCRAWLACTIVE, isLimitCrawlActive);
    }

    public boolean getIsLimitCrawlActive() {
        return nbPref.getBoolean(NBPREF_QUERY_ISLIMITCRAWLACTIVE, true);
    }

    public void setLimitCrawl(int limitCrawl) {
        nbPref.putInt(NBPREF_QUERY_LIMITCRAWL, limitCrawl);
    }

    public int getLimitCrawl() {
        return nbPref.getInt(NBPREF_QUERY_LIMITCRAWL, 50);
    }

    private Node createNode(Identity i) {

        Node node = graphModel.getGraph().getNode(i.getDid());
        if (node == null) {
            node = graphModel.factory().newNode(i.getDid());
            node.setLabel(i.getHandle());
            node.setAttribute("Description", i.getDescription());
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

    /**
     * Reports a failure to the user without the raw NetBeans stack-trace dialog:
     * the full technical detail (with stack trace) is logged for debugging, while
     * the user sees a concise, non-blocking notification bubble with an
     * actionable reason.
     */
    private void reportError(String title, String detail, Throwable t) {
        logger.log(Level.WARNING, title + " - " + detail, t);
        EventQueue.invokeLater(() -> {
            Icon icon = UIManager.getIcon("OptionPane.warningIcon");
            if (icon == null) {
                icon = new javax.swing.ImageIcon(
                        new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB));
            }
            NotificationDisplayer.getDefault().notify(title, icon, detail, null,
                    NotificationDisplayer.Priority.HIGH);
        });
    }

    private void fetchFollowerFollowsFromActor(String actor, List<String> listInit, boolean isFollowsActive, boolean isFollowersActive, boolean isDeepSearch) {
        // Run on a bounded background pool to keep the Gephi UI responsive.
        executor.submit(new FetchTask(actor, listInit, isFollowsActive, isFollowersActive, isDeepSearch));
    }

    /**
     * A single fetch job. Reads the network for one actor (and, when deep
     * search or a list is involved, for the discovered friends-of-a-friend)
     * and writes it into the graph.
     */
    private final class FetchTask implements Runnable {

        private final String actor;
        private final List<String> listInit;
        private final boolean isFollowsActive;
        private final boolean isFollowersActive;
        private final boolean isDeepSearch;

        private final Set<String> foaf = new HashSet<>();
        private volatile boolean cancelled = false;
        private volatile Thread worker;
        private ProgressTicket progressTicket;
        private LongConsumer onWaiting;

        FetchTask(String actor, List<String> listInit, boolean isFollowsActive, boolean isFollowersActive, boolean isDeepSearch) {
            this.actor = actor;
            this.listInit = listInit;
            this.isFollowsActive = isFollowsActive;
            this.isFollowersActive = isFollowersActive;
            this.isDeepSearch = isDeepSearch;
        }

        private boolean shouldStop() {
            return cancelled || Thread.currentThread().isInterrupted();
        }

        private void process(String actor, boolean isDeepSearch, Optional<Integer> limitCrawl) {
            if (client == null || graphModel == null) {
                return;
            }
            try {
                if (isFollowsActive && !shouldStop()) {
                    // Each page is written as it arrives; pagination stops when the
                    // worker is interrupted (cancellation).
                    client.appBskyGraphGetFollows(actor, limitCrawl, response -> {
                        if (shouldStop()) {
                            return;
                        }
                        Identity subject = response.getSubject();
                        if (subject == null || subject.getDid() == null) {
                            return;
                        }
                        // Capture the graph once so lock/unlock always target the
                        // same instance, and release it in finally so an exception
                        // (bad data, API error, cancellation) can never leak the
                        // write-lock and freeze Gephi.
                        Graph graph = graphModel.getGraph();
                        graph.writeLock();
                        try {
                            Node source = createNode(subject);
                            source.setColor(Color.GREEN);
                            List<Identity> follows = response.getFollows();
                            if (follows != null) {
                                for (var follow : follows) {
                                    if (follow == null || follow.getDid() == null) {
                                        continue;
                                    }
                                    if (isDeepSearch) {
                                        foaf.add(follow.getDid());
                                    }
                                    Node target = createNode(follow);
                                    createEdge(source, target);
                                }
                            }
                        } finally {
                            graph.writeUnlock();
                        }
                    }, onWaiting);
                }

                if (isFollowersActive && !shouldStop()) {
                    client.appBskyGraphGetFollowers(actor, limitCrawl, response -> {
                        if (shouldStop()) {
                            return;
                        }
                        Identity subject = response.getSubject();
                        if (subject == null || subject.getDid() == null) {
                            return;
                        }
                        Graph graph = graphModel.getGraph();
                        graph.writeLock();
                        try {
                            Node target = createNode(subject);
                            target.setColor(Color.GREEN);
                            List<Identity> followers = response.getFollowers();
                            if (followers != null) {
                                for (var follower : followers) {
                                    if (follower == null || follower.getDid() == null) {
                                        continue;
                                    }
                                    if (isDeepSearch) {
                                        foaf.add(follower.getDid());
                                    }
                                    Node source = createNode(follower);
                                    createEdge(source, target);
                                }
                            }
                        } finally {
                            graph.writeUnlock();
                        }
                    }, onWaiting);
                }
            } catch (AtProtoException e) {
                // Expected failure mode (auth, rate limit, bad handle, network):
                // report a clear reason plus the server's own response, not a stack trace.
                if (!cancelled) {
                    reportError("Bluesky: could not fetch \"" + actor + "\"", e.getUserMessageWithContent(), e);
                }
            } catch (Exception e) {
                if (!cancelled) {
                    reportError("Bluesky: could not fetch \"" + actor + "\"",
                            "Unexpected error: " + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? " - " + e.getMessage() : ""), e);
                }
            }
        }

        @Override
        public void run() {
            worker = Thread.currentThread();
            final String taskName = (actor != null) ? "[Bsky] fetching " + actor : "[Bsky] fetching List";
            worker.setName(taskName);

            progressTicket = Lookup.getDefault()
                    .lookup(ProgressTicketProvider.class)
                    .createTicket(taskName, () -> {
                        cancelled = true;
                        Thread w = worker;
                        if (w != null) {
                            w.interrupt();
                        }
                        return true;
                    });
            // Surface the client's rate-limit backoff as a live countdown in the progress bar.
            onWaiting = remainingMillis -> {
                if (remainingMillis <= 0L) {
                    Progress.setDisplayName(progressTicket, taskName);
                } else {
                    long seconds = (remainingMillis + 999L) / 1000L;
                    Progress.setDisplayName(progressTicket, taskName + " - rate limited, retrying in " + seconds + "s");
                }
            };
            try {
                Progress.start(progressTicket);
                Progress.switchToIndeterminate(progressTicket);

                if (listInit != null) {
                    foaf.addAll(listInit);
                }
                if (actor != null) {
                    process(actor, isDeepSearch, Optional.empty());
                }
                if ((listInit != null || isDeepSearch) && !shouldStop()) {
                    Progress.switchToDeterminate(progressTicket, foaf.size());
                    for (var foafActor : foaf) {
                        if (shouldStop()) {
                            break;
                        }
                        Progress.setDisplayName(progressTicket, "[Bsky] fetching " + actor + " n+1 > " + foafActor);
                        if (getIsLimitCrawlActive()) {
                            process(foafActor, false, Optional.of(getLimitCrawl()));
                        } else {
                            process(foafActor, false, Optional.empty());
                        }
                        Progress.progress(progressTicket);
                    }
                }
            } finally {
                Progress.finish(progressTicket);
            }
        }
    }

    private Stream<String> manageList(String listId) {
        List<String> dids = new ArrayList<>();
        client.appBskyGraphGetList(listId, page -> {
            if (page.getItems() != null) {
                for (var item : page.getItems()) {
                    if (item != null && item.getSubject() != null && item.getSubject().getDid() != null) {
                        dids.add(item.getSubject().getDid());
                    }
                }
            }
        }, null);
        return dids.stream();
    }

    private void initGraphTable() {
        // Create necessary model for the graph entities
        if (!graphModel.getNodeTable().hasColumn("Description")) {
            graphModel.getNodeTable().addColumn("Description", String.class);
        }
    }

    public void fetchFollowerFollowsFromActors(List<String> actors, boolean isFollowsActive, boolean isFollowersActive, boolean isBlocksActive) {
        // Guarantee a workspace exists before touching the graph model.
        ensureWorkspace();
        if (client == null) {
            logger.warning("Not connected to Bluesky. Please connect before fetching.");
            return;
        }
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        initGraphTable();
        actors.stream()
                .map(String::trim)
                .filter(actor -> !actor.isEmpty())
                .forEach(actor -> fetchFollowerFollowsFromActor(actor, null, isFollowsActive, isFollowersActive, getIsDeepSearch()));
    }

    public void fetchFollowerFollowsFromActors(List<String> actors) {
        // Guarantee a workspace exists before touching the graph model.
        ensureWorkspace();
        if (client == null) {
            logger.warning("Not connected to Bluesky. Please connect before fetching.");
            return;
        }
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        initGraphTable();

        List<String> cleanActors = actors.stream()
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());

        cleanActors.stream()
                .filter(x -> !x.contains("app.bsky.graph.list"))
                .forEach(actor -> fetchFollowerFollowsFromActor(actor, null, getIsFollowsActive(), getIsFollowersActive(), getIsDeepSearch()));

        List<String> listIds = cleanActors.stream()
                .filter(x -> x.contains("app.bsky.graph.list"))
                .collect(Collectors.toList());

        if (!listIds.isEmpty()) {
            // Resolving a list is itself several paged network calls, so do it
            // off the EDT instead of blocking the UI thread.
            executor.submit(() -> {
                try {
                    List<String> listActor = listIds.stream()
                            .flatMap(this::manageList)
                            .collect(Collectors.toList());
                    fetchFollowerFollowsFromActor(null, listActor, getIsFollowsActive(), getIsFollowersActive(), getIsDeepSearch());
                } catch (AtProtoException e) {
                    reportError("Bluesky: could not resolve list", e.getUserMessageWithContent(), e);
                } catch (Exception e) {
                    reportError("Bluesky: could not resolve list",
                            "Unexpected error: " + e.getClass().getSimpleName()
                            + (e.getMessage() != null ? " - " + e.getMessage() : ""), e);
                }
            });
        }
    }
}
