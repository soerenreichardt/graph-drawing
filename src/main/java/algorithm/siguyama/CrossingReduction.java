package algorithm.siguyama;

import algorithm.Algorithm;
import datastructure.CursorBasedArray;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CrossingReduction implements Algorithm<Map<Node<String>, Float>> {

    public static final int IGNORE = -1;
    public static final int ITERATIONS = 10;
    private Graph<String> graph;
    private Graph<String> properGraph;
    private final Map<Node<String>, Float> layerAssignment;

    private Map<Node<String>, List<Relationship<String>>> dummies;

    Map<Node<String>, Block> nodeBlockMapping;

    List<Block> finalBlocks;

    public CrossingReduction(Graph<String> graph, Map<Node<String>, Float> layerAssignment) {
        this.graph = graph;
        this.layerAssignment = new HashMap<>(layerAssignment);

        this.dummies = new HashMap<>();
        this.nodeBlockMapping = new HashMap<>();
    }

    @Override
    public Map<Node<String>, Float> compute() {
        Map<Node<String>, Float> result = new HashMap<>();

        Pair<List<Node<String>>, List<Relationship<String>>> dummyEntities = computeDummies();
        this.properGraph = createProperGraph(dummyEntities.getLeft(), dummyEntities.getRight());
        LinkedList<Block> blocks = createBlocks();

        for (int i = 0; i < ITERATIONS; i++) {
            for (int j = 0; j < blocks.size(); j++) {
                Block block = blocks.get(j);
                siftingStep(blocks, block);
            }
        }

        graph.forEachNode(node -> {
            result.put(node, (float) nodeBlockMapping.get(node).position);
        });

        this.finalBlocks = blocks;
        return result;
    }

    public Map<Node<String>, List<Relationship<String>>> dummies() {
        return this.dummies;
    }

    public Graph<String> properGraph() {
        return this.properGraph;
    }

    public List<Block> blocks() {
        return this.finalBlocks;
    }

    public Map<Node<String>, Float> layerAssigment() {
        return this.layerAssignment;
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
            block.clearArrays();
        }

        Map<Relationship<String>, Integer> positionCache = new HashMap<>();
        for (Block block : blocks) {
            Node<String> lower = block.lower;
            Node<String> upper = block.upper;

            properGraph.relationships().forEach(rel -> {
                Node<String> target = rel.target();
                if (target == upper) {
                    Block sourceBlock = nodeBlockMapping.get(rel.source());

                    int currentPosition;
                    if (sourceBlock.outgoingAdjacency.contains(target)) {
                        currentPosition = sourceBlock.outgoingAdjacency.find(target);
                    } else {
                        currentPosition = sourceBlock.outgoingAdjacency.add(target);
                    }

                    if (block.position < sourceBlock.position) {
                        positionCache.put(rel, currentPosition);
                    } else {
                        Integer position = positionCache.get(rel);
                        if (position == null) return;
                        sourceBlock.outgoingIndex.add(currentPosition, position);
                        block.incomingIndex.add(position, currentPosition);
                    }
                }
            });

            properGraph.relationships().forEach(rel -> {
                Node<String> source = rel.source();
                if (source == lower) {
                    Block targetBlock = nodeBlockMapping.get(rel.target());

                    int currentPosition;
                    if (targetBlock.incomingAdjacency.contains(source)) {
                        currentPosition = targetBlock.incomingAdjacency.find(source);
                    } else {
                        currentPosition = targetBlock.incomingAdjacency.add(source);
                    }

                    if (block.position < targetBlock.position) {
                        positionCache.put(rel, currentPosition);
                    } else {
                        Integer position = positionCache.get(rel);
                        if (position == null) return;
                        targetBlock.incomingIndex.add(currentPosition, position);
                        block.outgoingIndex.add(position, currentPosition);
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
            var nodeA = blockA.nodes.stream().filter(node -> layerAssignment.get(node).equals(level)).findFirst();
            var nodeB = blockB.nodes.stream().filter(node -> layerAssignment.get(node).equals(level)).findFirst();
            if (nodeA.isEmpty() || nodeB.isEmpty()) {
                continue;
            }
            delta += uswap(nodeA.get(), nodeB.get(), direction);
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

    private Pair<List<Node<String>>, List<Relationship<String>>> computeDummies() {
        List<Node<String>> nodes = new ArrayList<>();
        List<Relationship<String>> relationships = new ArrayList<>();
        String nameTemplate = "(%s->%s): %s";
        MutableInt dummyNodeId = new MutableInt(-1);
        graph.forEachRelationship((source, target) -> {
            int span = span(source, target);
            if (span > 1) {
                List<Relationship<String>> dummyRels = new ArrayList<>(2);
                var dummyNode = new Node<>(dummyNodeId.getAndDecrement(), String.format(nameTemplate, source, target, 0));
                var sourceDummyRel = new Relationship<>(source, dummyNode);

                nodes.add(dummyNode);
                relationships.add(sourceDummyRel);
                dummyRels.add(sourceDummyRel);
                layerAssignment.put(dummyNode, layerAssignment.get(source) + 1.0f);
                for (int i = 1; i < span - 1; i++) {
                    var tempDummy = new Node<>(dummyNodeId.getAndDecrement(), String.format(nameTemplate, source, target, i));
                    Relationship<String> dummyRelationship = new Relationship<>(dummyNode, tempDummy);
                    dummyRels.add(dummyRelationship);
                    nodes.add(tempDummy);
                    relationships.add(dummyRelationship);
                    dummyNode = tempDummy;
                    layerAssignment.put(dummyNode, layerAssignment.get(source) + i + 1.0f);
                }
                var dummyTargetRel = new Relationship<>(dummyNode, target);
                relationships.add(dummyTargetRel);
                dummyRels.add(dummyTargetRel);

                this.dummies.put(source, dummyRels);
            }
        });

        return Pair.of(nodes, relationships);
    }

    private Graph<String> createProperGraph(List<Node<String>> dummyNodes, List<Relationship<String>> dummyRelationships) {
        List<Node<String>> allNodes = graph.nodes();
        allNodes.addAll(dummyNodes);

        List<Relationship<String>> allRelationships = graph
                .relationships()
                .stream()
                .filter(rel -> span(rel.source(), rel.target()) == 1)
                .collect(Collectors.toList());
        allRelationships.addAll(dummyRelationships);

        Graph<String> properGraph = new Graph<>();
        allNodes.forEach(properGraph::addNode);
        allRelationships.forEach(properGraph::addRelationship);
        return properGraph;
    }

    private LinkedList<Block> createBlocks() {
        LinkedList<Block> blocks = new LinkedList<>();
        AtomicInteger blockPosition = new AtomicInteger(0);
        graph.forEachNode(node -> {
            blocks.add(new Block(node, blockPosition.getAndIncrement()));
            if (dummies.containsKey(node)) {
                List<Relationship<String>> dummyRelationships = dummies.get(node);
                List<Node<String>> innerNodes = new ArrayList<>();
                for (int i = 0; i < dummyRelationships.size() - 1; i++) {
                    innerNodes.add(dummyRelationships.get(i).target());
                }
                Block block = new Block(innerNodes, node, dummyRelationships.get(dummyRelationships.size() - 1).target(), blockPosition.getAndIncrement());
                blocks.add(block);
            }
        });

        blocks.sort(Comparator.comparingInt(block -> block.levels().size()));
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
        properGraph.forEachRelationship((source, target) -> {
            if (target == node) {
                incomingNeighbors.add(source);
            }
        });

        return incomingNeighbors;
    }

    private Set<Node<String>> outgoingSegmentNeighbors(Node<String> node) {
        Set<Node<String>> outgoingNeighbors = new HashSet<>();
        graph.forEachRelationship(node, (source, target) -> outgoingNeighbors.add(target));

        return outgoingNeighbors;
    }

    public static final Node<String> EMPTY_NODE = new Node<>(-1, null);

    public final class Block {

        int position;

        Node<String> lower;
        Node<String> upper;
        List<Node<String>> nodes;

        CursorBasedArray<Node<String>> outgoingAdjacency;
        CursorBasedArray<Node<String>> incomingAdjacency;
        CursorBasedArray<Integer> outgoingIndex;
        CursorBasedArray<Integer> incomingIndex;

        public Block(Node<String> node, int position) {
            this(List.of(node), node, node, position);
        }

        public Block(List<Node<String>> nodes, Node<String> lower, Node<String> upper, int position) {
            this.nodes = nodes;
            this.lower = lower;
            this.upper = upper;
            this.position = position;

            initializeArrays();
            setAdjacencies();
            nodes.forEach(node -> nodeBlockMapping.put(node, this));
        }

        public int position() {
            return this.position;
        }

        public void clearArrays() {
            outgoingAdjacency.clear();
            incomingAdjacency.clear();
            outgoingIndex.clear();
            incomingIndex.clear();
        }

        public Set<Integer> levels() {
            Set<Integer> levels = new HashSet<>();
            levels.add(layerAssignment.get(lower).intValue());
            levels.add(layerAssignment.get(upper).intValue());
            nodes
                    .stream()
                    .map(layerAssignment::get)
                    .mapToInt(Float::intValue)
                    .forEach(levels::add);

            return levels;
        }

        private void initializeArrays() {
            int inDegree = properGraph.inDegree(upper);
            int outDegree = properGraph.degree(lower);

            // sad java 😞
            Class<Node<String>> nodeClass = (Class<Node<String>>) new Node<String>(IGNORE, "").getClass();
            outgoingAdjacency = new CursorBasedArray<>(nodeClass, EMPTY_NODE, outDegree);
            incomingAdjacency = new CursorBasedArray<>(nodeClass, EMPTY_NODE,inDegree);
            outgoingIndex = new CursorBasedArray<>(Integer.class, -1, outDegree);
            incomingIndex = new CursorBasedArray<>(Integer.class, -1, inDegree);
        }

        private void setAdjacencies() {
            properGraph.forEachRelationship((source, target) -> {
                if (target == upper) {
                    incomingAdjacency.add(source);
                } else if (source == lower) {
                    outgoingAdjacency.add(target);
                }
            });
        }
    }
}
