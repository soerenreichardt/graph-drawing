package ui;

import algorithm.CoordinateTree;
import graph.Graph;
import graph.Node;
import tree.AbstractTree.TraverseStrategy;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;

public class GraphDrawer {

    public static final int NODE_SIZE = 40;
    public static final int LEVEL_OFFSET = 15;

    public static void drawGraph(Graph<String> graph, Map<Node<String>, Point2D.Float> nodePositions, Graphics2D g) {
        graph.forEachNode(node -> {
            int xPosition = (int) nodePositions.get(node).x * NODE_SIZE;
            int yPosition = (int) nodePositions.get(node).y * (NODE_SIZE + LEVEL_OFFSET);

            g.fillOval(xPosition, yPosition, NODE_SIZE, NODE_SIZE);

            int lineOffset = NODE_SIZE / 2;
            graph.forEachRelationship(node, (source, target) -> {
                int parentXPosition = (int) nodePositions.get(target).x * NODE_SIZE + lineOffset;
                int parentYPosition = (int) nodePositions.get(target).y * (NODE_SIZE + LEVEL_OFFSET) + lineOffset;
                g.drawLine(xPosition + lineOffset, yPosition + lineOffset, parentXPosition, parentYPosition);
                g.drawLine(xPosition + lineOffset - 1, yPosition + lineOffset - 1, parentXPosition, parentYPosition);
                g.drawLine(xPosition + lineOffset + 1, yPosition + lineOffset + 1, parentXPosition, parentYPosition);
            });
        });
    }
}
