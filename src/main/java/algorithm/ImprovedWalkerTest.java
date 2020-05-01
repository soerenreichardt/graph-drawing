package algorithm;

import io.NewickFormatImporter;
import tree.Tree;
import ui.TreeDrawWindow;

public class ImprovedWalkerTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
//        Tree<String> tree = newickFormatImporter.parse();

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

        var coordinateTree = CoordinateTree.from(tree);
        var walkAlgorithmResult = new ImprovedWalker<>(coordinateTree).compute();
        new TreeDrawWindow("ImprovedWalker", 400, 400, walkAlgorithmResult);
    }
}
