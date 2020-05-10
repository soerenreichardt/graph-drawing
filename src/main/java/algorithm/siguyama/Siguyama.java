package algorithm.siguyama;

import graph.Graph;

public class Siguyama {

    Graph<String> graph;

    public Siguyama(Graph<String> graph) {
        this.graph = graph;
    }

    public Graph<String> compute() {
        Graph<String> acyclicGraph = removeCycles();
        return null;
    }

    private Graph<String> removeCycles() {
        return new GreedyEdgeRemoval(graph).compute();
    }
}
