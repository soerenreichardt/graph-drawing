package algorithm.siguyama;

import graph.Graph;
import graph.Node;

import java.util.Map;

public class Siguyama {

    Graph<String> graph;

    public Siguyama(Graph<String> graph) {
        this.graph = graph;
    }

    public Graph<String> compute() {
        Graph<String> acyclicGraph = removeCycles();
        Map<Node<String>, Float> layeredNodes = layerAssignment();
        return null;
    }

    private Graph<String> removeCycles() {
        return new GreedyEdgeRemoval(graph).compute();
    }

    private Map<Node<String>, Float> layerAssignment() {
        return new LongestPath(graph).compute();
    }
}
