package tree;

import org.junit.jupiter.api.Test;
import tree.AbstractTree.TraverseStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeTest {

    @Test
    public void shouldCreateATree() {
        new Tree("root");
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
    public void shouldStoreLeftSiblings() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        assertEquals(child2.leftSibling, child1);
    }

    @Test
    public void shouldTraverseWithBfs() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        child2.addChild("child2.1");

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> actual.add(t.data));

        String[] expected = new String[]{ "root", "child1", "child2", "child1.1", "child1.2", "child2.1" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldTraverseWithBfsReversed() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        child2.addChild("child2.1");

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.BREADTH_FIRST_REVERSE, (t) -> actual.add(t.data));

        String[] expected = new String[]{ "root", "child2", "child1", "child2.1", "child1.2", "child1.1" };
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
        tree.traverse(TraverseStrategy.DEPTH_FIRST, (t) -> actual.add(t.data));

        String[] expected = new String[]{ "root", "child1", "child1.1", "child1.2", "child2" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldTraverseWithDfsReversed() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.DEPTH_FIRST_REVERSE, (t) -> actual.add(t.data));

        String[] expected = new String[]{ "root", "child2", "child1", "child1.2", "child1.1" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldTraverseWithPostOrder() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        child1.addChild("child1.1");
        child1.addChild("child1.2");

        child2.addChild(("child2.1"));

        List<String> actual = new ArrayList<>();
        tree.traverse(TraverseStrategy.POST_ORDER, (t) -> actual.add(t.data));

        String[] expected = new String[]{ "child1.1", "child1.2", "child1", "child2.1", "child2", "root" };
        assertArrayEquals(expected, actual.toArray());
    }

    @Test
    public void shouldGetLeftAndRightMostNodes() {
        Tree<String> tree = new Tree("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        Tree<String> child11 = child1.addChild("child1.1");
        Tree<String> child12 = child1.addChild("child1.2");

        assertEquals(child1, tree.getLeftmost(1).get());
        assertEquals(child2, tree.getRightmost(1).get());
        assertEquals(child11, tree.getLeftmost(2).get());
        assertEquals(child12, child1.getRightmost(2).get());

        assertEquals(child11, tree.getLeftmost(-1).get());
        assertEquals(child12, tree.getRightmost(-1).get());
    }

    @Test
    public void shouldFindLeftNeighbor() {
        Tree<String> tree = new Tree<>("root");
        Tree<String> child1 = tree.addChild("child1");
        Tree<String> child2 = tree.addChild("child2");

        Tree<String> child11 = child1.addChild("child1.1");
        child1.addChild("child1.2");

        child11.addChild("child1.1.1");

        Tree<String> child21 = child2.addChild("child2.1");
        child21.addChild("child2.1.1");

        tree.traverse(TraverseStrategy.BREADTH_FIRST, (t) -> {
            Optional<Tree<String>> leftNeighbor = tree.getLeftNeighbor(t);
            String lnName = "null";
            if (leftNeighbor.isPresent()) lnName = leftNeighbor.get().data();
            System.out.println(t.data() + " ln: " + lnName);
            return true;
        });
    }
}