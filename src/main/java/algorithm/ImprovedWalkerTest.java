package algorithm;

import io.NewickFormatImporter;
import tree.Tree;
import ui.DrawWindow;

public class ImprovedWalkerTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
        Tree<String> tree = newickFormatImporter.parse();

        var coordinateTree = CoordinateTree.from(tree);
        var walkAlgorithmResult = new ImprovedWalker<>(coordinateTree).compute();
        new TreeDrawWindow("ImprovedWalker", 400, 400, walkAlgorithmResult);
    }
}
