package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrossingReduction implements Algorithm<Map<Node<String>, Float>> {

    public static final int IGNORE = -1;
    public static final int ITERATIONS = 10;
    private Graph<String> graph;
    private Map<Node<String>, Float> layerAssignment;

    private Map<Node<String>, List<Relationship<String>>> dummies;

    private Map<Block, List<Node<String>>> outgoingAdjacency;
    private Map<Block, List<Node<String>>> incomingAdjacency;
    private Map<Block, List<Integer>> outgoingIndex;
    private Map<Block, List<Integer>> incomingIndex;

    Map<Node<String>, Block> nodeBlockMapping;

    public CrossingReduction(Graph<String> graph, Map<Node<String>, Float> layerAssignment) {
        this.graph = graph;
        this.layerAssignment = layerAssignment;

        this.dummies = new HashMap<>();

        this.outgoingAdjacency = new HashMap<>();
        this.incomingAdjacency = new HashMap<>();
        this.outgoingIndex = new HashMap<>();
        this.incomingIndex = new HashMap<>();

        this.nodeBlockMapping = new HashMap<>();
    }

    @Override
    public Map<Node<String>, Float> compute() {
        computeDummies();
        LinkedList<Block> blocks = createBlocks();

        for (int i = 0; i < ITERATIONS; i++) {
            for (Block block : blocks) {
                siftingStep(blocks, block);
            }
        }

        return null;
    }

    private void siftingStep(LinkedList<Block> blocks, Block block) {
        blocks.remove(block);
        blocks.addFirst(block);

        sortAdjacencies(blocks);
    }

    private void sortAdjacencies(List<Block> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            block.position = i;
            outgoingAdjacency.get(block).clear();
            incomingAdjacency.get(block).clear();
            outgoingIndex.get(block).clear();
            incomingIndex.get(block).clear();
        }

        for (Block block : blocks) {
            Node<String> upper = block.upper;
            Node<String> lower = block.lower;
            graph.forEachRelationship((source, target) -> {
                if (target == upper) {
                    Block sourceBlock = nodeBlockMapping.get(source);
                    this.outgoingAdjacency.putIfAbsent(sourceBlock, new ArrayList<>());
                    List<Node<String>> adjacencyList = this.outgoingAdjacency.get(sourceBlock);
                    int sourcePosition = adjacencyList.size();
                    adjacencyList.add(upper);

                    int targetPosition = this.incomingAdjacency.get(block).size();
                    if (!(block.position < sourceBlock.position)) {
                        outgoingIndex.get(sourceBlock).set(sourcePosition, targetPosition);
                        incomingIndex.get(block).set(targetPosition, sourcePosition);
                    }
                }
            });

            graph.forEachRelationship((source, target) -> {
                if (source == lower) {
                    Block targetBlock = nodeBlockMapping.get(target);
                    this.incomingIndex.putIfAbsent(targetBlock, new ArrayList<>());
                    List<Node<String>> adjacencyList = this.incomingAdjacency.get(targetBlock);
                    int targetPosition = adjacencyList.size();
                    adjacencyList.add(lower);

                    int sourcePosition = this.outgoingAdjacency.get(block).size();
                    if(!(block.position < targetBlock.position)) {
                        incomingIndex.get(targetBlock).set(targetPosition, sourcePosition);
                        outgoingIndex.get(block).set(sourcePosition, targetPosition);
                    }
                }
            });
        }
    }

    private void computeDummies() {
        graph.forEachRelationship((source, target) -> {
            int span = span(source, target);
            if (span > 1) {
                List<Relationship<String>> dummyRels = new ArrayList<>(2);
                var dummyNode = new Node<>(IGNORE, "dummy0");
                var sourceDummyRel = new Relationship<>(source, dummyNode);

                dummyRels.add(sourceDummyRel);
                for (int i = 1; i < span - 1; i++) {
                    var tempDummy = new Node<>(IGNORE, "dummy" + i);
                    dummyRels.add(new Relationship<>(dummyNode, tempDummy));
                    dummyNode = tempDummy;
                }
                var dummyTargetRel = new Relationship<>(dummyNode, target);
                dummyRels.add(dummyTargetRel);

                this.dummies.put(source, dummyRels);
            }
        });
    }

    private LinkedList<Block> createBlocks() {
        LinkedList<Block> blocks = new LinkedList<>();
        AtomicInteger blockPosition = new AtomicInteger(0);
        graph.forEachNode(node -> {
            blocks.add(new Block(node, blockPosition.getAndIncrement()));
            if (dummies.containsKey(node)) {
                List<Relationship<String>> dummyRelationships = dummies.get(node);
                Block block = new Block(node, dummyRelationships.get(dummyRelationships.size()).target(), blockPosition.getAndIncrement());
                for (int i = 0; i < dummyRelationships.size() - 1; i++) {
                    block.addNode(dummyRelationships.get(i).target());
                }
                blocks.add(block);
            }
        });

        return blocks;
    }

    private int span(Node<String> source, Node<String> target) {
        // TODO might be Math.abs(...)
        return (int) (layerAssignment.get(target) - layerAssignment.get(source));
    }

    private Set<Node<String>> incomingSegmentNeighbors(Node<String> node) {
        Set<Node<String>> incomingNeighbors = new HashSet<>();
        graph.forEachRelationship((source, target) -> {
            if (target == node && span(source, target) == 1) {
                incomingNeighbors.add(source);
            }
        });

        dummies.values().stream().flatMap(List::stream).forEach(relationship -> {
            if (relationship.target() == node) {
                incomingNeighbors.add(relationship.source());
            }
        });

        return incomingNeighbors;
    }

    private Set<Node<String>> outgoingSegmentNeighbors(Node<String> node) {
        Set<Node<String>> outgoingNeighbors = new HashSet<>();
        graph.outgoingNeighborsForNode(node).forEach(target -> {
            if (span(node, target) == 1) {
                outgoingNeighbors.add(target);
            }
        });

        dummies.get(node).forEach(relationship -> {
            if (relationship.source() == node) {
                outgoingNeighbors.add(relationship.target());
            }
        });

        return outgoingNeighbors;
    }

    public final class Block {

        int position;

        Node<String> upper;
        Node<String> lower;
        List<Node<String>> nodes;

        public Block(Node<String> node, int position) {
            this(List.of(node), node, node, position);
        }

        public Block(Node<String> upper, Node<String> lower, int position) {
            this(new ArrayList<>(), upper, lower, position);
        }

        public Block(List<Node<String>> nodes, Node<String> upper, Node<String> lower, int position) {
            this.nodes = nodes;
            this.upper = upper;
            this.lower = lower;
            this.position = position;

            nodes.forEach(node -> nodeBlockMapping.put(node, this));
        }

        public void addNode(Node<String> node) {
            this.nodes.add(node);
        }

    }
}
