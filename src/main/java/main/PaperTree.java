package main;

import tree.Tree;

public class PaperTree {

    public static Tree<String> build() {
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

        return tree;
    }
}
