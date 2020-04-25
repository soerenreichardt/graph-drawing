package io;

import tree.Tree;

public class LoaderTest {

    public static void main(String[] args) {
        NewickFormatImporter newickFormatImporter = new NewickFormatImporter("/phyliptree.nh");
        Tree<String> tree = newickFormatImporter.parse();
        tree.data();
    }
}
