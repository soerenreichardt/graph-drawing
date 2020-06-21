package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrandesAndKoepf implements Algorithm<Map<Node<String>, Float>> {

    private Graph<String> graph;
    private Graph<String> properGraph;
    private final List<CrossingReduction.Block> blocks;
    private Map<Node<String>, CrossingReduction.Block> nodeBlockMapping;
    private final Map<Node<String>, List<Relationship<String>>> innerSegments;
    private Map<Node<String>, Float> layerAssignment;

    private Map<Integer, List<Node<String>>> orderedNodes;

    public BrandesAndKoepf(
            Graph<String> graph,
            Graph<String> properGraph,
            List<CrossingReduction.Block> blocks,
            Map<Node<String>, CrossingReduction.Block> nodeBlockMapping,
            Map<Node<String>, List<Relationship<String>>> innerSegments,
            Map<Node<String>, Float> layerAssignment
    ) {
        this.graph = graph;
        this.properGraph = properGraph;
        this.blocks = blocks;
        this.nodeBlockMapping = nodeBlockMapping;
        this.innerSegments = innerSegments;
        this.layerAssignment = layerAssignment;

        orderedNodes = new TreeMap<>(Comparator.comparingInt(entry -> entry));
        layerAssignment.forEach((node, layer) -> {
            orderedNodes.putIfAbsent(layer.intValue(), new ArrayList<>());
            orderedNodes.get(layer.intValue()).add(node);
        });

        orderedNodes.forEach((layer, nodes) -> nodes.sort(Comparator.comparingInt(node-> nodeBlockMapping.get(node).position)));
    }

    @Override
    public Map<Node<String>, Float> compute() {
        return null;
    }

    private Set<Relationship<String>> markType1Conflicts() {
        Set<Relationship<String>> markedSegments = new HashSet<>();
        List<Integer> layers = List.copyOf(orderedNodes.keySet());
        for (int i = 0; i < layers.size() - 1; i++) {
            int k_0 = 0;
            int l = 1;
            List<Node<String>> nextLayerNodes = orderedNodes.get(layers.get(i + 1));
            for (int j = 1; j < nextLayerNodes.size(); j++) {
                if (j == nextLayerNodes.size() - 1 || incidentToInnerSegments(nextLayerNodes.get(j), i, i+1)) {
                    List<Node<String>> currentLayerNodes = orderedNodes.get(layers.get(i));
                    int k_1 = currentLayerNodes.size();
                    if (incidentToInnerSegments(nextLayerNodes.get(j), i, i+1)) {
                        var upperNeighbor = getUpperNeighbor(nextLayerNodes.get(j), currentLayerNodes);
                        k_1 = search(upperNeighbor, currentLayerNodes);
                    }
                    while (l <= j) {
                        for (Node<String> node : getUpperNeighbors(nextLayerNodes.get(l), currentLayerNodes)) {
                            int k = search(node, currentLayerNodes);
                            if (k < k_0 || k > k_1) {
                                markedSegments.add(findSegment(node, nextLayerNodes.get(l)));
                            }
                        }
                        l++;
                    }
                    k_0 = k_1;
                }
            }
        }
        return markedSegments;
    }

    private boolean incidentToInnerSegments(Node<String> node, int lowerLayer, int upperLayer) {
        return innerSegmentsStream()
                .filter(rel ->
                        layerAssignment.get(rel.source()) == lowerLayer && layerAssignment.get(rel.target()) == upperLayer
                                || layerAssignment.get(rel.source()) == upperLayer && layerAssignment.get(rel.target()) == lowerLayer
                )
                .filter(rel -> rel.source().equals(node) || rel.target().equals(node))
                .count() != 0;
    }

    private List<Node<String>> getUpperNeighbors(Node<String> node, List<Node<String>> searchSpace) {
        return innerSegmentsStream()
                .filter(rel -> (searchSpace.contains(rel.source()) && rel.target().equals(node))
                        || (searchSpace.contains(rel.target()) && rel.source().equals(node)))
                .map(rel -> rel.source().equals(node) ? rel.target() : rel.source())
                .collect(Collectors.toList());
    }

    private Node<String> getUpperNeighbor(Node<String> node, List<Node<String>> searchSpace) {
        return getUpperNeighbors(node, searchSpace)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Node %s should have an inner segment to node layer %s", node, searchSpace)));
    }

    private Stream<Relationship<String>> innerSegmentsStream() {
        return innerSegments
                .values()
                .stream()
                .flatMap(List::stream);
    }

    private Relationship<String> findSegment(Node<String> node1, Node<String> node2) {
        return innerSegmentsStream()
                .filter(rel -> rel.source().equals(node1) && rel.target().equals(node2) || rel.source().equals(node2) && rel.target().equals(node1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No segment found for nodes %s and %s", node1, node2)));
    }

    private int search(Node<String> element, List<Node<String>> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(element)) {
                return i;
            }
        }
        throw new IllegalStateException(String.format("Node %s was not found in list %s", element, list));
    }
}
