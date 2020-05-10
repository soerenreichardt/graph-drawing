package main;

import algorithm.CoordinateTree;
import algorithm.walker.ImprovedWalker;
import io.NewickFormatImporter;
import tree.Tree;
import ui.TreeDrawWindow;

public class ImprovedWalkerTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
        Tree<String> tree = newickFormatImporter.parse();

//        Tree<String> tree = PaperTree.build();

        var coordinateTree = CoordinateTree.from(tree);
        var walkAlgorithmResult = new ImprovedWalker<>(coordinateTree).compute();
        new TreeDrawWindow("ImprovedWalker", 400, 400, walkAlgorithmResult);
    }
}
