package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Siguyama implements Algorithm<Map<Node<String>, Point2D.Float>> {

    Graph<String> graph;

    public Siguyama(Graph<String> graph) {
        this.graph = graph;
    }

    public Map<Node<String>, Point2D.Float> compute() {
        Graph<String> acyclicGraph = removeCycles();
        Map<Node<String>, Float> layeredNodes = layerAssignment(acyclicGraph);
        CrossingReduction crossingReduction = crossingReduction(acyclicGraph, layeredNodes);
        Map<Node<String>, Float> verticalPositionedNodes = positionAssignment(
                crossingReduction.properGraph(),
                crossingReduction.nodeBlockMapping(),
                crossingReduction.dummies(),
                crossingReduction.layerAssignment());

        return verticalPositionedNodes
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new Point2D.Float(entry.getValue(), crossingReduction.layerAssignment().get(entry.getKey()))));
    }

    private Graph<String> removeCycles() {
        return new GreedyEdgeRemoval(graph).compute();
    }

    private Map<Node<String>, Float> layerAssignment(Graph<String> acyclicGraph) {
        return new LongestPath(acyclicGraph).compute();
    }

    private CrossingReduction crossingReduction(Graph<String> acyclicGraph, Map<Node<String>, Float> layerAssignment) {
        return new CrossingReduction(acyclicGraph, layerAssignment).compute();
    }

    private Map<Node<String>, Float> positionAssignment(
            Graph<String> properGraph,
            Map<Node<String>, CrossingReduction.Block> nodeBlockMapping,
            Map<Node<String>, List<Relationship<String>>> innerSegments,
            Map<Node<String>, Float> layerAssignment
    ) {
        return new BrandesAndKoepf(properGraph, nodeBlockMapping, innerSegments, layerAssignment).compute();
    }
}
