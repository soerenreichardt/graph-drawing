package ui;

import graph.Graph;
import graph.Node;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;

import static ui.TreeDrawer.LEVEL_OFFSET;
import static ui.TreeDrawer.NODE_SIZE;

public class GraphDrawWindow extends Canvas {

    static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    final JFrame frame;
    final GraphDrawer graphDrawer;

    private final Map<Node<String>, Point2D.Float> nodePositions;
    private final Graph<String> graph;

    public GraphDrawWindow(String title, float zoom, Graph<String> graph, Map<Node<String>, Point2D.Float> nodePositions) {
        this.graph = graph;
        this.frame = new JFrame(title);
        this.nodePositions = nodePositions;

        this.frame.add(this);
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Triple<Dimension, Pair<Integer, Integer>, Double> dimensionsAndScaling = computeScaling();
        frame.setSize(dimensionsAndScaling.getLeft());

        double scale = dimensionsAndScaling.getRight();
        Integer xOffset = dimensionsAndScaling.getMiddle().getLeft();
        Integer yOffset = dimensionsAndScaling.getMiddle().getRight();
        this.graphDrawer = new GraphDrawer(scale, yOffset, xOffset, zoom, nodePositions);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphDrawer.drawGraph(graph, graphics2D);
    }

    private Triple<Dimension, Pair<Integer, Integer>, Double> computeScaling() {
        int maxXValue = Integer.MIN_VALUE;
        int maxYValue = Integer.MIN_VALUE;
        int minXValue = Integer.MAX_VALUE;
        int minYValue = Integer.MAX_VALUE;
        for (Node<String> node : graph.nodes()) {
            Point2D.Float nodePosition = nodePositions.get(node);
            if ((int) nodePosition.x > maxXValue) {
                maxXValue = (int) nodePosition.x;
            }
            if ((int) nodePosition.y > maxYValue) {
                maxYValue = (int) nodePosition.y;
            }
            if ((int) nodePosition.x < minXValue) {
                minXValue = (int) nodePosition.x;
            }
            if ((int) nodePosition.y < minYValue) {
                minYValue = (int) nodePosition.y;
            }
        }
//        int xOffset = -minXValue;
//        int yOffset = -minYValue;

        int expectedWidth = (maxXValue + 1) * (LEVEL_OFFSET + NODE_SIZE);
        int expectedHeight = (maxYValue + 1) * NODE_SIZE;

        double scaleX = 1.0D;
        double scaleY = 1.0D;
        int finalWidth = expectedWidth;
        int finalHeight = expectedHeight;
        if (expectedHeight > SCREEN_HEIGHT) {
            scaleX = (double) SCREEN_HEIGHT / (double) expectedHeight;
            finalHeight = SCREEN_HEIGHT;
        }
        if (expectedWidth > SCREEN_WIDTH) {
            scaleY = (double) SCREEN_WIDTH / (double) expectedWidth;
            finalWidth = SCREEN_WIDTH;
        }

        Dimension frameSize = new Dimension(finalWidth, finalHeight);
        return Triple.of(frameSize, Pair.of(minXValue, minYValue), Math.max(scaleX, scaleY));
    }
}
