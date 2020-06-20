package main;

import algorithm.siguyama.Siguyama;
import graph.Graph;
import graph.Node;
import ui.GraphDrawWindow;

import java.awt.geom.Point2D;
import java.util.Map;

public class SiguyamaTest {

    public static void main(String[] args) {
        Graph<String> graph = new Graph<>();
        Node<String> node1 = graph.addNode("1");
        Node<String> node2 = graph.addNode("2");
        Node<String> node3 = graph.addNode("3");
        Node<String> node4 = graph.addNode("4");
        Node<String> node5 = graph.addNode("5");
        Node<String> node6 = graph.addNode("6");
        Node<String> node7 = graph.addNode("7");

        graph.addRelationship(node1, node6);
        graph.addRelationship(node2, node4);
        graph.addRelationship(node2, node7);
        graph.addRelationship(node3, node4);
        graph.addRelationship(node5, node6);
        graph.addRelationship(node5, node7);
        graph.addRelationship(node6, node7);

        Map<Node<String>, Point2D.Float> nodePositions = new Siguyama(graph).compute();

        new GraphDrawWindow("Siguyama", 300, 400, graph, nodePositions);
    }
}
