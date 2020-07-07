package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Siguyama implements Algorithm<Pair<Graph<String>, Map<Node<String>, Point2D.Float>>> {

    Graph<String> graph;

    public Siguyama(Graph<String> graph) {
        this.graph = graph;
    }

    public Pair<Graph<String>, Map<Node<String>, Point2D.Float>> compute() {
//        Graph<String> acyclicGraph = removeCycles();
        Graph<String> acyclicGraph = graph;
        Map<Node<String>, Float> layeredNodes = layerAssignment(acyclicGraph);
        CrossingReduction crossingReduction = crossingReduction(acyclicGraph, layeredNodes);
        Map<Node<String>, Float> verticalPositionedNodes = positionAssignment(
                crossingReduction.properGraph(),
                crossingReduction.nodeBlockMapping(),
                crossingReduction.dummies(),
                crossingReduction.layerAssignment());

//        Map<Node<String>, Point2D.Float> nodesWithCoordinates = verticalPositionedNodes
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> new Point2D.Float(entry.getValue(), crossingReduction.layerAssignment().get(entry.getKey()))));
        Map<Node<String>, Point2D.Float> nodesWithCoordinates = new HashMap<>();
        crossingReduction.properGraph().forEachNode(node -> nodesWithCoordinates.put(node, new Point2D.Float(
                (float) crossingReduction.nodeBlockMapping().get(node).position(),
                crossingReduction.layerAssignment().get(node)
        )));
        return Pair.of(crossingReduction.properGraph(), nodesWithCoordinates);
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
