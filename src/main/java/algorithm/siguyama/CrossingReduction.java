package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        Map<Node<String>, Float> result = new HashMap<>();

        computeDummies();
        LinkedList<Block> blocks = createBlocks();

        for (int i = 0; i < ITERATIONS; i++) {
            for (int j = 0; j < blocks.size(); j++) {
                Block block = blocks.get(j);
                siftingStep(blocks, block);
            }
        }

        List<Node<String>> allNodes = graph.nodes();
        allNodes.addAll(dummies.keySet());

        allNodes.forEach(node -> {
            result.put(node, (float) nodeBlockMapping.get(node).position);
        });

        return result;
    }

    private void siftingStep(LinkedList<Block> blocks, Block block) {
        blocks.remove(block);
        blocks.addFirst(block);

        sortAdjacencies(blocks);

        int currentCrossings = 0;
        int bestCrossings = 0;
        int bestBlockPosition = 0;

        for (int i = 1; i < blocks.size() - 1; i++) {
            currentCrossings += siftingSwap(block, blocks.get(i), blocks);
            if (currentCrossings < bestCrossings) {
                bestCrossings = currentCrossings;
                bestBlockPosition = i;
            }
        }

        blocks.remove(block);
        blocks.add(bestBlockPosition, block);
    }

    private void sortAdjacencies(List<Block> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            block.position = i;
            outgoingAdjacency.getOrDefault(block, new ArrayList<>()).clear();
            incomingAdjacency.getOrDefault(block, new ArrayList<>()).clear();
            outgoingIndex.getOrDefault(block, new ArrayList<>()).clear();
            incomingIndex.getOrDefault(block, new ArrayList<>()).clear();
        }

        for (Block block : blocks) {
            Node<String> upper = block.upper;
            Node<String> lower = block.lower;
            graph.forEachRelationship((source, target) -> {
                if (target == upper) {
                    Block sourceBlock = nodeBlockMapping.get(source);
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

    enum Direction {
        OUTGOING,
        INCOMING
    }

    private int siftingSwap(Block blockA, Block blockB, LinkedList<Block> blocksRef) {
        int delta = 0;
        Set<Pair<Float, Direction>> levelAndDirections = new HashSet<>();
        Set<Float> levelsA = blockA.nodes.stream().map(layerAssignment::get).collect(Collectors.toSet());
        Set<Float> levelsB = blockB.nodes.stream().map(layerAssignment::get).collect(Collectors.toSet());
        if (levelsB.contains(layerAssignment.get(blockA.upper))) {
            levelAndDirections.add(Pair.of(layerAssignment.get(blockA.upper), Direction.INCOMING));
        }
        if (levelsB.contains(layerAssignment.get(blockA.lower))) {
            levelAndDirections.add(Pair.of(layerAssignment.get(blockA.lower), Direction.OUTGOING));
        }
        if (levelsA.contains(layerAssignment.get(blockB.upper))) {
            levelAndDirections.add(Pair.of(layerAssignment.get(blockB.upper), Direction.INCOMING));
        }
        if (levelsA.contains(layerAssignment.get(blockB.lower))) {
            levelAndDirections.add(Pair.of(layerAssignment.get(blockB.lower), Direction.OUTGOING));
        }

        for (Pair<Float, Direction> levelAndDirection : levelAndDirections) {
            Float level = levelAndDirection.getLeft();
            Direction direction = levelAndDirection.getRight();
            Node<String> nodeA = blockA.nodes.stream().filter(node -> layerAssignment.get(node).equals(level)).findFirst().orElseThrow();
            Node<String> nodeB = blockB.nodes.stream().filter(node -> layerAssignment.get(node).equals(level)).findFirst().orElseThrow();
            delta += uswap(nodeA, nodeB, direction);
        }

        int blockAIndex = blocksRef.indexOf(blockA);
        int blockBIndex = blocksRef.indexOf(blockB);

        blocksRef.set(blockAIndex, blockB);
        blocksRef.set(blockBIndex, blockA);

        blockA.position++;
        blockB.position--;

        return delta;
    }

    private int uswap(Node<String> nodeA, Node<String> nodeB, Direction direction) {
        int c = 0;
        int i = 0;
        int j = 0;

        List<Node<String>> neighborsA = orderedSegmentNeighbors(nodeA, direction);
        List<Node<String>> neighborsB = orderedSegmentNeighbors(nodeB, direction);

        while (i < neighborsA.size() && j < neighborsB.size()) {
            int blockAPosition = nodeBlockMapping.get(neighborsA.get(i)).position;
            int blockBPosition = nodeBlockMapping.get(neighborsB.get(j)).position;
            if (blockAPosition < blockBPosition) {
                c += (neighborsB.size() - j);
                i++;
            } else if(blockAPosition > blockBPosition) {
                c -= (neighborsA.size() - i);
                j++;
            } else {
                c += (neighborsB.size() - j) - (neighborsA.size() - i);
                i++;
                j++;
            }
        }
        return c;
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
                Block block = new Block(node, dummyRelationships.get(dummyRelationships.size() - 1).target(), blockPosition.getAndIncrement());
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

    private List<Node<String>> orderedSegmentNeighbors(Node<String> node, Direction direction) {
        return List.copyOf(segmentNeighbors(node, direction))
                .stream()
                .map(n -> Pair.of(n, nodeBlockMapping.get(n).position))
                .sorted(Comparator.comparing(Pair::getRight))
                .map(Pair::getLeft)
                .collect(Collectors.toList());
    }

    private Set<Node<String>> segmentNeighbors(Node<String> node, Direction direction) {
        switch (direction) {
            case INCOMING:
                return incomingSegmentNeighbors(node);
            case OUTGOING:
                return outgoingSegmentNeighbors(node);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
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

        public int position() {
            return this.position;
        }

    }
}
