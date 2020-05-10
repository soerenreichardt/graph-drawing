package graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Graph<DATA> {

    AtomicLong nodeCount;
    AtomicLong relationshipCount;

    Map<Long, Node<DATA>> nodes;
    Map<Long, List<Relationship<DATA>>> relationships;

    Map<Long, Integer> inDegree;

    public Graph() {
        this.nodeCount = new AtomicLong(0L);
        this.relationshipCount = new AtomicLong(0L);

        this.nodes = new HashMap<>();
        this.relationships = new HashMap<>();

        this.inDegree = new HashMap<>();
    }

    public Node<DATA> addNode(DATA data) {
        long nodeId = nodeCount.getAndIncrement();
        return addNode(new Node<>(nodeId, data));
    }

    public Node<DATA> addNode(Node<DATA> node) {
        this.nodes.put(node.id(), node);
        return node;
    }

    public Relationship<DATA> addRelationship(long source, long target) {
        Node<DATA> sourceNode = nodes.get(source);
        if (sourceNode == null) {
            throw new IllegalArgumentException(String.format("Node with id %s was not found.", source));
        }

        Node<DATA> targetNode = nodes.get(source);
        if (targetNode == null) {
            throw new IllegalArgumentException(String.format("Node with id %s was not found.", target));
        }

        return addRelationship(sourceNode, targetNode);
    }

    public Relationship<DATA> addRelationship(Node<DATA> sourceNode, Node<DATA> targetNode) {
        return addRelationship(new Relationship<>(sourceNode, targetNode));
    }

    public Relationship<DATA> addRelationship(Relationship<DATA> relationship) {
        this.relationships.putIfAbsent(relationship.source.id(), new ArrayList<>());
        this.relationships.get(relationship.source.id()).add(relationship);
        this.inDegree.putIfAbsent(relationship.target.id(), 0);
        Integer currentInDegree = this.inDegree.get(relationship.target.id());
        this.inDegree.put(relationship.target.id(), currentInDegree + 1);
        return relationship;
    }

    public void forEachNode(NodeVisitor<DATA> visitor) {
        this.nodes.values().forEach(visitor::accept);
    }

    public void forEachRelationship(RelationshipVisitor<DATA> visitor) {
        this.relationships.values().stream().flatMap(List::stream).forEach(relationship -> {
            visitor.accept(relationship.source(), relationship.target());
        });
    }

    public void forEachRelationship(Node<DATA> node, RelationshipVisitor<DATA> visitor) {
        this.relationships.get(node.id()).forEach(relationship -> visitor.accept(relationship.source(), relationship.target()));
    }

    public int degree(Node<DATA> node) {
        if (!relationships.containsKey(node.id())) {
            return 0;
        }
        return relationships.get(node.id()).size();
    }

    public int inDegree(Node<DATA> node) {
        if (!inDegree.containsKey(node.id())) {
            return 0;
        }
        return inDegree.get(node.id());
    }

    public List<Node<DATA>> nodes() {
        return new ArrayList<>(this.nodes.values());
    }

    public List<Relationship<DATA>> relationships() {
        return this.relationships.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
