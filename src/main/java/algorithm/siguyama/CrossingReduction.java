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

public class CrossingReduction implements Algorithm<CrossingReduction> {

    public static final int IGNORE = Integer.MIN_VALUE;
    public static final int ITERATIONS = 1;
    public static final String DUMMY_PREFIX = "dummy_";

    private Graph<String> graph;
    private Graph<String> properGraph;

    private final Map<Node<String>, Float> layerAssignment;

    private Map<Node<String>, Map<Node<String>, List<Relationship<String>>>> dummies;

    Map<Node<String>, Block> nodeBlockMapping;

    List<Block> finalBlocks;

    public CrossingReduction(Graph<String> graph, Map<Node<String>, Float> layerAssignment) {
        this.graph = graph;
        this.layerAssignment = new HashMap<>(layerAssignment);

        this.dummies = new HashMap<>();
        this.nodeBlockMapping = new HashMap<>();
    }

    @Override
    public CrossingReduction compute() {
        Map<Node<String>, Float> result = new HashMap<>();

        Pair<List<Node<String>>, List<Relationship<String>>> dummyEntities = computeDummies();
        this.properGraph = createProperGraph(dummyEntities.getLeft(), dummyEntities.getRight());
        LinkedList<Block> blocks = createBlocks();

        for (int i = 0; i < ITERATIONS; i++) {
            for (int j = 0; j < blocks.size(); j++) {
                System.out.println(j + " / " + blocks.size());
                Block block = blocks.get(j);
                siftingStep(blocks, block);
            }
        }

        graph.forEachNode(node -> {
            result.put(node, (float) nodeBlockMapping.get(node).position);
        });

        this.finalBlocks = blocks;
        return this;
    }

    public Map<Node<String>, Map<Node<String>, List<Relationship<String>>>> dummies() {
        return this.dummies;
    }

    public Graph<String> properGraph() {
        return this.properGraph;
    }

    public Map<Node<String>, Block> nodeBlockMapping() {
        return nodeBlockMapping;
    }

    public Map<Node<String>, Float> layerAssignment() {
        return layerAssignment;
    }

    private void siftingStep(LinkedList<Block> blocks, Block block) {
        System.out.println("Start :: siftingStep");
        blocks.remove(block);
        blocks.addFirst(block);

        sortAdjacencies(blocks);

        int currentCrossings = 0;
        int bestCrossings = 0;
        int bestBlockPosition = 0;

        for (int i = 1; i < blocks.size(); i++) {
            currentCrossings += siftingSwap(block, blocks.get(i), blocks);
            if (currentCrossings < bestCrossings) {
                bestCrossings = currentCrossings;
                bestBlockPosition = i;
            }
        }

        blocks.remove(block);
        blocks.add(bestBlockPosition, block);
        System.out.println("Finish :: siftingStep");
    }

