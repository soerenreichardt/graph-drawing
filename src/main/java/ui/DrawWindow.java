package ui;

import algorithm.CoordinateWrappedTree;
import tree.AbstractTree.TraverseStrategy;

import javax.swing.*;
import java.awt.*;

public class DrawWindow extends Canvas {

    public static final int NODE_SIZE = 40;

    final JFrame frame;
    private CoordinateWrappedTree<?, ?> tree;

    public DrawWindow(int width, int height, CoordinateWrappedTree<?, ?> tree) {
        this.tree = tree;
        this.frame = new JFrame("Hello");

        this.setSize(width, height);

        this.frame.add(this);
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void paint(Graphics g) {
        float minX = tree.getLeftmost(-1).get().location().x;
        float maxX = tree.getRightmost(-1).get().location().x;

        Dimension windowSize = this.getSize();

        int centerOffset = (windowSize.width / 2) - ( 2 * (int)(maxX - minX));

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            int xPosition = (int) t.location().x * NODE_SIZE + centerOffset;
            int yPosition = (int) t.location().y * NODE_SIZE;
            g.fillOval(xPosition, yPosition, NODE_SIZE, NODE_SIZE);

            int lineOffset = NODE_SIZE / 2;
            if (t.parent() != null) {
                int parentXPosition = (int) t.parent().location().x * NODE_SIZE + lineOffset + centerOffset;
                int parentYPosition = (int) t.parent().location().y * NODE_SIZE + lineOffset;
                g.drawLine(xPosition + lineOffset, yPosition + lineOffset, parentXPosition, parentYPosition);
            }
            return true;
        });
    }
}
