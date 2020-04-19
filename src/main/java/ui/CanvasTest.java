package ui;

import algorithm.CoordinateWrappedTree;
import algorithm.Walker;
import tree.Tree;

import java.awt.*;
import java.util.concurrent.ExecutorService;

public class CanvasTest {

    public static int TARGET_FPS = 60;
    public static int MS_PER_SECOND = 1000;
    public static int TIME_PER_FRAME = TARGET_FPS / MS_PER_SECOND;


    public static void main(String[] args) {
        Tree<String> tree = new Tree<>("root");

        Tree<String> e = tree.addChild("E");
        tree.addChild("F");
        Tree<String> n = tree.addChild("N");

        e.addChild("A");
        Tree<String> d = e.addChild("D");

        n.addChild("G");
        Tree<String> m = n.addChild("M");

        d.addChild("B");
        d.addChild("C");

        m.addChild("H");
        m.addChild("I");
        m.addChild("J");
        m.addChild("K");
        m.addChild("L");

        CoordinateWrappedTree<String, Tree<String>> coordinateTree = CoordinateWrappedTree.from(tree);
        Walker<String, CoordinateWrappedTree<String, Tree<String>>> walkerAlgorithm = new Walker<>(coordinateTree);
        CoordinateWrappedTree<String, CoordinateWrappedTree<String, Tree<String>>> computeResult = walkerAlgorithm.compute();
        DrawWindow window = new DrawWindow(400, 400, computeResult);

    }
}
