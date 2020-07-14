package main;

import algorithm.siguyama.Siguyama;
import graph.Graph;
import graph.Node;
import io.XmlImporter;
import org.apache.commons.lang3.tuple.Pair;
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

        Node<String> node8 = graph.addNode("8");
        Node<String> node9 = graph.addNode("9");
        Node<String> node10 = graph.addNode("10");
        Node<String> node11 = graph.addNode("11");
        Node<String> node12 = graph.addNode("12");
        Node<String> node13 = graph.addNode("13");
        Node<String> node14 = graph.addNode("14");
        Node<String> node15 = graph.addNode("15");
        Node<String> node16 = graph.addNode("16");
        Node<String> node17 = graph.addNode("17");
        Node<String> node18 = graph.addNode("18");
        Node<String> node19 = graph.addNode("19");
        Node<String> node20 = graph.addNode("20");
        Node<String> node21 = graph.addNode("21");
        Node<String> node22 = graph.addNode("22");
        Node<String> node23 = graph.addNode("23");

        graph.addRelationship(node1, node3);
        graph.addRelationship(node1, node4);
        graph.addRelationship(node1, node13);
        graph.addRelationship(node1, node21);
        graph.addRelationship(node2, node3);
        graph.addRelationship(node2, node20);
        graph.addRelationship(node3, node4);
        graph.addRelationship(node3, node5);
        graph.addRelationship(node3, node23);
        graph.addRelationship(node4, node6);
        graph.addRelationship(node5, node7);
        graph.addRelationship(node6, node8);
        graph.addRelationship(node6, node16);
        graph.addRelationship(node6, node23);
        graph.addRelationship(node7, node9);
        graph.addRelationship(node8, node10);
        graph.addRelationship(node8, node11);
        graph.addRelationship(node9, node12);
        graph.addRelationship(node10, node13);
        graph.addRelationship(node10, node14);
        graph.addRelationship(node10, node15);
        graph.addRelationship(node11, node15);
        graph.addRelationship(node11, node16);
        graph.addRelationship(node12, node20);
        graph.addRelationship(node13, node17);
        graph.addRelationship(node14, node17);
        graph.addRelationship(node14, node18);
        graph.addRelationship(node16, node18);
        graph.addRelationship(node16, node19);
        graph.addRelationship(node16, node20);
        graph.addRelationship(node18, node21);
        graph.addRelationship(node19, node22);
        graph.addRelationship(node21, node23);
        graph.addRelationship(node22, node23);

//        graph.addRelationship(node1, node6);
//        graph.addRelationship(node2, node4);
//        graph.addRelationship(node2, node7);
//        graph.addRelationship(node3, node4);
//        graph.addRelationship(node4, node5);
//        graph.addRelationship(node5, node6);
//        graph.addRelationship(node5, node7);
//        graph.addRelationship(node6, node7);

        XmlImporter xmlImporter = new XmlImporter("/JUnit-4.12.graphml", "method-call");
        graph = xmlImporter.parse();

        Pair<Graph<String>, Map<Node<String>, Point2D.Float>> graphAndPositions = new Siguyama(graph).compute();

        float zoom = 1.5f;
        new GraphDrawWindow("Siguyama", zoom, graphAndPositions.getLeft(), graphAndPositions.getRight());
    }
}
