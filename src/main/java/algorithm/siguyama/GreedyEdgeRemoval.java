package algorithm.siguyama;

import algorithm.Algorithm;
import graph.Graph;
import graph.Node;
import graph.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GreedyEdgeRemoval implements Algorithm<Graph<String>> {

    Graph<String> graph;

    public GreedyEdgeRemoval(Graph<String> graph) {
        this.graph = graph;
    }

    public Graph<String> compute() {
        List<Node<String>> leftList = new ArrayList<>();
        List<Node<String>> rightList = new ArrayList<>();

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
                    rightList.add(node);
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
                    var outgoingRelationships = getOutgoingRelationships(relationships, nodeToRemove);
                    var incomingRelationships = getIncomingRelationships(relationships, nodeToRemove);

                    acyclicRelationships.addAll(outgoingRelationships);
                    relationships.removeAll(outgoingRelationships);
                    relationships.removeAll(incomingRelationships);
                }
            }
            nodes = new ArrayList<>(modifiedNodes);
        }

        Set<Node<String>> combinedNodeLists = new HashSet<>();
        combinedNodeLists.addAll(leftList);
        combinedNodeLists.addAll(rightList);
        return buildGraphFromLists(combinedNodeLists, acyclicRelationships);
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
        return acyclicGraph;
    }
}
