package io;

import graph.Graph;
import graph.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class XmlImporter {

    private final String relType;

    File graphFile;
    Document document;

    public XmlImporter(String path, String relType) {
        this.relType = relType;
        String resolvedPath = this.getClass().getResource(path).getPath();
        this.graphFile = new File(resolvedPath);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder =  factory.newDocumentBuilder();

            this.document = builder.parse(graphFile);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }

    public Graph<String> parse() {
        return parseGraphMl();
    }

    private Graph<String> parseGraphMl() {
        Graph<String> graph = new Graph<>();
        Map<String, Node<String>> seenNodes = new HashMap<>();

        NodeList relationships = document.getElementsByTagName("edge");
        for (int i = 0; i < relationships.getLength(); i++) {
            String key = relationships.item(i).getChildNodes().item(1).getAttributes().getNamedItem("key").getNodeValue();
            if (!key.equals(relType)) continue;
            String sourceName = relationships.item(i).getAttributes().getNamedItem("source").getNodeValue();
            String targetName = relationships.item(i).getAttributes().getNamedItem("target").getNodeValue();

            Node<String> sourceNode;
            if (seenNodes.containsKey(sourceName)) {
                sourceNode = seenNodes.get(sourceName);
            } else {
                sourceNode = graph.addNode(sourceName);
                seenNodes.put(sourceName, sourceNode);
            }

            Node<String> targetNode;
            if (seenNodes.containsKey(targetName)) {
                targetNode = seenNodes.get(targetName);
            } else {
                targetNode = graph.addNode(targetName);
                seenNodes.put(targetName, targetNode);
            }
            graph.addRelationship(sourceNode, targetNode);
        }
        System.out.println("Done loading the graph");
        return graph;
    }
}
