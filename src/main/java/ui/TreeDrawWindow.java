package ui;

import algorithm.CoordinateTree;
import org.apache.commons.lang3.mutable.MutableFloat;
import tree.AbstractTree.TraverseStrategy;

import javax.swing.*;
import java.awt.*;

import static ui.TreeDrawer.LEVEL_OFFSET;
import static ui.TreeDrawer.NODE_SIZE;

public class TreeDrawWindow extends Canvas {

    final JFrame frame;
    private final CoordinateTree<?, ?> tree;

    public TreeDrawWindow(String title, int width, int height, CoordinateTree<?, ?> tree) {
        this.tree = tree;
        this.frame = new JFrame(title);

        this.setSize(width, height);

        this.frame.add(this);
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MutableFloat maxXValue = new MutableFloat(Float.NEGATIVE_INFINITY);
        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            if (t.location().x > maxXValue.floatValue()) {
                maxXValue.setValue(t.location().x);
            }
            return true;
        });
        frame.setSize((maxXValue.intValue() + 1) * NODE_SIZE, (tree.maxDepth() + 2) * (LEVEL_OFFSET + NODE_SIZE));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        TreeDrawer.drawTree(tree, graphics2D);
    }
}
