package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;

import java.awt.geom.Point2D;
import java.util.*;

public class LongestPath implements Algorithm<Map<Node<String>, Float>> {

    Graph<String> graph;

    public LongestPath(Graph<String> graph) {
        this.graph = graph;
    }

    @Override
    public Map<Node<String>, Float> compute() {
        Map<Node<String>, Float> layeredNodes = new HashMap<>(graph.nodeCount());
        Set<Node<String>> nodesToVisit = new HashSet<>(graph.nodes());

        int nodeCount = graph.nodeCount();
        graph.forEachNode(node -> {
            if (graph.degree(node) == 0) {
                layeredNodes.put(node, (float) nodeCount);
                nodesToVisit.remove(node);
            }
        });
        while (!nodesToVisit.isEmpty()) {
            for (Node<String> node : nodesToVisit) {
                List<Node<String>> neighbors = graph.neighborsForNode(node);
                if (neighbors.stream().allMatch(layeredNodes::containsKey)) {
                    double minValue = neighbors.stream().mapToDouble(layeredNodes::get).min().getAsDouble();
                    layeredNodes.put(node, (float) minValue - 1.0f);
                    nodesToVisit.remove(node);
                }
            }
        }

        return layeredNodes;
    }
}
