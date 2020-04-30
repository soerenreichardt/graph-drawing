package io;

import algorithm.CoordinateTree;
import algorithm.Walker;
import tree.Tree;
import ui.DrawWindow;

public class NewickImporterTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
        Tree<String> tree = newickFormatImporter.parse();

        var coordinateTree = CoordinateTree.from(tree);
        var walkAlgorithmResult = new Walker<>(coordinateTree).compute();
        new DrawWindow(400, 400, walkAlgorithmResult);
    }
}
