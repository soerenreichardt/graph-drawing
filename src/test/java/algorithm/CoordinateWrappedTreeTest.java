package algorithm;

import org.junit.Test;
import tree.AbstractTree.TraverseStrategy;
import tree.Tree;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CoordinateWrappedTreeTest {

    @Test
    public void shouldCreateATree() {
        Tree<String> tree = new Tree("root");
        CoordinateWrappedTree.from(tree);
    }

    @Test
    public void shouldAddChildren() {
        Tree<String> tree = new Tree("root");
        tree.addChild("child1");
        tree.addChild("child2");

        CoordinateWrappedTree<String, Tree<String>> wrappedTree = CoordinateWrappedTree.from(tree);
        assertEquals("root", wrappedTree.data());
        assertEquals("child1", wrappedTree.children().get(0).data());
        assertEquals("child2", wrappedTree.children().get(1).data());
    }

    @Test
    public void shouldTraverseWithBfs() {
        Tree<String> tree = new Tree<>("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        List<String> actual = new ArrayList<>();
        CoordinateWrappedTree.from(tree).traverse(TraverseStrategy.BREADTH_FIRST, (t) -> actual.add(t.data()));

        String[] expected = new String[]{ "root", "child1", "child2", "child1.1", "child1.2" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldTraverseWithDfs() {
        Tree<String> tree = new Tree<>("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        List<String> actual = new ArrayList<>();
        CoordinateWrappedTree.from(tree).traverse(TraverseStrategy.DEPTH_FIRST, (t) -> actual.add(t.data()));

        String[] expected = new String[]{ "root", "child1", "child1.1", "child1.2", "child2" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldHaveEmptyLocations() {
        Tree<String> tree = new Tree<>("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        CoordinateWrappedTree.from(tree).traverse(TraverseStrategy.BREADTH_FIRST, (t) -> assertEquals(new Point2D.Float(), t.location()));
    }

    @Test
    public void shouldStoreLocations() {
        Tree<String> tree = new Tree<>("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        CoordinateWrappedTree<String, Tree<String>> coordinateTree = CoordinateWrappedTree.from(tree);
        coordinateTree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> t.setLocation(new Point2D.Float(1.0f, 2.0f)));
        coordinateTree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> assertEquals(new Point2D.Float(1.0f, 2.0f), t.location()));
    }
}