package io;

import algorithm.CoordinateWrappedTree;
import algorithm.Walker;
import tree.Tree;
import ui.DrawWindow;

public class NewickImporterTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/ce11.26way.nh");
        Tree<String> tree = newickFormatImporter.parse();

        var coordinateTree = CoordinateWrappedTree.from(tree);
        var walkAlgorithmResult = new Walker<>(coordinateTree).compute();
        new DrawWindow(400, 400, walkAlgorithmResult);
    }
}
