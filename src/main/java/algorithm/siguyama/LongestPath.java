package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;

import java.util.*;

public class LongestPath implements Algorithm<Map<Node<String>, Float>> {

    Graph<String> graph;
    private final List<Node<String>> nodeSequence;

    public LongestPath(Graph<String> graph, List<Node<String>> nodeSequence) {
        this.graph = graph;
        this.nodeSequence = nodeSequence;
    }

    @Override
    public Map<Node<String>, Float> compute() {
        Map<Node<String>, Float> layeredNodes = new HashMap<>(graph.nodeCount());
        List<Node<String>> nodesToVisit = new ArrayList<>(nodeSequence);

        int nodeCount = graph.nodeCount();
        for (Node<String> node : nodeSequence) {
            if (graph.degree(node) == 0 || (graph.degree(node) == 1 && graph.outgoingNeighborsForNode(node).get(0).equals(node))) {
                layeredNodes.put(node, (float) nodeCount);
                nodesToVisit.remove(node);
            }
        }

        while (!nodesToVisit.isEmpty()) {
            for (Node<String> node : List.copyOf(nodesToVisit)) {
                List<Node<String>> neighbors = graph.outgoingNeighborsForNode(node);
                if (neighbors.stream().allMatch(layeredNodes::containsKey)) {
                    double minValue = neighbors.stream().mapToDouble(layeredNodes::get).min().getAsDouble();
                    layeredNodes.put(node, (float) minValue - 1.0f);
                    nodesToVisit.remove(node);
                    break;
                }
            }
        }

        return layeredNodes;
    }
}