    private void sortAdjacencies(List<Block> blocks) {
        System.out.println("Start :: sortAdjacencies");
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

                    if (sourceBlock.outgoingAdjacency.contains(target)) return;
                    int currentPosition = sourceBlock.outgoingAdjacency.add(target);

                    if (block.position < sourceBlock.position) {
                        positionCache.put(rel, currentPosition);
                    } else {
                        Integer position = positionCache.get(rel);
                        sourceBlock.outgoingIndex.add(currentPosition, position);
                        block.incomingIndex.add(position, currentPosition);
                    }
                }
            });

            properGraph.relationships().forEach(rel -> {
                Node<String> source = rel.source();
                if (source == lower) {
                    Block targetBlock = nodeBlockMapping.get(rel.target());

                    if (targetBlock.incomingAdjacency.contains(source)) return;
                    int currentPosition = targetBlock.incomingAdjacency.add(source);

                    if (block.position < targetBlock.position) {
                        positionCache.put(rel, currentPosition);
                    } else {
                        Integer position = positionCache.get(rel);
                        targetBlock.incomingIndex.add(currentPosition, position);
                        block.outgoingIndex.add(position, currentPosition);
                    }
                }
            });
        }
        System.out.println("Finish :: sortAdjacencies");
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

            var adjacencyA = direction == Direction.OUTGOING ? blockA.outgoingAdjacency : blockA.incomingAdjacency;
            var adjacencyB = direction == Direction.OUTGOING ? blockB.outgoingAdjacency : blockB.incomingAdjacency;
            var indicesA = direction == Direction.OUTGOING ? blockA.outgoingIndex : blockA.incomingIndex;
            var indicesB = direction == Direction.OUTGOING ? blockB.outgoingIndex : blockB.incomingIndex;

            updateAdjacency(adjacencyA, indicesA, adjacencyB, indicesB, direction);
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

        List<Node<String>> neighborsA = getNeighbors(nodeA, direction);
        List<Node<String>> neighborsB = getNeighbors(nodeB, direction);

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

    private void updateAdjacency(
            CursorBasedArray<Node<String>> adjacencyA,
            CursorBasedArray<Integer> indicesA,
            CursorBasedArray<Node<String>> adjacencyB,
            CursorBasedArray<Integer> indicesB,
            Direction direction
    ) {
        int i = 0;
        int j = 0;
        while (i < adjacencyA.cursorPosition() && j < adjacencyB.cursorPosition()) {
            int adjacencyANodePosition = nodeBlockMapping.get(adjacencyA.get(i)).position();
            int adjacencyBNodePosition = nodeBlockMapping.get(adjacencyB.get(j)).position();
            if (adjacencyANodePosition < adjacencyBNodePosition) {
                i++;
            } else if(adjacencyANodePosition > adjacencyBNodePosition) {
                j++;
            } else {
                var z = adjacencyA.get(i);
                Integer nodeAIndex = indicesA.get(i);
                Integer nodeBIndex = indicesB.get(j);
                var zBlockAdjacency = direction == Direction.OUTGOING ? nodeBlockMapping.get(z).incomingAdjacency : nodeBlockMapping.get(z).outgoingAdjacency;
                var zBlockIndices = direction == Direction.OUTGOING ? nodeBlockMapping.get(z).incomingIndex : nodeBlockMapping.get(z).outgoingIndex;
                Node<String> tempNode = zBlockAdjacency.get(nodeAIndex);
                zBlockAdjacency.add(nodeAIndex, zBlockAdjacency.get(nodeBIndex));
                zBlockAdjacency.add(nodeBIndex, tempNode);

                Integer tempIndex = zBlockIndices.get(nodeAIndex);
                zBlockIndices.add(nodeAIndex, zBlockIndices.get(nodeBIndex));
                zBlockIndices.add(nodeBIndex, tempIndex);

                indicesA.add(i, indicesA.get(i) + 1);
                indicesB.add(j, indicesB.get(j) - 1);
                i++;
                j++;
            }
        }
    }

    private Pair<List<Node<String>>, List<Relationship<String>>> computeDummies() {
        List<Node<String>> nodes = new ArrayList<>();
        List<Relationship<String>> relationships = new ArrayList<>();
        String nameTemplate = DUMMY_PREFIX + "(%s->%s): %s";
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

                this.dummies.putIfAbsent(source, new HashMap<>());
                this.dummies.get(source).putIfAbsent(target, new ArrayList<>());
                this.dummies.get(source).get(target).addAll(dummyRels);
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
                for (List<Relationship<String>> dummyRelationships : dummies.get(node).values()) {
                    List<Node<String>> innerNodes = new ArrayList<>();
                    for (int i = 0; i < dummyRelationships.size() - 1; i++) {
                        innerNodes.add(dummyRelationships.get(i).target());
                    }
                    Block block = new Block(innerNodes, dummyRelationships.get(dummyRelationships.size() - 1).source(), dummyRelationships.get(0).target(), blockPosition.getAndIncrement());
                    blocks.add(block);
                }
            }
        });

        blocks.sort(Comparator.comparingInt(block -> block.levels().size()));
        return blocks;
    }

    private int span(Node<String> source, Node<String> target) {
        // TODO might be Math.abs(...)
        return (int) Math.abs(layerAssignment.get(target) - layerAssignment.get(source));
    }

    private List<Node<String>> getNeighbors(Node<String> node, Direction direction) {
        return getNeighbors(nodeBlockMapping.get(node), direction);
    }

    private List<Node<String>> getNeighbors(Block block, Direction direction) {
        var adjacency = direction == Direction.OUTGOING ? block.outgoingAdjacency : block.incomingAdjacency;
        return adjacency.asList().stream().filter(n -> !n.equals(adjacency.emptyValue())).collect(Collectors.toList());
    }

    public static final Node<String> EMPTY_NODE = new Node<>(IGNORE, null);

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
