package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrandesAndKoepf implements Algorithm<Map<Node<String>, Float>> {

    public static final int DELTA = 1;

    public enum VerticalDirection {
        TOP_DOWN,
        BOTTOM_UP
    }

    public enum HorizontalDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    private final Graph<String> properGraph;
    private final Map<Node<String>, Map<Node<String>, List<Relationship<String>>>> innerSegments;
    private final Map<Node<String>, Float> layerAssignment;

    private final Map<Integer, List<Node<String>>> orderedNodes;

    public BrandesAndKoepf(
            Graph<String> properGraph,
            Map<Node<String>, CrossingReduction.Block> nodeBlockMapping,
            Map<Node<String>, Map<Node<String>, List<Relationship<String>>>> innerSegments,
            Map<Node<String>, Float> layerAssignment
    ) {
        this.properGraph = properGraph;
        this.innerSegments = innerSegments;
        this.layerAssignment = layerAssignment;

        orderedNodes = new TreeMap<>(Comparator.comparingInt(integer -> -integer.intValue()));
        layerAssignment.forEach((node, layer) -> {
            orderedNodes.putIfAbsent(layer.intValue(), new ArrayList<>());
            orderedNodes.get(layer.intValue()).add(node);
        });

        orderedNodes.forEach((layer, nodes) -> nodes.sort(Comparator.comparingInt(node -> nodeBlockMapping.get(node).position)));
    }

    @Override
    public Map<Node<String>, Float> compute() {
        Set<Relationship<String>> markedSegments = markType1Conflicts();

        List<Map<Node<String>, Integer>> directionalCoordinates = new ArrayList<>(4);
        for (HorizontalDirection horizontalDirection : HorizontalDirection.values()) {
            for (VerticalDirection verticalDirection : VerticalDirection.values()) {
                var rootAndAlign = verticalAlignment(markedSegments, verticalDirection, horizontalDirection);
                var root = rootAndAlign.getLeft();
                var align = rootAndAlign.getRight();
                directionalCoordinates.add(horizontalCompaction(root, align));
            }
        }

        return directionalCoordinates.get(0)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().floatValue()));
    }

    private Set<Relationship<String>> markType1Conflicts() {
        Set<Relationship<String>> markedSegments = new HashSet<>();
        List<Integer> layers = List.copyOf(orderedNodes.keySet());
        for (int i = 2; i < layers.size() - 1; i++) {
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
                        for (Node<String> node : getNeighbors(nextLayerNodes.get(l), currentLayerNodes)) {
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

    private Pair<Map<Node<String>, Node<String>>, Map<Node<String>, Node<String>>> verticalAlignment(
            Set<Relationship<String>> markedSegments,
            VerticalDirection verticalDirection,
            HorizontalDirection horizontalDirection
    ) {
        Map<Node<String>, Node<String>> root = new IdentityHashMap<>();
        Map<Node<String>, Node<String>> align = new IdentityHashMap<>();

        for (var node : properGraph.nodes()) {
            root.put(node, node);
            align.put(node, node);
        }

        List<Integer> layers = List.copyOf(orderedNodes.keySet());
        for (int i = 1; i < layers.size(); i++) {
            int r = 0;
            List<Node<String>> currentLayer = orderedNodes.get(layers.get(i));
            for (int idInLayer = 1; idInLayer < currentLayer.size(); idInLayer++) {
                Node<String> currentNode = currentLayer.get(horizontalDirection == HorizontalDirection.LEFT_TO_RIGHT ? currentLayer.size() - idInLayer - 1 : idInLayer);
                List<Node<String>> neighbors = getNeighbors(currentNode, orderedNodes.get(layers.get(verticalDirection == VerticalDirection.BOTTOM_UP ? layers.size() - i - 1 : i - 1)));
                if (!neighbors.isEmpty()) {
                    int d = neighbors.size();

                    int medianLeft = (int) Math.floor(((double) d + 1.0D) / 2.0D);
                    int medianRight = (int) Math.ceil(((double) d + 1.0D) / 2.0D);

                    for (int m = medianLeft - 1; m < medianRight; m++) {
                        if (align.get(currentNode).equals(currentNode)) {
                            Node<String> medianUpperNeighbor = neighbors.get(m);
                            boolean isMarkedSegment = markedSegments.contains(findSegment(medianUpperNeighbor, currentNode));
                            if (!isMarkedSegment && r < m) {
                                align.put(medianUpperNeighbor, currentNode);
                                root.put(currentNode, root.get(medianUpperNeighbor));
                                align.put(currentNode, root.get(currentNode));
                                r = m;
                            }
                        }
                    }
                }
            }
        }

        return Pair.of(root, align);
    }

    private Map<Node<String>, Integer> horizontalCompaction(Map<Node<String>, Node<String>> root, Map<Node<String>, Node<String>> align) {
        Map<Node<String>, Node<String>> sink = new IdentityHashMap<>();
        Map<Node<String>, Integer> shift = new IdentityHashMap<>();
        Map<Node<String>, Integer> x = new IdentityHashMap<>();

        for (var node : properGraph.nodes()) {
            sink.put(node, node);
            shift.put(node, Integer.MAX_VALUE);
            x.put(node, null);
        }

        for (var node : properGraph.nodes()) {
            if (root.get(node).equals(node)) {
                placeBlock(node, sink, shift, x, root, align);
            }
        }

        for (var node : properGraph.nodes()) {
            x.put(node, x.get(root.get(node)));
            if (shift.get(sink.get(root.get(node))) < Integer.MAX_VALUE) {
                x.put(node, x.get(node) + shift.get(sink.get(root.get(node))));
            }
        }

        return x;
    }

    private void placeBlock(
            Node<String> node,
            Map<Node<String>, Node<String>> sink,
            Map<Node<String>, Integer> shift,
            Map<Node<String>, Integer> x,
            Map<Node<String>, Node<String>> root,
            Map<Node<String>, Node<String>> align
    ) {
        if (x.get(node) == null) {
            x.put(node, 0);
            var w = node;
            do {
                List<Node<String>> currentLayer = orderedNodes.get(layerAssignment.get(w).intValue());
                int positionInLayer = search(w, currentLayer);
                if (positionInLayer > 1) {
                    var u = root.get(currentLayer.get(positionInLayer - 1));
                    placeBlock(u, sink, shift, x, root, align);

                    if (sink.get(node).equals(node)) {
                        sink.put(node, sink.get(u));
                    }

                    if (sink.get(node) != sink.get(u)) {
                        shift.put(sink.get(u), Math.min(shift.get(sink.get(u)), x.get(node) - x.get(u) - DELTA));
                    } else {
                        x.put(node, Math.max(x.get(node), x.get(u) + 1));
                    }
                }
                w = align.get(w);
            } while(!w.equals(node));
        }
    }

    private boolean incidentToInnerSegments(Node<String> node, int lowerLayer, int upperLayer) {
        return innerSegmentsStream()
                .filter(rel ->
                        layerAssignment.get(rel.source()) == lowerLayer && layerAssignment.get(rel.target()) == upperLayer
                                || layerAssignment.get(rel.source()) == upperLayer && layerAssignment.get(rel.target()) == lowerLayer
                ).anyMatch(rel -> rel.source().equals(node) || rel.target().equals(node));
    }

    private List<Node<String>> getNeighbors(Node<String> node, List<Node<String>> searchSpace) {
        if (searchSpace == null) {
            return List.of();
        }
        return innerSegmentsStream()
                .filter(rel -> (searchSpace.contains(rel.source()) && rel.target().equals(node))
                        || (searchSpace.contains(rel.target()) && rel.source().equals(node)))
                .map(rel -> rel.source().equals(node) ? rel.target() : rel.source())
                .collect(Collectors.toList());
    }

    private Node<String> getUpperNeighbor(Node<String> node, List<Node<String>> searchSpace) {
        return getNeighbors(node, searchSpace)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Node %s should have an inner segment to node layer %s", node, searchSpace)));
    }

    private Stream<Relationship<String>> innerSegmentsStream() {
        return innerSegments
                .values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
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
