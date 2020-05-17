package main;

import algorithm.siguyama.Siguyama;
import graph.Graph;
import graph.Node;

public class SiguyamaTest {

    public static void main(String[] args) {
        Graph<String> graph = new Graph<>();
        Node<String> node1 = graph.addNode("1");
        Node<String> node2 = graph.addNode("2");
        Node<String> node3 = graph.addNode("3");
        Node<String> node4 = graph.addNode("4");

        graph.addRelationship(node1, node2);
        graph.addRelationship(node2, node3);
        graph.addRelationship(node3, node4);
        graph.addRelationship(node1, node4);

        new Siguyama(graph).compute();
    }
}
