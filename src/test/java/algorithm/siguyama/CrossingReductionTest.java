package algorithm.siguyama;

import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.GraphDrawWindow;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CrossingReductionTest {

    Graph<String> graph;
    Map<Node<String>, Float> layerAssignment;

    @BeforeEach
    void setup() {
        graph = new Graph<>();
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
        graph.addRelationship(node4, node5);
        graph.addRelationship(node5, node6);
        graph.addRelationship(node5, node7);
        graph.addRelationship(node6, node7);

        layerAssignment = new HashMap<>();
        layerAssignment.put(node1, 1.0f);
        layerAssignment.put(node2, 1.0f);
        layerAssignment.put(node3, 1.0f);
        layerAssignment.put(node4, 2.0f);
        layerAssignment.put(node5, 3.0f);
        layerAssignment.put(node6, 4.0f);
        layerAssignment.put(node7, 5.0f);
    }

    @Test
    void computeCorrectDummies() {
        CrossingReduction crossingReduction = new CrossingReduction(graph, layerAssignment);
        crossingReduction.compute();
        var expectedDummyNames = Set.of(
                "(1->6): 0",
                "(1->6): 1",
                "(2->7): 0",
                "(2->7): 1",
                "(2->7): 2",
                "(5->7): 0"
        );
        Map<Node<String>, Map<Node<String>, List<Relationship<String>>>> actualDummies = crossingReduction.dummies();
        var actualDummyNames = actualDummies
                .values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(List::stream)
                .flatMap(rel -> Stream.of(rel.source(), rel.target()))
                .filter(node -> !graph.containsNode(node))
                .map(Node::data)
                .collect(Collectors.toSet());

        expectedDummyNames.forEach(expected -> assertTrue(actualDummyNames.contains(expected)));
    }

    @Test
    void draw() throws InterruptedException {
        CrossingReduction crossingReduction = new CrossingReduction(graph, layerAssignment);
        CrossingReduction computeResult = crossingReduction.compute();
        Map<Node<String>, CrossingReduction.Block> nodeBlockMap = computeResult.nodeBlockMapping();
        Map<Node<String>, Float> newLayerAssigment = computeResult.layerAssignment();

        Map<Node<String>, Point2D.Float> nodeCoordinates = new HashMap<>();
        computeResult.properGraph().forEachNode(node -> nodeCoordinates.put(node, new Point2D.Float(
                (float) nodeBlockMap.get(node).position(),
                newLayerAssigment.get(node)
        )));

        new GraphDrawWindow("test", computeResult.properGraph(), nodeCoordinates);
        Thread.sleep(100000);
    }

//    @Test
//    void computeCorrectAdjacencies() {
//        CrossingReduction crossingReduction = new CrossingReduction(graph, layerAssignment);
//        Map<Node<String>, Float> result = crossingReduction.compute().;
//
//        var a = 1;
//    }

}