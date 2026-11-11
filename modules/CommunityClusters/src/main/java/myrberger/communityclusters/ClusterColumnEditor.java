package myrberger.communityclusters;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Drop-down for the "Cluster column" setting. Lists the categorical node columns of the current
 * workspace, plus an entry meaning "no column: detect communities with Leiden". The stored value is the
 * column id (an empty string for the Leiden entry).
 */
public final class ClusterColumnEditor extends PropertyEditorSupport {

    private static final Logger LOG = Logger.getLogger(ClusterColumnEditor.class.getName());

    private static String noneLabel() {
        return NbBundle.getMessage(ClusterColumnEditor.class, "CommunityClusters.clusterColumn.none");
    }

    /** Snapshot of the eligible columns right now. Never throws: on any problem the list is just empty. */
    private static void snapshot(List<String> ids, List<String> titles) {
        try {
            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
            GraphModel gm = gc == null ? null : gc.getGraphModel();
            if (gm == null) {
                return;
            }
            for (Column c : gm.getNodeTable().toArray()) {
                if (ClusterColumns.isCategorical(c.getTypeClass(), c.isArray(), c.isDynamic(), c.isProperty())) {
                    ids.add(c.getId());
                    titles.add(c.getTitle());
                }
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Could not list node columns", e);
        }
    }

    @Override
    public String[] getTags() {
        List<String> ids = new ArrayList<>(), titles = new ArrayList<>();
        snapshot(ids, titles);
        Map<String, Integer> labels = ClusterColumns.labels(ClusterColumns.toArray(ids), ClusterColumns.toArray(titles));
        List<String> tags = new ArrayList<>();
        tags.add(noneLabel());
        tags.addAll(labels.keySet());
        return tags.toArray(new String[0]);
    }

    @Override
    public String getAsText() {
        Object v = getValue();
        String stored = v == null ? "" : v.toString().trim();
        if (stored.isEmpty()) {
            return noneLabel();
        }
        List<String> ids = new ArrayList<>(), titles = new ArrayList<>();
        snapshot(ids, titles);
        String[] idArr = ClusterColumns.toArray(ids), titleArr = ClusterColumns.toArray(titles);
        int at = ClusterColumns.find(stored, idArr, titleArr);
        if (at < 0) {
            return stored; // a column that is not in the list (any more): show what is stored
        }
        for (Map.Entry<String, Integer> e : ClusterColumns.labels(idArr, titleArr).entrySet()) {
            if (e.getValue() == at) {
                return e.getKey();
            }
        }
        return stored;
    }

    @Override
    public void setAsText(String text) {
        if (text == null || text.trim().isEmpty() || text.equals(noneLabel())) {
            setValue("");
            return;
        }
        List<String> ids = new ArrayList<>(), titles = new ArrayList<>();
        snapshot(ids, titles);
        String[] idArr = ClusterColumns.toArray(ids), titleArr = ClusterColumns.toArray(titles);
        Integer at = ClusterColumns.labels(idArr, titleArr).get(text);
        setValue(at != null ? idArr[at] : text.trim());
    }
}
