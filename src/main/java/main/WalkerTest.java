package main;

import algorithm.CoordinateTree;
import algorithm.Walker;
import io.NewickFormatImporter;
import tree.Tree;
import ui.TreeDrawWindow;

public class WalkerTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
        Tree<String> tree = newickFormatImporter.parse();

//        Tree<String> tree = PaperTree.build();

        var coordinateTree = CoordinateTree.from(tree);
        var walkAlgorithmResult = new Walker<>(coordinateTree).compute();
        new TreeDrawWindow("Walker", 400, 400, walkAlgorithmResult);
    }
}
