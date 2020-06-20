package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public class Siguyama implements Algorithm<Map<Node<String>, Point2D.Float>> {

    Graph<String> graph;

    public Siguyama(Graph<String> graph) {
        this.graph = graph;
    }

    public Map<Node<String>, Point2D.Float> compute() {
        Graph<String> acyclicGraph = removeCycles();
        Map<Node<String>, Float> layeredNodes = layerAssignment();
        Map<Node<String>, Float> horizontalPositionedNodes = crossingReduction(layeredNodes);

        Map<Node<String>, Point2D.Float> nodePositions = new HashMap<>();
        layeredNodes.forEach((node, yPosition) -> {
            var xPosition = horizontalPositionedNodes.get(node);
            nodePositions.put(node, new Point2D.Float(xPosition, yPosition));
        });
        return nodePositions;
    }

    private Graph<String> removeCycles() {
        return new GreedyEdgeRemoval(graph).compute();
    }

    private Map<Node<String>, Float> layerAssignment() {
        return new LongestPath(graph).compute();
    }

    private Map<Node<String>, Float> crossingReduction(Map<Node<String>, Float> layerAssignment) {
        return new CrossingReduction(graph, layerAssignment).compute();
    }
}
