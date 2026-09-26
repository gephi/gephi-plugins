package pl.edu.wat.wcy.gephi.plugin.dbscan.util;

import org.gephi.graph.api.Node;

import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

public class Utils {

    public static Set<Node> spliteratorToSet(Spliterator<Node> spliterator) {
        return StreamSupport
                .stream(spliterator, false)
                .collect(toSet());
    }
}
