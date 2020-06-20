package ui;

import graph.Graph;
import graph.Node;
import org.apache.commons.lang3.mutable.MutableFloat;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;

import static ui.TreeDrawer.LEVEL_OFFSET;
import static ui.TreeDrawer.NODE_SIZE;

public class GraphDrawWindow extends Canvas {

    final JFrame frame;
    private Map<Node<String>, Point2D.Float> nodePositions;
    private final Graph<String> graph;

    public GraphDrawWindow(String title, int width, int height, Graph<String> graph, Map<Node<String>, Point2D.Float> nodePositions) {
        this.graph = graph;
        this.frame = new JFrame(title);
        this.nodePositions = nodePositions;

        this.setSize(width, height);

        this.frame.add(this);
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MutableFloat maxXValue = new MutableFloat(Float.NEGATIVE_INFINITY);
        MutableFloat maxYValue = new MutableFloat(Float.NEGATIVE_INFINITY);
        graph.forEachNode(node -> {
            if (nodePositions.get(node).x > maxXValue.floatValue()) {
                maxXValue.setValue(nodePositions.get(node).x);
            }
            if (nodePositions.get(node).y > maxYValue.floatValue()) {
                maxYValue.setValue(nodePositions.get(node).y);
            }
        });
        frame.setSize((maxXValue.intValue() + 1) * NODE_SIZE, (maxYValue.intValue() + 2) * (LEVEL_OFFSET + NODE_SIZE));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        GraphDrawer.drawGraph(graph, nodePositions, graphics2D);
    }
}
