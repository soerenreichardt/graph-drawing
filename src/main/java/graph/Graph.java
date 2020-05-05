package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Graph<DATA> {

    AtomicLong nodeCount;
    AtomicLong relationshipCount;

    Map<Long, Node<DATA>> nodes;
    Map<Long, List<Relationship<DATA>>> relationships;

    public Graph() {
        this.nodeCount = new AtomicLong(0L);
        this.relationshipCount = new AtomicLong(0L);

        this.nodes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public Node<DATA> addNode(DATA data) {
        long nodeId = nodeCount.getAndIncrement();
        Node<DATA> node = new Node<>(nodeId, data);
        this.nodes.put(nodeId, node);
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

        Relationship<DATA> relationship = new Relationship<>(sourceNode, targetNode);
        ArrayList<Relationship<DATA>> relationshipList = new ArrayList<>();
        this.relationships.putIfAbsent(source, relationshipList);
        this.relationships.get(source).add(relationship);
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
}
