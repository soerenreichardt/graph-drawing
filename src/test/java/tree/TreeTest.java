package tree;

import org.junit.Test;
import tree.AbstractTree.TraverseStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TreeTest {

    @Test
    public void shouldCreateATree() {
        Tree<String> tree = new Tree("root");
    }

    @Test
    public void shouldAddChildren() {
        Tree<String> tree = new Tree("root");
        tree.addChild("child1");
        tree.addChild("child2");

        assertEquals("root", tree.data);
        assertEquals("child1", tree.children().get(0).data);
        assertEquals("child2", tree.children().get(1).data);
    }

    @Test
    public void shouldTraverseWithBfs() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> actual.add((String) t.data));

        String[] expected = new String[]{ "root", "child1", "child2", "child1.1", "child1.2" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldTraverseWithDfs() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.DEPTH_FIRST, (t) -> actual.add((String) t.data));

        String[] expected = new String[]{ "root", "child1", "child1.1", "child1.2", "child2" };
        assertArrayEquals(expected, actual.toArray());
    }
}