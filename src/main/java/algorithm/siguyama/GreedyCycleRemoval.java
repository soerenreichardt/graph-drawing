package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class GreedyCycleRemoval implements Algorithm<Pair<Graph<String>, List<Node<String>>>> {

    Graph<String> graph;

    public GreedyCycleRemoval(Graph<String> graph) {
        this.graph = graph;
    }

    public Pair<Graph<String>, List<Node<String>>> compute() {
        List<Node<String>> leftList = new ArrayList<>();
        LinkedList<Node<String>> rightList = new LinkedList<>();

        List<Node<String>> nodes = graph.nodes();
        List<Node<String>> modifiedNodes = graph.nodes();
        List<Relationship<String>> relationships = graph.relationships();
        List<Relationship<String>> acyclicRelationships = new ArrayList<>();
        while (!nodes.isEmpty()) {
            for (Node<String> node : nodes) {
                if (inDegree(relationships, node) == 0 && outDegree(relationships, node) != 0) {
                    leftList.add(node);
                    modifiedNodes.remove(node);
                    var outgoingRelationships = getOutgoingRelationships(relationships, node);
                    acyclicRelationships.addAll(outgoingRelationships);
                    relationships.removeAll(outgoingRelationships);
                }
                if (outDegree(relationships, node) == 0) {
                    rightList.addFirst(node);
                    modifiedNodes.remove(node);
                    var incomingRelationships = getIncomingRelationships(relationships, node);
                    acyclicRelationships.addAll(incomingRelationships);
                    acyclicRelationships.removeAll(incomingRelationships);
                }
                if (!modifiedNodes.isEmpty()) {
                    Node<String> nodeToRemove = null;
                    int maxDegreeDifference = Integer.MIN_VALUE;
                    for (Node<String> n : modifiedNodes) {
                        int degreeDifference = graph.degree(n) - graph.inDegree(n);
                        if (degreeDifference > maxDegreeDifference) {
                            maxDegreeDifference = degreeDifference;
                            nodeToRemove = n;
                        }
                    }

                    modifiedNodes.remove(nodeToRemove);
                    leftList.add(nodeToRemove);
                    var outgoingRelationships = getOutgoingRelationships(relationships, nodeToRemove);
                    var incomingRelationships = getIncomingRelationships(relationships, nodeToRemove);

                    acyclicRelationships.addAll(outgoingRelationships);
                    relationships.removeAll(outgoingRelationships);
                    relationships.removeAll(incomingRelationships);
                }
            }
            nodes = new ArrayList<>(modifiedNodes);
        }

        List<Node<String>> combinedNodeLists = new ArrayList<>();
        combinedNodeLists.addAll(leftList);
        combinedNodeLists.addAll(rightList);
        Graph<String> graph = buildGraphFromLists(Set.copyOf(combinedNodeLists), acyclicRelationships);
        return Pair.of(graph, combinedNodeLists);
    }

    private List<Relationship<String>> getOutgoingRelationships(List<Relationship<String>> relationships, Node<String> node) {
        return relationships.stream().filter((relationship) -> relationship.source() == node).collect(Collectors.toList());
    }

    private List<Relationship<String>> getIncomingRelationships(List<Relationship<String>> relationships, Node<String> node) {
        return relationships.stream().filter((relationship) -> relationship.target() == node).collect(Collectors.toList());
    }

    private int outDegree(List<Relationship<String>> relationships, Node<String> node) {
        return (int) relationships.stream().filter(relationship -> relationship.source() == node).count();
    }

    private int inDegree(List<Relationship<String>> relationships, Node<String> node) {
        return (int) relationships.stream().filter(relationship -> relationship.target() == node).count();
    }

    private Graph<String> buildGraphFromLists(Set<Node<String>> nodes, List<Relationship<String>> relationships) {
        Graph<String> acyclicGraph = new Graph<>();
        nodes.forEach(acyclicGraph::addNode);
        relationships.forEach(acyclicGraph::addRelationship);
        return acyclicGraph.deduplicateRelationships();
    }
}
