package ui;

import algorithm.CoordinateWrappedTree;
import org.apache.commons.lang3.mutable.MutableFloat;
import tree.AbstractTree.TraverseStrategy;

import javax.swing.*;
import java.awt.*;

import static ui.TreeDrawer.LEVEL_OFFSET;
import static ui.TreeDrawer.NODE_SIZE;

public class DrawWindow extends Canvas {

    final JFrame frame;
    private final CoordinateWrappedTree<?, ?> tree;

    public DrawWindow(int width, int height, CoordinateWrappedTree<?, ?> tree) {
        this.tree = tree;
        this.frame = new JFrame("Hello");

        this.setSize(width, height);

        this.frame.add(this);
        this.frame.pack();
        this.frame.setVisible(true);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MutableFloat maxXValue = new MutableFloat(0.0f);
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
        TreeDrawer.drawTree(tree, g);
    }
}
