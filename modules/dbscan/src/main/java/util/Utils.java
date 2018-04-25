package util;

import org.gephi.graph.api.Node;

import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Utils {

    public static Set<Node> spliteratorToSet(Spliterator<Node> spliterator) {
        return StreamSupport
                .stream(spliterator, false)
                .collect(Collectors.toSet());
    }
}
