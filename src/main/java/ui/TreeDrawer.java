package ui;

import algorithm.CoordinateTree;
import tree.AbstractTree;

import java.awt.*;

public class TreeDrawer {

    public static final int NODE_SIZE = 40;
    public static final int LEVEL_OFFSET = 15;

    public static void drawTree(CoordinateTree<?, ?> tree, Graphics2D g) {
        tree.traverse(AbstractTree.TraverseStrategy.BREADTH_FIRST, (t) -> {
            int xPosition = (int) t.location().x * NODE_SIZE;
            int yPosition = (int) t.location().y * (NODE_SIZE + LEVEL_OFFSET);
            g.fillOval(xPosition, yPosition, NODE_SIZE, NODE_SIZE);

            int lineOffset = NODE_SIZE / 2;
            if (t.parent() != null) {
                int parentXPosition = (int) t.parent().location().x * NODE_SIZE + lineOffset;
                int parentYPosition = (int) t.parent().location().y * (NODE_SIZE + LEVEL_OFFSET) + lineOffset;
                g.drawLine(xPosition + lineOffset, yPosition + lineOffset, parentXPosition, parentYPosition);
                g.drawLine(xPosition + lineOffset - 1, yPosition + lineOffset - 1, parentXPosition, parentYPosition);
                g.drawLine(xPosition + lineOffset + 1, yPosition + lineOffset + 1, parentXPosition, parentYPosition);
            }
            return true;
        });
    }
}
