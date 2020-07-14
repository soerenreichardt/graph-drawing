package ui;

import graph.Graph;
import graph.Node;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;

import static algorithm.siguyama.CrossingReduction.DUMMY_PREFIX;

public class GraphDrawer {

    public static final int NODE_SIZE = 40;
    public static final int LEVEL_OFFSET = 15;

    int scaledNodeSize;
    int scaledLevelOffset;
    private Integer yOffset;
    private Integer xOffset;
    private final Map<Node<String>, Point2D.Float> nodePositions;

    public GraphDrawer(double scale, Integer yOffset, Integer xOffset, float zoom, Map<Node<String>, Point2D.Float> nodePositions) {
        this.scaledNodeSize = (int)(NODE_SIZE * scale * zoom);
        this.scaledLevelOffset = (int)(LEVEL_OFFSET * scale * zoom);
        this.yOffset = yOffset;
        this.xOffset = xOffset;
        this.nodePositions = nodePositions;
    }

    public void drawGraph(Graph<String> graph, Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        graph.forEachNode(node -> {
            System.out.println(node.data() + ": " + nodePositions.get(node));
            int xPosition = getNodeXPosition(node);
            int yPosition = getNodeYPosition(node);
//            System.out.println(xPosition);
//            System.out.println(yPosition + "\n");

            if (!node.data().contains(DUMMY_PREFIX)) {
                g.fillOval(xPosition, yPosition, scaledNodeSize, scaledNodeSize);
            }

            int lineOffset = scaledNodeSize / 2;
            graph.forEachRelationship(node, (source, target) -> {
                int neighborXPosition = getNodeXPosition(target) + lineOffset;
                int neighborYPosition = getNodeYPosition(target) + lineOffset;
                g.drawLine(xPosition + lineOffset, yPosition + lineOffset, neighborXPosition, neighborYPosition);
            });

//            if (!node.data().contains(DUMMY_PREFIX)) {
//                g.setColor(Color.WHITE);
//                g.drawString(node.data(), xPosition + (scaledNodeSize / 2), yPosition + (scaledNodeSize / 2));
//                g.setColor(Color.DARK_GRAY);
//            }
        });
    }

    private int getNodeXPosition(Node<String> node) {
        return (int) (nodePositions.get(node).x - xOffset) * scaledNodeSize;
    }

    private int getNodeYPosition(Node<String> node) {
        return (int) (nodePositions.get(node).y - yOffset) * (scaledNodeSize + scaledLevelOffset);
    }

}
