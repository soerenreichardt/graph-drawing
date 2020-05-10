package graph;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void shouldCreateGraph() {
        Graph<String> graph = new Graph<>();

        Node<String> node1 = graph.addNode("Node1");
        Node<String> node2 = graph.addNode("Node2");
        graph.addRelationship(node1.id(), node2.id());

        assertEquals(2, graph.nodes().size());
        assertEquals(1, graph.relationships().size());
    }

    @Test
    void shouldTraverseNodesCorrectly() {
        Graph<String> graph = new Graph<>();

        Node<String> node1 = graph.addNode("Node1");
        Node<String> node2 = graph.addNode("Node2");

        List<Node<String>> nodes = new ArrayList<>(2);
        graph.forEachNode(nodes::add);

        assertEquals(2, nodes.size());
        assertEquals(node1, nodes.get(0));
        assertEquals(node2, nodes.get(1));
    }

    @Test
    void shouldTraverseRelationshipsCorrectly() {
        Graph<String> graph = new Graph<>();

        Node<String> node1 = graph.addNode("Node1");
        Node<String> node2 = graph.addNode("Node2");
        graph.addRelationship(node1, node2);

        List<Node<String>> neighbors = new ArrayList<>(1);
        graph.forEachRelationship(node1, (s, t) -> {
            neighbors.add(t);
        });

        assertEquals(1, neighbors.size());
        assertEquals(node2, neighbors.get(0));
    }

    @Test
    void shouldStoreCorrectDegrees() {
        Graph<String> graph = new Graph<>();

        Node<String> node1 = graph.addNode("Node1");
        Node<String> node2 = graph.addNode("Node2");
        graph.addRelationship(node1, node2);
        graph.addRelationship(node1, node2);

        assertEquals(2, graph.degree(node1));
        assertEquals(0, graph.inDegree(node1));
        assertEquals(0, graph.degree(node2));
        assertEquals(2, graph.inDegree(node2));
    }

}