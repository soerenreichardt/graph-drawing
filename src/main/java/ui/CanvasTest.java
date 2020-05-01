package ui;

import algorithm.CoordinateTree;
import algorithm.Walker;
import tree.Tree;

public class CanvasTest {

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

        CoordinateTree<String, Tree<String>> coordinateTree = CoordinateTree.from(tree);
        Walker<String, CoordinateTree<String, Tree<String>>> walkerAlgorithm = new Walker<>(coordinateTree);
        CoordinateTree<String, CoordinateTree<String, Tree<String>>> computeResult = walkerAlgorithm.compute();
        new TreeDrawWindow("Walker", 400, 400, computeResult);

    }
}
